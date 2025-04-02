package app.web;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.service.AdvertService;
import app.bid.service.BidsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.CreateNewAdvertRequest;
import app.web.mapper.DtoMapper;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdsControllerTest {

    @Mock
    private AdvertService advertService;

    @Mock
    private UserService userService;

    @Mock
    private BidsService bidsService;
    
    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AdsController adsController;

    private User testUser;
    private Advert testAdvert;
    private UUID testUserId;
    private UUID testAdvertId;
    private AuthenticationMetadata authMetadata;
    private CreateNewAdvertRequest createRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAdvertId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .isActive(true)
                .build();
                
        testAdvert = Advert.builder()
                .id(testAdvertId)
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
        List<Advert> adverts = Arrays.asList(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAllShownAdvertsByPage(0, "DESC", "createdOn")).thenReturn(adverts);
        when(advertService.getAdvertCount()).thenReturn(1);

        // Act
        ModelAndView modelAndView = adsController.getFirstAdsPage(authMetadata);

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
        when(advertService.getAdvertById(testAdvertId)).thenReturn(testAdvert);
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.getAdvertInfoPage(testAdvertId, authMetadata);

        // Assert
        assertEquals("ad-info", modelAndView.getViewName());
        assertEquals(testAdvert, modelAndView.getModel().get("advert"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
        
        verify(advertService).updateAdvert(eq(testAdvertId), any(Advert.class));
    }

    @Test
    @DisplayName("Should return update advert page")
    void shouldReturnUpdateAdvertPage() {
        // Arrange
        when(advertService.getAdvertById(testAdvertId)).thenReturn(testAdvert);
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
        List<Advert> adverts = Arrays.asList(testAdvert);
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
        List<Advert> adverts = Arrays.asList(testAdvert);
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
        List<Advert> adverts = Arrays.asList(testAdvert);
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
        assertEquals(testUser, modelAndView.getModel().get("user"));
        assertEquals(createRequest, modelAndView.getModel().get("createAdvertRequest"));
    }

    @Test
    @DisplayName("Should reserve advert and redirect")
    void shouldReserveAdvertAndRedirect() {
        // Arrange
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = adsController.reserveAdvert(testAdvertId, authMetadata);

        // Assert
        assertEquals("redirect:/ads", modelAndView.getViewName());
        verify(advertService).reserveCarAdvert(testAdvertId, testUser);
    }
}