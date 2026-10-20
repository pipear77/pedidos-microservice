package com.pragma.pedidos.model.order.gateways;

public interface UserGateway {
    Long getRestaurantIdByEmployeeId(Long idEmpleado);
    String getPhoneByUserId(Long idUser);
    String getEmailByUserId(Long userId);
}