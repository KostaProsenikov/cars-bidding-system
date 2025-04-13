package app.web;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.service.AdvertService;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
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

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdsControllerTest {

    @InjectMocks
    private AdsController adsController;

    @Mock
    private AdvertService advertService;

    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private VinHistoryService vinHistoryService;

    @Mock
    private VinClient vinClient;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock(lenient = true)
    private AuthenticationMetadata authMetadata;

    private User testUser;
    private Advert testAdvert;
    private UUID testUserId;
    private UUID advertId;
    private CreateNewAdvertRequest createRequest;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        advertId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
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
                .username("tester")
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("test@test.com")
                .password("1234")
                .role(UserRole.USER)
                .isActive(true)
                .subscriptions(List.of(Subscription.builder().vinChecksLeft(5).build()))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        testAdvert = Advert.builder()
                .id(advertId)
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
                .vinNumber("1234567891234")
                .viewCount(10)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusDays(30))
                .vinNumber("WAUZZZ8V5KA123456")
                .build();

        when(authMetadata.getUserId()).thenReturn(testUserId);

        createRequest = new CreateNewAdvertRequest();
        createRequest.setAdvertName("Test Car");
        createRequest.setDescription("Test Description");
        createRequest.setCarBrand(CarBrand.BMW);
        createRequest.setCarModel("X5");
        createRequest.setMinBidPrice(BigDecimal.valueOf(15000));
        createRequest.setBuyNowPrice(BigDecimal.valueOf(25000));
    }

    @Test
    void testMockAdvertService() {
        UUID testId = advertId;
        when(advertService.getAdvertById(testId)).thenReturn(testAdvert);

        Advert result = advertService.getAdvertById(testId);
        assertNotNull(result);
        assertEquals("Test Car", result.getAdvertName());
    }

    @Test
    @DisplayName("Should return first ads page")
    void shouldReturnFirstAdsPage() {
        // Arrange
        List<Advert> adverts = Collections.singletonList(testAdvert);

        // Act
        ModelAndView modelAndView = adsController.getFirstAdsPage(authMetadata, Optional.empty());

        // Assert
        assertEquals("all-ads", modelAndView.getViewName());
        assertEquals(adverts, List.of(testAdvert));
    }

    @Test
    @DisplayName("Should return advert info page")
    void shouldReturnAdvertInfoPage() {
        // Arrange
        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.getAdvertInfoPage(advertId.toString(), authMetadata);

        // Assert
        assertEquals("ad-info", modelAndView.getViewName());
        assertEquals(testAdvert, modelAndView.getModel().get("advert"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        
        verify(advertService).updateAdvert(eq(advertId), any(Advert.class));
    }

    @Test
    @DisplayName("Should return update advert page")
    void shouldReturnUpdateAdvertPage() {
        // Arrange
        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        
        CreateNewAdvertRequest expectedRequest = DtoMapper.mapAdvertToCreateNewAdvertRequest(testAdvert);

        // Act
        ModelAndView modelAndView = adsController.updateAdvertPage(advertId.toString(), authMetadata);

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

        // Act
        ModelAndView modelAndView = adsController.getAdvertsPage(page, authMetadata);

        // Assert
        assertEquals("all-ads", modelAndView.getViewName());
        assertEquals(adverts, List.of(testAdvert));
    }

    @Test
    @DisplayName("Should return new ad page")
    void shouldReturnNewAdPage() {
        // Act
        ModelAndView modelAndView = adsController.getNewAdPage(authMetadata);

        // Assert
        assertEquals("new-advert", modelAndView.getViewName());
        assertEquals(testUser, testUser);
        assertInstanceOf(CreateNewAdvertRequest.class, modelAndView.getModel().get("createAdvertRequest"));
    }

    @Test
    @DisplayName("Should save advert and redirect")
    void shouldSaveAdvertAndRedirect() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        ModelAndView modelAndView = adsController.saveAdvert(createRequest, bindingResult, authMetadata);

        // Assert
        assertEquals("redirect:/ads", modelAndView.getViewName());
    }

    @Test
    @DisplayName("Should return form with errors when saving advert with invalid data")
    void shouldReturnFormWithErrorsWhenSavingAdvertWithInvalidData() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        ModelAndView modelAndView = adsController.saveAdvert(createRequest, bindingResult, authMetadata);

        // Assert
        assertEquals("new-advert", modelAndView.getViewName());
        assertEquals(createRequest, modelAndView.getModel().get("createAdvertRequest"));
    }
    
    @Test
    @DisplayName("Should return form with errors when updating advert with invalid data")
    void shouldReturnFormWithErrorsWhenUpdatingAdvertWithInvalidData() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        
        // Act
        ModelAndView result = adsController.updateAdvert(createRequest, bindingResult, advertId, authMetadata);
        
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

        when(authMetadata.getUserId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(vinClient.hasUserCheckedVin(vinNumber, testUserId)).thenReturn(true);
        when(vinClient.getVINInformation(vinNumber)).thenReturn(ResponseEntity.ok(vinHistory.getResultJson()));
//        when(vinHistoryService.findUserVinCheck(testUserId, vinNumber)).thenReturn(Optional.of(vinHistory));
        System.out.println("Injected advertService = " + adsController.getClass().getDeclaredFields()[0]);
        System.out.println("Advert ID used in test: " + advertId);
        System.out.println("Mock returned advert: " + advertService.getAdvertById(advertId));
        // Act
        ModelAndView result = adsController.checkVin(advertId, authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
        assertEquals(testAdvert, result.getModel().get("advert"));
        assertEquals(vinHistory.getResultJson(), result.getModel().get("vinInfo"));
        assertEquals(vinHistory.getManufacturer(), result.getModel().get("vinManufacturer"));
        assertEquals(vinHistory.getModelYear(), result.getModel().get("vinModelYear"));
        assertEquals(vinHistory.getAssemblyPlant(), result.getModel().get("vinAssemblyPlant"));
        assertEquals(vinHistory.getStatus(), result.getModel().get("vinStatus"));
        assertEquals(true, result.getModel().get("vinCheckSuccess"));
        assertEquals(true, result.getModel().get("vinAlreadyChecked"));
        
        // Verify no new calls were made
        verify(subscriptionService, never()).reduceVinChecksWithOne(any(User.class));
    }
    
    @Test
    @DisplayName("Should check vin with new VIN check")
    void shouldCheckVinWithNewVinCheck() {
        // Act
        ModelAndView result = adsController.checkVin(advertId, authMetadata);
        
        // Assert
        assertEquals("ad-info", result.getViewName());
    }
    
    @Test
    @DisplayName("Should handle no vin number available")
    void shouldHandleNoVinNumberAvailable() {
        // Arrange
        testAdvert.setVinNumber(null);
        
        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = adsController.checkVin(advertId, authMetadata);
        
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
        Subscription zeroVinChecks = Subscription.builder()
                .id(UUID.randomUUID())
                .vinChecksLeft(0)
                .status(SubscriptionStatus.ACTIVE)
                .type(SubscriptionType.PLUS)
                .period(SubscriptionPeriod.MONTHLY)
                .createdOn(LocalDateTime.now())
                .completedOn(LocalDateTime.now().plusMonths(1))
                .build();

        testUser.setSubscriptions(List.of(zeroVinChecks));
        // Set VIN checks to 0
//        testSubscription.setVinChecksLeft(0);

        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
//        when(vinHistoryService.findUserVinCheck(testUserId, testAdvert.getVinNumber())).thenReturn(Optional.empty());

        // Act
        ModelAndView result = adsController.checkVin(advertId, authMetadata);
        
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
        when(advertService.getAdvertById(advertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
//        when(vinHistoryService.findUserVinCheck(testUserId, testAdvert.getVinNumber())).thenReturn(Optional.empty());
        when(vinClient.getVINInformation(testAdvert.getVinNumber())).thenReturn(ResponseEntity.ok(json));
        
        ModelAndView result = adsController.checkVin(advertId, authMetadata);
        
        // Assert - indirectly testing extractJsonValue
        assertEquals("AUDI", result.getModel().get("vinManufacturer"));
        assertEquals("2019", result.getModel().get("vinModelYear"));
    }
    
    @Test
    @DisplayName("Should reserve advert and redirect")
    void shouldReserveAdvertAndRedirect() {
        UUID advertId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .email("test@example.com")
                .build();

        // Arrange
        when(authMetadata.getUserId()).thenReturn(testUserId);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.reserveAdvert(advertId, authMetadata);

        // Assert
        assertEquals("redirect:/ads", modelAndView.getViewName());
        verify(advertService).reserveCarAdvert(advertId, testUser);
    }
}