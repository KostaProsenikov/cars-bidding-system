package app.leasing.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Leasing {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn (name = "id")
    private User applicant;

    @Column (nullable = false)
    private Long leasingMonths;

    @Column(nullable = false)
    private BigDecimal leasingPrice;

    @Column(nullable = false)
    private String bankName;

    private BigDecimal monthlyPayment;

    private BigDecimal totalPayment;

    private BigDecimal monthlySalary;

    @Column(nullable = false)
    private Double interestRate;

}
