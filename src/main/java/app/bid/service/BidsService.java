package app.bid.service;

import app.advert.model.Advert;
import app.bid.model.Bid;
import app.bid.repository.BidsRepository;
import app.exception.DomainException;
import app.user.model.User;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BidsService {

    private final BidsRepository bidsRepository;

    @Autowired
    public BidsService(BidsRepository bidsRepository) {
        this.bidsRepository = bidsRepository;
    }

    public Bid getById(UUID id) {
        return bidsRepository.findById(id).orElseThrow(() ->
                new DomainException("Bid with id [" + id + "] does not exist"));
    }

    public Bid createNewBidForAdvert(Advert advert, Bid bidToCreate) {
        bidToCreate.setAdvert(advert);
        bidsRepository.save(bidToCreate);
        log.info("Successfully created new bid with id: [%s] and for advert [%s]!".formatted(bidToCreate.getId(), bidToCreate.getAdvert().getId()));
        return bidToCreate;
    }

    public List<Bid> getBidsForUserId(User user) {
        return bidsRepository.findAllByBidder(user);
    }

    public List<Bid> getBidsForAdvertIdAndUser(UUID advertId, User user) {
        return bidsRepository.findAllByAdvertIdAndBidder(advertId, user);
    }

}
