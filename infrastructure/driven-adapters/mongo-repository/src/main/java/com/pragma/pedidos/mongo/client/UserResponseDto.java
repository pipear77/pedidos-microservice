package com.pragma.pedidos.mongo.client;

public record UserResponseDto(Long id, String role, Long idRestaurant, String phone) {}
