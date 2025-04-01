package app.advert.repository;

import app.advert.model.Advert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdvertRepository extends JpaRepository<Advert, UUID> {

    List<Advert> findAdvertByOwnerId(UUID ownerId);

    List<Advert> findAdvertByWinnerId(UUID winnerId);

    List<Advert> findByVisibleTrue();

    List<Advert> findByVisible(boolean visible, Pageable pageable);

    List<Advert> findByExpireDate(LocalDateTime expireDate);
}
