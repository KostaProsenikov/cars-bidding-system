package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping ("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping ("/my-profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UserEditRequest userEditRequest) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        modelAndView.setViewName("my-profile");
        return modelAndView;
    }

    @PutMapping ("/{$userId}/profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            User user = userService.getById(authenticationMetadata.getUserId());
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            modelAndView.setViewName("my-profile");
            return modelAndView;
        }
        userService.updateUserDetails(authenticationMetadata.getUserId(), userEditRequest);
        return new ModelAndView("redirect:/users/my-profile");
    }
}
