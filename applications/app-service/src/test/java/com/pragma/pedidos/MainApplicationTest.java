package com.pragma.pedidos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainApplicationTest {
    @Test
    void main_ShouldStartWithoutExceptions() {
        assertDoesNotThrow(() -> MainApplication.main(new String[]{}));
    }
}