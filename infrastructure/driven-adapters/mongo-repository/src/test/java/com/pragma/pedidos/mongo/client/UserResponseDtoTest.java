package com.pragma.pedidos.mongo.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserResponseDtoTest {
    @Test
    void testRecordGetters() {
        UserResponseDto dto = new UserResponseDto(1L, "CLIENTE", 2L, "123456", "test@mail.com");
        assertEquals(1L, dto.id());
        assertEquals("CLIENTE", dto.role());
        assertEquals(2L, dto.idRestaurant());
        assertEquals("123456", dto.phone());
        assertEquals("test@mail.com", dto.email());
    }
}