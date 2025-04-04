package app.vin.repository;

import app.vin.model.VinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VinHistoryRepository extends JpaRepository<VinHistory, UUID> {
    
    List<VinHistory> findByUserIdOrderByCheckedOnDesc(UUID userId);
    
    Optional<VinHistory> findByUserIdAndVinNumber(UUID userId, String vinNumber);
}
