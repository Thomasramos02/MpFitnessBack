package com.example.MpFitness;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MpFitnessApplication {

	public static void main(String[] args) {
		SpringApplication.run(MpFitnessApplication.class, args);
	}

}
