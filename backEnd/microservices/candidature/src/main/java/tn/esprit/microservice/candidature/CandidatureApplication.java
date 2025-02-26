package tn.esprit.microservice.candidature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CandidatureApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidatureApplication.class, args);
	}

}
