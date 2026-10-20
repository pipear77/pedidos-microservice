package com.pragma.pedidos.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCasesConfigTest {
    @Test
    void configInstantiates() {
        UseCasesConfig config = new UseCasesConfig();
        assertNotNull(config);
    }
}