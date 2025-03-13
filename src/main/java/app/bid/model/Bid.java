package app.bid.model;

import app.advert.model.Advert;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bid {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User bidder;

    @ManyToOne
    private Advert advert;

    private BigDecimal bidPrice;

    private boolean isAccepted;

    private BigDecimal maxBidPrice;

    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
