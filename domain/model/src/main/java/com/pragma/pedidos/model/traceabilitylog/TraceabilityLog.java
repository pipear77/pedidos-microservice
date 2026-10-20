package com.pragma.pedidos.model.traceabilitylog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TraceabilityLog {
    private String orderId;
    private Long clientId;
    private String clientEmail;
    private String previousState;
    private String newState;
    private Long employeeId;
    private String employeeEmail;
}