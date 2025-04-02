package app.advert.service;

//import app.advert.model.Advert;
//import app.advert.model.CarBrand;
//import app.advert.model.FuelType;
//import app.advert.model.GearboxType;
//import app.advert.repository.AdvertRepository;
//import app.user.model.User;
//import app.user.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;

//@Service
//public class AdvertSeederService {
//
//    private final AdvertRepository advertRepository;
//    private final UserRepository userRepository; // Assuming you have a user repository
//
//    @Autowired
//    public AdvertSeederService(AdvertRepository advertRepository, UserRepository userRepository) {
//        this.advertRepository = advertRepository;
//        this.userRepository = userRepository;
//    }
//
//
//    public void seedAdverts() {
//        User owner = userRepository.findAll().get(0); // Fetch an existing user for testing
//
//        List<Advert> adverts = new ArrayList<>();
//        for (int i = 1; i <= 30; i++) {
//            Advert advert = Advert.builder()
//                    .advertName("Car " + i)
//                    .owner(owner)
//                    .createdOn(LocalDateTime.now())
//                    .updatedOn(LocalDateTime.now())
//                    .expireDate(LocalDateTime.now().plusDays(30))
//                    .visible(true)
//                    .viewCount(0)
//                    .carBrand(CarBrand.TOYOTA) // Replace with your enum values
//                    .carModel("Model " + i)
//                    .manufactureYear(2000 + (i % 23)) // Years from 2000 to 2023
//                    .horsePower(100 + i)
//                    .fuelType(FuelType.GASOLINE) // Replace with your enum values
//                    .gearboxType(GearboxType.AUTOMATIC) // Replace with your enum values
//                    .imageURL("https://example.com/car" + i + ".jpg")
//                    .buyNowPrice(new BigDecimal("10000.00").add(new BigDecimal(i * 500)))
//                    .description("Car " + i + " is a great vehicle.")
//                    .biddingOpen(true)
//                    .minBidPrice(new BigDecimal("5000.00"))
//                    .currentBidPrice(null)
//                    .build();
//            adverts.add(advert);
//        }
//
//        advertRepository.saveAll(adverts);
//        System.out.println("Seeded 30 adverts successfully!");
//    }
//}
