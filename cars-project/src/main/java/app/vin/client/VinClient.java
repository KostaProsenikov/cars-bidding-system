package app.vin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "vin-service", url = "http://localhost:8082/vin-svc/api/v1")
public interface VinClient {

    @GetMapping ("get-vin")
    ResponseEntity<String> getVINInformation(@RequestParam String vin);
}
