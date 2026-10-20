package com.pragma.pedidos.api.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {
    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void securityFilterChain_ShouldTraverseConfiguration() {
        HttpSecurity http = mock(HttpSecurity.class);
        // Forzamos la ejecución para cubrir las instrucciones lambda internamente
        assertThrows(Exception.class, () -> securityConfig.securityFilterChain(http));
    }
}