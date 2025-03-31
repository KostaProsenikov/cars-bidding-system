package app.bid.service;

import app.advert.model.Advert;
import app.advert.service.AdvertService;
import app.bid.model.Bid;
import app.bid.repository.BidsRepository;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BidsService {

    private final BidsRepository bidsRepository;

    @Autowired
    public BidsService(BidsRepository bidsRepository, AdvertService advertService) {
        this.bidsRepository = bidsRepository;
    }

    public Bid createNewBidForAdvert(User user, Advert advert, Bid bidToCreate) {
        bidToCreate.setAdvert(advert);
        bidsRepository.save(bidToCreate);
        log.info("Successfully created new bids with id: [%s] and for advert [%s]!".formatted(bidToCreate.getId(), bidToCreate.getAdvert().getId()));
        return bidToCreate;
    }

}
