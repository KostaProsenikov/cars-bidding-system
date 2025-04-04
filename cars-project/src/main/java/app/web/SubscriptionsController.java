package app.web;

import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.transaction.model.Transaction;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping ("/subscriptions")
public class SubscriptionsController {

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;

    @Autowired
    public SubscriptionsController(UserService userService, SubscriptionService subscriptionService, WalletService walletService) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
    }

    @GetMapping("")
    public ModelAndView getSubscriptionsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscriptions");
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/subscriptions");
        return modelAndView;
    }

    @PostMapping("/change/{planType}")
    public String changeSubscriptionType(@PathVariable int planType, @RequestParam("period") String period, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        SubscriptionPeriod subscriptionPeriod = SubscriptionPeriod.valueOf(period.toUpperCase());
        SubscriptionType subscriptionType = switch (planType) {
            case 1 -> SubscriptionType.PLUS;
            case 2 -> SubscriptionType.PROFESSIONAL;
            default -> SubscriptionType.DEFAULT;
        };
        UUID walletId;
        if (user.getWallets() == null || user.getWallets().isEmpty()) {
            // Create a wallet for the user if they don't have one
            walletId = walletService.initializeFirstWallet(user).getId();
            user = userService.getById(user.getId()); // Refresh user data
        } else {
            walletId = user.getWallets().get(0).getId();
        }
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .subscriptionPeriod(subscriptionPeriod)
                .walletId(walletId)
                .build();
        Transaction upgradeResult = subscriptionService.upgrade(user, subscriptionType, upgradeRequest);
        return "redirect:/transactions/" + upgradeResult.getId();
    }
}
