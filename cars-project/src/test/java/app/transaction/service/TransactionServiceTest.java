package app.transaction.service;

import app.exception.DomainException;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.repository.TransactionRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Wallet testWallet;
    private Transaction testTransaction;
    private UUID transactionId;
    private UUID userId;
    private UUID walletId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
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
                .id(transactionId)
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
    @DisplayName("Should get all transactions by owner ID")
    void shouldGetAllByOwnerId() {
        // Arrange
        List<Transaction> expectedTransactions = Collections.singletonList(testTransaction);
        when(transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(userId))
                .thenReturn(expectedTransactions);
        
        // Act
        List<Transaction> result = transactionService.getAllByOwnerId(userId);
        
        // Assert
        assertEquals(expectedTransactions, result);
        verify(transactionRepository).findAllByOwnerIdOrderByCreatedOnDesc(userId);
    }

    @Test
    @DisplayName("Should create new transaction")
    void shouldCreateNewTransaction() {
        // Arrange
        String sender = walletId.toString();
        String receiver = "SMART_WALLET_LTD.";
        BigDecimal amount = BigDecimal.TEN;
        BigDecimal balanceLeft = new BigDecimal("90.00");
        Currency currency = Currency.getInstance("EUR");
        String description = "Test transaction";
        
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = transactionService.createNewTransaction(
                testUser, sender, receiver, amount, balanceLeft, currency,
                TransactionType.WITHDRAWAL, TransactionStatus.SUCCEEDED, description, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getOwner());
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals(amount, result.getAmount());
        assertEquals(balanceLeft, result.getBalanceLeft());
        assertEquals(currency, result.getCurrency());
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(TransactionStatus.SUCCEEDED, result.getStatus());
        assertEquals(description, result.getDescription());
        assertNull(result.getFailureReason());
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction capturedTransaction = transactionCaptor.getValue();
        assertEquals(testUser, capturedTransaction.getOwner());
        assertNotNull(capturedTransaction.getCreatedOn());
    }

    @Test
    @DisplayName("Should get transaction by ID")
    void shouldGetById() {
        // Arrange
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(testTransaction));
        
        // Act
        Transaction result = transactionService.getById(transactionId);
        
        // Assert
        assertEquals(testTransaction, result);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    @DisplayName("Should throw exception when transaction not found by ID")
    void shouldThrowExceptionWhenTransactionNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> transactionService.getById(nonExistentId));
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    @DisplayName("Should get last four transactions by wallet")
    void shouldGetLastFourTransactionsByWallet() {
        // Arrange
        String walletIdString = walletId.toString();
        
        // Create multiple transactions
        List<Transaction> allTransactions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Transaction transaction = Transaction.builder()
                    .id(UUID.randomUUID())
                    .owner(testUser)
                    .sender(i % 2 == 0 ? walletIdString : "other")
                    .receiver(i % 2 == 0 ? "other" : walletIdString)
                    .amount(BigDecimal.valueOf(i + 1))
                    .status(TransactionStatus.SUCCEEDED)
                    .createdOn(LocalDateTime.now().minusDays(i))
                    .build();
            allTransactions.add(transaction);
        }
        
        when(transactionRepository.findAllBySenderOrReceiverOrderByCreatedOnDesc(
                walletIdString, walletIdString))
                .thenReturn(allTransactions);
        
        // Act
        List<Transaction> result = transactionService.getLastFourTransactionsByWallet(testWallet);
        
        // Assert
        assertEquals(4, result.size());
        
        // Verify the transactions are ordered by created date descending
        LocalDateTime previousDate = null;
        for (Transaction transaction : result) {
            if (previousDate != null) {
                assertTrue(previousDate.isAfter(transaction.getCreatedOn()) || 
                        previousDate.isEqual(transaction.getCreatedOn()));
            }
            previousDate = transaction.getCreatedOn();
        }
    }
}