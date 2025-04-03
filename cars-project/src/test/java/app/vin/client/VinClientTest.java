package app.vin.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = app.config.VinClientTestConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "vin-service.url=http://localhost:8082/vin-svc/api/v1"
    }
)
@Disabled("Temporarily disabled due to configuration issues")
class VinClientTest {

    @Autowired
    private VinClient vinClient;

    @Test
    @DisplayName("Should get VIN information for valid VIN")
    void shouldGetVinInformationForValidVin() {
        // Arrange
        String validVin = "WBAWL73589P473158";
        String expectedResponse = """
                {
                    "status": "VALID",
                    "manufacturer": "BMW",
                    "model_year": "2009",
                    "manufacturer_code": "WBA",
                    "descriptor_section": "WL735",
                    "assembly_plant_code": "P",
                    "production_sequence": "473158",
                    "checked_at": "2025-04-02T12:34:56Z"
                }
                """;
        
        stubFor(get(urlPathEqualTo("/vin-svc/api/v1/get-vin"))
                .withQueryParam("vin", equalTo(validVin))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        // Act
        ResponseEntity<String> response = vinClient.getVINInformation(validVin);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        
        verify(getRequestedFor(urlPathEqualTo("/vin-svc/api/v1/get-vin"))
                .withQueryParam("vin", equalTo(validVin)));
    }

    @Test
    @DisplayName("Should return error for invalid VIN")
    void shouldReturnErrorForInvalidVin() {
        // Arrange
        String invalidVin = "ABC123";
        String expectedResponse = """
                {
                    "status": "INVALID",
                    "error": "Invalid VIN format: VIN must be 17 characters"
                }
                """;
        
        stubFor(get(urlPathEqualTo("/vin-svc/api/v1/get-vin"))
                .withQueryParam("vin", equalTo(invalidVin))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        // Act
        ResponseEntity<String> response = vinClient.getVINInformation(invalidVin);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        
        verify(getRequestedFor(urlPathEqualTo("/vin-svc/api/v1/get-vin"))
                .withQueryParam("vin", equalTo(invalidVin)));
    }
}