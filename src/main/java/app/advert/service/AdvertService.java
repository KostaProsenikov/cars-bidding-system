package app.advert.service;

import app.advert.model.Advert;
import app.advert.model.CarStatus;
import app.advert.repository.AdvertRepository;
import app.exception.DomainException;
import app.user.model.User;
import app.web.dto.CreateNewAdvertRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        return (int) advertRepository.count();
    }

    public Advert getAdvertById(UUID id) {
        return advertRepository.findById(id).orElseThrow(() -> new DomainException("Advert with ID [%s] is not found!".formatted(id)));
    }

    public void reserveCarAdvert(UUID id) {
        Advert advert = getAdvertById(id);
        advert.setVisible(false);
        advert.setUpdatedOn(LocalDateTime.now());
        advert.setCarStatus(CarStatus.RESERVED);
        advertRepository.save(advert);
    }

    public void updateAdvert(UUID id, Advert advert) {
        Advert advertToUpdate = getAdvertById(id);
        advertToUpdate.setAdvertName(advert.getAdvertName());
        advertToUpdate.setDescription(advert.getDescription());
        advertToUpdate.setCarBrand(advert.getCarBrand());
        advertToUpdate.setCarModel(advert.getCarModel());
        advertToUpdate.setManufactureYear(advert.getManufactureYear());
        advertToUpdate.setHorsePower(advert.getHorsePower());
        advertToUpdate.setFuelType(advert.getFuelType());
        advertToUpdate.setGearboxType(advert.getGearboxType());
        advertRepository.save(advertToUpdate);
    }

    public List<Advert> getFirst20VisibleAdverts() {
        return advertRepository.findByVisibleTrue().stream().filter(Advert::isVisible).limit(20).toList();
    }
}
