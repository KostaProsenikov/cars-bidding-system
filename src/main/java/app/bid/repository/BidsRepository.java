package app.bid.repository;

import app.bid.model.Bid;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BidsRepository extends JpaRepository<Bid, UUID> {

   List<Bid> findAllByAdvertIdAndBidder(UUID advertId, User bidder);

   List<Bid> findAllByBidder(User bidder);
}
