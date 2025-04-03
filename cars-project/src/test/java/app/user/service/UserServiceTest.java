package app.user.service;

import app.exception.DomainException;
import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private UUID testUserId;
    private RegisterRequest registerRequest;
    private UserEditRequest userEditRequest;
    private Subscription testSubscription;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .password("encoded-password")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        
        userEditRequest = new UserEditRequest();
        userEditRequest.setFirstName("Updated");
        userEditRequest.setLastName("User");
        userEditRequest.setEmail("updated@example.com");
        userEditRequest.setProfilePicture("new-avatar.png");
        
        testSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .type(SubscriptionType.DEFAULT)
                .period(SubscriptionPeriod.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .price(BigDecimal.valueOf(9.99))
                .createdOn(LocalDateTime.now())
                .completedOn(LocalDateTime.now().plusMonths(1))
                .build();
        
        testWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(testUser)
                .balance(BigDecimal.valueOf(100))
                .status(WalletStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .username("newuser")
                .password("encoded-password")
                .firstName("New")
                .lastName("User")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(subscriptionService.createDefaultSubscription(savedUser)).thenReturn(testSubscription);
        when(walletService.initializeFirstWallet(savedUser)).thenReturn(testWallet);

        // Act
        User result = userService.register(registerRequest);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        
        assertEquals("newuser", capturedUser.getUsername());
        assertEquals("encoded-password", capturedUser.getPassword());
        assertEquals("New", capturedUser.getFirstName());
        assertEquals("User", capturedUser.getLastName());
        assertEquals(UserRole.USER, capturedUser.getRole());
        assertTrue(capturedUser.getIsActive());
        
        assertEquals(savedUser, result);
        assertEquals(Collections.singletonList(testSubscription), result.getSubscriptions());
        assertEquals(Collections.singletonList(testWallet), result.getWallets());
    }

    @Test
    @DisplayName("Should throw exception when registering with existing username")
    void shouldThrowExceptionWhenRegisteringWithExistingUsername() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> userService.register(registerRequest));
                
        assertTrue(exception.getMessage().contains("Username [newuser] is already in use"));
    }

    @Test
    @DisplayName("Should update user details")
    void shouldUpdateUserDetails() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.updateUserDetails(testUserId, userEditRequest);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        
        assertEquals("Updated", capturedUser.getFirstName());
        assertEquals("User", capturedUser.getLastName());
        assertEquals("updated@example.com", capturedUser.getEmail());
        assertEquals("new-avatar.png", capturedUser.getProfilePicture());
        assertNotNull(capturedUser.getUpdatedOn());
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(users, result);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should get user by ID")
    void shouldGetUserById() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getById(testUserId);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> userService.getById(nonExistentId));
                
        assertTrue(exception.getMessage().contains("User with ID [" + nonExistentId + "] is not found!"));
    }

    @Test
    @DisplayName("Should load user by username")
    void shouldLoadUserByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userService.loadUserByUsername("testuser");

        // Assert
        assertTrue(result instanceof AuthenticationMetadata);
        AuthenticationMetadata authMetadata = (AuthenticationMetadata) result;
        
        assertEquals(testUserId, authMetadata.getUserId());
        assertEquals("testuser", authMetadata.getUsername());
        assertEquals("encoded-password", authMetadata.getPassword());
        assertEquals(UserRole.USER, authMetadata.getRole());
        assertTrue(authMetadata.isEnabled());
    }

    @Test
    @DisplayName("Should throw exception when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        DomainException exception = assertThrows(DomainException.class, 
                () -> userService.loadUserByUsername("nonexistent"));
                
        assertTrue(exception.getMessage().contains("User with username [nonexistent] is not found!"));
    }
}