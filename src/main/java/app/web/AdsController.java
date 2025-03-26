package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateNewAdvertRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ads")
public class AdsController {

    private final AdvertService advertService;
    private final UserService userService;

    @Autowired
    public AdsController(AdvertService advertService, UserService userService) {
        this.advertService = advertService;
        this.userService = userService;
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
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("advert", advert);
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

    @PostMapping ("/new")
    public ModelAndView createNewAd(@Valid CreateNewAdvertRequest createNewAdvertRequest, BindingResult bindingResult, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-advert");
            modelAndView.addObject("createAdvertRequest", createNewAdvertRequest);
            return modelAndView;
        }
        User user = userService.getById(authenticationMetadata.getUserId());
        advertService.createNewAd(createNewAdvertRequest, user);
        return new ModelAndView("redirect:/");
    }

}
