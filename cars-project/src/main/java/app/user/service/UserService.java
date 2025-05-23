package app.user.service;

import app.exception.DomainException;
import app.security.AuthenticationMetadata;
import app.subscription.service.SubscriptionService;
import app.subscription.model.Subscription;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, WalletService walletService, SubscriptionService subscriptionService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.subscriptionService = subscriptionService;
        this.passwordEncoder = passwordEncoder;
    }

    @CacheEvict (value = "users", allEntries = true)
    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if (userOptional.isPresent()) {
            throw new DomainException("Username [%s] is already in use.".formatted(registerRequest.getUsername()));
        }

        User user = userRepository.save(initializeUser(registerRequest));

        System.out.println(user);

        Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);
        user.setSubscriptions(List.of(defaultSubscription));

        Wallet standardWallet = walletService.initializeFirstWallet(user);
        user.setWallets(List.of(standardWallet));

        log.info("Successfully created new user account for username [%s] and id [%s].".formatted(user.getUsername(), user.getId()));

        return user;
    }

    @CacheEvict (value = "users", allEntries = true)
    public void updateUserDetails(UUID id, UserEditRequest userEditRequest) {
        User user = getById(id);
        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePicture(userEditRequest.getProfilePicture());
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "users", allEntries = true)
    public void updateUserActiveStatus(UUID id, boolean isActive) {
        User user = getById(id);
        user.setIsActive(isActive);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
        log.info("User with ID [{}] active status set to [{}]", id, isActive);
    }

    private User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(UserRole.USER)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    public User getById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new DomainException("User with ID [%s] is not found!".formatted(id)));
        
        // Ensure user has subscriptions
        if (user.getSubscriptions() == null || user.getSubscriptions().isEmpty()) {
            log.info("Creating missing default subscription for user [{}]", user.getUsername());
            Subscription defaultSubscription = subscriptionService.createDefaultSubscription(user);
            user.setSubscriptions(List.of(defaultSubscription));
            userRepository.save(user);
        }
        
        // Ensure user has wallets
        if (user.getWallets() == null || user.getWallets().isEmpty()) {
            log.info("Creating missing default wallet for user [{}]", user.getUsername());
            Wallet standardWallet = walletService.initializeFirstWallet(user);
            user.setWallets(List.of(standardWallet));
            userRepository.save(user);
        }
        
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new DomainException("User with username [%s] is not found!".formatted(username)));
        return new AuthenticationMetadata(
                user.getId(),
                username,
                user.getPassword(),
                user.getRole(),
                user.getIsActive()
        );
    }

    @CacheEvict(value = "users", allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserRole(UUID userId, UserRole userRole) {
        User user = getById(userId);
        user.setRole(userRole);
        user.setUpdatedOn(LocalDateTime.now());
        userRepository.save(user);
        log.info("User with ID [{}] has been set the role: [{}]", userId, userRole.name());
    }
}