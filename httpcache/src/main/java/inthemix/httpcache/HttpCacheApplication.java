package inthemix.httpcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@SpringBootApplication
public class HttpCacheApplication {

	public static void main(String[] args) {
		SpringApplication.run(HttpCacheApplication.class, args);
	}

	@Bean
	FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagHeaderFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean(new ShallowEtagHeaderFilter());
		registration.addUrlPatterns("/shallow/*");
		return registration;
	}
}
