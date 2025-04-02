package app.svc_vin_check.web;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vin-svc/api/v1")
public class IndexController {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    @GetMapping("/get-vin")
    public ResponseEntity<?> getVinInfo(@RequestParam String vin) {
        // Basic VIN validation (standard VINs are 17 characters)
        if (vin == null || !VIN_PATTERN.matcher(vin).matches()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid VIN format. VIN must be 17 characters with valid characters.");
            error.put("status", "INVALID");
            return ResponseEntity.badRequest().body(error);
        }

        // For demonstration, extract and return information from the VIN
        Map<String, Object> vinInfo = decodeVin(vin);
        if(vinInfo.get("manufacturer").equals("Unknown manufacturer")) {
            vinInfo.put("status", "INVALID");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid VIN format.");
            error.put("status", "INVALID");
            return ResponseEntity.badRequest().body(error);
        } else {
            vinInfo.put("status", "VALID");
        }

        vinInfo.put("checked_at", LocalDateTime.now().toString());

        return ResponseEntity.ok(vinInfo);
    }

    private Map<String, Object> decodeVin(String vin) {
        Map<String, Object> info = new HashMap<>();

        // Extract basic information from VIN (simplified for demonstration)
        // Real implementation would use more sophisticated decoding

        // World Manufacturer Identifier (first 3 characters)
        String wmi = vin.substring(0, 3);
        info.put("manufacturer_code", wmi);

        // Map common manufacturer codes
        switch (wmi) {
            case "1HD": info.put("manufacturer", "Harley-Davidson"); break;
            case "1G1": info.put("manufacturer", "Chevrolet USA"); break;
            case "1G3": info.put("manufacturer", "Oldsmobile USA"); break;
            case "1GC": info.put("manufacturer", "Chevrolet Truck USA"); break;
            case "1GT": info.put("manufacturer", "GMC Truck USA"); break;
            case "1FA": info.put("manufacturer", "Ford USA"); break;
            case "1FT": info.put("manufacturer", "Ford Truck USA"); break;
            case "1FM": info.put("manufacturer", "Ford SUV USA"); break;
            case "JH4": info.put("manufacturer", "Acura"); break;
            case "JHM": info.put("manufacturer", "Honda"); break;
            case "JT4": info.put("manufacturer", "Toyota"); break;
            case "JTD": info.put("manufacturer", "Toyota"); break;
            case "WVW": info.put("manufacturer", "Volkswagen"); break;
            case "WAU": info.put("manufacturer", "Audi"); break;
            case "WBA": info.put("manufacturer", "BMW"); break;
            case "WBS": info.put("manufacturer", "BMW M"); break;
            case "WDD": info.put("manufacturer", "Mercedes-Benz"); break;
            case "YV1": info.put("manufacturer", "Volvo"); break;
            case "5YJ": info.put("manufacturer", "Tesla"); break;
            case "7SA": info.put("manufacturer", "Tesla"); break;
            default: info.put("manufacturer", "Unknown manufacturer");
        }

        // Vehicle descriptor section (positions 4-8)
        String vds = vin.substring(3, 8);
        info.put("descriptor_section", vds);

        // Model year (position 10)
        char yearCode = vin.charAt(9);
        String year = decodeModelYear(yearCode);
        info.put("model_year", year);

        // Assembly plant (position 11)
        char plantCode = vin.charAt(10);
        info.put("assembly_plant_code", String.valueOf(plantCode));

        // Production sequence (positions 12-17)
        String sequence = vin.substring(11);
        info.put("production_sequence", sequence);

        return info;
    }

    private String decodeModelYear(char yearCode) {
        // Simplified year decoding
        switch (yearCode) {
            // Years 1980-2000
            case 'A': return "1980 or 2010";
            case 'B': return "1981 or 2011";
            case 'C': return "1982 or 2012";
            case 'D': return "1983 or 2013";
            case 'E': return "1984 or 2014";
            case 'F': return "1985 or 2015";
            case 'G': return "1986 or 2016";
            case 'H': return "1987 or 2017";
            case 'J': return "1988 or 2018";
            case 'K': return "1989 or 2019";
            case 'L': return "1990 or 2020";
            case 'M': return "1991 or 2021";
            case 'N': return "1992 or 2022";
            case 'P': return "1993 or 2023";
            case 'R': return "1994 or 2024";
            case 'S': return "1995 or 2025";
            case 'T': return "1996";
            case 'V': return "1997";
            case 'W': return "1998";
            case 'X': return "1999";
            case 'Y': return "2000";
            // Years 2001-2009 use 1-9
            case '1': return "2001";
            case '2': return "2002";
            case '3': return "2003";
            case '4': return "2004";
            case '5': return "2005";
            case '6': return "2006";
            case '7': return "2007";
            case '8': return "2008";
            case '9': return "2009";
            default: return "Unknown";
        }
    }
}
