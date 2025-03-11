package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author raoxin
 */
@Configuration
public class WebSocketConfiguration {
    /**
     * 该方法用于创建并返回一个 ServerEndpointExporter 实例。ServerEndpointExporter
     * 是 Spring WebSocket 的一个组件，负责注册和管理所有的 WebSocket 端点。
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
