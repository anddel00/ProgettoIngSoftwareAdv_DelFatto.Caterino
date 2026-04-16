package com.ProgettoISA.WMS.Integration;// Adattalo se lo metti in una sottocartella
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 1. Avvia Spring su una porta casuale per non litigare con altri server
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// 2. Forza Spring a leggere il file application-test.properties
@ActiveProfiles("test")
public class LoginIT {

    // Questo è il nostro "finto browser" o "finto Axios" che fa le chiamate
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testLoginConCredenzialiCorrette() {
        System.out.println("🚀 Inizio test: Login con credenziali corrette...");

        // 3. Prepariamo il corpo della richiesta (l'equivalente del JSON che invia Vue)
        Map<String, String> credenziali = Map.of(
                "email", "admin@wms.it",
                "password", "admin123"
        );

        // 4. Facciamo la chiamata POST al controller (Assicurati che l'URL sia quello giusto del tuo controller!)
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", // Cambialo in /login o /api/utenti/login se il tuo controller è mappato diversamente
                credenziali,
                String.class
        );

        // 5. Verifichiamo il risultato: ci aspettiamo un 200 OK
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Il login dovrebbe restituire 200 OK");

        System.out.println("✅ Login riuscito! Risposta del server: " + response.getBody());
    }

    @Test
    void testLoginConCredenzialiErrate() {
        System.out.println("🚀 Inizio test: Login con password errata...");

        // 1. Prepariamo il corpo della richiesta con una password palesemente sbagliata
        Map<String, String> credenzialiErrate = Map.of(
                "email", "admin@wms.it",
                "password", "passwordSbagliata123"
        );

        // 2. Facciamo la chiamata POST al controller
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login",
                credenzialiErrate,
                String.class
        );

        // 3. Verifichiamo il risultato: ci aspettiamo che il server blocchi l'accesso.
        // Solitamente Spring Boot e le API REST restituiscono 401 (UNAUTHORIZED) o 403 (FORBIDDEN)
        // a seconda di come hai configurato la gestione degli errori nel tuo Controller.
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode(), "Il login con password errata deve restituire 401 Unauthorized");

        // 4. (Opzionale) Verifichiamo che la risposta non contenga i dati sensibili dell'utente
        System.out.println("✅ Accesso negato correttamente! Risposta del server (Status Code): " + response.getStatusCode());
    }
}