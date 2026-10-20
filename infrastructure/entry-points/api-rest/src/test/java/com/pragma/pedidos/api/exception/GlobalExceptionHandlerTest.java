package com.pragma.pedidos.api.exception;

import com.pragma.pedidos.usecase.order.exception.ActiveOrderExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleActiveOrderExistsException() {
        ActiveOrderExistsException ex = new ActiveOrderExistsException("Activo");
        ResponseEntity<Map<String, Object>> response = handler.handleActiveOrderExistsException(ex);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Activo", response.getBody().get("message"));
    }

    @Test
    void handleValidationExceptions() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("object", "field", "error message")));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void handleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Estado incorrecto");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalStateException(ex);
        assertEquals(409, response.getStatusCode().value());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);
        assertEquals(400, response.getStatusCode().value());
    }
}