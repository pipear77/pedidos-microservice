package com.pragma.pedidos.usecase.order;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.model.order.gateways.SmsGateway;
import com.pragma.pedidos.model.order.gateways.UserGateway;
import com.pragma.pedidos.usecase.order.exception.ActiveOrderExistsException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class OrderUseCase {

    private final OrderRepository orderRepository;
    private final UserGateway userGateway;
    private final SmsGateway smsGateway;

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

    public Order assignOrder(String idOrder, Long idEmpleado) {
        // 1. Buscar el pedido
        Order order = orderRepository.findById(idOrder);
        if (order == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }

        // 2. Validar que el pedido esté PENDIENTE
        if (order.getEstado() != OrderStatus.PENDIENTE) {
            throw new IllegalArgumentException("El pedido no se encuentra en estado PENDIENTE.");
        }

        // 3. Validar que el empleado pertenece al mismo restaurante que el pedido
        Long idRestauranteEmpleado = userGateway.getRestaurantIdByEmployeeId(idEmpleado);
        if (!order.getIdRestaurante().equals(idRestauranteEmpleado)) {
            throw new IllegalArgumentException("El empleado no tiene permisos para modificar pedidos de este restaurante.");
        }

        // 4. Asignar empleado (idChef) y cambiar estado (Criterio 5 y HU13)
        Order updatedOrder = order.toBuilder()
                .idChef(idEmpleado)
                .estado(OrderStatus.EN_PREPARACION)
                .build();

        // 5. Guardar los cambios
        return orderRepository.saveOrder(updatedOrder);
    }

    // NUEVO MÉTODO HU14
    public Order markOrderAsReady(String idOrder, Long idEmpleado) {
        Order order = orderRepository.findById(idOrder);
        if (order == null) throw new IllegalArgumentException("El pedido no existe.");

        if (order.getEstado() != OrderStatus.EN_PREPARACION) {
            throw new IllegalArgumentException("El pedido debe estar en preparación para marcarse como listo.");
        }
        if (!order.getIdChef().equals(idEmpleado)) {
            throw new IllegalArgumentException("No puedes modificar un pedido que no estás preparando.");
        }

        // Generar PIN de 6 dígitos
        String pin = String.format("%06d", new Random().nextInt(999999));

        Order updatedOrder = order.toBuilder()
                .estado(OrderStatus.LISTO)
                .pinSeguridad(pin)
                .build();

        Order savedOrder = orderRepository.saveOrder(updatedOrder);

        // Notificar al cliente
        String phone = userGateway.getPhoneByUserId(savedOrder.getIdCliente());
        if (phone != null && !phone.isBlank()) {
            smsGateway.sendSms(phone, "Tu pedido está listo para ser reclamado. Usa este PIN de seguridad: " + pin);
        }

        return savedOrder;
    }

    // NUEVO MÉTODO HU15
    public Order deliverOrder(String idOrder, String pinIngresado, Long idEmpleado) {
        Order order = orderRepository.findById(idOrder);
        if (order == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }

        // Criterio 1 y 2: Solo pedidos LISTO pueden ser entregados
        if (order.getEstado() != OrderStatus.LISTO) {
            throw new IllegalArgumentException("El pedido solo puede entregarse si está en estado LISTO.");
        }

        // Validación de seguridad (opcional pero recomendada): Solo el empleado asignado lo entrega
        if (!order.getIdChef().equals(idEmpleado)) {
            throw new IllegalArgumentException("No tienes permisos para entregar este pedido.");
        }

        // Criterio 3: Validar el PIN de seguridad (Null-Safe)
        if (pinIngresado == null || !pinIngresado.equals(order.getPinSeguridad())) {
            throw new IllegalArgumentException("El PIN de seguridad es incorrecto o no ha sido generado.");
        }

        // Cambiar a estado final
        Order updatedOrder = order.toBuilder()
                .estado(OrderStatus.ENTREGADO)
                .build();

        return orderRepository.saveOrder(updatedOrder);
    }

    // NUEVO MÉTODO HU16
    public Order cancelOrder(String idOrder, Long idClienteLogueado) {
        Order order = orderRepository.findById(idOrder);
        if (order == null) {
            throw new IllegalArgumentException("El pedido no existe.");
        }

        // Seguridad: Un cliente no puede cancelar el pedido de otro cliente
        if (!order.getIdCliente().equals(idClienteLogueado)) {
            throw new IllegalArgumentException("No tienes permisos para cancelar este pedido.");
        }

        // Criterio 1 y 2: Solo si está PENDIENTE. Si no, lanzar mensaje exacto.
        if (order.getEstado() != OrderStatus.PENDIENTE) {
            throw new IllegalStateException("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
        }

        Order updatedOrder = order.toBuilder()
                .estado(OrderStatus.CANCELADO)
                .build();

        return orderRepository.saveOrder(updatedOrder);
    }
}