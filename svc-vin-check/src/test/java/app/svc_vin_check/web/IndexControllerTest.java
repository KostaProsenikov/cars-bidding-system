package app.svc_vin_check.web;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.svc_vin_check.config.SecurityConfig;
import app.svc_vin_check.dto.VinCheckRequest;
import app.svc_vin_check.model.VinCheck;
import app.svc_vin_check.repository.VinCheckRepository;

@WebMvcTest(IndexController.class)
@Import(SecurityConfig.class)
public class IndexControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private VinCheckRepository vinCheckRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should allow access to VIN endpoint without authentication")
    public void shouldAllowAccessToVinEndpointWithoutAuthentication() throws Exception {
        // Valid BMW VIN
        String validVin = "WBAWL73589P473158";
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", validVin))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status", is("VALID")))
                .andExpect(jsonPath("$.manufacturer", is("BMW")))
                .andExpect(jsonPath("$.model_year", containsString("2009")))
                .andExpect(jsonPath("$.manufacturer_code", is("WBA")))
                .andExpect(jsonPath("$.descriptor_section", is("WL735")))
                .andExpect(jsonPath("$.assembly_plant_code", is("P")))
                .andExpect(jsonPath("$.production_sequence", is("473158")))
                .andExpect(jsonPath("$.checked_at", not(emptyString())));
    }
    
    @Test
    @DisplayName("Should return error for invalid VIN")
    public void shouldReturnErrorForInvalidVin() throws Exception {
        // Invalid VIN (too short)
        String invalidVin = "ABC123";
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", invalidVin))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status", is("INVALID")))
                .andExpect(jsonPath("$.error", containsString("Invalid VIN format")));
    }
    
    @Test
    @DisplayName("Should validate VIN contains only valid characters")
    public void shouldValidateVinContainsOnlyValidCharacters() throws Exception {
        // Invalid VIN (contains invalid characters)
        String invalidVin = "WBAWL735Q9P473158"; // Contains 'Q' which is invalid
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", invalidVin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("INVALID")));
    }
    
    @Test
    @DisplayName("Should identify Harley-Davidson")
    public void shouldIdentifyHarleyDavidson() throws Exception {
        String vin = "1HDKDZKC5EJ020525";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Harley-Davidson")));
    }
    
    @Test
    @DisplayName("Should identify Chevrolet USA")
    public void shouldIdentifyChevroletUSA() throws Exception {
        String vin = "1G1YY22G4X5800307";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Chevrolet USA")));
    }
    
    @Test
    @DisplayName("Should identify Oldsmobile USA")
    public void shouldIdentifyOldsmobileUSA() throws Exception {
        String vin = "1G3AR69H0JM731531";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Oldsmobile USA")));
    }
    
    @Test
    @DisplayName("Should identify Chevrolet Truck USA")
    public void shouldIdentifyChevroletTruckUSA() throws Exception {
        String vin = "1GCEC14X57Z565899";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Chevrolet Truck USA")));
    }
    
    @Test
    @DisplayName("Should identify GMC Truck USA")
    public void shouldIdentifyGMCTruckUSA() throws Exception {
        String vin = "1GTEC14X67Z565899";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("GMC Truck USA")));
    }
    
    @Test
    @DisplayName("Should identify Ford USA")
    public void shouldIdentifyFordUSA() throws Exception {
        String vin = "1FAFP45X83F383546";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Ford USA")));
    }
    
    @Test
    @DisplayName("Should identify Ford Truck USA")
    public void shouldIdentifyFordTruckUSA() throws Exception {
        String vin = "1FTPW14V48FA56897";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Ford Truck USA")));
    }
    
    @Test
    @DisplayName("Should identify Ford SUV USA")
    public void shouldIdentifyFordSUVUSA() throws Exception {
        String vin = "1FMCU0F78DUC44368";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Ford SUV USA")));
    }
    
    @Test
    @DisplayName("Should identify Acura")
    public void shouldIdentifyAcura() throws Exception {
        String vin = "JH4KA7631NC003943";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Acura")));
    }
    
    @Test
    @DisplayName("Should identify Honda")
    public void shouldIdentifyHonda() throws Exception {
        String vin = "JHMCG56492C023604";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Honda")));
    }
    
    @Test
    @DisplayName("Should identify Toyota")
    public void shouldIdentifyToyota() throws Exception {
        String vin = "JT4RN01P0N7083936";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Toyota")));
    }
    
    @Test
    @DisplayName("Should identify Toyota TD variant")
    public void shouldIdentifyToyotaTD() throws Exception {
        String vin = "JTDKN3DU0D5587558";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Toyota")));
    }
    
    @Test
    @DisplayName("Should identify Volkswagen")
    public void shouldIdentifyVolkswagen() throws Exception {
        String vin = "WVWZZZ1JZ3W386752";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Volkswagen")));
    }
    
    @Test
    @DisplayName("Should identify Audi")
    public void shouldIdentifyAudi() throws Exception {
        String vin = "WAUZZZ8E87A010965";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Audi")));
    }
    
    @Test
    @DisplayName("Should identify BMW")
    public void shouldIdentifyBMW() throws Exception {
        String vin = "WBAWL73589P473158";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("BMW")));
    }
    
    @Test
    @DisplayName("Should identify BMW M")
    public void shouldIdentifyBMWM() throws Exception {
        String vin = "WBSBL93484JR13555";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("BMW M")));
    }
    
    @Test
    @DisplayName("Should identify Mercedes-Benz")
    public void shouldIdentifyMercedesBenz() throws Exception {
        String vin = "WDDGF4HB7DR258998";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Mercedes-Benz")));
    }
    
    @Test
    @DisplayName("Should identify Volvo")
    public void shouldIdentifyVolvo() throws Exception {
        String vin = "YV1LS5543N1037901";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Volvo")));
    }
    
    @Test
    @DisplayName("Should identify Tesla Model 3/Y")
    public void shouldIdentifyTeslaModelY() throws Exception {
        String vin = "5YJ3E1EA1PF446330";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Tesla")));
    }
    
    @Test
    @DisplayName("Should identify Tesla Model S/X")
    public void shouldIdentifyTeslaModelS() throws Exception {
        String vin = "7SAXS2349NF412280";
        mockMvc.perform(get("/vin-svc/api/v1/get-vin").param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manufacturer", is("Tesla")));
    }
    
    @ParameterizedTest
    @DisplayName("Should correctly decode model years")
    @ValueSource(strings = {
        "WVWZZZ1JZ1W386752", // '1' = 2001
        "WVWZZZ1JZ2W386752", // '2' = 2002
        "WVWZZZ1JZ3W386752", // '3' = 2003
        "WVWZZZ1JZ4W386752", // '4' = 2004
        "WVWZZZ1JZ5W386752", // '5' = 2005
        "WVWZZZ1JZ6W386752", // '6' = 2006
        "WVWZZZ1JZ7W386752", // '7' = 2007
        "WVWZZZ1JZ8W386752", // '8' = 2008
        "WVWZZZ1JZ9W386752", // '9' = 2009
        "WVWZZZ1JZAW386752", // 'A' = 1980 or 2010
        "WVWZZZ1JZBW386752", // 'B' = 1981 or 2011
        "WVWZZZ1JZCW386752", // 'C' = 1982 or 2012
        "WVWZZZ1JZDW386752", // 'D' = 1983 or 2013
        "WVWZZZ1JZEW386752", // 'E' = 1984 or 2014
        "WVWZZZ1JZFW386752", // 'F' = 1985 or 2015
        "WVWZZZ1JZGW386752", // 'G' = 1986 or 2016
        "WVWZZZ1JZHW386752", // 'H' = 1987 or 2017
        "WVWZZZ1JZJW386752", // 'J' = 1988 or 2018
        "WVWZZZ1JZKW386752", // 'K' = 1989 or 2019
        "WVWZZZ1JZLW386752", // 'L' = 1990 or 2020
        "WVWZZZ1JZMW386752", // 'M' = 1991 or 2021
        "WVWZZZ1JZNW386752", // 'N' = 1992 or 2022
        "WVWZZZ1JZPW386752", // 'P' = 1993 or 2023
        "WVWZZZ1JZRW386752", // 'R' = 1994 or 2024
        "WVWZZZ1JZSW386752", // 'S' = 1995 or 2025
        "WVWZZZ1JZTW386752", // 'T' = 1996
        "WVWZZZ1JZVW386752", // 'V' = 1997
        "WVWZZZ1JZWW386752", // 'W' = 1998
        "WVWZZZ1JZXW386752", // 'X' = 1999
        "WVWZZZ1JZYW386752"  // 'Y' = 2000
    })
    public void shouldDecodeModelYears(String vin) throws Exception {
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", vin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model_year", not(nullValue())))
                .andExpect(jsonPath("$.model_year", not(is("Unknown"))));
    }
    
    @Test
    @DisplayName("Should return Unknown for invalid year code")
    public void shouldReturnUnknownForInvalidYearCode() throws Exception {
        // Valid VIN but with 'Z' in the year position which should return Unknown
        String vinWithInvalidYear = "WVWZZZ1JZZ0386752";
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", vinWithInvalidYear))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model_year", is("Unknown")));
    }
    
    @Test
    @DisplayName("Should handle unknown manufacturer")
    public void shouldHandleUnknownManufacturer() throws Exception {
        // Valid VIN but with unknown manufacturer
        String unknownVin = "ZZZ123456789ABCDE";
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", unknownVin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("INVALID")));
    }
    
    @Test
    @DisplayName("Should handle null VIN parameter")
    public void shouldHandleNullVinParameter() throws Exception {
        mockMvc.perform(get("/vin-svc/api/v1/get-vin"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should validate VIN length")
    public void shouldValidateVinLength() throws Exception {
        // Valid characters but wrong length (too long)
        String invalidVin = "WBA12345678901234567";
        
        mockMvc.perform(get("/vin-svc/api/v1/get-vin")
                .param("vin", invalidVin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("INVALID")));
    }
    
    @Test
    @DisplayName("Should save VIN check")
    public void shouldSaveVinCheck() throws Exception {
        // Setup mock repository
        UUID userId = UUID.randomUUID();
        String vin = "WBAWL73589P473158";
        
        VinCheck savedCheck = VinCheck.builder()
            .id(UUID.randomUUID())
            .vinNumber(vin)
            .userId(userId)
            .manufacturer("BMW")
            .modelYear("2009")
            .assemblyPlant("P")
            .status("VALID")
            .resultJson("{\"status\":\"VALID\",\"manufacturer\":\"BMW\"}")
            .build();
        
        when(vinCheckRepository.save(any(VinCheck.class))).thenReturn(savedCheck);
        
        // Create request
        VinCheckRequest request = new VinCheckRequest();
        request.setVinNumber(vin);
        request.setUserId(userId);
        
        mockMvc.perform(post("/vin-svc/api/v1/save-vin-check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vinNumber", is(vin)))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.manufacturer", is("BMW")));
    }
    
    @Test
    @DisplayName("Should get user VIN history")
    public void shouldGetUserVinHistory() throws Exception {
        // Setup
        UUID userId = UUID.randomUUID();
        List<VinCheck> history = new ArrayList<>();
        
        VinCheck check1 = VinCheck.builder()
            .id(UUID.randomUUID())
            .vinNumber("WBAWL73589P473158")
            .userId(userId)
            .manufacturer("BMW")
            .modelYear("2009")
            .build();
        
        history.add(check1);
        
        when(vinCheckRepository.findByUserIdOrderByCheckedAtDesc(userId)).thenReturn(history);
        
        // Execute
        mockMvc.perform(get("/vin-svc/api/v1/user-vin-history")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].manufacturer", is("BMW")));
    }
    
    @Test
    @DisplayName("Should check if user has checked VIN before")
    public void shouldCheckIfUserHasCheckedVinBefore() throws Exception {
        // Setup
        UUID userId = UUID.randomUUID();
        String vin = "WBAWL73589P473158";
        List<VinCheck> history = new ArrayList<>();
        
        VinCheck check = VinCheck.builder()
            .id(UUID.randomUUID())
            .vinNumber(vin)
            .userId(userId)
            .build();
        
        history.add(check);
        
        when(vinCheckRepository.findByUserIdAndVinNumber(userId, vin)).thenReturn(history);
        
        // Execute
        mockMvc.perform(get("/vin-svc/api/v1/has-checked-vin")
                .param("userId", userId.toString())
                .param("vinNumber", vin))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    @Test
    @DisplayName("Should return false if user has not checked VIN before")
    public void shouldReturnFalseIfUserHasNotCheckedVinBefore() throws Exception {
        // Setup
        UUID userId = UUID.randomUUID();
        String vin = "WBAWL73589P473158";
        
        when(vinCheckRepository.findByUserIdAndVinNumber(userId, vin)).thenReturn(new ArrayList<>());
        
        // Execute
        mockMvc.perform(get("/vin-svc/api/v1/has-checked-vin")
                .param("userId", userId.toString())
                .param("vinNumber", vin))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}