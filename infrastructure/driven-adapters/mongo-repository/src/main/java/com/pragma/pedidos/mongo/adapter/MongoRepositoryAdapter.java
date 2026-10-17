package com.pragma.pedidos.mongo.adapter;

import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.model.order.gateways.OrderRepository;
import com.pragma.pedidos.mongo.MongoDBRepository;
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

    @Override
    public List<Order> findByIdRestauranteAndEstado(Long idRestaurante, OrderStatus estado, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return repository.findByIdRestauranteAndEstado(idRestaurante, estado, pageable)
                .stream()
                .map(this::toEntity) // toEntity en AdapterOperations convierte de Document a Domain (Order)
                .toList();
    }

    @Override
    public Order findById(String idOrder) {
        // Buscamos en BD y si existe lo mapeamos al objeto de Dominio
        return repository.findById(idOrder)
                .map(this::toEntity)
                .orElse(null);
    }
}