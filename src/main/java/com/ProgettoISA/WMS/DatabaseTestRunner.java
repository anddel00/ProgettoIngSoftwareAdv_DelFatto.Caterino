package com.ProgettoISA.WMS;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTestRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        System.out.println("⏳ Test connessione al database AWS RDS in corso...");

        try {
            // Eseguiamo una query nativa di PostgreSQL per farci dire la versione
            String result = jdbcTemplate.queryForObject("SELECT version();", String.class);

            System.out.println("✅ CONNESSIONE STABILITA CON SUCCESSO!");
            System.out.println("🐘 Versione Database: " + result);

        } catch (Exception e) {
            System.err.println("❌ ERRORE DI CONNESSIONE: " + e.getMessage());
        }
    }
}