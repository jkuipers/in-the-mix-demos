package inthemix.autoconfig;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
public class HttpBinController {
    private RestTemplate restTemplate;

    public HttpBinController(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("/")
    Map<String, Object> callHttpBin() {
        return restTemplate.getForObject("http://httpbin/json", Map.class);
    }
}
