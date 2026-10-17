package com.pragma.pedidos.usecase.order;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.model.order.gateways.UserGateway;
import com.pragma.pedidos.usecase.order.exception.ActiveOrderExistsException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderRepository orderRepository;
    private final UserGateway userGateway;

    public Order createOrder(Order order) {
        // 1. Definimos los estados que consideramos "en proceso"
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDIENTE,
                OrderStatus.EN_PREPARACION,
                OrderStatus.LISTO
        );

        // 2. Validamos la regla de negocio fundamental de la HU11
        boolean hasActiveOrder = orderRepository.existsByIdClienteAndEstadoIn(order.getIdCliente(), activeStatuses);

        if (hasActiveOrder) {
            throw new ActiveOrderExistsException("El cliente ya tiene un pedido en proceso. No puede crear uno nuevo.");
        }

        // 3. Si pasa la validación, armamos el objeto final a guardar
        Order newOrder = order.toBuilder()
                .estado(OrderStatus.PENDIENTE)
                .fecha(LocalDateTime.now())    // Registramos la fecha de creación
                .build();

        // 4. Guardamos en la base de datos
        return orderRepository.saveOrder(newOrder);
    }

    // NUEVO MÉTODO HU12
    public List<Order> getOrdersByStatus(Long idEmpleado, OrderStatus estado, int page, int size) {
        // 1. Obtenemos el restaurante al que pertenece el empleado logueado (Criterio 4)
        Long idRestaurante = userGateway.getRestaurantIdByEmployeeId(idEmpleado);
        if (idRestaurante == null) {
            throw new IllegalArgumentException("El empleado no pertenece a ningún restaurante.");
        }

        // 2. Retornamos la lista paginada y filtrada por estado (Criterios 1, 2 y 3)
        return orderRepository.findByIdRestauranteAndEstado(idRestaurante, estado, page, size);
    }
}