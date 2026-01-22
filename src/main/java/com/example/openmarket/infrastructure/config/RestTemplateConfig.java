package com.example.openmarket.infrastructure.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Value("${http.client.type:simple}")
    private String clientType;

    @Value("${http.client.connect-timeout:2000}")
    private int connectTimeout;

    @Value("${http.client.read-timeout:10000}")
    private int readTimeout;

    @Value("${http.client.pool.max-total:50}")
    private int poolMaxTotal;

    @Value("${http.client.pool.max-per-route:20}")
    private int poolMaxPerRoute;

    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestFactory factory = "pooled".equalsIgnoreCase(clientType)
            ? createPooledFactory()
            : createSimpleFactory();

        log.info("RestTemplate configured with {} client (connect={}ms, read={}ms)",
            clientType, connectTimeout, readTimeout);

        return new RestTemplate(factory);
    }

    private ClientHttpRequestFactory createSimpleFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

    private ClientHttpRequestFactory createPooledFactory() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setSocketTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(poolMaxTotal);
        connectionManager.setDefaultMaxPerRoute(poolMaxPerRoute);
        connectionManager.setDefaultConnectionConfig(connectionConfig);
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(5));

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(500))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .evictIdleConnections(TimeValue.ofMinutes(5))
            .build();

        log.info("Connection pool configured: maxTotal={}, maxPerRoute={}", poolMaxTotal, poolMaxPerRoute);

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
