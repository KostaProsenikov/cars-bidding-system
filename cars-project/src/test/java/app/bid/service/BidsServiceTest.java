package app.bid.service;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.bid.model.Bid;
import app.bid.repository.BidsRepository;
import app.exception.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidsServiceTest {

    @Mock
    private BidsRepository bidsRepository;

    @InjectMocks
    private BidsService bidsService;

    @Captor
    private ArgumentCaptor<Bid> bidCaptor;

    private User testUser;
    private Advert testAdvert;
    private Bid testBid;
    private UUID testBidId;
    private UUID testAdvertId;

    @BeforeEach
    void setUp() {
        testBidId = UUID.randomUUID();
        testAdvertId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
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
                .build();
                
        testBid = Bid.builder()
                .id(testBidId)
                .bidder(testUser)
                .advert(testAdvert)
                .bidAmount(BigDecimal.valueOf(16000))
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get bid by ID")
    void shouldGetBidById() {
        // Arrange
        when(bidsRepository.findById(testBidId)).thenReturn(Optional.of(testBid));

        // Act
        Bid result = bidsService.getById(testBidId);

        // Assert
        assertEquals(testBid, result);
        verify(bidsRepository).findById(testBidId);
    }

    @Test
    @DisplayName("Should throw exception when bid not found by ID")
    void shouldThrowExceptionWhenBidNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(bidsRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> bidsService.getById(nonExistentId));
                
        assertTrue(exception.getMessage().contains("Bid with id [" + nonExistentId + "] does not exist"));
    }

    @Test
    @DisplayName("Should create new bid for advert")
    void shouldCreateNewBidForAdvert() {
        // Arrange
        Bid bidToCreate = Bid.builder()
                .bidder(testUser)
                .bidAmount(BigDecimal.valueOf(16000))
                .createdOn(LocalDateTime.now())
                .build();
                
        when(bidsRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Bid result = bidsService.createNewBidForAdvert(testAdvert, bidToCreate);

        // Assert
        verify(bidsRepository).save(bidCaptor.capture());
        Bid capturedBid = bidCaptor.getValue();
        
        assertEquals(testAdvert, capturedBid.getAdvert());
        assertEquals(bidToCreate, result);
    }

    @Test
    @DisplayName("Should get bids for user ID")
    void shouldGetBidsForUserId() {
        // Arrange
        List<Bid> expectedBids = Collections.singletonList(testBid);
        when(bidsRepository.findAllByBidderOrderByCreatedOnDesc(testUser)).thenReturn(expectedBids);

        // Act
        List<Bid> result = bidsService.getBidsForUserId(testUser);

        // Assert
        assertEquals(expectedBids, result);
        verify(bidsRepository).findAllByBidderOrderByCreatedOnDesc(testUser);
    }

    @Test
    @DisplayName("Should get bids for advert ID and user")
    void shouldGetBidsForAdvertIdAndUser() {
        // Arrange
        List<Bid> expectedBids = Collections.singletonList(testBid);
        when(bidsRepository.findAllByAdvertIdAndBidder(testAdvertId, testUser)).thenReturn(expectedBids);

        // Act
        List<Bid> result = bidsService.getBidsForAdvertIdAndUser(testAdvertId, testUser);

        // Assert
        assertEquals(expectedBids, result);
        verify(bidsRepository).findAllByAdvertIdAndBidder(testAdvertId, testUser);
    }
}