package app.web;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

    @GetMapping ("/new")
    public ModelAndView getNewAdPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-advert");
        modelAndView.addObject("createAdvertRequest", new CreateNewAdvertRequest());
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
