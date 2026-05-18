package com.example.MpFitness;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"security.jwt.secret=0123456789abcdef0123456789abcdef",
		"security.jwt.expiration-ms=36000000",
		"mercado_pago.access_token=test-access-token",
		"frontend.url=http://localhost:3000",
		"ADMIN_INITIAL_PASSWORD=admin-test-password",
		"spring.security.oauth2.client.registration.google.client-id=test-google-client-id",
		"spring.security.oauth2.client.registration.google.client-secret=test-google-client-secret",
		"spring.datasource.url=jdbc:h2:mem:mpfitness-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class MpFitnessApplicationTests {

	@Test
	void contextLoads() {
	}

}
