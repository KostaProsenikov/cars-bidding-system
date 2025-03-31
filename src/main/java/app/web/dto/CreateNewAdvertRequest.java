package app.web.dto;

import app.advert.model.CarBrand;
import app.advert.model.CarStatus;
import app.advert.model.FuelType;
import app.advert.model.GearboxType;
import app.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNewAdvertRequest {

    private UUID id;

    @NotNull
    @Size (min = 5, max = 50, message = "The ad must be at least 5 characters and maximum 50!")
    private String advertName;

    @NotNull
    @Size (min = 5, max = 500, message = "The description must be at least 5 characters and maximum 500!")
    private String description;

    @URL (message = "Image URL should be a valid URL.")
    private String imageURL;

    @NotNull
    @Positive(message = "Horse power must be a positive number.")
    private Integer horsePower;

    @Nullable
    private Boolean isBiddingOpen;

    @Positive(message = "Price must be a positive number.")
    private Integer price;

    private User owner;

    @NotNull(message = "Fuel type must be selected.")
    private FuelType fuelType;

    @NotNull(message = "Gearbox type must be selected.")
    private GearboxType gearboxType;

    @PositiveOrZero(message = "Mileage must be a positive number.")
    private BigDecimal mileage;

    @NotNull(message = "Car brand must be selected.")
    private CarBrand carBrand;

    @NotNull
    private String carModel;

    @Positive
    @Min (1950)
    @Max (2025)
    private Integer manufactureYear;

    @Positive
    @NotNull
    private BigDecimal buyNowPrice;

    @PositiveOrZero
    private BigDecimal minBidPrice;

    @PositiveOrZero
    private BigDecimal currentBidPrice;

    @Nullable
    private Boolean visible;

    private CarStatus carStatus;

    private Integer viewCount;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private LocalDateTime expireDate;

    private User winner;
}
