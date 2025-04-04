package app.subscription.repository;

import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.user.model.User;
import app.user.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(app.config.SubscriptionRepositoryTestConfig.class)
@Disabled("Temporarily disabled due to configuration issues")
public class SubscriptionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    private User createTestUser(String username) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .username(username)
                .firstName("Test")
                .lastName("User")
                .email(username + "@example.com")
                .password("password")
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    @Test
    @DisplayName("Should find subscription by status and owner ID")
    void shouldFindSubscriptionByStatusAndOwnerId() {
        // Arrange
        User user = createTestUser("testuser");
        
        entityManager.persist(user);
        entityManager.flush();
        
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
        
        entityManager.persist(subscription);
        entityManager.flush();
        
        // Act
        Optional<Subscription> found = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals(subscription.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should not find subscription when status doesn't match")
    void shouldNotFindSubscriptionWhenStatusDoesntMatch() {
        // Arrange
        User user = createTestUser("testuser2");
        
        entityManager.persist(user);
        entityManager.flush();
        
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.COMPLETED)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
        
        entityManager.persist(subscription);
        entityManager.flush();
        
        // Act
        Optional<Subscription> found = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        
        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all subscriptions by status and completed on date")
    void shouldFindAllSubscriptionsByStatusAndCompletedOnDate() {
        // Arrange
        User user1 = createTestUser("user1");
        User user2 = createTestUser("user2");
        
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusDays(1);
        
        Subscription subscription1 = Subscription.builder()
                .owner(user1)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(now.minusMonths(1))
                .completedOn(past)
                .build();
        
        Subscription subscription2 = Subscription.builder()
                .owner(user2)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(BigDecimal.ZERO)
                .renewalAllowed(true)
                .createdOn(now.minusMonths(1))
                .completedOn(now.plusDays(1))
                .build();
        
        entityManager.persist(subscription1);
        entityManager.persist(subscription2);
        entityManager.flush();
        
        // Act
        List<Subscription> foundSubscriptions = subscriptionRepository.findAllByStatusAndCompletedOnLessThanEqual(
                SubscriptionStatus.ACTIVE, now);
        
        // Assert
        assertEquals(1, foundSubscriptions.size());
        assertEquals(subscription1.getId(), foundSubscriptions.get(0).getId());
    }
}