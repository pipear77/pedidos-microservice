package com.pragma.pedidos.model.traceabilitylog.gateways;

import com.pragma.pedidos.model.traceabilitylog.TraceabilityLog;

public interface TraceabilityLogRepository {
    void saveTraceabilityLog(TraceabilityLog log);
}