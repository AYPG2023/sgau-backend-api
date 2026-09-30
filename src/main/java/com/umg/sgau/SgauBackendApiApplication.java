package com.umg.sgau;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class SgauBackendApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgauBackendApiApplication.class, args);
	}

}
