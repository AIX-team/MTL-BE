package com.example.mytravellink.config;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.valves.RemoteIpValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedTomcatConfig {

    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers((Connector connector) -> {
            connector.setAsyncTimeout(300000); // 5분
            connector.setConnectionTimeout(300000); // 5분
        });
        tomcat.addContextValves(remoteIpValve());
        return tomcat;
    }

    private RemoteIpValve remoteIpValve() {
        RemoteIpValve valve = new RemoteIpValve();
        valve.setProtocolHeader("X-Forwarded-Proto");
        valve.setRemoteIpHeader("X-Forwarded-For");
        return valve;
    }
} 