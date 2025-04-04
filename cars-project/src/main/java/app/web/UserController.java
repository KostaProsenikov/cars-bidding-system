package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.vin.client.VinClient;
import app.vin.model.VinHistory;
import app.vin.service.VinHistoryService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping ("/users")
public class UserController {

    private final UserService userService;
    private final VinHistoryService vinHistoryService;
    private final VinClient vinClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserController(UserService userService, VinHistoryService vinHistoryService, VinClient vinClient, ObjectMapper objectMapper) {
        this.userService = userService;
        this.vinHistoryService = vinHistoryService;
        this.vinClient = vinClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping ("/my-profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UserEditRequest userEditRequest) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));
        modelAndView.addObject("currentUri", "/users/my-profile");
        
        // Fetch VIN check history from microservice - no local fallback
        try {
            System.out.println("Requesting VIN history for user: " + user.getId());
            
            ResponseEntity<Object[]> vinHistoryResponse = vinClient.getUserVinHistory(user.getId());
            Object[] responseBody = vinHistoryResponse.getBody();
            
            if (responseBody != null) {
                // Convert to list of maps with consistent keys
                List<HashMap<String, Object>> historyList = new java.util.ArrayList<>();
                for (Object item : responseBody) {
                    HashMap<String, Object> recordMap = new HashMap<>();
                    
                    if (item instanceof Map) {
                        // If it's already a map, normalize keys
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        for (Object key : itemMap.keySet()) {
                            recordMap.put(key.toString(), itemMap.get(key));
                        }
                    } else if (item instanceof VinHistory) {
                        // If it's a VinHistory entity, convert to map
                        VinHistory history = (VinHistory) item;
                        recordMap.put("vinNumber", history.getVinNumber());
                        recordMap.put("manufacturer", history.getManufacturer());
                        recordMap.put("modelYear", history.getModelYear());
                        recordMap.put("assemblyPlant", history.getAssemblyPlant());
                        recordMap.put("status", history.getStatus());
                        recordMap.put("checkedAt", history.getCheckedOn().toString());
                    }
                    
                    historyList.add(recordMap);
                }
                
                modelAndView.addObject("vinHistory", historyList);
                System.out.println("VIN history entries from microservice: " + historyList.size());
            } else {
                // Empty history list if no results
                modelAndView.addObject("vinHistory", List.of());
                System.out.println("No VIN history found in microservice");
            }
        } catch (Exception e) {
            System.err.println("Error fetching VIN history from microservice: " + e.getMessage());
            // Empty history list on error
            modelAndView.addObject("vinHistory", List.of());
            System.out.println("Error retrieving VIN history, showing empty list");
        }
        
        modelAndView.setViewName("my-profile");
        return modelAndView;
    }
    
    @GetMapping("/vin-history")
    public ModelAndView getVinHistoryPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("vin-history");
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/users/vin-history");
        
        // Use VIN microservice to get history - no local fallback
        try {
            System.out.println("Requesting VIN history for user in vin-history page: " + user.getId());
            
            ResponseEntity<Object[]> vinHistoryResponse = vinClient.getUserVinHistory(user.getId());
            Object[] responseBody = vinHistoryResponse.getBody();
            
            if (responseBody != null) {
                // Convert to list of maps with consistent keys
                List<HashMap<String, Object>> historyList = new java.util.ArrayList<>();
                for (Object item : responseBody) {
                    HashMap<String, Object> recordMap = new HashMap<>();
                    
                    if (item instanceof Map) {
                        // If it's already a map, normalize keys
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        for (Object key : itemMap.keySet()) {
                            recordMap.put(key.toString(), itemMap.get(key));
                        }
                    } else if (item instanceof VinHistory) {
                        // If it's a VinHistory entity, convert to map
                        VinHistory history = (VinHistory) item;
                        recordMap.put("vinNumber", history.getVinNumber());
                        recordMap.put("manufacturer", history.getManufacturer());
                        recordMap.put("modelYear", history.getModelYear());
                        recordMap.put("assemblyPlant", history.getAssemblyPlant());
                        recordMap.put("status", history.getStatus());
                        recordMap.put("checkedAt", history.getCheckedOn().toString());
                    }
                    
                    historyList.add(recordMap);
                }
                
                modelAndView.addObject("vinHistory", historyList);
                System.out.println("VIN history entries from microservice for vin-history page: " + historyList.size());
            } else {
                // Empty history list if no results
                modelAndView.addObject("vinHistory", List.of());
                System.out.println("No VIN history found in microservice for vin-history page");
            }
        } catch (Exception e) {
            System.err.println("Error fetching VIN history from microservice for vin-history page: " + e.getMessage());
            // Empty history list on error
            modelAndView.addObject("vinHistory", List.of());
            System.out.println("Error retrieving VIN history, showing empty list");
        }
        
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
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        List<User> allUsers = userService.getAllUsers();
        User admin = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("users", allUsers);
        modelAndView.addObject("user", admin);
        modelAndView.addObject("currentUri", "/users");
        modelAndView.setViewName("admin/users");
        return modelAndView;
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle-active")
    public ModelAndView toggleUserActiveStatus(@PathVariable UUID id, 
                                              @RequestParam boolean isActive,
                                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserActiveStatus(id, isActive);
            String statusMessage = isActive ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "User has been " + statusMessage + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return new ModelAndView("redirect:/users");
    }
}
