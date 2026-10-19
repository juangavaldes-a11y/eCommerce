package com.ecomerce.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundMapsTo404WithMessageAndTimestamp() {
        ResponseEntity<Object> response = handler.handleNotFound(new NotFoundException("Product not found: 5"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Product not found: 5", body.get("message"));
        assertInstanceOf(Instant.class, body.get("timestamp"));
    }

    @Test
    void unexpectedErrorsMapTo500WithTheirMessage() {
        ResponseEntity<Object> response = handler.handleGeneric(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("boom", body.get("message"));
        assertInstanceOf(Instant.class, body.get("timestamp"));
    }

    @Test
    void exceptionsWithoutAMessageStillProduceAResponse() {
        ResponseEntity<Object> response = handler.handleGeneric(new RuntimeException());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Unexpected error", body.get("message"));
    }

    @Test
    void notFoundExceptionCarriesItsMessage() {
        assertEquals("missing", new NotFoundException("missing").getMessage());
    }
}
