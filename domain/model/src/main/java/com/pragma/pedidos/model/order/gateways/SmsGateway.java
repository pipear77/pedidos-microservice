package com.pragma.pedidos.model.order.gateways;

public interface SmsGateway {
    void sendSms(String phone, String message);
}