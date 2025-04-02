package app.security;

import app.user.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationMetadataTest {

    @Test
    @DisplayName("Should create authentication metadata with correct values")
    void shouldCreateAuthenticationMetadataWithCorrectValues() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String password = "password";
        UserRole role = UserRole.USER;
        boolean isActive = true;
        
        // Act
        AuthenticationMetadata metadata = new AuthenticationMetadata(
                userId, 
                username, 
                password, 
                role, 
                isActive
        );
        
        // Assert
        assertEquals(userId, metadata.getUserId());
        assertEquals(userId, metadata.getId());
        assertEquals(username, metadata.getUsername());
        assertEquals(password, metadata.getPassword());
        assertEquals(role, metadata.getUserRole());
        assertEquals(isActive, metadata.isEnabled());
    }
    
    @Test
    @DisplayName("Should return correct authorities for USER role")
    void shouldReturnCorrectAuthoritiesForUserRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata metadata = new AuthenticationMetadata(
                userId, 
                "testuser", 
                "password", 
                UserRole.USER, 
                true
        );
        
        // Act
        Collection<? extends GrantedAuthority> authorities = metadata.getAuthorities();
        
        // Assert
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }
    
    @Test
    @DisplayName("Should return correct authorities for ADMIN role")
    void shouldReturnCorrectAuthoritiesForAdminRole() {
        // Arrange
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata metadata = new AuthenticationMetadata(
                userId, 
                "admin", 
                "password", 
                UserRole.ADMIN, 
                true
        );
        
        // Act
        Collection<? extends GrantedAuthority> authorities = metadata.getAuthorities();
        
        // Assert
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }
    
    @Test
    @DisplayName("Should return correct values for account details methods")
    void shouldReturnCorrectValuesForAccountDetailsMethods() {
        // Arrange
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata metadata = new AuthenticationMetadata(
                userId, 
                "testuser", 
                "password", 
                UserRole.USER, 
                true
        );
        
        // Act & Assert
        assertTrue(metadata.isAccountNonExpired());
        assertTrue(metadata.isAccountNonLocked());
        assertTrue(metadata.isCredentialsNonExpired());
    }
}