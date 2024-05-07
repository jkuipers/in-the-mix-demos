package inthemix.errorhandling;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    private SimpleService service;

    public SimpleController(SimpleService service) {
        this.service = service;
    }

    @GetMapping("/timeout")
    String timeout() {
        return service.timeout();
    }

    @GetMapping("/unexpected")
    String unexpected() {
        throw new RuntimeException("unexpected error, hope we handle this...");
    }

    @PostMapping("/validate")
    void validate(@Valid @RequestBody Person person) {}

    record Person(@NotBlank String name) { }
}
