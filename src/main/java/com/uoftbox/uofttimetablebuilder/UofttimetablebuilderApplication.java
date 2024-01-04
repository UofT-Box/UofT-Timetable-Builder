package com.uoftbox.uofttimetablebuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UofttimetablebuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(UofttimetablebuilderApplication.class, args);
	}

}
