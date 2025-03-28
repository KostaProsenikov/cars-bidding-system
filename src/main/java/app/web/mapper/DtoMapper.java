package app.web.mapper;

import app.advert.model.Advert;
import app.user.model.User;
import app.web.dto.CreateNewAdvertRequest;
import app.web.dto.UserEditRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class DtoMapper {

    public static UserEditRequest mapUserToUserEditRequest(User user) {
        return UserEditRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    public static CreateNewAdvertRequest mapAdvertToCreateNewAdvertRequest(Advert advert) {
        return CreateNewAdvertRequest.builder()
                .id(advert.getId())
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
                .visible(advert.isVisible())
                .isBiddingOpen(advert.isBiddingOpen())
                .build();
    }

    public static Advert mapCreateNewAdvertRequestToAdvert(CreateNewAdvertRequest createAdvertRequest, Advert existingAdvert) {
        // Map DTO to entity, updating existing entity fields
        LocalDateTime now = LocalDateTime.now();
        return Advert.builder()
                .id(existingAdvert.getId())
                .advertName(createAdvertRequest.getAdvertName())
                .description(createAdvertRequest.getDescription())
                .carBrand(createAdvertRequest.getCarBrand())
                .carModel(createAdvertRequest.getCarModel())
                .manufactureYear(createAdvertRequest.getManufactureYear())
                .horsePower(createAdvertRequest.getHorsePower())
                .fuelType(createAdvertRequest.getFuelType())
                .gearboxType(createAdvertRequest.getGearboxType())
                .buyNowPrice(createAdvertRequest.getBuyNowPrice())
                .imageURL(createAdvertRequest.getImageURL())
                .visible(Boolean.TRUE.equals(createAdvertRequest.getVisible()))
                .isBiddingOpen(Boolean.TRUE.equals(createAdvertRequest.getIsBiddingOpen()))
                .minBidPrice(createAdvertRequest.getMinBidPrice())
                .mileage(createAdvertRequest.getMileage())
                .currentBidPrice(createAdvertRequest.getCurrentBidPrice())
                .owner(existingAdvert.getOwner())
                .createdOn(existingAdvert.getCreatedOn())
                .expireDate(existingAdvert.getExpireDate())
                .viewCount(existingAdvert.getViewCount())
                .updatedOn(now)
                .build();
    }

}