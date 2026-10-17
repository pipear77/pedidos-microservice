package com.pragma.pedidos.api;

import com.pragma.pedidos.api.dto.OrderRequestDto;
import com.pragma.pedidos.model.order.Order;
import com.pragma.pedidos.model.order.OrderDish;
import com.pragma.pedidos.model.order.enums.OrderStatus;
import com.pragma.pedidos.usecase.order.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequestDto request) {

        // 1. Extraemos el ID del cliente logueado desde el JWT
        Long idClienteLogueado = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();

        // 2. Mapeamos
        List<OrderDish> platos = request.getPlatos().stream()
                .map(p -> OrderDish.builder()
                        .idPlato(p.getIdPlato())
                        .cantidad(p.getCantidad())
                        .build())
                .collect(Collectors.toList());

        // 3. Armamos la orden
        Order order = Order.builder()
                .idCliente(idClienteLogueado)
                .idRestaurante(request.getIdRestaurante())
                .platos(platos)
                .build();

        // 4. Guardamos
        Order savedOrder = orderUseCase.createOrder(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @RequestParam(name = "estado") OrderStatus estado,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Long idEmpleadoLogueado = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
        List<Order> orders = orderUseCase.getOrdersByStatus(idEmpleadoLogueado, estado, page, size);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{idOrder}/assign")
    public ResponseEntity<Order> assignOrder(@PathVariable("idOrder") String idOrder) {
        Long idEmpleadoLogueado = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();

        Order updatedOrder = orderUseCase.assignOrder(idOrder, idEmpleadoLogueado);

        return ResponseEntity.ok(updatedOrder);
    }
}