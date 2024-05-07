package inthemix.masking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MaskingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaskingApplication.class, args);
	}

}
