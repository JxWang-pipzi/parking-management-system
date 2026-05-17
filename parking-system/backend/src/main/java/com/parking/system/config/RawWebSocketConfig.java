package com.parking.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class RawWebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new SafeServerEndpointExporter();
    }

    private static class SafeServerEndpointExporter extends ServerEndpointExporter {
        @Override
        public void afterPropertiesSet() {
            try {
                super.afterPropertiesSet();
            } catch (IllegalStateException ex) {
                if (!isMissingServerContainer(ex)) {
                    throw ex;
                }
            }
        }

        @Override
        public void afterSingletonsInstantiated() {
            try {
                super.afterSingletonsInstantiated();
            } catch (IllegalStateException ex) {
                if (!isMissingServerContainer(ex)) {
                    throw ex;
                }
            }
        }

        private boolean isMissingServerContainer(IllegalStateException ex) {
            String message = ex.getMessage();
            return message != null && (message.contains("ServerContainer not available")
                    || message.contains("No ServerContainer set"));
        }
    }
}
