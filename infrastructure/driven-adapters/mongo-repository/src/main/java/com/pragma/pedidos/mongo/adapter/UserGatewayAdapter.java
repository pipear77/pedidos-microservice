package com.pragma.pedidos.mongo.adapter;

import com.pragma.pedidos.model.order.gateways.UserGateway;
import com.pragma.pedidos.mongo.client.UserResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserGatewayAdapter implements UserGateway {

    private final RestClient restClient;

    public UserGatewayAdapter() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081/api/v1/users")
                .build();
    }

    @Override
    public Long getRestaurantIdByEmployeeId(Long idEmpleado) {
        try {
            UserResponseDto user = restClient.get()
                    .uri("/{id}", idEmpleado)
                    .retrieve()
                    .body(UserResponseDto.class);

            // Retornamos el idRestaurant asociado al empleado
            return user != null ? user.idRestaurant() : null;
        } catch (Exception e) {
            // Si el usuario no existe o hay un fallo de comunicación
            throw new IllegalArgumentException("No se pudo validar el restaurante del empleado en el servicio de usuarios.");
        }
    }

    @Override
    public String getPhoneByUserId(Long idUser) {
        try {
            UserResponseDto user = restClient.get().uri("/{id}", idUser).retrieve().body(UserResponseDto.class);
            return user != null ? user.phone() : null;
        } catch (Exception e) {
            return null;
        }
    }
}