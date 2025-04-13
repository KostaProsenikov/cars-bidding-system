package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.exception.AdvertNotFoundException;
import app.exception.DomainException;
import app.exception.NumberFormatExceptionHandler;
import app.exception.UserNotAllowedToEditAdvert;
import app.security.AuthenticationMetadata;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.service.UserService;
import app.utils.Utilities;
import app.vin.client.VinClient;
import app.vin.service.VinHistoryService;
import app.web.dto.CreateNewAdvertRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/ads")
public class AdsController {

    private final AdvertService advertService;
    private final UserService userService;
    private final VinClient vinClient;
    private final SubscriptionService subscriptionService;
    private final VinHistoryService vinHistoryService;

    public AdsController(AdvertService advertService, UserService userService,
                        VinClient vinClient, SubscriptionService subscriptionService,
                        VinHistoryService vinHistoryService) {
        this.advertService = advertService;
        this.userService = userService;
        this.vinClient = vinClient;
        this.subscriptionService = subscriptionService;
        this.vinHistoryService = vinHistoryService;
    }

    @GetMapping("")
    public ModelAndView getFirstAdsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                        @RequestParam("error") Optional<Integer> error) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("all-ads");
        int currentPage = 0;
        String sortType = "DESC";
        String sortField = "createdOn";
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Advert> adverts = advertService.getAllShownAdvertsByPage(currentPage, sortType, sortField);
        int totalVisibleAds = advertService.getAdvertCount();
        int totalPages = (int) Math.ceil((double) totalVisibleAds / 20);
        if (error.isPresent() && error.get() == 1) {
            modelAndView.addObject("error", "Advert not found!");
        } else if (error.isPresent() && error.get() == 2) {
            modelAndView.addObject("error", "You are not allowed to edit this advert!");
        }
        modelAndView.addObject("adverts", adverts);
        modelAndView.addObject("user", user);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", currentPage + 1);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("currentUri", "/ads"); // Add current URI for menu highlighting
        return modelAndView;
    }

    @GetMapping("{id}/info")
    public ModelAndView getAdvertInfoPage(@PathVariable String id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID advertId = Utilities.isValidUUID(id) ? UUID.fromString(id) : null;
        if (advertId == null) {
            throw new AdvertNotFoundException("Advert with id [%s] is found!".formatted(id));
        }
        Advert advert =  advertService.getAdvertById(advertId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("ad-info");
//        Update view count of the advert
        advert.setViewCount(advert.getViewCount() + 1);
        advertService.updateAdvert(UUID.fromString(id), advert);
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("advert", advert);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("{id}/edit")
    public ModelAndView updateAdvertPage(@PathVariable String id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        UUID advertId = Utilities.isValidUUID(id) ? UUID.fromString(id) : null;
        if (advertId == null) {
            throw new AdvertNotFoundException("Advert with id [%s] found and cannot be updated!".formatted(id));
        }
        Advert advert = advertService.getAdvertById(advertId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("new-advert");
//        Update view count of the advert
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.addObject("createAdvertRequest", DtoMapper.mapAdvertToCreateNewAdvertRequest(advert));
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("page/{page}")
    public ModelAndView getAdvertsPage(@PathVariable int page, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("all-ads");
        User user = userService.getById(authenticationMetadata.getUserId());
        page = page < 1 ? 1 : page - 1;
        String sortType = "DESC";
        String sortField = "createdOn";
        List<Advert> adverts = advertService.getAllShownAdvertsByPage(page, sortType, sortField);
        modelAndView.addObject("adverts", adverts);
        int totalVisibleAds = advertService.getAdvertCount();
        int totalPages = (int) Math.ceil((double) totalVisibleAds / 20);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", page + 1);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/my-ads")
    public ModelAndView getMyAdvertsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-ads");
        User user = userService.getById(authenticationMetadata.getUserId());
        List<Advert> adverts = advertService.getAdvertsByOwnerId(user.getId());
        int totalVisibleAds = adverts.size();
        int totalPages = 1;
        int currentPage = 0;
        modelAndView.addObject("adverts", adverts);
        modelAndView.addObject("totalVisibleAds", totalVisibleAds);
        modelAndView.addObject("currentPage", currentPage + 1);
        modelAndView.addObject("totalPages", totalPages);
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/ads/my-ads");
        return modelAndView;
    }

    @GetMapping("/my-reservations")
    public ModelAndView getMyReservationsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("my-reservations");
        List<Advert> reservedCarAdverts = advertService.getAdvertsByWinnerId(user.getId());
        modelAndView.addObject("reservedCars", reservedCarAdverts);
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/ads/my-reservations");
        return modelAndView;
    }

//    @GetMapping("seed-adverts")
//    public ResponseEntity<String> seedAdverts() {
//        advertSeederService.seedAdverts();
//        return ResponseEntity.ok("Adverts seeded successfully!");
//    }

    @GetMapping ("/new")
    public ModelAndView getNewAdPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(authenticationMetadata.getUserId());
        modelAndView.setViewName("new-advert");
        modelAndView.addObject("createAdvertRequest", new CreateNewAdvertRequest());
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentUri", "/ads/new");
        return modelAndView;
    }

    @PostMapping("/new")
    public ModelAndView saveAdvert(
            @Valid CreateNewAdvertRequest createAdvertRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-advert");
            modelAndView.addObject("org.springframework.validation.BindingResult.createAdvertRequest", bindingResult);
            modelAndView.addObject("user", authenticationMetadata.getUserId() != null ? userService.getById(authenticationMetadata.getUserId()) : null);
            modelAndView.addObject("createAdvertRequest", createAdvertRequest);
            return modelAndView;
        }
        User user = userService.getById(authenticationMetadata.getUserId());
        advertService.createNewAd(createAdvertRequest, user);
        return new ModelAndView("redirect:/ads");
    }

    @PutMapping("/{id}/update")
    public ModelAndView updateAdvert(@Valid CreateNewAdvertRequest createAdvertRequest,
                                     BindingResult bindingResult,
                                     @PathVariable UUID id,
                                     @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("new-advert");
            modelAndView.addObject("createAdvertRequest", createAdvertRequest);
            modelAndView.addObject("org.springframework.validation.BindingResult.createAdvertRequest", bindingResult);
            modelAndView.addObject("user", userService.getById(authenticationMetadata.getUserId()));
            return modelAndView;
        }
        User user = userService.getById(authenticationMetadata.getUserId());
        Advert updatedAdvert = DtoMapper.mapCreateNewAdvertRequestToAdvert(createAdvertRequest, advertService.getAdvertById(id));
        if (updatedAdvert.getOwner().getId() != user.getId() && !user.getRole().name().equals("ADMIN")) {
            throw new UserNotAllowedToEditAdvert("You are not allowed to edit this advert!");
        }

        advertService.saveAdvert(updatedAdvert);
        return new ModelAndView("redirect:/ads");
    }

    @PostMapping("{id}/reserve")
    public ModelAndView reserveAdvert(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        User user = userService.getById(authenticationMetadata.getUserId());
        if (user.getId() == null) {
            throw new DomainException("You are not logged in!");
        }
        advertService.reserveCarAdvert(id, user);
        return new ModelAndView("redirect:/ads");
    }

    @GetMapping("/{id}/check-vin")
    public ModelAndView checkVin(@PathVariable UUID id, @AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {
        Advert advert = advertService.getAdvertById(id);
        User user = userService.getById(authenticationMetadata.getUserId());
        ModelAndView modelAndView = new ModelAndView("ad-info");

        // Check if VIN exists
        if (advert != null && advert.getVinNumber() != null && !advert.getVinNumber().isEmpty()) {
            boolean alreadyChecked = vinClient.hasUserCheckedVin(advert.getVinNumber(), user.getId());

            if (alreadyChecked) {
                System.out.println("User already checked this VIN: " + advert.getVinNumber());
                modelAndView.addObject("vinAlreadyChecked", true);

                ResponseEntity<String> vinResponse = vinClient.getVINInformation(advert.getVinNumber());
                String responseBody = vinResponse.getBody();
                modelAndView.addObject("vinInfo", responseBody);

                if (responseBody != null && responseBody.contains("manufacturer")) {
                    modelAndView.addObject("vinManufacturer", extractJsonValue(responseBody, "manufacturer"));
                    modelAndView.addObject("vinModelYear", extractJsonValue(responseBody, "model_year"));
                    modelAndView.addObject("vinAssemblyPlant", extractJsonValue(responseBody, "assembly_plant_code"));
                    modelAndView.addObject("vinStatus", extractJsonValue(responseBody, "status"));
                    modelAndView.addObject("vinCheckSuccess", true);
                }
            }
            else if (!user.getSubscriptions().isEmpty() && user.getSubscriptions().get(0).getVinChecksLeft() > 0) {
                try {
                    // Reduce user vin checks count - only for new checks
                    subscriptionService.reduceVinChecksWithOne(user);

                    user = userService.getById(authenticationMetadata.getUserId());
                    // First get basic VIN information from microservice
                    ResponseEntity<String> vinResponse = vinClient.getVINInformation(advert.getVinNumber());
                    String responseBody = vinResponse.getBody();

                    // Store the raw response for debugging
                    modelAndView.addObject("vinInfo", responseBody);

                    // Default values for invalid/error cases
                    String manufacturer = "Unknown";
                    String modelYear = "Unknown";
                    String assemblyPlant = "Unknown";
                    String status = "Invalid";
                    boolean isValid = false;

                    // Extract information from the JSON response for the view
                    if (responseBody != null && responseBody.contains("manufacturer")) {
                        manufacturer = extractJsonValue(responseBody, "manufacturer");
                        modelAndView.addObject("vinManufacturer", manufacturer);
                        modelYear = extractJsonValue(responseBody, "model_year");
                        modelAndView.addObject("vinModelYear", modelYear);
                        assemblyPlant = extractJsonValue(responseBody, "assembly_plant_code");
                        modelAndView.addObject("vinAssemblyPlant", assemblyPlant);
                        status = extractJsonValue(responseBody, "status");
                        modelAndView.addObject("vinStatus", status);
                        isValid = true;
                    } else {
                        // Set view attributes for invalid VIN
                        modelAndView.addObject("vinManufacturer", manufacturer);
                        modelAndView.addObject("vinModelYear", modelYear);
                        modelAndView.addObject("vinAssemblyPlant", assemblyPlant);
                        modelAndView.addObject("vinStatus", status);
                    }

                    // Save the VIN check using the microservice's POST endpoint
                    try {
                        System.out.println("Before calling VIN microservice save for: " + advert.getVinNumber());
                        ResponseEntity<Map<String, Object>> savedCheckResponse = vinClient.saveVinCheck(advert.getVinNumber(), user.getId());
                        System.out.println("VIN check saved to microservice with status: " + savedCheckResponse.getStatusCode());
                        System.out.println("Response body: " + savedCheckResponse.getBody());
                    } catch (Exception ex) {
                        // Log the error but don't use local fallback
                        System.err.println("Error saving to VIN microservice: " + ex.getMessage());
                        System.out.println("VIN check not saved due to microservice error");
                    }

                    modelAndView.addObject("vinCheckSuccess", isValid);
                    if (!isValid) {
                        modelAndView.addObject("vinCheckWarning", "The VIN appears to be invalid or could not be verified. The check has been recorded in your history.");
                    }
                } catch (Exception e) {
                    // In case of microservice failure, try to save via microservice one more time
                    try {
                        vinClient.saveVinCheck(advert.getVinNumber(), user.getId());
                    } catch (Exception ex) {
                        // If microservice completely fails, fall back to local save
                        vinHistoryService.saveVinCheck(
                            user,
                            advert.getVinNumber(),
                            "Error: " + e.getMessage(),
                            "Error",
                            "Error",
                            "Error",
                            "Error"
                        );
                    }

                    modelAndView.addObject("user", user);
                    modelAndView.addObject("vinCheckError", "Could not verify VIN: " + e.getMessage());
                }
            }
            else {
                modelAndView.addObject("vinCheckError", "Not enough VIN checks left. Please upgrade your subscription.");
            }
        } else {
            modelAndView.addObject("vinCheckError", "No VIN number available for this vehicle");
        }

        modelAndView.addObject("advert", advert);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    private String extractJsonValue(String json, String key) {
        // Simple JSON value extractor
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) {
            return "N/A";
        }
        
        int valueStart = keyIndex + searchKey.length();
        int commaIndex = json.indexOf(",", valueStart);
        int braceIndex = json.indexOf("}", valueStart);
        
        int valueEnd = Math.min(
            commaIndex != -1 ? commaIndex : Integer.MAX_VALUE,
            braceIndex != -1 ? braceIndex : Integer.MAX_VALUE
        );
        
        if (valueEnd == Integer.MAX_VALUE) {
            return "N/A";
        }
        
        String value = json.substring(valueStart, valueEnd).trim();
        // Remove quotes if present
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        
        return value;
    }

    @ExceptionHandler(AdvertNotFoundException.class)
    public String handleAdvertNotFound() {
        return "redirect:/ads?error=1";
    }

    @ExceptionHandler(UserNotAllowedToEditAdvert.class)
    public String handleUserNotAllowedToEditAdvert() {
        return "redirect:/ads?error=2";
    }

    @ExceptionHandler(NumberFormatExceptionHandler.class)
    public String handleUUIDNotValid() {
        return "redirect:/ads?error=3";
    }
}
