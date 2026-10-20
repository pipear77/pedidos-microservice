package com.pragma.pedidos.mongo.adapter;

import com.pragma.pedidos.mongo.client.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGatewayAdapterTest {

    @Test
    void methods_ShouldHandleExceptions_WhenRestClientFails() {
        UserGatewayAdapter adapter = new UserGatewayAdapter();

        // Mockeamos el RestClient internamente para forzar fallos y cubrir los bloques catch
        RestClient mockClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec mockUriSpec = mock(RestClient.RequestHeadersUriSpec.class);

        when(mockClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(Long.class))).thenThrow(new RuntimeException("Error simulado"));

        ReflectionTestUtils.setField(adapter, "restClient", mockClient);

        assertThrows(IllegalArgumentException.class, () -> adapter.getRestaurantIdByEmployeeId(1L));
        assertNull(adapter.getPhoneByUserId(1L));
        assertNull(adapter.getEmailByUserId(1L));
    }

    @Test
    void methods_ShouldReturnValues_WhenRestClientSucceeds() {
        UserGatewayAdapter adapter = new UserGatewayAdapter();
        org.springframework.web.client.RestClient mockClient = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.class);
        org.springframework.web.client.RestClient.RequestHeadersUriSpec mockUriSpec = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.RequestHeadersUriSpec.class);
        org.springframework.web.client.RestClient.ResponseSpec mockResponseSpec = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.ResponseSpec.class);

        when(mockClient.get()).thenReturn(mockUriSpec);
        when(mockUriSpec.uri(anyString(), any(Long.class))).thenReturn(mockUriSpec);
        when(mockUriSpec.retrieve()).thenReturn(mockResponseSpec);

        UserResponseDto mockResponse = new UserResponseDto(1L, "EMPLEADO", 5L, "3001234", "emp@mail.com");
        when(mockResponseSpec.body(UserResponseDto.class)).thenReturn(mockResponse);

        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "restClient", mockClient);

        assertEquals(5L, adapter.getRestaurantIdByEmployeeId(1L));
        assertEquals("3001234", adapter.getPhoneByUserId(1L));
        assertEquals("emp@mail.com", adapter.getEmailByUserId(1L));
    }
}