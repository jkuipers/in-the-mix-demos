package com.example.demo;

import io.micrometer.common.KeyValues;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;

import java.util.Objects;

@Configuration
public class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> commonTags(Environment env) {
        String serviceName = env.getProperty("spring.application.name", "unknown-service");
        String hostname = Objects.requireNonNullElse(System.getenv("HOSTNAME"), "localhost");
        return registry -> registry.config().commonTags(
                "service", serviceName,
                // this one gets mapped to a hostname by the DatadogMeterRegistry, ensuring metrics are unique across PODs
                "instance", hostname
        );
    }










    /**
     * Starting with Boot 3, the {@code client.name} tag is no longer included by default
     * in the {@code http.client.requests} metrics. Restore it by overriding
     * {@link DefaultClientRequestObservationConvention#getLowCardinalityKeyValues(ClientRequestObservationContext)}.
     *
     * @return {@link ClientRequestObservationConvention} that adds the {@code client.name} to the low cardinality key-values.
     */
//    @Bean
    ClientRequestObservationConvention clientNameAddingObservationConvention() {
        return new DefaultClientRequestObservationConvention() {
            @Override
            public KeyValues getLowCardinalityKeyValues(ClientRequestObservationContext context) {
                return super.getLowCardinalityKeyValues(context).and(this.clientName(context));
            }
        };
    }







    /**
     * Ensures that <code>uri</code> tags of HTTP-related metrics do not include request parameters,
     * to reduce cardinality. The {@code @Order} ensures this filter runs before the auto-configured
     * ones that check if the maximum cardinality for the {@code uri} tag has been reached.
     *
     * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.metrics.supported.spring-mvc">MVC metrics</a>
     * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#actuator.metrics.supported.http-clients">RestTemplate metrics</a>
     */
//    @Bean @Order(-10)
    MeterFilter queryParameterStrippingMeterFilter() {
        return MeterFilter.replaceTagValues("uri", url -> {
            int i = url.indexOf('?');
            return i == -1 ? url : url.substring(0, i);
        });
    }

}
