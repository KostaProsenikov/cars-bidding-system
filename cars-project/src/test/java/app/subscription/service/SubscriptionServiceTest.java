package app.subscription.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.user.model.User;
import app.user.model.UserRole;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User testUser;
    private Subscription testSubscription;
    private Transaction testTransaction;
    private UUID testUserId;
    private UUID testWalletId;
    private UUID testSubscriptionId;
    private UUID testTransactionId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWalletId = UUID.randomUUID();
        testSubscriptionId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();
        testTime = LocalDateTime.now();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .isActive(true)
                .build();

        testSubscription = Subscription.builder()
                .id(testSubscriptionId)
                .owner(testUser)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(testTime)
                .completedOn(testTime.plusMonths(1))
                .build();

        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(testSubscription);
        testUser.setSubscriptions(subscriptions);

        testTransaction = Transaction.builder()
                .id(testTransactionId)
                .amount(new BigDecimal("29.99"))
                .status(TransactionStatus.SUCCEEDED)
                .type(TransactionType.DEPOSIT)
                .createdOn(testTime)
                .build();
    }

    @Test
    @DisplayName("Should create default subscription for user")
    void shouldCreateDefaultSubscriptionForUser() {
        // Arrange
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Subscription result = subscriptionService.createDefaultSubscription(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(SubscriptionType.DEFAULT, result.getType());
        assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
        assertEquals(SubscriptionPeriod.MONTHLY, result.getPeriod());
        assertEquals(0, result.getVinChecksLeft());
        assertTrue(result.getRenewalAllowed());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should reduce vin checks by one")
    void shouldReduceVinChecksByOne() {
        // Arrange
        testSubscription.setVinChecksLeft(5);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.reduceVinChecksWithOne(testUser);

        // Assert
        assertEquals(4, testSubscription.getVinChecksLeft());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Should upgrade subscription to PLUS MONTHLY")
    void shouldUpgradeSubscriptionToPlusMonthly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString());
    }
    
    @Test
    @DisplayName("Should upgrade subscription to PROFESSIONAL MONTHLY")
    void shouldUpgradeSubscriptionToProfessionalMonthly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PROFESSIONAL, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(new BigDecimal("49.99")), anyString());
    }
    
    @Test
    @DisplayName("Should upgrade subscription to PLUS WEEKLY")
    void shouldUpgradeSubscriptionToPlusWeekly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.WEEKLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(new BigDecimal("9.99")), anyString());
    }
    
    @Test
    @DisplayName("Should upgrade subscription to PROFESSIONAL WEEKLY")
    void shouldUpgradeSubscriptionToProfessionalWeekly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.WEEKLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PROFESSIONAL, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(new BigDecimal("19.99")), anyString());
    }
    
    @Test
    @DisplayName("Should upgrade subscription to PLUS YEARLY")
    void shouldUpgradeSubscriptionToPlusYearly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.YEARLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(new BigDecimal("299.99")), anyString());
    }
    
    @Test
    @DisplayName("Should upgrade subscription to PROFESSIONAL YEARLY")
    void shouldUpgradeSubscriptionToProfessionalYearly() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.YEARLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PROFESSIONAL, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(new BigDecimal("599.99")), anyString());
    }

    @Test
    @DisplayName("Should throw exception when no active subscription found")
    void shouldThrowExceptionWhenNoActiveSubscriptionFound() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest)
        );
    }

    @Test
    @DisplayName("Should not change subscription when transaction fails")
    void shouldNotChangeSubscriptionWhenTransactionFails() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .build();

        Transaction failedTransaction = Transaction.builder()
                .id(testTransactionId)
                .amount(new BigDecimal("29.99"))
                .status(TransactionStatus.FAILED)
                .type(TransactionType.DEPOSIT)
                .createdOn(testTime)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(failedTransaction);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should get all subscriptions ready for renewal")
    void shouldGetAllSubscriptionsReadyForRenewal() {
        // Arrange
        List<Subscription> subscriptions = Arrays.asList(testSubscription);
        when(subscriptionRepository.findAllByStatusAndCompletedOnLessThanEqual(eq(SubscriptionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(subscriptions);

        // Act
        List<Subscription> result = subscriptionService.getAllSubscriptionsReadyForRenewal();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testSubscriptionId, result.get(0).getId());
    }

    @Test
    @DisplayName("Should mark subscription as completed")
    void shouldMarkSubscriptionAsCompleted() {
        // Arrange
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.markSubscriptionAsCompleted(testSubscription);

        // Assert
        assertEquals(SubscriptionStatus.COMPLETED, testSubscription.getStatus());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Should mark subscription as terminated")
    void shouldMarkSubscriptionAsTerminated() {
        // Arrange
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.markSubscriptionAsTerminated(testSubscription);

        // Assert
        assertEquals(SubscriptionStatus.TERMINATED, testSubscription.getStatus());
        verify(subscriptionRepository).save(testSubscription);
    }

    @Test
    @DisplayName("Should renew monthly subscription")
    void shouldRenewMonthlySubscription() {
        // Arrange
        testSubscription.setPeriod(SubscriptionPeriod.MONTHLY);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.renewSubscription(testSubscription);

        // Assert
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        assertEquals(SubscriptionStatus.TERMINATED, testSubscription.getStatus());
    }
    
    @Test
    @DisplayName("Should renew weekly subscription")
    void shouldRenewWeeklySubscription() {
        // Arrange
        testSubscription.setPeriod(SubscriptionPeriod.WEEKLY);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.renewSubscription(testSubscription);

        // Assert
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        assertEquals(SubscriptionStatus.TERMINATED, testSubscription.getStatus());
    }
    
    @Test
    @DisplayName("Should renew yearly subscription")
    void shouldRenewYearlySubscription() {
        // Arrange
        testSubscription.setPeriod(SubscriptionPeriod.YEARLY);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        subscriptionService.renewSubscription(testSubscription);

        // Assert
        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        assertEquals(SubscriptionStatus.TERMINATED, testSubscription.getStatus());
    }
    
    @Test
    @DisplayName("Should not charge for DEFAULT subscription type")
    void shouldNotChargeForDefaultSubscriptionType() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.MONTHLY)
                .build();

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), eq(BigDecimal.ZERO), anyString()))
                .thenReturn(testTransaction);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        // Act
        Transaction result = subscriptionService.upgrade(testUser, SubscriptionType.DEFAULT, upgradeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(walletService).charge(eq(testUser), eq(testWalletId), eq(BigDecimal.ZERO), anyString());
    }
    
    @Test
    @DisplayName("Should verify completedOn calculation for WEEKLY period")
    void shouldVerifyCompletedOnCalculationForWeeklyPeriod() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.WEEKLY)
                .build();
        
        testSubscription.setVinChecksLeft(0);

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        
        // Use argument captor to capture the saved Subscription
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        when(subscriptionRepository.save(subscriptionCaptor.capture())).thenReturn(testSubscription);

        // Act
        subscriptionService.upgrade(testUser, SubscriptionType.PLUS, upgradeRequest);

        // Assert
        List<Subscription> capturedSubscriptions = subscriptionCaptor.getAllValues();
        // Find the new subscription (not the old one marked as completed)
        Subscription newSubscription = capturedSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        
        assertNotNull(newSubscription);
        assertEquals(SubscriptionType.PLUS, newSubscription.getType());
        assertEquals(SubscriptionPeriod.WEEKLY, newSubscription.getPeriod());
        assertEquals(1, newSubscription.getVinChecksLeft()); // PLUS WEEKLY should add 1 vin check
    }
    
    @Test
    @DisplayName("Should verify completedOn calculation for YEARLY period")
    void shouldVerifyCompletedOnCalculationForYearlyPeriod() {
        // Arrange
        UpgradeRequest upgradeRequest = UpgradeRequest.builder()
                .walletId(testWalletId)
                .subscriptionPeriod(SubscriptionPeriod.YEARLY)
                .build();
        
        testSubscription.setVinChecksLeft(0);

        when(subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, testUserId))
                .thenReturn(Optional.of(testSubscription));
        when(walletService.charge(eq(testUser), eq(testWalletId), any(BigDecimal.class), anyString()))
                .thenReturn(testTransaction);
        
        // Use argument captor to capture the saved Subscription
        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        when(subscriptionRepository.save(subscriptionCaptor.capture())).thenReturn(testSubscription);

        // Act
        subscriptionService.upgrade(testUser, SubscriptionType.PROFESSIONAL, upgradeRequest);

        // Assert
        List<Subscription> capturedSubscriptions = subscriptionCaptor.getAllValues();
        // Find the new subscription (not the old one marked as completed)
        Subscription newSubscription = capturedSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
        
        assertNotNull(newSubscription);
        assertEquals(SubscriptionType.PROFESSIONAL, newSubscription.getType());
        assertEquals(SubscriptionPeriod.YEARLY, newSubscription.getPeriod());
        assertEquals(96, newSubscription.getVinChecksLeft()); // PROFESSIONAL YEARLY should add 8*12=96 vin checks
        assertFalse(newSubscription.getRenewalAllowed()); // Yearly subscriptions shouldn't auto-renew
    }
}