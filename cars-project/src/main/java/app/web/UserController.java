package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.vin.model.VinHistory;
import app.vin.service.VinHistoryService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping ("/users")
public class UserController {

    private final UserService userService;
    private final VinHistoryService vinHistoryService;

    @Autowired
    public UserController(UserService userService, VinHistoryService vinHistoryService) {
        this.userService = userService;
        this.vinHistoryService = vinHistoryService;
    }

    @GetMapping ("/my-profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UserEditRequest userEditRequest) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        
        // Add VIN check history to the model
        List<VinHistory> vinHistory = vinHistoryService.getUserVinHistory(user.getId());
        modelAndView.addObject("vinHistory", vinHistory);
        
        modelAndView.setViewName("my-profile");
        return modelAndView;
    }
    
    @GetMapping("/vin-history")
    public ModelAndView getVinHistoryPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        List<VinHistory> vinHistory = vinHistoryService.getUserVinHistory(user.getId());
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vin-history");
        modelAndView.addObject("user", user);
        modelAndView.addObject("vinHistory", vinHistory);
        
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
