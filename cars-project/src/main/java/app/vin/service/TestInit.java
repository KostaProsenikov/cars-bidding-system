package app.vin.service;

import app.vin.client.VinClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestInit  implements ApplicationRunner {

    private final VinClient vinClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("TestInit");
        String vinValue = "234";
        ResponseEntity<String> response = vinClient.getVINInformation(vinValue);
        System.out.println(response.getBody());
    }

//    private HttpHeaders getHeaders(final HttpServletRequest httpServletRequest) {
//        final HttpHeaders headers = new HttpHeaders();
//        headers.add("authorization", httpServletRequest.getHeader("authorization"));
//        return headers;
//    }

    @Autowired
    public TestInit(VinClient vinClient) {
        this.vinClient = vinClient;
    }
}
