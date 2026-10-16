package com.pragma.pedidos.mongo;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.mongo.document.OrderDocument;
import com.pragma.pedidos.mongo.helper.AdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MongoRepositoryAdapter
        extends AdapterOperations<Order, OrderDocument, String, MongoDBRepository>
        implements OrderRepository {

    public MongoRepositoryAdapter(MongoDBRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Order.class));
    }

    @Override
    public Order saveOrder(Order order) {
        // La clase AdapterOperations ya tiene un método save() que hace la conversión por nosotros
        return super.save(order);
    }

    @Override
    public boolean existsByIdClienteAndEstadoIn(Long idCliente, List<OrderStatus> estados) {
        // Llamamos al método mágico de Spring Data
        return repository.existsByIdClienteAndEstadoIn(idCliente, estados);
    }
}