package app.bid.repository;

import app.bid.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BidsRepository extends JpaRepository<Bid, UUID> {

}
