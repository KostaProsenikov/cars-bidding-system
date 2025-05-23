package app.web;

import app.exception.AdvertNotFoundException;
import app.exception.UsernameNotFoundException;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.utils.Utilities;
import app.vin.client.VinClient;
import app.vin.model.VinHistory;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping ("/users")
public class UsersController {

    private final UserService userService;
    private final VinClient vinClient;

    @Autowired
    public UsersController(UserService userService, VinClient vinClient) {
        this.userService = userService;
        this.vinClient = vinClient;
    }

    @GetMapping ("/my-profile")
    public ModelAndView getProfilePage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
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
                    
                    if (item instanceof Map<?, ?> itemMap) {
                        for (Object key : itemMap.keySet()) {
                            recordMap.put(key.toString(), itemMap.get(key));
                        }
                    } else if (item instanceof VinHistory history) {
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
                modelAndView.addObject("vinHistory", List.of());
                System.out.println("No VIN history found in microservice");
            }
        } catch (Exception e) {
            System.err.println("Error fetching VIN history from microservice: " + e.getMessage());
            modelAndView.addObject("vinHistory", List.of());
            System.out.println("Error retrieving VIN history, showing empty list");
        }
        
        modelAndView.setViewName("my-profile");
        return modelAndView;
    }

    @PostMapping("/vin-history/delete-old")
    public String deleteOldVinChecks(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, RedirectAttributes redirectAttributes) {
        UUID userId = authenticationMetadata.getUserId();
        // Retrieve user's VIN history
        ResponseEntity<Object[]> userVinChecksResponse = vinClient.getUserVinHistory(userId);
        Object[] vinChecksArray = userVinChecksResponse.getBody();

        if (vinChecksArray != null) {
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);

            for (Object vinCheckObj : vinChecksArray) {
                Map<String, Object> vinCheck = (Map<String, Object>)vinCheckObj;
                UUID vinCheckId = UUID.fromString(vinCheck.get("id").toString());
                LocalDateTime checkDate = LocalDateTime.parse(vinCheck.get("checkedAt").toString());

                if (checkDate.isBefore(thresholdDate)) {
                    vinClient.deleteVinCheck(userId, vinCheckId);
                }
            }
        }

        redirectAttributes.addFlashAttribute("success", "VIN checks older than 30 days were successfully deleted.");
        return "redirect:/users/vin-history";
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
                    
                    if (item instanceof Map<?, ?> itemMap) {
                        // If it's already a map, normalize keys
                        for (Object key : itemMap.keySet()) {
                            Object value = itemMap.get(key);
                            if ("checkedAt".equals(key.toString()) && value instanceof String) {
                                recordMap.put(
                                        key.toString(),
                                        LocalDateTime.parse((String) value, DateTimeFormatter.ISO_DATE_TIME)
                                );
                            } else {
                                recordMap.put(key.toString(), value);
                            }
                        }

                    } else if (item instanceof VinHistory history) {
                        // If it's a VinHistory entity, convert to map
                        recordMap.put("vinNumber", history.getVinNumber());
                        recordMap.put("manufacturer", history.getManufacturer());
                        recordMap.put("modelYear", history.getModelYear());
                        recordMap.put("assemblyPlant", history.getAssemblyPlant());
                        recordMap.put("status", history.getStatus());
                        recordMap.put("checked", history.getCheckedOn().toLocalTime());
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
    public ModelAndView toggleUserActiveStatus(@PathVariable String id,
                 @RequestParam boolean isActive,
                                              RedirectAttributes redirectAttributes) {
        try {
            UUID userId = Utilities.isValidUUID(id) ? UUID.fromString(id) : null;
            if (userId == null) {
                throw new AdvertNotFoundException("User with id [%s] is not found and cannot be updated!".formatted(id));
            }
            userService.updateUserActiveStatus(userId, isActive);
            String statusMessage = isActive ? "activated" : "deactivated";
            redirectAttributes.addFlashAttribute("success", "User has been " + statusMessage + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return new ModelAndView("redirect:/users");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle-admin")
    public ModelAndView toggleAdminStatus(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            UUID userId = Utilities.isValidUUID(id) ? UUID.fromString(id) : null;
            if (userId == null) {
                throw new AdvertNotFoundException("User with id [%s] not found and cannot be updated!".formatted(id));
            }
            User user = userService.getById(userId);
            UserRole role = user.getRole().name().equals(UserRole.ADMIN.name()) ? UserRole.USER : UserRole.ADMIN;
            userService.updateUserRole(userId, role);
            String statusMessage = role.name().substring(0, 1).toUpperCase() + role.name().substring(1).toLowerCase();
            String username = user.getUsername();
            redirectAttributes.addFlashAttribute("success", "User role [" + statusMessage + "] has been set to [" + username + "] successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return new ModelAndView("redirect:/users");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotValid() {
        return "redirect:/index?error=1";
    }
}
