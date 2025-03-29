package app.scheduler;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdvertsScheduler {

    private final AdvertService advertService;

    @Autowired
    public AdvertsScheduler(AdvertService advertService) {
        this.advertService = advertService;
    }


    @Scheduled(initialDelay = 1000, fixedDelay = 600000)
    public void expireAdverts() {

        List<Advert> adverts = advertService.getAllExpiredAdverts();

        if(adverts.isEmpty()) {
            log.info("No adverts have expired yet!");
        }

        for (Advert advert : adverts) {
            advertService.expireAdvert(advert);
        }
    }
}
