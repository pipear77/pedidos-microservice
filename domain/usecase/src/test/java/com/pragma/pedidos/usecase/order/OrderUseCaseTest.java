package com.pragma.pedidos.usecase.order;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.model.order.gateways.SmsGateway;
import com.pragma.pedidos.model.order.gateways.UserGateway;
import com.pragma.pedidos.model.traceabilitylog.gateways.TraceabilityLogRepository;
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

    @Mock
    private UserGateway userGateway;

    @Mock
    private SmsGateway smsGateway;

    @Mock
    private TraceabilityLogRepository traceabilityLogRepository; // Nuevo mock para trazabilidad

    @InjectMocks
    private OrderUseCase orderUseCase;

    @Test
    void createOrder_Success_ShouldReturnSavedOrder() {
        Order order = Order.builder().idCliente(10L).idRestaurante(1L).build();
        Order savedOrder = order.toBuilder().id("123").estado(OrderStatus.PENDIENTE).build();

        when(orderRepository.existsByIdClienteAndEstadoIn(eq(10L), anyList())).thenReturn(false);
        when(orderRepository.saveOrder(any(Order.class))).thenReturn(savedOrder);

        Order result = orderUseCase.createOrder(order);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDIENTE, result.getEstado());
        verify(orderRepository).saveOrder(any(Order.class));
        verify(traceabilityLogRepository).saveTraceabilityLog(any()); // Verificamos envío de log
    }

    @Test
    void createOrder_WhenActiveOrderExists_ShouldThrowException() {
        Order order = Order.builder().idCliente(10L).build();

        when(orderRepository.existsByIdClienteAndEstadoIn(eq(10L), anyList())).thenReturn(true);

        assertThrows(ActiveOrderExistsException.class, () -> orderUseCase.createOrder(order));

        verify(orderRepository, never()).saveOrder(any());
        verify(traceabilityLogRepository, never()).saveTraceabilityLog(any());
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
        Order existingOrder = Order.builder()
                .id(orderId)
                .estado(OrderStatus.PENDIENTE)
                .idRestaurante(restaurantId)
                .build();

        Order updatedOrder = existingOrder.toBuilder()
                .idChef(employeeId)
                .estado(OrderStatus.EN_PREPARACION)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(existingOrder);
        when(userGateway.getRestaurantIdByEmployeeId(employeeId)).thenReturn(restaurantId);
        when(orderRepository.saveOrder(any(Order.class))).thenReturn(updatedOrder);

        Order result = orderUseCase.assignOrder(orderId, employeeId);

        assertNotNull(result);
        assertEquals(OrderStatus.EN_PREPARACION, result.getEstado());
        assertEquals(employeeId, result.getIdChef());
        verify(traceabilityLogRepository).saveTraceabilityLog(any()); // Verificamos envío de log
    }

    @Test
    void assignOrder_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById("invalid")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("invalid", 5L));
    }

    @Test
    void assignOrder_OrderNotPending_ShouldThrowException() {
        Order existingOrder = Order.builder()
                .id("123").estado(OrderStatus.EN_PREPARACION).build();
        when(orderRepository.findById("123")).thenReturn(existingOrder);

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("123", 5L));
    }

    @Test
    void assignOrder_EmployeeNotFromRestaurant_ShouldThrowException() {
        Order existingOrder = Order.builder()
                .id("123").estado(OrderStatus.PENDIENTE).idRestaurante(1L).build();

        when(orderRepository.findById("123")).thenReturn(existingOrder);
        when(userGateway.getRestaurantIdByEmployeeId(5L)).thenReturn(2L); // Restaurante distinto

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.assignOrder("123", 5L));
    }

    @Test
    void markOrderAsReady_Success() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.EN_PREPARACION).idChef(5L).idCliente(10L).build();
        Order savedOrder = order.toBuilder().estado(OrderStatus.LISTO).pinSeguridad("123456").build();

        when(orderRepository.findById("123")).thenReturn(order);
        when(orderRepository.saveOrder(any())).thenReturn(savedOrder);
        when(userGateway.getPhoneByUserId(10L)).thenReturn("+573000000000");

        Order result = orderUseCase.markOrderAsReady("123", 5L);

        assertEquals(OrderStatus.LISTO, result.getEstado());
        assertNotNull(result.getPinSeguridad());
        verify(smsGateway).sendSms(eq("+573000000000"), anyString());
        verify(traceabilityLogRepository).saveTraceabilityLog(any()); // Verificamos envío de log
    }

    @Test
    void markOrderAsReady_WrongStateOrChef_ShouldThrow() {
        Order orderWrongState = Order.builder()
                .id("1").estado(OrderStatus.PENDIENTE).idChef(5L).build();
        when(orderRepository.findById("1")).thenReturn(orderWrongState);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.markOrderAsReady("1", 5L));

        Order orderWrongChef = Order.builder()
                .id("2").estado(OrderStatus.EN_PREPARACION).idChef(99L).build();
        when(orderRepository.findById("2")).thenReturn(orderWrongChef);
        assertThrows(IllegalArgumentException.class, () -> orderUseCase.markOrderAsReady("2", 5L));
    }

    @Test
    void deliverOrder_Success() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.LISTO)
                .idChef(5L).pinSeguridad("123456").build();
        Order savedOrder = order.toBuilder().estado(OrderStatus.ENTREGADO).build();

        when(orderRepository.findById("123")).thenReturn(order);
        when(orderRepository.saveOrder(any())).thenReturn(savedOrder);

        Order result = orderUseCase.deliverOrder("123", "123456", 5L);

        assertEquals(OrderStatus.ENTREGADO, result.getEstado());
        verify(traceabilityLogRepository).saveTraceabilityLog(any()); // Verificamos envío de log
    }

    @Test
    void deliverOrder_WrongPin_ShouldThrowException() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.LISTO)
                .idChef(5L).pinSeguridad("123456").build();

        when(orderRepository.findById("123")).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.deliverOrder("123", "000000", 5L));
    }

    @Test
    void deliverOrder_WrongState_ShouldThrowException() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.EN_PREPARACION)
                .idChef(5L).pinSeguridad("123456").build();

        when(orderRepository.findById("123")).thenReturn(order);

        assertThrows(IllegalArgumentException.class, () -> orderUseCase.deliverOrder("123", "123456", 5L));
    }

    @Test
    void cancelOrder_Success() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.PENDIENTE).idCliente(7L).build();
        Order savedOrder = order.toBuilder().estado(OrderStatus.CANCELADO).build();

        when(orderRepository.findById("123")).thenReturn(order);
        when(orderRepository.saveOrder(any())).thenReturn(savedOrder);

        Order result = orderUseCase.cancelOrder("123", 7L);

        assertEquals(OrderStatus.CANCELADO, result.getEstado());
        verify(traceabilityLogRepository).saveTraceabilityLog(any()); // Verificamos envío de log
    }

    @Test
    void cancelOrder_WrongState_ShouldThrowExceptionWithExactMessage() {
        Order order = Order.builder()
                .id("123").estado(OrderStatus.EN_PREPARACION).idCliente(7L).build();

        when(orderRepository.findById("123")).thenReturn(order);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> orderUseCase.cancelOrder("123", 7L));
        assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", exception.getMessage());
        verify(traceabilityLogRepository, never()).saveTraceabilityLog(any());
    }
}