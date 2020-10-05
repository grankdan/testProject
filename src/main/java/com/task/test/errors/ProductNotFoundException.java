package com.task.test.errors;

public class ProductNotFoundException extends RuntimeException {
    private static final String PRODUCT_NOT_FOUND_ERROR_MSG = "Product with id %s was not found.";

    public ProductNotFoundException(final Long id) {
        super(String.format(PRODUCT_NOT_FOUND_ERROR_MSG, id));
    }
}
