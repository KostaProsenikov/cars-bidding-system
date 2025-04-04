package app.svc_vin_check.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vin_checks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinCheck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String vinNumber;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private LocalDateTime checkedAt;
    
    private String manufacturer;
    
    private String modelYear;
    
    private String assemblyPlant;
    
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String resultJson;
}