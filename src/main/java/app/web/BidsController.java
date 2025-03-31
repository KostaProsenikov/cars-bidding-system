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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("bids/")
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

    @PostMapping ("{advertId}")
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
                .build();
        Bid createdBid = this.bidsService.createNewBidForAdvert(user, advert, bidToCreate);
        return "redirect:/bids/" + createdBid.getId();
    }
}
