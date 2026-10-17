package com.pragma.pedidos.mongo.adapter;

import com.pragma.pedidos.model.order.gateways.SmsGateway;
import org.springframework.stereotype.Component;

@Component
public class SmsGatewayAdapter implements SmsGateway {
    @Override
    public void sendSms(String phone, String message) {
        System.out.println("\n========== SMS SIMULATOR ==========");
        System.out.println("Destinatario : " + phone);
        System.out.println("Mensaje      : " + message);
        System.out.println("===================================\n");
    }
}