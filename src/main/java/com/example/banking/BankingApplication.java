package com.example.banking;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BankingApplication {

	@Value("${banking.openapi.info.title}")
	private String title;

	@Value("${banking.openapi.info.description}")
	private String description;

	@Value("${banking.openapi.contact.url}")
	private String url;

	public static void main(String[] args) {
		SpringApplication.run(BankingApplication.class, args);
	}

	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
				.title(title)
				.description(description)
				.contact(new Contact().url(url));

		return new OpenAPI().info(info);
	}

	@Bean
	public ObjectMapper objectMapper() {
		var mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		return mapper;
	}

}
