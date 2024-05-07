package com.example.demo;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@RestController
public class SalesController {

    private RestTemplate restTemplate;
    private MeterRegistry meterRegistry;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SalesController(RestTemplateBuilder builder, MeterRegistry meterRegistry) {
        this.restTemplate = builder.build();
        this.meterRegistry = meterRegistry;
    }

    enum Category { INSTRUMENT, SPEAKER, LIGHTING }
    record Order(String ean, Category category, int quantity, BigDecimal price) {}

    @PostMapping("/order")
    void order(@RequestBody Order order) {
        placeOrder(order);
        registerMetric(order);
    }

    private void registerMetric(Order order) {
        DistributionSummary.builder("orders")
                .baseUnit("euros")
                .tag("category", order.category.name())
                // other tags here...
                .register(meterRegistry)
                .record(order.price().doubleValue());
    }

    private void placeOrder(Order order) {
        logger.info("Placing the order");
        // in this particular case you *should* use URI template vars, but that's not always an option
        restTemplate.postForObject("http://httpbin/post?ean=" + order.ean(), order, String.class);
    }
}
