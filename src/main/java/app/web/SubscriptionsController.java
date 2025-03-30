package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping ("/subscriptions")
public class SubscriptionsController {

    private final UserService userService;

    @Autowired
    public SubscriptionsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ModelAndView getSubscriptionsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscriptions");
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("user", user);
        return modelAndView;
    }
}
