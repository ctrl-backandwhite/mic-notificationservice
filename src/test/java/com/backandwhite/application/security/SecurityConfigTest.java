package com.backandwhite.application.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void corsConfigurationSource_containsAllowedHeaders() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest());

        assertThat(config.getAllowedHeaders())
                .contains("Authorization", "Content-Type", "Accept")
                .doesNotContain("*");
    }

    @Test
    void corsConfigurationSource_allowsExpectedMethods() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest());

        assertThat(config.getAllowedMethods())
                .contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }

    @Test
    void corsConfigurationSource_allowsCredentials() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest());

        assertThat(config.getAllowCredentials()).isTrue();
    }
}
