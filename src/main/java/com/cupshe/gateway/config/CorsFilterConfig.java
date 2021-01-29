package com.cupshe.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * CorsFilterConfig
 *
 * @author zxy
 */
@Configuration
public class CorsFilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource cs = new UrlBasedCorsConfigurationSource();
        cs.registerCorsConfiguration("/**", getCorsConfig());
        return new CorsWebFilter(cs);
    }

    private CorsConfiguration getCorsConfig() {
        CorsConfiguration result = new CorsConfiguration();
        result.setAllowCredentials(true);
        result.addAllowedOrigin("*");
        result.addAllowedMethod("*");
        result.addAllowedHeader("*");
        result.setMaxAge(3600L);
        return result;
    }
}
