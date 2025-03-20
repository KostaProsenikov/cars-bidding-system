package app.advert.model;

import app.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Advert {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private String advertName;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdOn;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private LocalDateTime expireDate;

    private boolean visible;

    @Min(0)
    private int viewCount;

    @Enumerated(EnumType.STRING)
    private CarBrand carBrand;

    private String carModel;

    @Min(1950)
    @Max(2025)
    private int manufactureYear;

    @Min(0)
    @Max(1000)
    private int horsePower;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private GearboxType gearboxType;

    @URL
    private String imageURL;

    @NotNull
    private BigDecimal buyNowPrice;

    @NotNull
    @Size(min = 5, max = 1000)
    private String description;

    private boolean isBiddingOpen;

    private BigDecimal minBidPrice;

    @Min(0)
    private BigDecimal currentBidPrice;
}
