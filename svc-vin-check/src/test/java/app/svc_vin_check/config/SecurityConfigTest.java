package app.svc_vin_check.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import app.svc_vin_check.repository.VinCheckRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private VinCheckRepository vinCheckRepository;

    @Test
    @DisplayName("Should allow access to VIN API endpoint without authentication")
    public void shouldAllowAccessToVinApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", "WBAWL73589P473158"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require authentication for other endpoints")
    public void shouldRequireAuthenticationForOtherEndpoints() throws Exception {
        mockMvc.perform(get("/some-other-endpoint"))
                .andExpect(status().is(403)); // 403 Forbidden (Spring Security returns this instead of 401)
    }
}