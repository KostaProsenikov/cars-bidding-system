package app.advert.service;

import app.advert.model.Advert;
import app.advert.repository.AdvertRepository;
import app.web.dto.CreateNewAdvertRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AdvertService {

    private final AdvertRepository advertRepository;

    @Autowired
    public AdvertService(AdvertRepository advertRepository) {
        this.advertRepository = advertRepository;
    }

    @Transactional
    public Advert createNewAd(CreateNewAdvertRequest createNewAdvertRequest) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireDate = now.plusDays(30);

        Advert advert = Advert.builder()
                .advertName(createNewAdvertRequest.getAdvertName())
                .description(createNewAdvertRequest.getDescription())
                .owner(createNewAdvertRequest.getOwner())
                .carBrand(createNewAdvertRequest.getCarBrand())
                .carModel(createNewAdvertRequest.getCarModel())
                .releaseYear(LocalDate.ofEpochDay(createNewAdvertRequest.getReleaseYear()))
                .horsePower(createNewAdvertRequest.getHorsePower())
                .fuelType(createNewAdvertRequest.getFuelType())
                .gearboxType(createNewAdvertRequest.getGearboxType())
                .buyNowPrice(createNewAdvertRequest.getBuyNowPrice())
                .imageURL(createNewAdvertRequest.getImageURL())
                .expireDate(expireDate)
                .createdOn(now)
                .updatedOn(now)
                .build();

        System.out.println(advert);

        return advert;
    }
}
