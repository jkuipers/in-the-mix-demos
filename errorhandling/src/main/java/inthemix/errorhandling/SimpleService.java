package inthemix.errorhandling;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SimpleService {

    private RestClient restClient;

    public SimpleService(RestClient.Builder builder) {
        var httpRequestFactory = new JdkClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout(1_000); // 1 second
        this.restClient = builder.requestFactory(httpRequestFactory).build();
    }

    public String timeout() {
        return restClient.get().uri("http://httpbin/delay/2").retrieve().body(String.class);
    }

}
