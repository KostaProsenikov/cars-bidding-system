package app.user.service;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class UserSeederService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Autowired
    public UserSeederService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            SubscriptionService subscriptionService, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
    }

    @Override
    public void run(String... args) {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        
        if (existingAdmin.isEmpty()) {
            User adminUser = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@example.com")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();
            
            adminUser = userRepository.save(adminUser);
            
            // Create default subscription for admin
            Subscription defaultSubscription = subscriptionService.createDefaultSubscription(adminUser);
            adminUser.setSubscriptions(List.of(defaultSubscription));
            
            // Create wallet for admin
            Wallet standardWallet = walletService.initializeFirstWallet(adminUser);
            adminUser.setWallets(List.of(standardWallet));
            
            userRepository.save(adminUser);
            log.info("Created default admin user with username: {}", adminUsername);
        } else {
            log.info("Admin user already exists with username: {}", adminUsername);
            
            // Check if admin has a subscription, create one if not
            User admin = existingAdmin.get();
            if (admin.getSubscriptions() == null || admin.getSubscriptions().isEmpty()) {
                Subscription defaultSubscription = subscriptionService.createDefaultSubscription(admin);
                admin.setSubscriptions(List.of(defaultSubscription));
                userRepository.save(admin);
                log.info("Added default subscription to existing admin user");
            }
            
            // Check if admin has a wallet, create one if not
            if (admin.getWallets() == null || admin.getWallets().isEmpty()) {
                Wallet standardWallet = walletService.initializeFirstWallet(admin);
                admin.setWallets(List.of(standardWallet));
                userRepository.save(admin);
                log.info("Added default wallet to existing admin user");
            }
        }
    }
}