package app.vin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "vin-service", url = "http://localhost:8082/vin-svc/api/v1")
public class VinClient {

    @PostMapping("get-vins")
    public String getVINInformation() {
        return "VINs";
    }
}
