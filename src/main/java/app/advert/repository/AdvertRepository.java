package app.advert.repository;

import app.advert.model.Advert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvertRepository extends JpaRepository<Advert, UUID> {

    List<Advert> findAdvertByOwnerId(UUID ownerId);

    List<Advert> findByVisibleTrue();
}
