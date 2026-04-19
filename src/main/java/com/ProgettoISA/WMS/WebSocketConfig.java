package com.ProgettoISA.WMS;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. Definiamo i "canali" in uscita (dove il server invia messaggi)
        // /topic -> per messaggi broadcast a tutti (es. statistiche per l'Admin)
        // /queue -> per messaggi privati a una singola persona (es. task assegnato a Andrea)
        config.enableSimpleBroker("/topic", "/queue");

        // 2. Definiamo il prefisso per i messaggi in entrata (se il frontend vuole inviare qualcosa al server)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 3. Creiamo l'endpoint a cui il frontend Vue.js si connetterà inizialmente.
        // ".setAllowedOriginPatterns("*")" permette la connessione da localhost:5173 (Vite/Vue)
        // ".withSockJS()" è un "paracadute" nel caso i WebSockets non funzionino sul browser del cliente.
        registry.addEndpoint("/ws-wms")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}