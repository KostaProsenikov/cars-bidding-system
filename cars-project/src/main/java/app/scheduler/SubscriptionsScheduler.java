package app.scheduler;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubscriptionsScheduler {

    private final SubscriptionService subscriptionService;

    @Autowired
    public SubscriptionsScheduler(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    public void getExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionService.getAllSubscriptionsReadyForRenewal();

        if(expiredSubscriptions.isEmpty()) {
            log.info("No expired subscriptions yet!");
        }

        for (Subscription subscription : expiredSubscriptions) {
            if (subscription.getRenewalAllowed()) {
                subscriptionService.renewSubscription(subscription);
            }
        }
    }

}
