package com.pragma.pedidos.mongo;

import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.mongo.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoDBRepository extends MongoRepository<OrderDocument, String> {

    // Spring Data convierte esto automáticamente en una consulta a MongoDB:
    // db.orders.countDocuments({ idCliente: ?, estado: { $in: [?] } }) > 0
    boolean existsByIdClienteAndEstadoIn(Long idCliente, List<OrderStatus> estados);
}