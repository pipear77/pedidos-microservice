package com.pragma.pedidos.usecase.order.exception;

public class ActiveOrderExistsException extends RuntimeException {
    public ActiveOrderExistsException(String message) {
        super(message);
    }
}