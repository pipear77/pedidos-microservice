package com.pragma.pedidos.usecase.order;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.usecase.order.exception.ActiveOrderExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderUseCase orderUseCase;

    @Mock
    private com.pragma.pedidos.model.order.gateways.UserGateway userGateway;

    @Test
    void createOrder_Success_ShouldReturnSavedOrder() {
        Order order = Order.builder().idCliente(10L).idRestaurante(1L).build();

        when(orderRepository.existsByIdClienteAndEstadoIn(eq(10L), anyList())).thenReturn(false);
        when(orderRepository.saveOrder(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderUseCase.createOrder(order);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        assertNotNull(result.getFecha());
        verify(orderRepository).saveOrder(any(Order.class));
    }

    @Test
    void createOrder_WhenActiveOrderExists_ShouldThrowException() {
        Order order = Order.builder().idCliente(10L).build();

        when(orderRepository.existsByIdClienteAndEstadoIn(eq(10L), anyList())).thenReturn(true);

        assertThrows(ActiveOrderExistsException.class, () -> orderUseCase.createOrder(order));

        verify(orderRepository, never()).saveOrder(any());
    }

    @Test
    void getOrdersByStatus_Success_ShouldReturnList() {
        when(userGateway.getRestaurantIdByEmployeeId(4L)).thenReturn(1L);
        when(orderRepository.findByIdRestauranteAndEstado(eq(1L), eq(OrderStatus.PENDIENTE), eq(0), eq(10)))
                .thenReturn(List.of(new Order()));

        List<Order> result = orderUseCase.getOrdersByStatus(4L, OrderStatus.PENDIENTE, 0, 10);

        assertFalse(result.isEmpty());
        verify(userGateway).getRestaurantIdByEmployeeId(4L);
        verify(orderRepository).findByIdRestauranteAndEstado(1L, OrderStatus.PENDIENTE, 0, 10);
    }

    @Test
    void getOrdersByStatus_NoRestaurant_ShouldThrowException() {
        when(userGateway.getRestaurantIdByEmployeeId(4L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                orderUseCase.getOrdersByStatus(4L, OrderStatus.PENDIENTE, 0, 10));
    }
}