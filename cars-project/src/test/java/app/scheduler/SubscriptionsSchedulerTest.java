package app.scheduler;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionsSchedulerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionsScheduler subscriptionsScheduler;

    private Subscription testSubscriptionRenewable;
    private Subscription testSubscriptionNonRenewable;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        
        testSubscriptionRenewable = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PLUS)
                .vinChecksLeft(2)
                .price(new BigDecimal("29.99"))
                .renewalAllowed(true)
                .createdOn(testTime.minusMonths(1))
                .completedOn(testTime.minusDays(1))
                .build();
                
        testSubscriptionNonRenewable = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.YEARLY)
                .type(SubscriptionType.PROFESSIONAL)
                .vinChecksLeft(10)
                .price(new BigDecimal("599.99"))
                .renewalAllowed(false)
                .createdOn(testTime.minusYears(1))
                .completedOn(testTime.minusDays(1))
                .build();
    }

    @Test
    @DisplayName("Should do nothing when no expired subscriptions")
    void shouldDoNothingWhenNoExpiredSubscriptions() {
        // Arrange
        when(subscriptionService.getAllSubscriptionsReadyForRenewal()).thenReturn(new ArrayList<>());

        // Act
        subscriptionsScheduler.getExpiredSubscriptions();

        // Assert
        verify(subscriptionService, never()).renewSubscription(any(Subscription.class));
    }

    @Test
    @DisplayName("Should renew only renewable subscriptions")
    void shouldRenewOnlyRenewableSubscriptions() {
        // Arrange
        List<Subscription> expiredSubscriptions = Arrays.asList(
                testSubscriptionRenewable,
                testSubscriptionNonRenewable
        );
        
        when(subscriptionService.getAllSubscriptionsReadyForRenewal()).thenReturn(expiredSubscriptions);

        // Act
        subscriptionsScheduler.getExpiredSubscriptions();

        // Assert
        verify(subscriptionService, times(1)).renewSubscription(testSubscriptionRenewable);
        verify(subscriptionService, never()).renewSubscription(testSubscriptionNonRenewable);
    }

    @Test
    @DisplayName("Should renew all renewable subscriptions")
    void shouldRenewAllRenewableSubscriptions() {
        // Arrange
        Subscription renewableSubscription1 = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PLUS)
                .vinChecksLeft(2)
                .price(new BigDecimal("29.99"))
                .renewalAllowed(true)
                .createdOn(testTime.minusMonths(1))
                .completedOn(testTime.minusDays(1))
                .build();
                
        Subscription renewableSubscription2 = Subscription.builder()
                .id(UUID.randomUUID())
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.PROFESSIONAL)
                .vinChecksLeft(5)
                .price(new BigDecimal("49.99"))
                .renewalAllowed(true)
                .createdOn(testTime.minusMonths(1))
                .completedOn(testTime.minusDays(1))
                .build();
        
        List<Subscription> expiredSubscriptions = Arrays.asList(
                renewableSubscription1,
                renewableSubscription2
        );
        
        when(subscriptionService.getAllSubscriptionsReadyForRenewal()).thenReturn(expiredSubscriptions);

        // Act
        subscriptionsScheduler.getExpiredSubscriptions();

        // Assert
        verify(subscriptionService, times(1)).renewSubscription(renewableSubscription1);
        verify(subscriptionService, times(1)).renewSubscription(renewableSubscription2);
    }
}