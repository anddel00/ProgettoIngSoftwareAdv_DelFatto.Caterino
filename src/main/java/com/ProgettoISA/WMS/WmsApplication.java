package com.ProgettoISA.WMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class WmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WmsApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public org.springframework.boot.CommandLineRunner schemaFixer(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE \"TASK\" DROP CONSTRAINT IF EXISTS \"Id.ScaffaleFine\"");
				jdbcTemplate.execute("ALTER TABLE \"TASK\" DROP CONSTRAINT IF EXISTS \"Id.ScaffaleInizio\"");
				System.out.println("[DB FIX] Vecchie constraint FK_Scaffali rimosse con successo da TASK.");
			} catch (Exception e) {
				System.out.println("[DB FIX] Impossibile rimuovere le vecchie constraint (forse già rimosse). Errore: " + e.getMessage());
			}
		};
	}

}
