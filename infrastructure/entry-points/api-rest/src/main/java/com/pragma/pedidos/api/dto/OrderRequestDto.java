package com.pragma.pedidos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDto {
    @NotNull(message = "El id del restaurante es obligatorio")
    private Long idRestaurante;

    @NotEmpty(message = "Debe incluir al menos un plato en el pedido")
    @Valid
    private List<OrderDishRequestDto> platos;
}