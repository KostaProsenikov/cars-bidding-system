package app.web;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AdvertService advertService;

    @Mock
    private AuthenticationMetadata authenticationMetadata;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private IndexController indexController;

    private User testUser;
    private UUID userId;
    private List<Advert> adverts;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
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

        adverts = new ArrayList<>();
        adverts.add(Advert.builder().id(UUID.randomUUID()).advertName("Test Advert").build());

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
    }

    @Test
    @DisplayName("Should get index page for authenticated user")
    void shouldGetIndexPageForAuthenticatedUser() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        when(advertService.getFirst20VisibleAdverts()).thenReturn(adverts);
        
        // Act
        ModelAndView result = indexController.getIndexPage(authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("index", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        assertEquals(adverts, result.getModel().get("adverts"));
        
        verify(userService).getById(userId);
        verify(advertService).getFirst20VisibleAdverts();
    }

    @Test
    @DisplayName("Should get index page for anonymous user")
    void shouldGetIndexPageForAnonymousUser() {
        // Arrange
        when(advertService.getFirst20VisibleAdverts()).thenReturn(adverts);
        
        // Act
        ModelAndView result = indexController.getIndexPage(null);
        
        // Assert
        assertNotNull(result);
        assertEquals("index", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        assertEquals(adverts, result.getModel().get("adverts"));
        
        verify(userService, never()).getById(any());
        verify(advertService).getFirst20VisibleAdverts();
    }

    @Test
    @DisplayName("Should get login page")
    void shouldGetLoginPage() {
        // Act
        ModelAndView result = indexController.GetLoginPage(null);
        
        // Assert
        assertNotNull(result);
        assertEquals("login", result.getViewName());
        assertNotNull(result.getModel().get("loginRequest"));
    }

    @Test
    @DisplayName("Should get login page with error message")
    void shouldGetLoginPageWithErrorMessage() {
        // Act
        ModelAndView result = indexController.GetLoginPage("true");
        
        // Assert
        assertNotNull(result);
        assertEquals("login", result.getViewName());
        assertNotNull(result.getModel().get("loginRequest"));
        assertEquals("Invalid username or password!", result.getModel().get("errorMessage"));
    }

    @Test
    @DisplayName("Should get register page")
    void shouldGetRegisterPage() {
        // Act
        ModelAndView result = indexController.GetRegisterPage();
        
        // Assert
        assertNotNull(result);
        assertEquals("register", result.getViewName());
        assertNotNull(result.getModel().get("registerRequest"));
    }

    @Test
    @DisplayName("Should get about us page for authenticated user")
    void shouldGetAboutUsPageForAuthenticatedUser() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = indexController.GetAboutUsPage(authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("about-us", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        
        verify(userService).getById(userId);
    }

    @Test
    @DisplayName("Should get about us page for anonymous user")
    void shouldGetAboutUsPageForAnonymousUser() {
        // Act
        ModelAndView result = indexController.GetAboutUsPage(null);
        
        // Assert
        assertNotNull(result);
        assertEquals("about-us", result.getViewName());
        assertNotNull(result.getModel().get("user"));
        
        verify(userService, never()).getById(any());
    }

    @Test
    @DisplayName("Should get contact page")
    void shouldGetContactPage() {
        // Arrange
        when(authenticationMetadata.getUserId()).thenReturn(userId);
        when(userService.getById(userId)).thenReturn(testUser);
        
        // Act
        ModelAndView result = indexController.GetContactsPage(authenticationMetadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("contact", result.getViewName());
        assertEquals(testUser, result.getModel().get("user"));
        
        verify(userService).getById(userId);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register(registerRequest)).thenReturn(testUser);
        
        // Act
        ModelAndView result = indexController.registerNewUser(registerRequest, bindingResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("redirect:/login", result.getViewName());
        
        verify(userService).register(registerRequest);
    }

    @Test
    @DisplayName("Should return to register page when validation fails")
    void shouldReturnToRegisterPageWhenValidationFails() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        
        // Act
        ModelAndView result = indexController.registerNewUser(registerRequest, bindingResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("register", result.getViewName());
        
        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("Should return to register page when user registration fails")
    void shouldReturnToRegisterPageWhenUserRegistrationFails() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register(registerRequest)).thenReturn(null);
        
        // Act
        ModelAndView result = indexController.registerNewUser(registerRequest, bindingResult);
        
        // Assert
        assertNotNull(result);
        assertEquals("register", result.getViewName());
        
        verify(userService).register(registerRequest);
    }
}