package app.advert.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String advertName;

    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Column(nullable = false)
    private LocalDateTime expireDate;

    @Column(nullable = false)
    private boolean visible;

    private int viewCount;

    @Enumerated(EnumType.STRING)
    private CarBrand carBrand;

    private String carModel;

    private int manufactureYear;

    private int horsePower;

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    private GearboxType gearboxType;

    private String imageURL;

    private BigDecimal buyNowPrice;

    private String description;

    private boolean isBiddingOpen;

    private BigDecimal minBidPrice;

    private BigDecimal currentBidPrice;
}
