package com.pragma.pedidos.mongo.document;

import com.pragma.pedidos.model.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocument {
    @Id
    private String id;
    private Long idCliente;
    private LocalDateTime fecha;
    private OrderStatus estado;
    private Long idChef;
    private Long idRestaurante;
    private List<OrderDishDocument> platos;
}