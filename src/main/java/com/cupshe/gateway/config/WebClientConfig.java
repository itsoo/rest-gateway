package com.cupshe.gateway.config;

import com.cupshe.gateway.config.properties.RestGatewayProperties;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClientConfig
 *
 * @author zxy
 */
@Data
@Configuration
@EnableConfigurationProperties(RestGatewayProperties.class)
public class WebClientConfig {

    private static final String PROVIDER_THREAD_NAME = "webclient-cp";

    private static final String EVENT_LOOP_THREAD_NAME = "webclient-el";

    private final RestGatewayProperties properties;

    public WebClientConfig(RestGatewayProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create(getConnectionProvider())
                .compress(properties.isCompress())
                .tcpConfiguration(tcpClient -> tcpClient
                        .runOn(getLoopResources(), false)
                        .doOnConnected(this::setConnectionTimeout)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeout())
                        .option(ChannelOption.TCP_NODELAY, properties.isTcpNodelay())
                        .option(ChannelOption.SO_KEEPALIVE, properties.isSoKeepAlive())
                        .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT));
        return WebClient.builder()
                .exchangeStrategies(getExchangeStrategies())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private ConnectionProvider getConnectionProvider() {
        return ConnectionProvider.fixed(
                PROVIDER_THREAD_NAME,
                properties.getMaxConnections(),
                properties.getAcquireTimeout(),
                Duration.ofMillis(properties.getMaxIdleTime()));
    }

    private LoopResources getLoopResources() {
        LoopResources result = LoopResources.create(
                EVENT_LOOP_THREAD_NAME,
                Runtime.getRuntime().availableProcessors(),
                true);
        result.onServer(false);
        return result;
    }

    private void setConnectionTimeout(Connection connection) {
        connection
                .addHandlerLast(new ReadTimeoutHandler(properties.getReadTimeout(), TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeout(), TimeUnit.MILLISECONDS));
    }

    private ExchangeStrategies getExchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(-1))
                .build();
    }
}
