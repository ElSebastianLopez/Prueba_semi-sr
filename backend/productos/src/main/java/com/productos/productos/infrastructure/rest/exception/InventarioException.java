package com.productos.productos.infrastructure.rest.exception;

public class InventarioException extends RuntimeException {
    public InventarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventarioException(String message) {
        super(message);
    }
}
