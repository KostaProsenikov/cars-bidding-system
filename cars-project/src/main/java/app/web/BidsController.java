package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.bid.model.Bid;
import app.bid.service.BidsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("bids")
public class BidsController {

    private final AdvertService advertService;
    private final UserService userService;
    private final BidsService bidsService;

    @Autowired
    public BidsController(AdvertService advertService, UserService userService, BidsService bidsService) {
        this.advertService = advertService;
        this.userService = userService;
        this.bidsService = bidsService;
    }

    @GetMapping("")
    public ModelAndView getBidsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Bid> bids = bidsService.getBidsForUserId(user);
        ModelAndView modelAndView = new ModelAndView("bids");
        modelAndView.addObject("user", user);
        modelAndView.addObject("bids", bids);
        modelAndView.addObject("currentUri", "/bids");
        return modelAndView;
    }

    @GetMapping("/{bidId}")
    public ModelAndView getInformationAboutBigPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                                   @PathVariable UUID bidId) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Bid bid = bidsService.getById(bidId);
        ModelAndView modelAndView = new ModelAndView("bid-info");
        modelAndView.addObject("user", user);
        modelAndView.addObject("bid", bid);
        modelAndView.addObject("currentUri", "/bids");
        return modelAndView;
    }

    @PostMapping ("/{advertId}")
    public String addBid(@PathVariable UUID advertId, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                         @RequestParam("bidPrice") BigDecimal bidPrice,
                         @RequestParam("maxBidPrice") BigDecimal maxBidPrice) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Advert advert = advertService.getAdvertById(advertId);
        LocalDateTime now = LocalDateTime.now();
        Bid bidToCreate = Bid.builder()
                .bidPrice(bidPrice)
                .maxBidPrice(maxBidPrice)
                .createdOn(now)
                .updatedOn(now)
                .isAccepted(false)
                .bidder(user)
                .build();
        Bid createdBid = this.bidsService.createNewBidForAdvert(advert, bidToCreate);
        if (createdBid != null) {
            advert.setCurrentBidPrice(createdBid.getBidPrice());
            advert.setLastBidder(user);
            advert.setLastBidDate(now);
            advertService.updateAdvert(advertId, advert);
            return "redirect:/bids/" + createdBid.getId();
        }
        return "redirect:/ads/" + advertId;
    }
}
