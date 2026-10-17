package com.pragma.pedidos.usecase.order;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.model.order.gateways.SmsGateway;
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

    @Mock private SmsGateway smsGateway;

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

    @Test
    void assignOrder_Success_ShouldReturnUpdatedOrder() {
        String orderId = "123";
        Long employeeId = 5L;
        Long restaurantId = 1L;
        com.pragma.pedidos.model.order.Order existingOrder = com.pragma.pedidos.model.order.Order.builder()
                .id(orderId)
                .estado(com.pragma.pedidos.model.order.enums.OrderStatus.PENDIENTE)
                .idRestaurante(restaurantId)
                .build();

        com.pragma.pedidos.model.order.Order updatedOrder = existingOrder.toBuilder()
                .idChef(employeeId)
                .estado(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(existingOrder);
        when(userGateway.getRestaurantIdByEmployeeId(employeeId)).thenReturn(restaurantId);
        when(orderRepository.saveOrder(any(com.pragma.pedidos.model.order.Order.class))).thenReturn(updatedOrder);

        com.pragma.pedidos.model.order.Order result = orderUseCase.assignOrder(orderId, employeeId);

        assertNotNull(result);
        assertEquals(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION, result.getEstado());
        assertEquals(employeeId, result.getIdChef());
    }

    @Test
    void assignOrder_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById("invalid")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("invalid", 5L));
    }

    @Test
    void assignOrder_OrderNotPending_ShouldThrowException() {
        com.pragma.pedidos.model.order.Order existingOrder = com.pragma.pedidos.model.order.Order.builder()
                .id("123").estado(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION).build();
        when(orderRepository.findById("123")).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("123", 5L));
    }

    @Test
    void assignOrder_EmployeeNotFromRestaurant_ShouldThrowException() {
        com.pragma.pedidos.model.order.Order existingOrder = com.pragma.pedidos.model.order.Order.builder()
                .id("123").estado(com.pragma.pedidos.model.order.enums.OrderStatus.PENDIENTE).idRestaurante(1L).build();

        when(orderRepository.findById("123")).thenReturn(existingOrder);
        when(userGateway.getRestaurantIdByEmployeeId(5L)).thenReturn(2L); // Restaurante distinto

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("123", 5L));
    }

    @Test
    void markOrderAsReady_Success() {
        com.pragma.pedidos.model.order.Order order = com.pragma.pedidos.model.order.Order.builder()
                .id("123").estado(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION).idChef(5L).idCliente(10L).build();

        when(orderRepository.findById("123")).thenReturn(order);
        when(orderRepository.saveOrder(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userGateway.getPhoneByUserId(10L)).thenReturn("+573000000000");

        com.pragma.pedidos.model.order.Order result = orderUseCase.markOrderAsReady("123", 5L);

        assertEquals(com.pragma.pedidos.model.order.enums.OrderStatus.LISTO, result.getEstado());
        assertNotNull(result.getPinSeguridad());
        verify(smsGateway).sendSms(eq("+573000000000"), anyString());
    }

    @Test
    void markOrderAsReady_WrongStateOrChef_ShouldThrow() {
        com.pragma.pedidos.model.order.Order orderWrongState = com.pragma.pedidos.model.order.Order.builder()
                .id("1").estado(com.pragma.pedidos.model.order.enums.OrderStatus.PENDIENTE).idChef(5L).build();
        when(orderRepository.findById("1")).thenReturn(orderWrongState);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.markOrderAsReady("1", 5L));

        com.pragma.pedidos.model.order.Order orderWrongChef = com.pragma.pedidos.model.order.Order.builder()
                .id("2").estado(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION).idChef(99L).build();
        when(orderRepository.findById("2")).thenReturn(orderWrongChef);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.markOrderAsReady("2", 5L));
    }
}