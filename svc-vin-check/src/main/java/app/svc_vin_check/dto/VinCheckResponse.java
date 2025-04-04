package app.svc_vin_check.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinCheckResponse {
    
    private UUID id;
    private String vinNumber;
    private UUID userId;
    private LocalDateTime checkedAt;
    private String manufacturer;
    private String modelYear;
    private String assemblyPlant;
    private String status;
    private String resultJson;
}