package app.web.dto;

import app.advert.model.CarBrand;
import app.advert.model.FuelType;
import app.advert.model.GearboxType;
import app.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewAdvertRequest {

    @NotNull
    @Size (min = 5, max = 50, message = "The ad must be at least 5 characters and maximum 50!")
    private String advertName;

    @NotNull
    @Size (min = 5, max = 500, message = "The description must be at least 5 characters and maximum 500!")
    private String description;

    @URL (message = "Image URL should be a valid URL.")
    private String imageURL;

    @NotNull
    @Positive
    private int horsePower;

    @Nullable
    private Boolean isBiddingOpen;

    @NotNull
    private int price;

    @NotNull
    private User owner;

    @NotNull
    private FuelType fuelType;

    @NotNull
    private GearboxType gearboxType;

    @NotNull
    private CarBrand carBrand;

    @NotNull
    private String carModel;

    @Positive
    @Min (1950)
    @Max (2025)
    private int manufactureYear;

    @Positive
    @NotNull
    private BigDecimal buyNowPrice;

    @PositiveOrZero
    private BigDecimal minBidPrice;

    @PositiveOrZero
    private BigDecimal currentBidPrice;

    @Nullable
    private Boolean visible;
}
