package app.advert.service;

import app.advert.model.Advert;
import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.model.FuelType;
import app.advert.model.GearboxType;
import app.advert.repository.AdvertRepository;
import app.exception.AdvertNotFoundException;
import app.user.model.User;
import app.user.model.UserRole;
import app.web.dto.CreateNewAdvertRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertServiceTest {

    @Mock
    private AdvertRepository advertRepository;

    @InjectMocks
    private AdvertService advertService;

    @Captor
    private ArgumentCaptor<Advert> advertCaptor;

    private User testUser;
    private Advert testAdvert;
    private CreateNewAdvertRequest createRequest;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .build();

        testAdvert = Advert.builder()
                .id(testId)
                .advertName("Test Car")
                .description("Test Description")
                .owner(testUser)
                .carBrand(CarBrand.BMW)
                .carModel("X5")
                .manufactureYear(2020)
                .mileage(BigDecimal.valueOf(10000))
                .horsePower(300)
                .fuelType(FuelType.GASOLINE)
                .gearboxType(GearboxType.AUTOMATIC)
                .minBidPrice(BigDecimal.valueOf(15000))
                .buyNowPrice(BigDecimal.valueOf(25000))
                .biddingOpen(true)
                .visible(true)
                .carStatus(CarStatus.AVAILABLE)
                .viewCount(0)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusDays(30))
                .build();

        createRequest = new CreateNewAdvertRequest();
        createRequest.setAdvertName("Test Car");
        createRequest.setDescription("Test Description");
        createRequest.setCarBrand(CarBrand.BMW);
        createRequest.setCarModel("X5");
        createRequest.setManufactureYear(2020);
        createRequest.setMileage(BigDecimal.valueOf(10000));
        createRequest.setHorsePower(300);
        createRequest.setFuelType(FuelType.GASOLINE);
        createRequest.setGearboxType(GearboxType.AUTOMATIC);
        createRequest.setMinBidPrice(BigDecimal.valueOf(15000));
        createRequest.setBuyNowPrice(BigDecimal.valueOf(25000));
        createRequest.setIsBiddingOpen(true);
        createRequest.setVisible(true);
        createRequest.setImageURL("test-image.jpg");
    }

    @Test
    @DisplayName("Should create a new ad with the correct properties")
    void shouldCreateNewAd() {
        // Arrange
        when(advertRepository.save(any(Advert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        advertService.createNewAd(createRequest, testUser);

        // Assert
        verify(advertRepository).save(advertCaptor.capture());
        Advert capturedAdvert = advertCaptor.getValue();

        assertEquals("Test Car", capturedAdvert.getAdvertName());
        assertEquals("Test Description", capturedAdvert.getDescription());
        assertEquals(testUser, capturedAdvert.getOwner());
        assertEquals(CarBrand.BMW, capturedAdvert.getCarBrand());
        assertEquals("X5", capturedAdvert.getCarModel());
        assertEquals(2020, capturedAdvert.getManufactureYear());
        assertTrue(capturedAdvert.getBiddingOpen());
        assertTrue(capturedAdvert.getVisible());
        assertEquals(CarStatus.AVAILABLE, capturedAdvert.getCarStatus());
        assertEquals(0, capturedAdvert.getViewCount());
        assertTrue(capturedAdvert.getExpireDate().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should get adverts by owner ID")
    void shouldGetAdvertsByOwnerId() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        List<Advert> expectedAdverts = Collections.singletonList(testAdvert);
        when(advertRepository.findAdvertByOwnerId(ownerId)).thenReturn(expectedAdverts);

        // Act
        List<Advert> result = advertService.getAdvertsByOwnerId(ownerId);

        // Assert
        assertEquals(expectedAdverts, result);
        verify(advertRepository).findAdvertByOwnerId(ownerId);
    }

    @Test
    @DisplayName("Should get adverts by winner ID")
    void shouldGetAdvertsByWinnerId() {
        // Arrange
        UUID winnerId = UUID.randomUUID();
        List<Advert> expectedAdverts = Collections.singletonList(testAdvert);
        when(advertRepository.findAdvertByWinnerId(winnerId)).thenReturn(expectedAdverts);

        // Act
        List<Advert> result = advertService.getAdvertsByWinnerId(winnerId);

        // Assert
        assertEquals(expectedAdverts, result);
        verify(advertRepository).findAdvertByWinnerId(winnerId);
    }

    @Test
    @DisplayName("Should get all shown adverts by page with ascending sort")
    void shouldGetAllShownAdvertsByPageWithAscendingSort() {
        // Arrange
        int page = 0;
        String sortType = "ASC";
        String sortField = "createdOn";
        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, 20, sort);
        
        List<Advert> expectedAdverts = Collections.singletonList(testAdvert);
        when(advertRepository.findByVisible(true, pageable)).thenReturn(expectedAdverts);

        // Act
        List<Advert> result = advertService.getAllShownAdvertsByPage(page, sortType, sortField);

        // Assert
        assertEquals(expectedAdverts, result);
        verify(advertRepository).findByVisible(true, pageable);
    }

    @Test
    @DisplayName("Should get all shown adverts by page with descending sort")
    void shouldGetAllShownAdvertsByPageWithDescendingSort() {
        // Arrange
        int page = 0;
        String sortType = "DESC";
        String sortField = "createdOn";
        Sort sort = Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 20, sort);
        
        List<Advert> expectedAdverts = Collections.singletonList(testAdvert);
        when(advertRepository.findByVisible(true, pageable)).thenReturn(expectedAdverts);

        // Act
        List<Advert> result = advertService.getAllShownAdvertsByPage(page, sortType, sortField);

        // Assert
        assertEquals(expectedAdverts, result);
        verify(advertRepository).findByVisible(true, pageable);
    }

    @Test
    @DisplayName("Should save an advert")
    void shouldSaveAdvert() {
        // Arrange
        when(advertRepository.save(testAdvert)).thenReturn(testAdvert);
        
        // Act
        advertService.saveAdvert(testAdvert);

        // Assert
        verify(advertRepository).save(testAdvert);
    }

    @Test
    @DisplayName("Should get advert count")
    void shouldGetAdvertCount() {
        // Arrange
        List<Advert> adverts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            adverts.add(testAdvert);
        }
        
        when(advertRepository.findByVisible(eq(true), any(Pageable.class))).thenReturn(adverts);

        // Act
        int count = advertService.getAdvertCount();

        // Assert
        assertEquals(5, count);
    }

    @Test
    @DisplayName("Should get advert by ID")
    void shouldGetAdvertById() {
        // Arrange
        when(advertRepository.findById(testId)).thenReturn(Optional.of(testAdvert));

        // Act
        Advert result = advertService.getAdvertById(testId);

        // Assert
        assertEquals(testAdvert, result);
    }


    @Test
    @DisplayName("Should throw AdvertNotFoundException when advert is not found")
    void shouldThrowAdvertNotFoundExceptionWhenAdvertIsNotFound()
    {
        UUID advertId = UUID.randomUUID();

        assertThatThrownBy(() -> advertService.getAdvertById(advertId))
                .isInstanceOf(AdvertNotFoundException.class)
                .hasMessageContaining("Advert with ID [" + advertId + "] is not found!");
    }

    @Test
    @DisplayName("Should reserve car advert")
    void shouldReserveCarAdvert() {
        // Arrange
        User winner = User.builder()
                .id(UUID.randomUUID())
                .email("winner@example.com")
                .firstName("Winner")
                .lastName("User")
                .role(UserRole.USER)
                .build();
        
        when(advertRepository.findById(testId)).thenReturn(Optional.of(testAdvert));
        when(advertRepository.save(any(Advert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        advertService.reserveCarAdvert(testId, winner);

        // Assert
        verify(advertRepository).save(advertCaptor.capture());
        Advert capturedAdvert = advertCaptor.getValue();
        
        assertEquals(CarStatus.RESERVED, capturedAdvert.getCarStatus());
        assertFalse(capturedAdvert.getVisible());
        assertEquals(winner, capturedAdvert.getWinner());
    }

    @Test
    @DisplayName("Should update advert")
    void shouldUpdateAdvert() {
        // Arrange
        Advert updatedAdvert = Advert.builder()
                .advertName("Updated Car")
                .description("Updated Description")
                .carBrand(CarBrand.AUDI)
                .carModel("A6")
                .manufactureYear(2021)
                .mileage(BigDecimal.valueOf(5000))
                .horsePower(400)
                .fuelType(FuelType.DIESEL)
                .gearboxType(GearboxType.MANUAL)
                .minBidPrice(BigDecimal.valueOf(20000))
                .buyNowPrice(BigDecimal.valueOf(30000))
                .biddingOpen(false)
                .visible(false)
                .viewCount(10)
                .build();
        
        when(advertRepository.findById(testId)).thenReturn(Optional.of(testAdvert));
        when(advertRepository.save(any(Advert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        advertService.updateAdvert(testId, updatedAdvert);

        // Assert
        verify(advertRepository).save(advertCaptor.capture());
        Advert capturedAdvert = advertCaptor.getValue();
        
        assertEquals("Updated Car", capturedAdvert.getAdvertName());
        assertEquals("Updated Description", capturedAdvert.getDescription());
        assertEquals(CarBrand.AUDI, capturedAdvert.getCarBrand());
        assertEquals("A6", capturedAdvert.getCarModel());
        assertEquals(2021, capturedAdvert.getManufactureYear());
        assertEquals(BigDecimal.valueOf(5000), capturedAdvert.getMileage());
        assertFalse(capturedAdvert.getBiddingOpen());
        assertFalse(capturedAdvert.getVisible());
        assertNotNull(capturedAdvert.getUpdatedOn());
    }

    @Test
    @DisplayName("Should get first 20 visible adverts")
    void shouldGetFirst20VisibleAdverts() {
        // Arrange
        List<Advert> visibleAdverts = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Advert advert = Advert.builder()
                    .id(UUID.randomUUID())
                    .advertName("Test " + i)
                    .visible(true)
                    .build();
            visibleAdverts.add(advert);
        }
        
        when(advertRepository.findByVisibleTrue()).thenReturn(visibleAdverts);

        // Act
        List<Advert> result = advertService.getFirst20VisibleAdverts();

        // Assert
        assertEquals(20, result.size());
    }

    @Test
    @DisplayName("Should get all expired adverts")
    void shouldGetAllExpiredAdverts() {
        // Arrange
        List<Advert> expiredAdverts = Collections.singletonList(testAdvert);
        when(advertRepository.findByExpireDate(any(LocalDateTime.class))).thenReturn(expiredAdverts);

        // Act
        List<Advert> result = advertService.getAllExpiredAdverts();

        // Assert
        assertEquals(expiredAdverts, result);
        verify(advertRepository).findByExpireDate(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should expire advert")
    void shouldExpireAdvert() {
        // Arrange
        when(advertRepository.save(any(Advert.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        advertService.expireAdvert(testAdvert);

        // Assert
        verify(advertRepository).save(advertCaptor.capture());
        Advert capturedAdvert = advertCaptor.getValue();
        
        assertFalse(capturedAdvert.getVisible());
        assertNotNull(capturedAdvert.getUpdatedOn());
    }
}