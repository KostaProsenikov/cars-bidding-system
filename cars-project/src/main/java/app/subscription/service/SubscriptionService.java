package app.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.exception.DomainException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionStatus;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionRepository;
import app.transaction.model.Transaction;
import app.transaction.model.TransactionStatus;
import app.user.model.User;
import app.wallet.service.WalletService;
import app.web.dto.UpgradeRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WalletService walletService;

    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, WalletService walletService) {
        this.subscriptionRepository = subscriptionRepository;
        this.walletService = walletService;
    }

    public Subscription createDefaultSubscription(User user) {
        Subscription subscription = subscriptionRepository.save(initializeSubscription(user));
        log.info("Successfully create new subscription with id [%s] and type [%s]!".formatted(subscription.getId(), subscription.getType()));
        return subscription;
    }

    private Subscription initializeSubscription(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .vinChecksLeft(0)
                .price(new BigDecimal("0.00"))
                .renewalAllowed(true)
                .createdOn(now)
                .completedOn(now.plusMonths(1))
                .build();
    }

    public void reduceVinChecksWithOne(User user) {
        Subscription subscription = user.getSubscriptions().getFirst();
        int vinCheckLeft = subscription.getVinChecksLeft() - 1;
        subscription.setVinChecksLeft(vinCheckLeft);
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public Transaction upgrade(User user, SubscriptionType subscriptionType, UpgradeRequest upgradeRequest) {

        Optional<Subscription> optionalSubscription = subscriptionRepository.findByStatusAndOwnerId(SubscriptionStatus.ACTIVE, user.getId());
        if (optionalSubscription.isEmpty()) {
            throw new DomainException("No active subscription has been found for user [%s] with id [%s]!".formatted(user.getUsername(), user.getId()));
        }
        Subscription currentSubscription = optionalSubscription.get();

        SubscriptionPeriod subscriptionPeriod = upgradeRequest.getSubscriptionPeriod();

        BigDecimal subscriptionPrice = getSubscriptionPrice(subscriptionType, subscriptionPeriod);
        String type = subscriptionPeriod.name().substring(0, 1).toUpperCase() + subscriptionPeriod.name().substring(1).toLowerCase();
        String period = subscriptionType.name().substring(0, 1).toUpperCase() + subscriptionType.name().substring(1).toLowerCase();
        String chargeDescription = "Purchase of [%s] [%s] subscription for user [%s] with id [%s]".formatted(type, period, user.getUsername(), user.getId());

        Transaction chargeResult = walletService.charge(user, upgradeRequest.getWalletId(), subscriptionPrice, chargeDescription);

        if (chargeResult.getStatus() == TransactionStatus.FAILED) {
            log.warn("Failed to charge user [%s] with id [%s] for subscription upgrade with type [%s] and period [%s]".formatted(user.getUsername(), user.getId(), subscriptionType, subscriptionPeriod));
            return chargeResult;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime completedOn = LocalDateTime.now();
        if (subscriptionPeriod == SubscriptionPeriod.MONTHLY) {
            completedOn.plusMonths(1);
        } else if (subscriptionPeriod.equals(SubscriptionPeriod.YEARLY)) {
            completedOn.plusYears(1);
        }else {
            completedOn.plusWeeks(1);
        }

        int vinChecksLeft = currentSubscription.getVinChecksLeft();

        switch (subscriptionPeriod) {
            case WEEKLY:
                if (subscriptionType == SubscriptionType.PLUS) {
                    vinChecksLeft = vinChecksLeft +  1;
                } else if (subscriptionType == SubscriptionType.PROFESSIONAL) {
                    vinChecksLeft = vinChecksLeft + 2;
                }   break;
            case MONTHLY:
                if (subscriptionType == SubscriptionType.PLUS) {
                    vinChecksLeft = vinChecksLeft + 4;
                } else if (subscriptionType == SubscriptionType.PROFESSIONAL) {
                    vinChecksLeft = vinChecksLeft + 8;
                }   break;
            default:
                if (subscriptionType == SubscriptionType.PLUS) {
                    vinChecksLeft = vinChecksLeft + (4 * 12);
                } else if (subscriptionType == SubscriptionType.PROFESSIONAL) {
                    vinChecksLeft = vinChecksLeft + (8 * 12);
                }   break;
        }

        Subscription newSubscription = Subscription.builder()
                .owner(user)
                .status(SubscriptionStatus.ACTIVE)
                .period(subscriptionPeriod)
                .type(subscriptionType)
                .price(subscriptionPrice)
                .vinChecksLeft(vinChecksLeft)
                .renewalAllowed(subscriptionPeriod == SubscriptionPeriod.MONTHLY)
                .createdOn(now)
                .completedOn(completedOn)
                .build();

        currentSubscription.setCompletedOn(now);
        currentSubscription.setStatus(SubscriptionStatus.COMPLETED);

        subscriptionRepository.save(currentSubscription);

        subscriptionRepository.save(newSubscription);

        return chargeResult;
    }

    private BigDecimal getSubscriptionPrice(SubscriptionType subscriptionType, SubscriptionPeriod subscriptionPeriod) {
        if (subscriptionType == SubscriptionType.DEFAULT) {
            return BigDecimal.ZERO;
        } else if (subscriptionType == SubscriptionType.PLUS) {
            if (subscriptionPeriod == SubscriptionPeriod.WEEKLY) {
                return new BigDecimal("9.99");
            } else if (subscriptionPeriod == SubscriptionPeriod.MONTHLY) {
                return new BigDecimal("29.99");
            }  else if (subscriptionPeriod == SubscriptionPeriod.YEARLY) {
                return new BigDecimal("299.99");
            }
        } else {
            if (subscriptionPeriod == SubscriptionPeriod.WEEKLY) {
                return new BigDecimal("19.99");
            } else if (subscriptionPeriod == SubscriptionPeriod.MONTHLY) {
                return new BigDecimal("49.99");
            } else if (subscriptionPeriod == SubscriptionPeriod.YEARLY) {
                return new BigDecimal("599.99");
            }
        }
        return BigDecimal.ZERO;
    }

    public List<Subscription> getAllSubscriptionsReadyForRenewal() {
        return subscriptionRepository.findAllByStatusAndCompletedOnLessThanEqual(SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }

    public void markSubscriptionAsCompleted(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.COMPLETED);
        subscription.setCompletedOn(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    public void markSubscriptionAsTerminated(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.TERMINATED);
        subscription.setCompletedOn(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    public void renewSubscription(Subscription subscription) {
        Subscription newSubscription = subscription;
        newSubscription.setPeriod(subscription.getPeriod());
        LocalDateTime now = LocalDateTime.now();
        newSubscription.setCreatedOn(now);
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        LocalDateTime completedOn = LocalDateTime.now();
        if (subscription.getPeriod().name().equals(SubscriptionPeriod.WEEKLY.name())) {
            completedOn.plusWeeks(1);
        } else if (subscription.getPeriod().name().equals(SubscriptionPeriod.MONTHLY.name())) {
            completedOn.plusMonths(1);
        } else {
            completedOn.plusYears(1);
        }
        newSubscription.setCompletedOn(completedOn);
        subscriptionRepository.save(newSubscription);
        System.out.printf("Subscription [%s] has been renewed for [%s].".formatted(subscription.getId(), subscription.getPeriod()));
        this.markSubscriptionAsTerminated(subscription);
    }
}
