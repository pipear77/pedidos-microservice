package com.pragma.pedidos.mongo;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.mongo.adapter.MongoRepositoryAdapter;
import com.pragma.pedidos.mongo.document.OrderDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoRepositoryAdapterTest {

    @Mock
    private MongoDBRepository repository;

    @Mock
    private ObjectMapper mapper;

    private MongoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MongoRepositoryAdapter(repository, mapper);
    }

    @Test
    void saveOrder_ShouldReturnSavedOrder() {
        Order order = new Order();
        OrderDocument document = new OrderDocument();

        when(mapper.map(order, OrderDocument.class)).thenReturn(document);
        when(repository.save(document)).thenReturn(document);
        when(mapper.map(document, Order.class)).thenReturn(order);

        Order result = adapter.saveOrder(order);

        assertNotNull(result);
        verify(repository).save(document);
    }

    @Test
    void existsByIdClienteAndEstadoIn_ShouldReturnBoolean() {
        List<OrderStatus> statuses = List.of(OrderStatus.PENDIENTE);
        when(repository.existsByIdClienteAndEstadoIn(1L, statuses)).thenReturn(true);

        boolean result = adapter.existsByIdClienteAndEstadoIn(1L, statuses);

        assertTrue(result);
        verify(repository).existsByIdClienteAndEstadoIn(1L, statuses);
    }

    @Test
    void findByIdRestauranteAndEstado_ShouldReturnList() {
        com.pragma.pedidos.model.order.Order order = new com.pragma.pedidos.model.order.Order();
        com.pragma.pedidos.mongo.document.OrderDocument document = new com.pragma.pedidos.mongo.document.OrderDocument();
        org.springframework.data.domain.Page<com.pragma.pedidos.mongo.document.OrderDocument> page =
                new org.springframework.data.domain.PageImpl<>(List.of(document));

        when(repository.findByIdRestauranteAndEstado(eq(1L), eq(OrderStatus.PENDIENTE), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        when(mapper.map(document, com.pragma.pedidos.model.order.Order.class)).thenReturn(order);

        List<com.pragma.pedidos.model.order.Order> result = adapter.findByIdRestauranteAndEstado(1L, OrderStatus.PENDIENTE, 0, 10);

        assertFalse(result.isEmpty());
        verify(repository).findByIdRestauranteAndEstado(eq(1L), eq(OrderStatus.PENDIENTE), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void findById_Success_ShouldReturnOrder() {
        String orderId = "123";
        com.pragma.pedidos.mongo.document.OrderDocument document = new com.pragma.pedidos.mongo.document.OrderDocument();
        com.pragma.pedidos.model.order.Order order = new com.pragma.pedidos.model.order.Order();

        when(repository.findById(orderId)).thenReturn(java.util.Optional.of(document));
        when(mapper.map(document, com.pragma.pedidos.model.order.Order.class)).thenReturn(order);

        com.pragma.pedidos.model.order.Order result = adapter.findById(orderId);

        assertNotNull(result);
        verify(repository).findById(orderId);
    }

    @Test
    void findById_NotFound_ShouldReturnNull() {
        when(repository.findById("invalid")).thenReturn(java.util.Optional.empty());

        com.pragma.pedidos.model.order.Order result = adapter.findById("invalid");

        assertNull(result);
    }
}