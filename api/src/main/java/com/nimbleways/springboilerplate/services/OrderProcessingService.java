package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.order.OrderDto;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exceptions.ProductProcessorNotFoundException;
import com.nimbleways.springboilerplate.mappers.OrderMapper;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final List<ProductService> productServices;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDto processOrder(Long orderId) {
        log.info("processing order with id: {}", orderId);
        Order order = findOrderById(orderId);
        processOrderItems(order.getItems());
        log.info("order with id: {} processed", order);
        return orderMapper.toDto(order);
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void processOrderItems(Set<Product> products) {
        products.forEach(this::processProduct);
    }

    private void processProduct(Product product) {
        log.debug("processing product with id: {}", product.getId());
        ProductType productType = product.getType();
        ProductService processor = findProcessorForProduct(productType);
        processor.processProduct(product);
        log.debug("product with id: {} processed", product.getId());
    }

    private ProductService findProcessorForProduct(ProductType productType) {
        return productServices.stream()
                .filter(processor -> processor.supports(productType))
                .findFirst()
                .orElseThrow(() -> new ProductProcessorNotFoundException(productType));
    }
}