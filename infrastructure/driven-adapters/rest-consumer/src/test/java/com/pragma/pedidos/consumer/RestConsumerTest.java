package com.pragma.pedidos.consumer;

import com.pragma.pedidos.model.traceabilitylog.TraceabilityLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class RestConsumerTest {

    private RestConsumer restConsumer;

    @BeforeEach
    void setUp() {
        // Instanciamos el consumidor directamente con una URL de prueba
        restConsumer = new RestConsumer("http://localhost:8083");
    }

    @Test
    void saveTraceabilityLog_Success_ShouldExecuteAsyncWithoutExceptions() throws InterruptedException {
        TraceabilityLog log = TraceabilityLog.builder()
                .orderId("123")
                .clientId(7L)
                .build();

        // Validamos que el método se dispare de forma asíncrona sin bloquear ni lanzar excepciones
        assertDoesNotThrow(() -> restConsumer.saveTraceabilityLog(log));

        // Pausa de 200ms para permitir que el hilo asíncrono ejecute su bloque try-catch interno
        Thread.sleep(200);
    }
}