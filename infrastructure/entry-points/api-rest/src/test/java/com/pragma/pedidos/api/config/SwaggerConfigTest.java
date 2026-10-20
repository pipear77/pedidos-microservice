package com.pragma.pedidos.api.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {
    @Test
    void swaggerConfigInstantiates() {
        SwaggerConfig config = new SwaggerConfig();
        assertNotNull(config);
    }
}