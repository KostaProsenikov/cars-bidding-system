package app.svc_vin_check.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class IndexController {

    @PostMapping ("/get-vin")
    public String getVinInfo() {
        return "Hello World!";
    }
}
