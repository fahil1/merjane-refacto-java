package com.nimbleways.springboilerplate.exceptions;

import com.nimbleways.springboilerplate.entities.ProductType;

/**
 * Exception thrown when no processor is found for a product type
 */
public class ProductProcessorNotFoundException extends RuntimeException {

    public ProductProcessorNotFoundException(ProductType productType) {
        super("No processor found for product type: " + productType);
    }
}