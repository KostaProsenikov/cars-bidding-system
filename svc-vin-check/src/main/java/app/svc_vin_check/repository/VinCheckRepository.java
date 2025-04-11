package app.svc_vin_check.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.svc_vin_check.model.VinCheck;

@Repository
public interface VinCheckRepository extends JpaRepository<VinCheck, UUID> {
    
    List<VinCheck> findByUserIdOrderByCheckedAtDesc(UUID userId);
    
    List<VinCheck> findByVinNumberOrderByCheckedAtDesc(String vinNumber);
    
    List<VinCheck> findByUserIdAndVinNumber(UUID userId, String vinNumber);

    Optional<VinCheck> findByIdAndUserId(UUID vinCheckId, UUID userId);
}