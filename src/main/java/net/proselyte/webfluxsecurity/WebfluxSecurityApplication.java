package net.proselyte.webfluxsecurity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@SpringBootApplication
@EnableR2dbcAuditing
public class WebfluxSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxSecurityApplication.class, args);
	}

}
