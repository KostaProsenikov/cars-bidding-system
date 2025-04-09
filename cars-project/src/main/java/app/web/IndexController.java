package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import app.web.dto.LoginRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import app.user.service.UserService;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping ("")
public class IndexController {

    private final UserService userService;
    private final AdvertService advertService;

    @Autowired
    public IndexController(UserService userService, AdvertService advertService) {
        this.userService = userService;
        this.advertService = advertService;
    }

    @GetMapping("")
    public ModelAndView getIndexPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                   @RequestParam(required = false) String path) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        User user = new User();
        if (authenticationMetadata != null) {
            user = userService.getById(authenticationMetadata.getUserId());
        }
        List<Advert> adverts = advertService.getFirst20VisibleAdverts();
        modelAndView.addObject("user", user);
        modelAndView.addObject("adverts", adverts);
        modelAndView.addObject("currentUri", path != null ? path : "/");
        return modelAndView;
    }

    @GetMapping("/login")
    public ModelAndView GetLoginPage(@RequestParam(value = "error", required = false) String errorParam) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());
        modelAndView.addObject("currentUri", "/login");

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Invalid username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView GetRegisterPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());
        modelAndView.addObject("currentUri", "/register");
        return modelAndView;
    }

    @GetMapping("/about-us")
    public ModelAndView GetAboutUsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        return getModelAndView(authenticationMetadata, "about-us" );
    }

    @GetMapping("/contact")
    public ModelAndView GetContactsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        return getModelAndView(authenticationMetadata, "contact");
    }

    private ModelAndView getModelAndView(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, String viewName) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(viewName);
        User user = new User();
        if (authenticationMetadata != null) {
            user = userService.getById(authenticationMetadata.getUserId());
        }
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/" + viewName);
        return modelAndView;
    }

    @PostMapping ("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        boolean errorsFound = false;
        if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 50) {
            bindingResult.rejectValue("password", "<PASSWORD>", "Password must be between 8 and 50 characters");
            errorsFound = true;
        } else if (!registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
            bindingResult.rejectValue("repeatPassword", "<PASSWORD>", "Passwords do not match");
            errorsFound = true;
        }

        if (errorsFound) {
            return new ModelAndView("register");
        }

        User user = userService.register(registerRequest);

        if (user != null) {
            return new ModelAndView("redirect:/login");
        } else {
            return new ModelAndView("register");
        }
    }
}
