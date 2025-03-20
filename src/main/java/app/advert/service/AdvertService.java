package app.advert.service;

import app.advert.model.Advert;
import app.advert.repository.AdvertRepository;
import app.user.model.User;
import app.web.dto.CreateNewAdvertRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvertService {

    private final AdvertRepository advertRepository;

    @Autowired
    public AdvertService(AdvertRepository advertRepository) {
        this.advertRepository = advertRepository;
    }

    @Transactional
    public Advert createNewAd(CreateNewAdvertRequest createNewAdvertRequest, User user) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireDate = now.plusDays(30);


        Advert advert = Advert.builder()
                .advertName(createNewAdvertRequest.getAdvertName())
                .description(createNewAdvertRequest.getDescription())
                .owner(user)
                .carBrand(createNewAdvertRequest.getCarBrand())
                .carModel(createNewAdvertRequest.getCarModel())
                .isBiddingOpen(createNewAdvertRequest.getIsBiddingOpen())
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

        System.out.printf("Advert created: %s%n", advert);

        return advert;
    }

    public List<Advert> getFirst20VisibleAdverts() {
        List<Advert> adverts = advertRepository.findByVisibleTrue().stream().filter(a -> a.isVisible()).limit(20).toList();
        return adverts;
    }
}
