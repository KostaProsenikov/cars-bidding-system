package app.vin.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Client for interacting with the VIN service
 */
@Component
public class VinClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${vin.service.url:http://localhost:8082}")
    private String vinServiceUrl;
    
    /**
     * Get VIN information from external service
     * @param vin The Vehicle Identification Number
     * @return Response with VIN data
     */
    public ResponseEntity<String> getVINInformation(String vin) {
        try {
            String url = vinServiceUrl + "/vin-svc/api/v1/get-vin?vin=" + vin;
            return restTemplate.getForEntity(url, String.class);
        } catch (Exception e) {
            // Fallback response for demo purposes
            return ResponseEntity.ok("{ \"manufacturer\": \"Demo Car\", \"model_year\": \"2025\", \"assembly_plant_code\": \"Z\", \"status\": \"VALID\" }");
        }
    }
    
    /**
     * Save VIN check to database
     * @param vin The Vehicle Identification Number
     * @param userId The ID of the user checking the VIN
     * @return Response with saved VIN check data
     */
    public ResponseEntity<Map<String, Object>> saveVinCheck(String vin, UUID userId) {
        try {
            String url = vinServiceUrl + "/vin-svc/api/v1/save-vin-check";
            
            // Log for debugging
            System.out.println("Saving VIN check to microservice: " + url);
            System.out.println("VIN: " + vin + ", User ID: " + userId);
            
            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vinNumber", vin);
            requestBody.put("userId", userId);  // Send UUID directly, not as string
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create HTTP entity with headers and body
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Make the POST request with proper parameterized type
            ResponseEntity<HashMap<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new org.springframework.core.ParameterizedTypeReference<HashMap<String, Object>>() {}
            );
            
            // Log success
            System.out.println("Successfully saved VIN check to microservice: " + response.getStatusCode());
            
            return (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) response;
        } catch (Exception e) {
            // Log the exception and return a fallback response
            System.err.println("Error saving VIN check: " + e.getMessage());
            e.printStackTrace();
            
            // Create a fallback response for demo purposes
            Map<String, Object> fallbackResponse = new HashMap<>();
            fallbackResponse.put("id", UUID.randomUUID().toString());
            fallbackResponse.put("vinNumber", vin);
            fallbackResponse.put("userId", userId.toString());
            fallbackResponse.put("status", "VALID");
            fallbackResponse.put("manufacturer", "Demo Car");
            fallbackResponse.put("modelYear", "2025");
            fallbackResponse.put("assemblyPlant", "Z");
            
            return ResponseEntity.ok(fallbackResponse);
        }
    }
    
    /**
     * Get VIN check history for a user
     * @param userId The ID of the user
     * @return Response with list of VIN checks
     */
    public ResponseEntity<Object[]> getUserVinHistory(UUID userId) {
        try {
            String url = vinServiceUrl + "/vin-svc/api/v1/user-vin-history?userId=" + userId;
            
            // Log for debugging
            System.out.println("Calling VIN microservice: " + url);
            
            ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);
            
            // Log the response for debugging
            System.out.println("VIN microservice response status: " + response.getStatusCode());
            System.out.println("VIN microservice response body length: " + 
                (response.getBody() != null ? response.getBody().length : 0));
            
            return response;
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error getting VIN history from microservice: " + e.getMessage());
            e.printStackTrace();
            
            // Return empty array for fallback
            return ResponseEntity.ok(new Object[0]);
        }
    }
    
    /**
     * Check if user has already checked this VIN
     * @param vinNumber The VIN to check
     * @param userId The user ID
     * @return true if the user has already checked this VIN
     */
    public boolean hasUserCheckedVin(String vinNumber, UUID userId) {
        try {
            String url = vinServiceUrl + "/vin-svc/api/v1/has-checked-vin?vinNumber=" + vinNumber + "&userId=" + userId;
            
            // Log for debugging
            System.out.println("Checking if user has checked VIN: " + url);
            
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            
            // Log the response for debugging
            System.out.println("VIN check exists response: " + response.getBody());
            
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error checking VIN history from microservice: " + e.getMessage());
            
            // Assume not checked in case of error
            return false;
        }
    }
}
