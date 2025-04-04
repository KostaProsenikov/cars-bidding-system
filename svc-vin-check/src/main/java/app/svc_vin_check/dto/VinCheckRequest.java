package app.svc_vin_check.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VinCheckRequest {
    
    @NotBlank(message = "VIN number is required")
    private String vinNumber;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
}