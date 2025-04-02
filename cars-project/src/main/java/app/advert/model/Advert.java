package app.advert.model;

import app.bid.model.Bid;
import app.user.model.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Nullable
    private LocalDateTime lastBidDate;

    @ManyToOne(fetch = FetchType.EAGER)
    private User lastBidder;

    @Column(nullable = false)
    private LocalDateTime expireDate;

    private Boolean visible;

    @Min(0)
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    private CarBrand carBrand;

    @Size(min = 1, max = 100)
    private String carModel;

    @Min(1950)
    @Max(2025)
    private Integer manufactureYear;

    @Min(0)
    @Max(1000)
    private Integer horsePower;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private GearboxType gearboxType;

    @URL
    private String imageURL;

    @NotNull
    private BigDecimal mileage;

    @NotNull
    private BigDecimal buyNowPrice;

    @NotNull
    @Size(min = 4, max = 1000)
    private String description;

    @Column(name = "is_bidding_open")
    private Boolean biddingOpen;

    @Column(name = "min_bid_price")
    private BigDecimal minBidPrice;

    @Min(0)
    private BigDecimal currentBidPrice;

    @Enumerated(EnumType.STRING)
    private CarStatus carStatus;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "advert")
    @OrderBy("createdOn DESC")
    private List<Bid> bids = new ArrayList<>();

    @Nullable
    @ManyToOne(fetch = FetchType.EAGER)
    private User winner;

    @Nullable
    @Size(min = 17, max = 17, message = "VIN number must be exactly 17 characters")
    private String vinNumber;
}
