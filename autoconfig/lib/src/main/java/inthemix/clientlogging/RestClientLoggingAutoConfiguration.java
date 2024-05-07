package inthemix.clientlogging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

@AutoConfiguration(before = RestTemplateAutoConfiguration.class)
@EnableConfigurationProperties(RestClientLoggingProperties.class)
public class RestClientLoggingAutoConfiguration {

    @Bean @ConditionalOnProperty(name = "client.logging.enabled", matchIfMissing = true)
    RestTemplateCustomizer loggingRestTemplateCustomizer(RestClientLoggingProperties properties) {
        return restTemplate -> {
            ClientHttpRequestFactory requestFactory = extractRequestFactory(restTemplate);
            if (!(requestFactory instanceof BufferingClientHttpRequestFactory)) {
                restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
            }
            restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor(properties.getExcludeHeaders()));
        };
    }

    /**
     * Extracts the request factory from the given template,
     * unwrapping an {@link InterceptingClientHttpRequestFactory} if present.
     */
    private ClientHttpRequestFactory extractRequestFactory(RestTemplate restTemplate) {
        ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (requestFactory instanceof InterceptingClientHttpRequestFactory) {
            Field requestFactoryField = ReflectionUtils.findField(RestTemplate.class, "requestFactory");
            ReflectionUtils.makeAccessible(requestFactoryField);
            requestFactory = (ClientHttpRequestFactory) ReflectionUtils.getField(requestFactoryField, restTemplate);
        }
        return requestFactory;
    }

}
