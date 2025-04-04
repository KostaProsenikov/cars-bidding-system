package app.web;

import app.security.AuthenticationMetadata;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.transaction.model.TransactionType;
import app.transaction.service.TransactionService;
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
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationMetadata authenticationMetadata;

    @InjectMocks
    private TransactionController transactionController;

    private User testUser;
    private Transaction testTransaction;
    private UUID userId;
    private UUID transactionId;
    private List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .role(UserRole.USER)
                .password("password")
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        testTransaction = Transaction.builder()
                .id(transactionId)
                .owner(testUser)
                .sender("wallet123")
                .receiver("merchant456")
                .amount(new BigDecimal("100.00"))
                .balanceLeft(new BigDecimal("900.00"))
                .currency(Currency.getInstance("EUR"))
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .description("Purchase payment")
                .createdOn(LocalDateTime.now())
                .build();

        transactions = new ArrayList<>();
        transactions.add(testTransaction);
    }

    @Test
    @DisplayName("Should get all transactions")
    void shouldGetAllTransactions() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        when(transactionService.getAllByOwnerId(userId)).thenReturn(transactions);
        
        // Act
        ModelAndView result = transactionController.getAllTransactions(authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("transactions", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(transactions, result.getModel().get("transactions"));
        
        verify(userService).getById(userId);
        verify(transactionService).getAllByOwnerId(userId);
    }

    @Test
    @DisplayName("Should get transaction by id")
    void shouldGetTransactionById() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        when(transactionService.getById(transactionId)).thenReturn(testTransaction);
        
        // Act
        ModelAndView result = transactionController.getTransactionById(transactionId, authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("transaction-result", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(testTransaction, result.getModel().get("transaction"));
        
        verify(userService).getById(userId);
        verify(transactionService).getById(transactionId);
    }
}