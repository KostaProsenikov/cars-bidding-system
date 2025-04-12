package app.web;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.service.AdvertService;
import app.bid.model.Bid;
import app.bid.service.BidsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidsControllerTest {

    @Mock
    private AdvertService advertService;

    @Mock
    private UserService userService;

    @Mock
    private BidsService bidsService;

    @InjectMocks
    private BidsController bidsController;

    private User testUser;
    private Advert testAdvert;
    private Bid testBid;
    private UUID testUserId;
    private UUID testAdvertId;
    private UUID testBidId;
    private AuthenticationMetadata authMetadata;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAdvertId = UUID.randomUUID();
        testBidId = UUID.randomUUID();
        testTime = LocalDateTime.now();
        
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
                .createdOn(testTime)
                .updatedOn(testTime)
                .expireDate(testTime.plusDays(30))
                .build();
                
        testBid = Bid.builder()
                .id(testBidId)
                .bidPrice(BigDecimal.valueOf(16000))
                .maxBidPrice(BigDecimal.valueOf(20000))
                .bidder(testUser)
                .createdOn(testTime)
                .updatedOn(testTime)
                .isAccepted(false)
                .build();
                
        authMetadata = new AuthenticationMetadata(
                testUserId,
                "testuser",
                "password",
                UserRole.USER,
                true
        );
    }

    @Test
    @DisplayName("Should return bids page with user bids")
    void shouldReturnBidsPage() {
        // Arrange
        List<Bid> bids = Arrays.asList(testBid);
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(bidsService.getBidsForUserId(testUser)).thenReturn(bids);

        // Act
        ModelAndView modelAndView = bidsController.getBidsPage(authMetadata);

        // Assert
        assertEquals("bids", modelAndView.getViewName());
        assertEquals(bids, modelAndView.getModel().get("bids"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
    }

    @Test
    @DisplayName("Should return bid info page")
    void shouldReturnBidInfoPage() {
        // Arrange
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(bidsService.getById(testBidId)).thenReturn(testBid);

        // Act
        ModelAndView modelAndView = bidsController.getInformationAboutBigPage(authMetadata, testBidId);

        // Assert
        assertEquals("bid-info", modelAndView.getViewName());
        assertEquals(testBid, modelAndView.getModel().get("bid"));
        assertEquals(testUser, modelAndView.getModel().get("user"));
    }

    @Test
    @DisplayName("Should create new bid and redirect to bid info page")
    void shouldCreateNewBidAndRedirectToBidInfoPage() {
        // Arrange
        BigDecimal bidPrice = BigDecimal.valueOf(16000);
        BigDecimal maxBidPrice = BigDecimal.valueOf(20000);
        
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertById(testAdvertId)).thenReturn(testAdvert);

        // Act
        String redirectUrl = bidsController.addBid(testAdvertId, authMetadata, bidPrice, maxBidPrice);

        // Assert
        assertEquals("redirect:/ads/" + testAdvertId, redirectUrl);
    }

    @Test
    @DisplayName("Should redirect to advert page when bid creation fails")
    void shouldRedirectToAdvertPageWhenBidCreationFails() {
        // Arrange
        BigDecimal bidPrice = BigDecimal.valueOf(16000);
        BigDecimal maxBidPrice = BigDecimal.valueOf(20000);
        
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(advertService.getAdvertById(testAdvertId)).thenReturn(testAdvert);
        when(bidsService.createNewBidForAdvert(eq(testAdvert), any(Bid.class))).thenReturn(null);

        // Act
        String redirectUrl = bidsController.addBid(testAdvertId, authMetadata, bidPrice, maxBidPrice);

        // Assert
        assertEquals("redirect:/ads/" + testAdvertId, redirectUrl);
    }
}