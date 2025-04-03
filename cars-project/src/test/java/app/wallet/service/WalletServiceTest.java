package app.wallet.service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private Wallet testWallet;
    private Subscription testSubscription;
    private Transaction testTransaction;
    private UUID walletId;
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(userId)
                .username(TEST_USERNAME)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
        testSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(LocalDateTime.now())
                .completedOn(LocalDateTime.now().plusMonths(1))
                .build();
        
        List<Subscription> subscriptions = new ArrayList<>();
        subscriptions.add(testSubscription);
        testUser.setSubscriptions(subscriptions);
        
        testWallet = Wallet.builder()
                .id(walletId)
                .owner(testUser)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("100.00"))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .sender(walletId.toString())
                .receiver("SMART_WALLET_LTD.")
                .amount(BigDecimal.TEN)
                .balanceLeft(new BigDecimal("90.00"))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .description("Test transaction")
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should get all wallets by username")
    void shouldGetAllWalletsByUsername() {
        // Arrange
        List<Wallet> wallets = Collections.singletonList(testWallet);
        when(walletRepository.findAllByOwnerUsername(TEST_USERNAME)).thenReturn(wallets);

        // Act
        List<Wallet> result = walletService.getAllWalletsByUsername(TEST_USERNAME);

        // Assert
        assertEquals(wallets, result);
        verify(walletRepository).findAllByOwnerUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should initialize first wallet with initial balance")
    void shouldInitializeFirstWallet() {
        // Arrange
        when(walletRepository.findAllByOwnerUsername(TEST_USERNAME)).thenReturn(Collections.emptyList());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Wallet result = walletService.initializeFirstWallet(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getOwner());
        assertEquals(WalletStatus.ACTIVE, result.getStatus());
        assertEquals(new BigDecimal("20.00"), result.getBalance());
        assertEquals(Currency.getInstance("EUR"), result.getCurrency());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should throw exception when initializing first wallet for user with existing wallets")
    void shouldThrowExceptionWhenInitializingFirstWalletForUserWithExistingWallets() {
        // Arrange
        List<Wallet> existingWallets = Collections.singletonList(testWallet);
        when(walletRepository.findAllByOwnerUsername(TEST_USERNAME)).thenReturn(existingWallets);
        
        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> walletService.initializeFirstWallet(testUser));
        
        assertTrue(exception.getMessage().contains("already has"));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should unlock new wallet for premium user")
    void shouldUnlockNewWalletForPremiumUser() {
        // Arrange
        testSubscription.setType(SubscriptionType.PLUS);
        List<Wallet> existingWallets = Collections.singletonList(testWallet);
        when(walletRepository.findAllByOwnerUsername(TEST_USERNAME)).thenReturn(existingWallets);
        
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        
        // Act
        walletService.unlockNewWallet(testUser);
        
        // Assert
        verify(walletRepository).save(walletCaptor.capture());
        Wallet savedWallet = walletCaptor.getValue();
        
        assertEquals(testUser, savedWallet.getOwner());
        assertEquals(WalletStatus.ACTIVE, savedWallet.getStatus());
        assertEquals(new BigDecimal("0.00"), savedWallet.getBalance());
        assertEquals(Currency.getInstance("EUR"), savedWallet.getCurrency());
    }

    @Test
    @DisplayName("Should throw exception when unlocking new wallet with default plan and max wallets")
    void shouldThrowExceptionWhenUnlockingNewWalletWithDefaultPlanAndMaxWallets() {
        // Arrange
        testSubscription.setType(SubscriptionType.DEFAULT);
        List<Wallet> existingWallets = Collections.singletonList(testWallet);
        when(walletRepository.findAllByOwnerUsername(TEST_USERNAME)).thenReturn(existingWallets);
        
        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> walletService.unlockNewWallet(testUser));
        
        assertTrue(exception.getMessage().contains("Max wallet count reached"));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should top up wallet successfully")
    void shouldTopUpWalletSuccessfully() {
        // Arrange
        BigDecimal amount = new BigDecimal("50.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        
        Transaction expectedTransaction = Transaction.builder()
                .status(TransactionStatus.SUCCEEDED)
                .build();
        when(transactionService.createNewTransaction(
                eq(testUser), 
                anyString(), 
                anyString(), 
                eq(amount), 
                any(BigDecimal.class), 
                any(Currency.class), 
                eq(TransactionType.DEPOSIT), 
                eq(TransactionStatus.SUCCEEDED), 
                anyString(), 
                isNull())).thenReturn(expectedTransaction);
        
        // Act
        Transaction result = walletService.topUp(walletId, amount);
        
        // Assert
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(walletRepository).save(any(Wallet.class));
        
        // Verify balance was updated correctly
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(new BigDecimal("150.00"), walletCaptor.getValue().getBalance());
    }

    @Test
    @DisplayName("Should fail top up for deactivated wallet")
    void shouldFailTopUpForDeactivatedWallet() {
        // Arrange
        testWallet.setStatus(WalletStatus.DEACTIVATED);
        BigDecimal amount = new BigDecimal("50.00");
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        
        Transaction expectedTransaction = Transaction.builder()
                .status(TransactionStatus.FAILED)
                .build();
        when(transactionService.createNewTransaction(
                eq(testUser), 
                anyString(), 
                anyString(), 
                eq(amount), 
                any(BigDecimal.class), 
                any(Currency.class), 
                eq(TransactionType.DEPOSIT), 
                eq(TransactionStatus.FAILED), 
                anyString(), 
                anyString())).thenReturn(expectedTransaction);
        
        // Act
        Transaction result = walletService.topUp(walletId, amount);
        
        // Assert
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should charge wallet successfully")
    void shouldChargeWalletSuccessfully() {
        // Arrange
        BigDecimal amount = new BigDecimal("50.00");
        String description = "Test charge";
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        
        Transaction expectedTransaction = Transaction.builder()
                .status(TransactionStatus.SUCCEEDED)
                .build();
        when(transactionService.createNewTransaction(
                eq(testUser), 
                anyString(), 
                anyString(), 
                eq(amount), 
                any(BigDecimal.class), 
                any(Currency.class), 
                eq(TransactionType.WITHDRAWAL), 
                eq(TransactionStatus.SUCCEEDED), 
                eq(description), 
                isNull())).thenReturn(expectedTransaction);
        
        // Act
        Transaction result = walletService.charge(testUser, walletId, amount, description);
        
        // Assert
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(walletRepository).save(any(Wallet.class));
        
        // Verify balance was updated correctly
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(new BigDecimal("50.00"), walletCaptor.getValue().getBalance());
    }

    @Test
    @DisplayName("Should fail charge for insufficient balance")
    void shouldFailChargeForInsufficientBalance() {
        // Arrange
        BigDecimal amount = new BigDecimal("150.00"); // More than wallet balance
        String description = "Test charge";
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        
        Transaction expectedTransaction = Transaction.builder()
                .status(TransactionStatus.FAILED)
                .build();
        when(transactionService.createNewTransaction(
                eq(testUser), 
                anyString(), 
                anyString(), 
                eq(amount), 
                any(BigDecimal.class), 
                any(Currency.class), 
                eq(TransactionType.WITHDRAWAL), 
                eq(TransactionStatus.FAILED), 
                eq(description), 
                anyString())).thenReturn(expectedTransaction);
        
        // Act
        Transaction result = walletService.charge(testUser, walletId, amount, description);
        
        // Assert
        assertEquals(TransactionStatus.FAILED, result.getStatus());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should get last four transactions by wallet")
    void shouldGetLastFourTransactionsByWallet() {
        // Arrange
        List<Wallet> wallets = Collections.singletonList(testWallet);
        List<Transaction> transactions = Collections.singletonList(testTransaction);
        when(transactionService.getLastFourTransactionsByWallet(testWallet)).thenReturn(transactions);
        
        // Act
        Map<UUID, List<Transaction>> result = walletService.getLastFourTransactions(wallets);
        
        // Assert
        assertEquals(1, result.size());
        assertTrue(result.containsKey(walletId));
        assertEquals(transactions, result.get(walletId));
    }

    @Test
    @DisplayName("Should transfer funds successfully")
    void shouldTransferFundsSuccessfully() {
        // Arrange
        UUID receiverWalletId = UUID.randomUUID();
        User receiverUser = User.builder()
                .id(UUID.randomUUID())
                .username("receiver")
                .build();
        
        Wallet receiverWallet = Wallet.builder()
                .id(receiverWalletId)
                .owner(receiverUser)
                .status(WalletStatus.ACTIVE)
                .balance(new BigDecimal("50.00"))
                .currency(Currency.getInstance("EUR"))
                .build();
        
        TransferRequest transferRequest = TransferRequest.builder()
                .fromWalletId(walletId)
                .toUsername("receiver")
                .amount(new BigDecimal("20.00"))
                .build();
        
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findAllByOwnerUsername("receiver"))
                .thenReturn(Collections.singletonList(receiverWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Mock successful withdrawal transaction
        Transaction withdrawalTransaction = Transaction.builder()
                .status(TransactionStatus.SUCCEEDED)
                .build();
        when(transactionService.createNewTransaction(
                eq(testUser), 
                anyString(), 
                anyString(), 
                any(BigDecimal.class), 
                any(BigDecimal.class), 
                any(Currency.class), 
                eq(TransactionType.WITHDRAWAL), 
                eq(TransactionStatus.SUCCEEDED), 
                anyString(), 
                isNull())).thenReturn(withdrawalTransaction);
        
        // Act
        Transaction result = walletService.transferFunds(testUser, transferRequest);
        
        // Assert
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        
        // Verify both wallets were updated correctly
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(2)).save(walletCaptor.capture());
        List<Wallet> capturedWallets = walletCaptor.getAllValues();
        
        // One wallet should have decreased balance (sender)
        boolean foundSenderUpdate = false;
        // One wallet should have increased balance (receiver)
        boolean foundReceiverUpdate = false;
        
        for (Wallet wallet : capturedWallets) {
            if (wallet.getId().equals(walletId) && wallet.getBalance().compareTo(new BigDecimal("80.00")) == 0) {
                foundSenderUpdate = true;
            }
            if (wallet.getId().equals(receiverWalletId) && wallet.getBalance().compareTo(new BigDecimal("70.00")) == 0) {
                foundReceiverUpdate = true;
            }
        }
        
        assertTrue(foundSenderUpdate, "Sender wallet balance should be updated correctly");
        assertTrue(foundReceiverUpdate, "Receiver wallet balance should be updated correctly");
    }

    @Test
    @DisplayName("Should switch wallet status")
    void shouldSwitchWalletStatus() {
        // Arrange
        UUID ownerId = testUser.getId();
        when(walletRepository.findByIdAndOwnerId(walletId, ownerId)).thenReturn(Optional.of(testWallet));
        
        // Act
        walletService.switchStatus(walletId, ownerId);
        
        // Assert
        verify(walletRepository).save(any(Wallet.class));
        
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(WalletStatus.DEACTIVATED, walletCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should throw exception when switching status for non-existent wallet")
    void shouldThrowExceptionWhenSwitchingStatusForNonExistentWallet() {
        // Arrange
        UUID ownerId = testUser.getId();
        when(walletRepository.findByIdAndOwnerId(walletId, ownerId)).thenReturn(Optional.empty());
        
        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> walletService.switchStatus(walletId, ownerId));
        
        assertTrue(exception.getMessage().contains("does not belong to user"));
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}