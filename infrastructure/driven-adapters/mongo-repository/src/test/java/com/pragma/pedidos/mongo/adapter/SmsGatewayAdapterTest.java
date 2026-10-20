package com.pragma.pedidos.mongo.adapter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SmsGatewayAdapterTest {
    @Test
    void sendSms_ShouldPrintToConsole() {
        SmsGatewayAdapter adapter = new SmsGatewayAdapter();
        assertDoesNotThrow(() -> adapter.sendSms("+573000000000", "Tu pedido está listo"));
    }
}