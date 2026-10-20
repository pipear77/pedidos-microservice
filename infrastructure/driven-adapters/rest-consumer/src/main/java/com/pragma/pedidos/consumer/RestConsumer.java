package com.pragma.pedidos.consumer;

import com.pragma.pedidos.model.traceabilitylog.TraceabilityLog;
import com.pragma.pedidos.model.traceabilitylog.gateways.TraceabilityLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@Service
public class RestConsumer implements TraceabilityLogRepository {

    private final RestClient restClient;

    // Utilizamos el método estático builder() de RestClient para no depender del contenedor de Spring
    public RestConsumer(@Value("${adapter.traceability.url}") String traceabilityUrl) {
        this.restClient = RestClient.builder().baseUrl(traceabilityUrl).build();
    }

    @Override
    public void saveTraceabilityLog(TraceabilityLog log) {
        // Ejecutamos la petición en un hilo paralelo (Fire-and-Forget)
        CompletableFuture.runAsync(() -> {
            try {
                restClient
                        .post()
                        .uri("/api/v1/traceability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(log)
                        .retrieve()
                        .toBodilessEntity(); // Enviamos sin esperar cuerpo de respuesta
            } catch (Exception e) {
                // No bloqueamos el hilo principal si trazabilidad falla
                System.err.println("Advertencia: No se pudo enviar el log de trazabilidad - " + e.getMessage());
            }
        });
    }
}