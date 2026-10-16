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
}