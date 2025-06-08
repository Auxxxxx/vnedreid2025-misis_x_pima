package com.example.predictionservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MLApiConfig {

    @Value("${ml.api.host}")
    private String mlApiHost;

    @Value("${ml.api.port}")
    private int mlApiPort;

    @Value("${ml.api.endpoint}")
    private String mlApiEndpoint;

    @Value("${ml.api.timeout}")
    private int mlApiTimeout;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(mlApiTimeout);
        factory.setReadTimeout(mlApiTimeout);
        return new RestTemplate(factory);
    }

    public String getMlApiUrl() {
        return String.format("http://%s:%d%s", mlApiHost, mlApiPort, mlApiEndpoint);
    }

    // Getters для использования в сервисах
    public String getMlApiHost() { return mlApiHost; }
    public int getMlApiPort() { return mlApiPort; }
    public String getMlApiEndpoint() { return mlApiEndpoint; }
    public int getMlApiTimeout() { return mlApiTimeout; }
} 