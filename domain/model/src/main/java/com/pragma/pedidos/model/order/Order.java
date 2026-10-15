package com.pragma.pedidos.model.order;

import com.pragma.pedidos.model.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Order {
    private String id;
    private Long idCliente;
    private LocalDateTime fecha;
    private OrderStatus estado;
    private Long idChef;
    private Long idRestaurante;
    private List<OrderDish> platos;
}