package app.scheduler;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertsSchedulerTest {

    @Mock
    private AdvertService advertService;

    @InjectMocks
    private AdvertsScheduler advertsScheduler;

    private List<Advert> expiredAdverts;

    @BeforeEach
    void setUp() {
        expiredAdverts = new ArrayList<>();
        
        Advert advert1 = Advert.builder()
                .id(UUID.randomUUID())
                .advertName("Expired Car 1")
                .build();
                
        Advert advert2 = Advert.builder()
                .id(UUID.randomUUID())
                .advertName("Expired Car 2")
                .build();
                
        expiredAdverts.add(advert1);
        expiredAdverts.add(advert2);
    }

    @Test
    @DisplayName("Should expire all expired adverts")
    void shouldExpireAllExpiredAdverts() {
        // Arrange
        when(advertService.getAllExpiredAdverts()).thenReturn(expiredAdverts);

        // Act
        advertsScheduler.expireAdverts();

        // Assert
        verify(advertService).getAllExpiredAdverts();
        verify(advertService, times(1)).expireAdvert(expiredAdverts.get(0));
        verify(advertService, times(1)).expireAdvert(expiredAdverts.get(1));
    }

    @Test
    @DisplayName("Should not expire any adverts when none are expired")
    void shouldNotExpireAnyAdvertsWhenNoneAreExpired() {
        // Arrange
        when(advertService.getAllExpiredAdverts()).thenReturn(Collections.emptyList());

        // Act
        advertsScheduler.expireAdverts();

        // Assert
        verify(advertService).getAllExpiredAdverts();
        verify(advertService, never()).expireAdvert(any(Advert.class));
    }
}