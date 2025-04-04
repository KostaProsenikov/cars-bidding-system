package app.vin.service;

import app.user.model.User;
import app.user.model.UserRole;
import app.vin.model.VinHistory;
import app.vin.repository.VinHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VinHistoryServiceTest {

    @Mock
    private VinHistoryRepository vinHistoryRepository;

    @InjectMocks
    private VinHistoryService vinHistoryService;

    private User testUser;
    private VinHistory testVinHistory;
    private UUID userId;
    private String testVinNumber;
    private String testResultJson;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testVinNumber = "WBAPK5C52BA123456";
        testResultJson = "{\"status\":\"VALID\",\"manufacturer\":\"BMW\"}";
        
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
        
        testVinHistory = VinHistory.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .vinNumber(testVinNumber)
                .checkedOn(LocalDateTime.now())
                .resultJson(testResultJson)
                .manufacturer("BMW")
                .modelYear("2011")
                .assemblyPlant("Munich")
                .status("VALID")
                .build();
    }

    @Test
    @DisplayName("Should save VIN check")
    void shouldSaveVinCheck() {
        // Arrange
        when(vinHistoryRepository.save(any(VinHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        VinHistory result = vinHistoryService.saveVinCheck(
                testUser, 
                testVinNumber, 
                testResultJson, 
                "BMW", 
                "2011", 
                "Munich", 
                "VALID"
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testVinNumber, result.getVinNumber());
        assertEquals(testResultJson, result.getResultJson());
        assertEquals("BMW", result.getManufacturer());
        assertEquals("2011", result.getModelYear());
        assertEquals("Munich", result.getAssemblyPlant());
        assertEquals("VALID", result.getStatus());
        
        ArgumentCaptor<VinHistory> vinHistoryCaptor = ArgumentCaptor.forClass(VinHistory.class);
        verify(vinHistoryRepository).save(vinHistoryCaptor.capture());
        
        VinHistory savedVinHistory = vinHistoryCaptor.getValue();
        assertNotNull(savedVinHistory.getCheckedOn());
    }

    @Test
    @DisplayName("Should get user VIN history")
    void shouldGetUserVinHistory() {
        // Arrange
        List<VinHistory> expectedHistory = Collections.singletonList(testVinHistory);
        when(vinHistoryRepository.findByUserIdOrderByCheckedOnDesc(userId))
                .thenReturn(expectedHistory);
        
        // Act
        List<VinHistory> result = vinHistoryService.getUserVinHistory(userId);
        
        // Assert
        assertEquals(expectedHistory, result);
        verify(vinHistoryRepository).findByUserIdOrderByCheckedOnDesc(userId);
    }

    @Test
    @DisplayName("Should find user VIN check by user ID and VIN number")
    void shouldFindUserVinCheckByUserIdAndVinNumber() {
        // Arrange
        when(vinHistoryRepository.findByUserIdAndVinNumber(userId, testVinNumber))
                .thenReturn(Optional.of(testVinHistory));
        
        // Act
        Optional<VinHistory> result = vinHistoryService.findUserVinCheck(userId, testVinNumber);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(testVinHistory, result.get());
        verify(vinHistoryRepository).findByUserIdAndVinNumber(userId, testVinNumber);
    }

    @Test
    @DisplayName("Should return empty when user VIN check not found")
    void shouldReturnEmptyWhenUserVinCheckNotFound() {
        // Arrange
        String nonExistentVin = "NONEXISTENT12345678";
        when(vinHistoryRepository.findByUserIdAndVinNumber(userId, nonExistentVin))
                .thenReturn(Optional.empty());
        
        // Act
        Optional<VinHistory> result = vinHistoryService.findUserVinCheck(userId, nonExistentVin);
        
        // Assert
        assertTrue(result.isEmpty());
        verify(vinHistoryRepository).findByUserIdAndVinNumber(userId, nonExistentVin);
    }
}