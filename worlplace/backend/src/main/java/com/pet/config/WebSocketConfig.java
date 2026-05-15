package com.pet.config;

import com.pet.websocket.MyHandshakeInterceptor;
import com.pet.websocket.NotificationWebSocketHandler;
import com.pet.websocket.OrderWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderWebSocketHandler orderWebSocketHandler;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final MyHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketHandler, "/ws/order/{userId}")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(notificationWebSocketHandler, "/ws/notification/{userId}")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
