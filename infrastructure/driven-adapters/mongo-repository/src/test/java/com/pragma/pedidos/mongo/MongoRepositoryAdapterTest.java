package com.pragma.pedidos.mongo;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
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
}