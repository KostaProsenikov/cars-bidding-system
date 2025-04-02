package app.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DomainExceptionTest {

    @Test
    @DisplayName("Should create domain exception with message")
    void shouldCreateDomainExceptionWithMessage() {
        // Arrange
        String errorMessage = "Test error message";
        
        // Act
        DomainException exception = new DomainException(errorMessage);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertNotNull(exception);
    }
}