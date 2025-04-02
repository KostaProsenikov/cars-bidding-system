package app.subscription.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import app.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionPeriod period;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    private BigDecimal price;

    private Boolean renewalAllowed;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime completedOn;

    @Column(nullable = true)
    private Integer vinChecksLeft;
}
