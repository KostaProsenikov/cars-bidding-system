package app.web;

import app.security.AuthenticationMetadata;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.web.dto.UpgradeRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionsControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionsController subscriptionsController;

    private User testUser;
    private UUID testUserId;
    private UUID testWalletId;
    private UUID testTransactionId;
    private AuthenticationMetadata authMetadata;
    private Wallet testWallet;
    private Transaction testTransaction;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testWalletId = UUID.randomUUID();
        testTransactionId = UUID.randomUUID();
        testTime = LocalDateTime.now();
        
        testWallet = Wallet.builder()
                .id(testWalletId)
                .balance(BigDecimal.valueOf(500))
                .status(WalletStatus.ACTIVE)
                .createdOn(testTime)
                .build();
        
        List<Wallet> wallets = new ArrayList<>();
        wallets.add(testWallet);
        
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .isActive(true)
                .wallets(wallets)
                .build();
                
        testTransaction = Transaction.builder()
                .id(testTransactionId)
                .amount(BigDecimal.valueOf(20))
                .status(TransactionStatus.SUCCEEDED)
                .type(TransactionType.DEPOSIT)
                .createdOn(testTime)
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
    @DisplayName("Should return subscriptions page")
    void shouldReturnSubscriptionsPage() {
        // Arrange
        when(userService.getById(testUserId)).thenReturn(testUser);

        // Act
        ModelAndView modelAndView = subscriptionsController.getSubscriptionsPage(authMetadata);

        // Assert
        assertEquals("subscriptions", modelAndView.getViewName());
        assertEquals(testUser, modelAndView.getModel().get("user"));
    }

    @Test
    @DisplayName("Should upgrade to PLUS subscription and redirect to transaction")
    void shouldUpgradeToPlusSubscriptionAndRedirect() {
        // Arrange
        int planType = 1;
        String period = "MONTHLY";
        
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(subscriptionService.upgrade(eq(testUser), eq(SubscriptionType.PLUS), any(UpgradeRequest.class)))
                .thenReturn(testTransaction);

        // Act
        String redirectUrl = subscriptionsController.changeSubscriptionType(planType, period, authMetadata);

        // Assert
        assertEquals("redirect:/transactions/" + testTransactionId, redirectUrl);
        
        // Verify the upgrade request had correct values
        verify(subscriptionService).upgrade(
                eq(testUser), 
                eq(SubscriptionType.PLUS), 
                any(UpgradeRequest.class));
    }

    @Test
    @DisplayName("Should upgrade to PROFESSIONAL subscription and redirect to transaction")
    void shouldUpgradeToProfessionalSubscriptionAndRedirect() {
        // Arrange
        int planType = 2;
        String period = "YEARLY";
        
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(subscriptionService.upgrade(eq(testUser), eq(SubscriptionType.PROFESSIONAL), any(UpgradeRequest.class)))
                .thenReturn(testTransaction);

        // Act
        String redirectUrl = subscriptionsController.changeSubscriptionType(planType, period, authMetadata);

        // Assert
        assertEquals("redirect:/transactions/" + testTransactionId, redirectUrl);
        
        // Verify the upgrade request had correct values
        verify(subscriptionService).upgrade(
                eq(testUser), 
                eq(SubscriptionType.PROFESSIONAL), 
                any(UpgradeRequest.class));
    }

    @Test
    @DisplayName("Should default to DEFAULT subscription for invalid plan type")
    void shouldDefaultToDefaultSubscriptionForInvalidPlanType() {
        // Arrange
        int planType = 99; // Invalid plan type
        String period = "MONTHLY";
        
        when(userService.getById(testUserId)).thenReturn(testUser);
        when(subscriptionService.upgrade(eq(testUser), eq(SubscriptionType.DEFAULT), any(UpgradeRequest.class)))
                .thenReturn(testTransaction);

        // Act
        String redirectUrl = subscriptionsController.changeSubscriptionType(planType, period, authMetadata);

        // Assert
        assertEquals("redirect:/transactions/" + testTransactionId, redirectUrl);
        
        // Verify the upgrade request had correct values
        verify(subscriptionService).upgrade(
                eq(testUser), 
                eq(SubscriptionType.DEFAULT), 
                any(UpgradeRequest.class));
    }
}