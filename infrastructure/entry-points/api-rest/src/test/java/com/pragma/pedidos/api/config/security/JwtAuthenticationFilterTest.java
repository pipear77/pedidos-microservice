package com.pragma.pedidos.api.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secret", "clave-falsa-para-tests-unitarios-1234567890123456");
    }

    @Test
    void doFilterInternal_NoHeader_ShouldContinueChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer tokenInvalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        assertEquals(401, response.getStatus());
    }

    @Test
    void doFilterInternal_ValidToken_ShouldSetAuthentication() throws Exception {
        String secretKeyString = "una-clave-secreta-muy-larga-y-segura-para-firmar-el-token-123456";
        org.springframework.test.util.ReflectionTestUtils.setField(filter, "secret", secretKeyString);

        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(secretKeyString.getBytes());
        String token = io.jsonwebtoken.Jwts.builder()
                .subject("user@mail.com")
                .claim("role", "ROLE_CLIENTE")
                .claim("id", 10L)
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}