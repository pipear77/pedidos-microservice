package com.pragma.pedidos.api.exception;

import com.pragma.pedidos.usecase.order.exception.ActiveOrderExistsException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Hidden // <-- 2. Añade esta anotación para que Swagger no intente leer esta clase
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ActiveOrderExistsException.class)
    public ResponseEntity<Map<String, Object>> handleActiveOrderExistsException(ActiveOrderExistsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Conflict - Regla de Negocio");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.CONFLICT) // 409 Conflict es ideal para reglas de estado
                .body(java.util.Map.of("mensaje", ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return org.springframework.http.ResponseEntity
                .status(org.springframework.http.HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(java.util.Map.of("mensaje", ex.getMessage()));
    }
}