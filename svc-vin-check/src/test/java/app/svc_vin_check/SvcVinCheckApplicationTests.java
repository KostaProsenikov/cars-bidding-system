package app.svc_vin_check;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import app.svc_vin_check.config.SecurityConfig;
import app.svc_vin_check.repository.VinCheckRepository;
import app.svc_vin_check.web.IndexController;

import java.util.Map;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SvcVinCheckApplicationTests {

	@Autowired
	private IndexController indexController;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private TestRestTemplate restTemplate;
	
	@MockBean
	private VinCheckRepository vinCheckRepository;

	@Test
	@DisplayName("Context loads and all components are created")
	void contextLoads() {
		// Verify main components are loaded
		assertThat(indexController).isNotNull();
		assertThat(securityConfig).isNotNull();
		assertThat(applicationContext).isNotNull();
	}
	
	@Test
	@DisplayName("Application can be started")
	@org.junit.jupiter.api.Disabled("Disabled due to port conflict in CI pipeline")
	void applicationStarts() {
		// Test the main method (mostly for coverage)
		SvcVinCheckApplication.main(new String[]{});
		
		// If we get here without exception, the test passes
		assertThat(true).isTrue();
	}
	
	@Test
	@DisplayName("Integration test - VIN endpoint returns valid information")
	void vinEndpointReturnsValidInformation() {
		// Given a valid BMW VIN
		String validVin = "WBAWL73589P473158";
		
		// When we call the API
		ResponseEntity<Map> response = restTemplate.getForEntity(
			"/vin-svc/api/v1/get-vin?vin={vin}", 
			Map.class, 
			validVin
		);
		
		// Then we should get valid response
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().get("status")).isEqualTo("VALID");
		assertThat(response.getBody().get("manufacturer")).isEqualTo("BMW");
		assertThat(response.getBody().get("manufacturer_code")).isEqualTo("WBA");
		assertThat(response.getBody().get("descriptor_section")).isEqualTo("WL735");
		assertThat(response.getBody().get("assembly_plant_code")).isEqualTo("P");
		assertThat(response.getBody().get("production_sequence")).isEqualTo("473158");
		assertThat(response.getBody().get("checked_at")).isNotNull();
	}
	
	@Test
	@DisplayName("Integration test - Invalid VIN returns error")
	void invalidVinReturnsError() {
		// Given an invalid VIN
		String invalidVin = "INVALID";
		
		// When we call the API
		ResponseEntity<Map> response = restTemplate.getForEntity(
			"/vin-svc/api/v1/get-vin?vin={vin}", 
			Map.class, 
			invalidVin
		);
		
		// Then we should get an error response
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().get("status")).isEqualTo("INVALID");
		assertThat(response.getBody().get("error").toString()).contains("Invalid VIN format");
	}
	
	@Test
	@DisplayName("Integration test - Missing VIN parameter returns error")
	void missingVinParameterReturnsError() {
		// When we call the API without a VIN parameter
		ResponseEntity<Map> response = restTemplate.getForEntity(
			"/vin-svc/api/v1/get-vin", 
			Map.class
		);
		
		// Then we should get an error response
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
}