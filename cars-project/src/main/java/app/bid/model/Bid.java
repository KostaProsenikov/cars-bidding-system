package app.bid.model;

import app.advert.model.Advert;
import app.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User bidder;

    @ManyToOne
    private Advert advert;

    @NotNull
    private BigDecimal bidPrice;

    private Boolean isAccepted;

    private BigDecimal maxBidPrice;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
