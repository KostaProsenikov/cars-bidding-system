package app.vin.model;

import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class VinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, name = "id")
    private UUID id;
    
    @ManyToOne
    private User user;
    
    @Column(nullable = false)
    private String vinNumber;
    
    @Column(nullable = false)
    private LocalDateTime checkedOn;
    
    @Column(columnDefinition = "TEXT")
    private String resultJson;
    
    private String manufacturer;
    
    private String modelYear;
    
    private String assemblyPlant;
    
    private String status;
}
