package app.web;

import app.security.AuthenticationMetadata;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.vin.model.VinHistory;
import app.vin.service.VinHistoryService;
import app.web.dto.UserEditRequest;
import app.web.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private VinHistoryService vinHistoryService;

    @Mock
    private AuthenticationMetadata authenticationMetadata;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UsersController usersController;

    private User testUser;
    private UUID userId;
    private List<VinHistory> vinHistoryList;
    private UserEditRequest userEditRequest;
    private UUID testSubscriptionId;
    private LocalDateTime testTime;


    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();
        userId = UUID.randomUUID();
        testSubscriptionId = UUID.randomUUID();
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

        vinHistoryList = new ArrayList<>();
        VinHistory vinHistory = VinHistory.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .vinNumber("WBAWL73589P473158")
                .checkedOn(LocalDateTime.now())
                .resultJson("{\"status\":\"VALID\"}")
                .manufacturer("BMW")
                .modelYear("2009")
                .assemblyPlant("P")
                .status("VALID")
                .build();
        vinHistoryList.add(vinHistory);

        userEditRequest = new UserEditRequest();
        userEditRequest.setFirstName("Test");
        userEditRequest.setLastName("User");
        userEditRequest.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Should get profile page")
    void shouldGetProfilePage() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        
        try (MockedStatic<DtoMapper> mockedStatic = mockStatic(DtoMapper.class)) {
            mockedStatic.when(() -> DtoMapper.mapUserToUserEditRequest(testUser))
                    .thenReturn(userEditRequest);
            
            // Act
            ModelAndView result = usersController.getProfilePage(authenticationMetadata);
            
            // Assert
            assertNotNull(result);
            assertEquals("my-profile", result.getViewName());
            assertEquals(testUser, result.getModel().get("user"));
            assertEquals(userEditRequest, result.getModel().get("userEditRequest"));
            
            verify(userService).getById(userId);
        }
    }

    @Test
    @DisplayName("Should get VIN history page")
    void shouldGetVinHistoryPage() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = usersController.getVinHistoryPage(authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("vin-history", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        
        verify(userService).getById(userId);
    }

    @Test
    @DisplayName("Should update user profile when validation passes")
    void shouldUpdateUserProfileWhenValidationPasses() {
        Subscription testSubscription = Subscription.builder()
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
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        
        // Act
        ModelAndView result = usersController.getProfilePage(authenticationMetadata, userEditRequest, bindingResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("redirect:/users/my-profile", result.getViewName());
        
        verify(userService).updateUserDetails(userId, userEditRequest);
    }

    @Test
    @DisplayName("Should return to profile page with errors when validation fails")
    void shouldReturnToProfilePageWithErrorsWhenValidationFails() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = usersController.getProfilePage(authenticationMetadata, userEditRequest, bindingResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("my-profile", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(userEditRequest, result.getModel().get("userEditRequest"));
        
        verify(userService).getById(userId);
        verify(userService, never()).updateUserDetails(any(), any());
    }
}