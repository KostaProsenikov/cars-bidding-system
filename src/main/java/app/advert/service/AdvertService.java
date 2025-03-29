package app.advert.service;

import app.advert.model.Advert;
import app.advert.model.CarStatus;
import app.advert.repository.AdvertRepository;
import app.exception.DomainException;
import app.user.model.User;
import app.web.dto.CreateNewAdvertRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AdvertService {

    private final AdvertRepository advertRepository;

    @Autowired
    public AdvertService(AdvertRepository advertRepository) {
        this.advertRepository = advertRepository;
    }

    public void createNewAd(CreateNewAdvertRequest createNewAdvertRequest, User user) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireDate = now.plusDays(30);

        boolean isBiddingOpen = createNewAdvertRequest.getIsBiddingOpen() != null ? createNewAdvertRequest.getIsBiddingOpen() : false;
        boolean isVisible = createNewAdvertRequest.getVisible() != null ? createNewAdvertRequest.getVisible() : false;
        Advert advert = Advert.builder()
                .advertName(createNewAdvertRequest.getAdvertName())
                .description(createNewAdvertRequest.getDescription())
                .owner(user)
                .viewCount(0)
                .visible(isVisible)
                .carBrand(createNewAdvertRequest.getCarBrand())
                .carModel(createNewAdvertRequest.getCarModel())
                .mileage(createNewAdvertRequest.getMileage())
                .isBiddingOpen(isBiddingOpen)
                .manufactureYear(createNewAdvertRequest.getManufactureYear())
                .horsePower(createNewAdvertRequest.getHorsePower())
                .fuelType(createNewAdvertRequest.getFuelType())
                .gearboxType(createNewAdvertRequest.getGearboxType())
                .buyNowPrice(createNewAdvertRequest.getBuyNowPrice())
                .imageURL(createNewAdvertRequest.getImageURL())
                .expireDate(expireDate)
                .createdOn(now)
                .updatedOn(now)
                .build();

        advertRepository.save(advert);

        System.out.printf("Created a new ad with name [%s] and description [%s] %n", advert.getAdvertName(), advert.getDescription());
    }

    public List<Advert> getAdvertsByOwnerId(UUID ownerId) {
        return advertRepository.findAdvertByOwnerId(ownerId);
    }

    public List<Advert> getAdvertsByWinnerId(UUID winnerId) {
        return advertRepository.findAdvertByWinnerId(winnerId);
    }

    public List<Advert> getAllShownAdvertsByPage(int page, String sortType, String sortField) {
        Sort sort = sortType.equals("ASC") ? Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 20, sort);
        return advertRepository.findByVisible(true, pageable);
    }

    public void saveAdvert(Advert advert) {
        advertRepository.save(advert);
    }

    public int getAdvertCount() {
        Sort sort = Sort.by("createdOn").descending();
        Pageable pageable = PageRequest.of(0, 10000, sort);
        return advertRepository.findByVisible(true, pageable).size();
    }

    public Advert getAdvertById(UUID id) {
        return advertRepository.findById(id).orElseThrow(() -> new DomainException("Advert with ID [%s] is not found!".formatted(id)));
    }

    public void reserveCarAdvert(UUID id, User winner) {
        Advert advert = getAdvertById(id);
        advert.setVisible(false);
        advert.setUpdatedOn(LocalDateTime.now());
        advert.setCarStatus(CarStatus.RESERVED);
        advert.setWinner(winner);
        advertRepository.save(advert);
    }

    public void updateAdvert(UUID id, Advert advert) {
        Advert advertToUpdate = getAdvertById(id);
        LocalDateTime now = LocalDateTime.now();
        Advert build = advertToUpdate.builder()
                .id(advertToUpdate.getId())
                .owner(advertToUpdate.getOwner())
                .advertName(advert.getAdvertName())
                .description(advert.getDescription())
                .carBrand(advert.getCarBrand())
                .carModel(advert.getCarModel())
                .mileage(advert.getMileage())
                .manufactureYear(advert.getManufactureYear())
                .horsePower(advert.getHorsePower())
                .fuelType(advert.getFuelType())
                .gearboxType(advert.getGearboxType())
                .buyNowPrice(advert.getBuyNowPrice())
                .imageURL(advert.getImageURL())
                .expireDate(advert.getExpireDate())
                .updatedOn(LocalDateTime.now())
                .visible(advert.isVisible())
                .viewCount(advert.getViewCount())
                .winner(advert.getWinner())
                .carStatus(advert.getCarStatus())
                .createdOn(advertToUpdate.getCreatedOn())
                .updatedOn(now)
                .build();
        advertRepository.save(build);
    }

    public List<Advert> getFirst20VisibleAdverts() {
        return advertRepository.findByVisibleTrue().stream().filter(Advert::isVisible).limit(20).toList();
    }

    public List<Advert> getAllExpiredAdverts() {
        LocalDateTime now = LocalDateTime.now();
        return advertRepository.findByExpireDate(now);
    }

    public void expireAdvert(Advert advert) {
        advert.setVisible(false);
        advert.setUpdatedOn(LocalDateTime.now());
        advertRepository.save(advert);
        log.info("Advert with id [%s] has expired!".formatted(advert.getId()));
    }

}
