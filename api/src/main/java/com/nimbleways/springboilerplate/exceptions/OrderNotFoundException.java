package com.nimbleways.springboilerplate.exceptions;

/**
 * Exception thrown when an order is not found
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }
}