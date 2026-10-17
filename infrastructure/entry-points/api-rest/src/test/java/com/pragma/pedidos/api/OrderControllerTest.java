package com.pragma.pedidos.api;

import com.pragma.pedidos.api.dto.OrderDishRequestDto;
import com.pragma.pedidos.api.dto.OrderRequestDto;
import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.usecase.order.OrderUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderUseCase orderUseCase;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        // Mockeamos el SecurityContext para simular un usuario logueado con ID 4
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(4L);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_Success_ShouldReturnCreated() {
        OrderDishRequestDto dishDto = new OrderDishRequestDto();
        dishDto.setIdPlato(1L);
        dishDto.setCantidad(2);

        OrderRequestDto requestDto = new OrderRequestDto();
        requestDto.setIdRestaurante(1L);
        requestDto.setPlatos(List.of(dishDto));

        Order savedOrder = Order.builder().id("mongo-id-123").idCliente(4L).build();

        when(orderUseCase.createOrder(any(Order.class))).thenReturn(savedOrder);

        ResponseEntity<Order> response = orderController.createOrder(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mongo-id-123", response.getBody().getId());
        verify(orderUseCase).createOrder(any(Order.class));
    }

    @Test
    void getOrdersByStatus_Success_ShouldReturnOk() {
        com.pragma.pedidos.model.order.Order order = com.pragma.pedidos.model.order.Order.builder().id("123").build();
        when(orderUseCase.getOrdersByStatus(eq(4L), eq(com.pragma.pedidos.model.order.enums.OrderStatus.PENDIENTE), eq(0), eq(10)))
                .thenReturn(List.of(order));

        ResponseEntity<List<com.pragma.pedidos.model.order.Order>> response =
                orderController.getOrdersByStatus(com.pragma.pedidos.model.order.enums.OrderStatus.PENDIENTE, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void assignOrder_Success_ShouldReturnOk() {
        // 1. Objeto REAL de autenticación
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("empleado@test.com", null);
        auth.setDetails(5L); // El ID de nuestro empleado
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        // 2. Preparamos la respuesta esperada
        String orderId = "123";
        com.pragma.pedidos.model.order.Order updatedOrder = com.pragma.pedidos.model.order.Order.builder()
                .id(orderId)
                .estado(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION)
                .idChef(5L)
                .build();

        // 3. ¡LA SOLUCIÓN DEFINITIVA! Usamos anyString() y anyLong() para evitar cualquier problema de strict stubbing
        org.mockito.Mockito.when(orderUseCase.assignOrder(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong()
        )).thenReturn(updatedOrder);

        // 4. Ejecutamos el controlador
        org.springframework.http.ResponseEntity<com.pragma.pedidos.model.order.Order> response =
                orderController.assignOrder(orderId);

        // 5. Validamos
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        org.junit.jupiter.api.Assertions.assertEquals(com.pragma.pedidos.model.order.enums.OrderStatus.EN_PREPARACION, response.getBody().getEstado());

        // 6. Limpiamos el contexto de seguridad
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void markOrderAsReady_Success_ShouldReturnOk() {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("emp@test.com", null);
        auth.setDetails(5L);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        com.pragma.pedidos.model.order.Order order = com.pragma.pedidos.model.order.Order.builder().estado(com.pragma.pedidos.model.order.enums.OrderStatus.LISTO).build();
        when(orderUseCase.markOrderAsReady(anyString(), anyLong())).thenReturn(order);

        org.springframework.http.ResponseEntity<com.pragma.pedidos.model.order.Order> response = orderController.markOrderAsReady("123");
        assertEquals(org.springframework.http.HttpStatus.OK, response.getStatusCode());
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}