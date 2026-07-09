package quizmaster.quiz.config.websocket;

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
        // /topic = broadcast (ex: sala, jogo)
        // /queue = mensagens por utilizador (ex: pergunta específica do jogador)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket nativo (para Flutter Web / stomp_dart_client)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        // Endpoint com SockJS como fallback (para browsers que não suportam WS nativo)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}