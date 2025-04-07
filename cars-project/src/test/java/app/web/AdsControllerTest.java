package app.web;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.service.AdvertService;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.vin.client.VinClient;
import app.vin.model.VinHistory;
import app.vin.service.VinHistoryService;
import app.web.dto.CreateNewAdvertRequest;
import app.web.mapper.DtoMapper;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdsControllerTest {

    @Mock
    private AdvertService advertService;

    @Mock
    private UserService userService;
    
    @Mock
    private BindingResult bindingResult;
    
    @Mock
    private VinClient vinClient;
    
    @Mock
    private SubscriptionService subscriptionService;
    
    @Mock
    private VinHistoryService vinHistoryService;

    @InjectMocks
    private AdsController adsController;

    private User testUser;
    private Advert testAdvert;
    private UUID testUserId;
    private String testAdvertId;
    private AuthenticationMetadata authMetadata;
    private CreateNewAdvertRequest createRequest;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAdvertId = UUID.randomUUID().toString();
        
        // Setup subscription for VIN checks
        testSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .status(app.subscription.model.SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PLUS)
                .vinChecksLeft(5)
                .createdOn(LocalDateTime.now())
                .completedOn(LocalDateTime.now().plusMonths(1))
                .build();
        
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(testSubscription);
        
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .isActive(true)
                .subscriptions(subscriptions)
                .build();
                
        testAdvert = Advert.builder()
                .id(UUID.fromString(testAdvertId))
                .advertName("Test Car")
                .description("Test Description")
                .owner(testUser)
                .carBrand(CarBrand.BMW)
                .carModel("X5")
                .carStatus(CarStatus.AVAILABLE)
                .biddingOpen(true)
                .minBidPrice(BigDecimal.valueOf(15000))
                .buyNowPrice(BigDecimal.valueOf(25000))
                .visible(true)
                .viewCount(10)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusDays(30))
                .vinNumber("WAUZZZ8V5KA123456")
                .build();
                
        authMetadata = new AuthenticationMetadata(
                testUserId,
                "testuser",
                "password",
                UserRole.USER,
                true
        );
        
        createRequest = new CreateNewAdvertRequest();
        createRequest.setAdvertName("Test Car");
        createRequest.setDescription("Test Description");
        createRequest.setCarBrand(CarBrand.BMW);
        createRequest.setCarModel("X5");
        createRequest.setMinBidPrice(BigDecimal.valueOf(15000));
        createRequest.setBuyNowPrice(BigDecimal.valueOf(25000));
    }

    @Test
    @DisplayName("Should return first ads page")
    void shouldReturnFirstAdsPage() {
        // Arrange
        List<Advert> adverts = Collections.singletonList(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAllShownAdvertsByPage(0, "DESC", "createdOn")).thenReturn(adverts);
        when(advertService.getAdvertCount()).thenReturn(1);

        // Act
        ModelAndView modelAndView = adsController.getFirstAdsPage(authMetadata, Optional.empty());

        // Assert
        assertEquals("all-ads", modelAndView.getViewName());
        assertEquals(adverts, modelAndView.getModel().get("adverts"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        assertEquals(1, modelAndView.getModel().get("totalVisibleAds"));
        assertEquals(1, modelAndView.getModel().get("currentPage"));
        assertEquals(1, modelAndView.getModel().get("totalPages"));
    }

    @Test
    @DisplayName("Should return advert info page")
    void shouldReturnAdvertInfoPage() {
        // Arrange
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.getAdvertInfoPage(testAdvertId, authMetadata);

        // Assert
        assertEquals("ad-info", modelAndView.getViewName());
        assertEquals(testAdvert, modelAndView.getModel().get("advert"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        
        verify(advertService).updateAdvert(eq(UUID.fromString(testAdvertId)), any(Advert.class));
    }

    @Test
    @DisplayName("Should return update advert page")
    void shouldReturnUpdateAdvertPage() {
        // Arrange
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        
        CreateNewAdvertRequest expectedRequest = DtoMapper.mapAdvertToCreateNewAdvertRequest(testAdvert);

        // Act
        ModelAndView modelAndView = adsController.updateAdvertPage(testAdvertId, authMetadata);

        // Assert
        assertEquals("new-advert", modelAndView.getViewName());
        assertEquals(testUser, modelAndView.getModel().get("user"));
        
        CreateNewAdvertRequest result = (CreateNewAdvertRequest) modelAndView.getModel().get("createAdvertRequest");
        assertEquals(expectedRequest.getAdvertName(), result.getAdvertName());
        assertEquals(expectedRequest.getDescription(), result.getDescription());
        assertEquals(expectedRequest.getCarBrand(), result.getCarBrand());
    }

    @Test
    @DisplayName("Should return adverts by page")
    void shouldReturnAdvertsByPage() {
        // Arrange
        int page = 2;
        List<Advert> adverts = Collections.singletonList(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAllShownAdvertsByPage(1, "DESC", "createdOn")).thenReturn(adverts);
        when(advertService.getAdvertCount()).thenReturn(25);

        // Act
        ModelAndView modelAndView = adsController.getAdvertsPage(page, authMetadata);

        // Assert
        assertEquals("all-ads", modelAndView.getViewName());
        assertEquals(adverts, modelAndView.getModel().get("adverts"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        assertEquals(25, modelAndView.getModel().get("totalVisibleAds"));
        assertEquals(2, modelAndView.getModel().get("currentPage"));
        assertEquals(2, modelAndView.getModel().get("totalPages"));
    }

    @Test
    @DisplayName("Should return my adverts page")
    void shouldReturnMyAdvertsPage() {
        // Arrange
        List<Advert> adverts = Collections.singletonList(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertsByOwnerId(testUserId)).thenReturn(adverts);

        // Act
        ModelAndView modelAndView = adsController.getMyAdvertsPage(authMetadata);

        // Assert
        assertEquals("my-ads", modelAndView.getViewName());
        assertEquals(adverts, modelAndView.getModel().get("adverts"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        assertEquals(1, modelAndView.getModel().get("totalVisibleAds"));
        assertEquals(1, modelAndView.getModel().get("currentPage"));
        assertEquals(1, modelAndView.getModel().get("totalPages"));
    }

    @Test
    @DisplayName("Should return my reservations page")
    void shouldReturnMyReservationsPage() {
        // Arrange
        List<Advert> adverts = Collections.singletonList(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertsByWinnerId(testUserId)).thenReturn(adverts);

        // Act
        ModelAndView modelAndView = adsController.getMyReservationsPage(authMetadata);

        // Assert
        assertEquals("my-reservations", modelAndView.getViewName());
        assertEquals(adverts, modelAndView.getModel().get("reservedCars"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
    }

    @Test
    @DisplayName("Should return new ad page")
    void shouldReturnNewAdPage() {
        // Arrange
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.getNewAdPage(authMetadata);

        // Assert
        assertEquals("new-advert", modelAndView.getViewName());
        assertEquals(testUser, modelAndView.getModel().get("user"));
        assertInstanceOf(CreateNewAdvertRequest.class, modelAndView.getModel().get("createAdvertRequest"));
    }

    @Test
    @DisplayName("Should save advert and redirect")
    void shouldSaveAdvertAndRedirect() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.saveAdvert(createRequest, bindingResult, authMetadata);

        // Assert
        assertEquals("redirect:/ads", modelAndView.getViewName());
        verify(advertService).createNewAd(createRequest, testUser);
    }

    @Test
    @DisplayName("Should return form with errors when saving advert with invalid data")
    void shouldReturnFormWithErrorsWhenSavingAdvertWithInvalidData() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.saveAdvert(createRequest, bindingResult, authMetadata);

        // Assert
        assertEquals("new-advert", modelAndView.getViewName());
        assertEquals(createRequest, modelAndView.getModel().get("createAdvertRequest"));
    }

    @Test
    @DisplayName("Should add test for update advert method")
    void shouldTestUpdateAdvertMethod() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        
        // Act
        ModelAndView result = adsController.updateAdvert(createRequest, bindingResult, UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("redirect:/ads", result.getViewName());
        verify(advertService).saveAdvert(any(Advert.class));
    }
    
    @Test
    @DisplayName("Should return form with errors when updating advert with invalid data")
    void shouldReturnFormWithErrorsWhenUpdatingAdvertWithInvalidData() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(testUserId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = adsController.updateAdvert(createRequest, bindingResult, UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("new-advert", result.getViewName());
        assertEquals(createRequest, result.getModel().get("createAdvertRequest"));
        verify(advertService, never()).saveAdvert(any(Advert.class));
    }
    
    @Test
    @DisplayName("Should check vin with existing VIN history")
    void shouldCheckVinWithExistingVinHistory() {
        // Arrange
        String vinNumber = "WAUZZZ8V5KA123456";
        VinHistory vinHistory = VinHistory.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .vinNumber(vinNumber)
                .checkedOn(LocalDateTime.now())
                .resultJson("{\"manufacturer\":\"AUDI\",\"model_year\":\"2019\",\"assembly_plant_code\":\"Germany\",\"status\":\"CLEAN\"}")
                .manufacturer("AUDI")
                .modelYear("2019")
                .assemblyPlant("Germany")
                .status("CLEAN")
                .build();
                
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(vinHistoryService.findUserVinCheck(testUserId, vinNumber)).thenReturn(Optional.of(vinHistory));
        
        // Act
        ModelAndView result = adsController.checkVin(UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(testAdvert, result.getModel().get("advert"));
        assertEquals(vinHistory.getResultJson(), result.getModel().get("vinInfo"));
        assertEquals(vinHistory.getManufacturer(), result.getModel().get("vinManufacturer"));
        assertEquals(vinHistory.getModelYear(), result.getModel().get("vinModelYear"));
        assertEquals(vinHistory.getAssemblyPlant(), result.getModel().get("vinAssemblyPlant"));
        assertEquals(vinHistory.getStatus(), result.getModel().get("vinStatus"));
        assertEquals(true, result.getModel().get("vinCheckSuccess"));
        assertEquals(true, result.getModel().get("vinAlreadyChecked"));
        
        // Verify no new calls were made
        verify(vinClient, never()).getVINInformation(anyString());
        verify(subscriptionService, never()).reduceVinChecksWithOne(any(User.class));
    }
    
    @Test
    @DisplayName("Should check vin with new VIN check")
    void shouldCheckVinWithNewVinCheck() {
        // Arrange
        String vinNumber = "WAUZZZ8V5KA123456";
        String vinResponse = "{\"manufacturer\":\"AUDI\",\"model_year\":\"2019\",\"assembly_plant_code\":\"Germany\",\"status\":\"CLEAN\"}";
        
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(vinHistoryService.findUserVinCheck(testUserId, vinNumber)).thenReturn(Optional.empty());
        when(vinClient.getVINInformation(vinNumber)).thenReturn(ResponseEntity.ok(vinResponse));
        
        // Act
        ModelAndView result = adsController.checkVin(UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(testAdvert, result.getModel().get("advert"));
        assertEquals(vinResponse, result.getModel().get("vinInfo"));
        assertEquals("AUDI", result.getModel().get("vinManufacturer"));
        assertEquals("2019", result.getModel().get("vinModelYear"));
        assertEquals("Germany", result.getModel().get("vinAssemblyPlant"));
        assertEquals("CLEAN", result.getModel().get("vinStatus"));
        assertEquals(true, result.getModel().get("vinCheckSuccess"));
        
        // Verify calls were made
        verify(vinClient).getVINInformation(vinNumber);
        verify(subscriptionService).reduceVinChecksWithOne(testUser);
        verify(vinHistoryService).saveVinCheck(
            eq(testUser), 
            eq(vinNumber), 
            eq(vinResponse), 
            eq("AUDI"), 
            eq("2019"), 
            eq("Germany"), 
            eq("CLEAN")
        );
    }
    
    @Test
    @DisplayName("Should handle no vin number available")
    void shouldHandleNoVinNumberAvailable() {
        // Arrange
        testAdvert.setVinNumber(null);
        
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = adsController.checkVin(UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(testAdvert, result.getModel().get("advert"));
        assertEquals("No VIN number available for this vehicle", result.getModel().get("vinCheckError"));
        
        // Verify no calls were made
        verify(vinClient, never()).getVINInformation(anyString());
        verify(subscriptionService, never()).reduceVinChecksWithOne(any(User.class));
    }
    
    @Test
    @DisplayName("Should handle not enough VIN checks left")
    void shouldHandleNotEnoughVinChecksLeft() {
        // Arrange
        // Set VIN checks to 0
        testSubscription.setVinChecksLeft(0);
        
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(vinHistoryService.findUserVinCheck(testUserId, testAdvert.getVinNumber())).thenReturn(Optional.empty());
        
        // Act
        ModelAndView result = adsController.checkVin(UUID.fromString(testAdvertId), authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(testAdvert, result.getModel().get("advert"));
        assertEquals("Not enough VIN checks left. Please upgrade your subscription.", result.getModel().get("vinCheckError"));
        
        // Verify no calls were made
        verify(vinClient, never()).getVINInformation(anyString());
        verify(subscriptionService, never()).reduceVinChecksWithOne(any(User.class));
    }
    
    @Test
    @DisplayName("Should test extractJsonValue method with valid JSON")
    void shouldExtractValueFromValidJson() {
        // Arrange
        String json = "{\"manufacturer\":\"AUDI\",\"model_year\":\"2019\"}";
        
        // Act - We can't directly call the private method so we use checkVin that uses it
        when(advertService.getAdvertById(UUID.fromString(testAdvertId))).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(vinHistoryService.findUserVinCheck(testUserId, testAdvert.getVinNumber())).thenReturn(Optional.empty());
        when(vinClient.getVINInformation(testAdvert.getVinNumber())).thenReturn(ResponseEntity.ok(json));
        
        ModelAndView result = adsController.checkVin(UUID.fromString(testAdvertId), authMetadata);
        
        // Assert - indirectly testing extractJsonValue
        assertEquals("AUDI", result.getModel().get("vinManufacturer"));
        assertEquals("2019", result.getModel().get("vinModelYear"));
    }
    
    @Test
    @DisplayName("Should reserve advert and redirect")
    void shouldReserveAdvertAndRedirect() {
        // Arrange
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.reserveAdvert(UUID.fromString(testAdvertId), authMetadata);

        // Assert
        assertEquals("redirect:/ads", modelAndView.getViewName());
        verify(advertService).reserveCarAdvert(UUID.fromString(testAdvertId), testUser);
    }
}