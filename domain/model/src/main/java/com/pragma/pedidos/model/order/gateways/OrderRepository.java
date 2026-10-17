package com.pragma.pedidos.model.order.gateways;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;

import java.util.List;

public interface OrderRepository {
    Order saveOrder(Order order);

    // Método para validar que el cliente no tenga un pedido en proceso (HU11)
    boolean existsByIdClienteAndEstadoIn(Long idCliente, List<OrderStatus> estados);

    // NUEVO MÉTODO PARA HU12: Obtener pedidos por restaurante y estado paginados
    List<Order> findByIdRestauranteAndEstado(Long idRestaurante, OrderStatus estado, int page, int size);
}