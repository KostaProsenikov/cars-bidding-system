package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.bid.model.Bid;
import app.bid.service.BidsService;
import app.exception.DomainException;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateNewAdvertRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ads")
public class AdsController {

    private final AdvertService advertService;
    private final UserService userService;
    private final BidsService bidsService;

    @Autowired
    public AdsController(AdvertService advertService, UserService userService, BidsService bidsService) {
        this.advertService = advertService;
        this.userService = userService;
        this.bidsService = bidsService;
    }

    @GetMapping("")
    public ModelAndView getFirstAdsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("all-ads");
        int currentPage = 0;
        String sortType = "DESC";
        String sortField = "createdOn";
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Advert> adverts = advertService.getAllShownAdvertsByPage(currentPage, sortType, sortField);
        int totalVisibleAds = advertService.getAdvertCount();
        int totalPages = (int) Math.ceil((double) totalVisibleAds / 20);
        modelAndView.addObject("adverts", adverts);
        modelAndView.addObject("user", user);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", currentPage + 1);
        modelAndView.addObject("totalPages", totalPages);
        return modelAndView;
    }

    @GetMapping("{id}/info")
    public ModelAndView getAdvertInfoPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        Advert advert = advertService.getAdvertById(id);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("ad-info");
//        Update view count of the advert
        advert.setViewCount(advert.getViewCount() + 1);
        advertService.updateAdvert(id, advert);
        User user = userService.getById(authenticationMetadata.getUserId());
//        List<Bid> bids = bidsService.getBidsForAdvertIdAndUser(id, user);
        modelAndView.addObject("advert", advert);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("{id}/edit")
    public ModelAndView updateAdvertPage(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        Advert advert = advertService.getAdvertById(id);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-advert");
//        Update view count of the advert
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("createAdvertRequest", DtoMapper.mapAdvertToCreateNewAdvertRequest(advert));
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("page/{page}")
    public ModelAndView getAdvertsPage(@PathVariable int page, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("all-ads");
        User user = userService.getById(authenticationMetadata.getUserId());
        page = page < 1 ? 1 : page - 1;
        String sortType = "DESC";
        String sortField = "createdOn";
        List<Advert> adverts = advertService.getAllShownAdvertsByPage(page, sortType, sortField);
        modelAndView.addObject("adverts", adverts);
        int totalVisibleAds = advertService.getAdvertCount();
        int totalPages = (int) Math.ceil((double) totalVisibleAds / 20);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", page + 1);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/my-ads")
    public ModelAndView getMyAdvertsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-ads");
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Advert> adverts = advertService.getAdvertsByOwnerId(user.getId());
        int totalVisibleAds = adverts.size();
        int totalPages = 1;
        int currentPage = 0;
        modelAndView.addObject("adverts", adverts);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", currentPage + 1);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/my-reservations")
    public ModelAndView getMyReservationsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-reservations");
        List<Advert> reservedCarAdverts = advertService.getAdvertsByWinnerId(user.getId());
        modelAndView.addObject("reservedCars", reservedCarAdverts);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

//    @GetMapping("seed-adverts")
//    public ResponseEntity<String> seedAdverts() {
//        advertSeederService.seedAdverts();
//        return ResponseEntity.ok("Adverts seeded successfully!");
//    }

    @GetMapping ("/new")
    public ModelAndView getNewAdPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.setViewName("new-advert");
        modelAndView.addObject("createAdvertRequest", new CreateNewAdvertRequest());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView saveAdvert(
            @Valid CreateNewAdvertRequest createAdvertRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-advert");
            modelAndView.addObject("user", authenticationMetadata.getUserId() != null ? userService.getById(authenticationMetadata.getUserId()) : null);
            modelAndView.addObject("createAdvertRequest", createAdvertRequest);
            return modelAndView;
        }
        User user = userService.getById(authenticationMetadata.getUserId());
        advertService.createNewAd(createAdvertRequest, user);
        return new ModelAndView("redirect:/ads");
    }

    @PutMapping("/{id}/update")
    public ModelAndView updateAdvert(@Valid CreateNewAdvertRequest createAdvertRequest,
                                     BindingResult bindingResult,
                                     @PathVariable UUID id,
                                     @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-advert");
            modelAndView.addObject("createAdvertRequest", createAdvertRequest);
            modelAndView.addObject("org.springframework.validation.BindingResult.createAdvertRequest", bindingResult);
            modelAndView.addObject("user", userService.getById(authenticationMetadata.getUserId()));
            return modelAndView;
        }
        User user = userService.getById(authenticationMetadata.getUserId());
        Advert updatedAdvert = DtoMapper.mapCreateNewAdvertRequestToAdvert(createAdvertRequest, advertService.getAdvertById(id));
        if (updatedAdvert.getOwner().getId() != user.getId() && !user.getRole().name().equals("ADMIN")) {
            throw new DomainException("You are not allowed to edit this advert!");
        }

        advertService.saveAdvert(updatedAdvert);
        return new ModelAndView("redirect:/ads");
    }

    @PostMapping("{id}/reserve")
    public ModelAndView reserveAdvert(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        if (user.getId() == null) {
            throw new DomainException("You are not logged in!");
        }
        advertService.reserveCarAdvert(id, user);
        return new ModelAndView("redirect:/ads");
    }
}
