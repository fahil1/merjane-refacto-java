package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.order.OrderDto;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exceptions.ProductProcessorNotFoundException;
import com.nimbleways.springboilerplate.mappers.OrderMapper;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productServiceNormal;

    @Mock
    private ProductService productServiceSeasonal;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderProcessingService service;

    @Test
    void shouldProcessSimpleOrder() {
        Long orderId = 1L;
        Product product = createProduct(ProductType.NORMAL);
        Order order = createOrder(orderId, product);
        OrderDto orderDto = new OrderDto(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productServiceNormal.supports(ProductType.NORMAL)).thenReturn(true);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        service = new OrderProcessingService(
            orderRepository,
            List.of(productServiceNormal),
            orderMapper
        );

        OrderDto result = service.processOrder(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        verify(orderRepository).findById(orderId);
        verify(productServiceNormal).processProduct(product);
        verify(orderMapper).toDto(order);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        service = new OrderProcessingService(
            orderRepository,
            List.of(productServiceNormal),
            orderMapper
        );

        assertThatThrownBy(() -> service.processOrder(orderId))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("Order not found with id: 999");
    }

    @Test
    void shouldProcessMultipleProducts() {
        Long orderId = 5L;
        Product product1 = createProduct(ProductType.NORMAL);
        Product product2 = createProduct(ProductType.SEASONAL);
        Order order = createOrder(orderId, product1, product2);
        OrderDto orderDto = new OrderDto(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        
        lenient().when(productServiceNormal.supports(ProductType.NORMAL)).thenReturn(true);
        lenient().when(productServiceNormal.supports(ProductType.SEASONAL)).thenReturn(false);
        
        lenient().when(productServiceSeasonal.supports(ProductType.NORMAL)).thenReturn(false);
        lenient().when(productServiceSeasonal.supports(ProductType.SEASONAL)).thenReturn(true);
        
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        service = new OrderProcessingService(
            orderRepository,
            List.of(productServiceNormal, productServiceSeasonal),
            orderMapper
        );

        service.processOrder(orderId);

        verify(productServiceNormal).processProduct(product1);
        verify(productServiceSeasonal).processProduct(product2);
    }

    @Test
    void shouldThrowExceptionWhenNoProductProcessorFound() {
        Long orderId = 3L;
        Product product = createProduct(ProductType.EXPIRABLE);
        Order order = createOrder(orderId, product);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productServiceNormal.supports(any())).thenReturn(false);

        service = new OrderProcessingService(
            orderRepository,
            List.of(productServiceNormal),
            orderMapper
        );

        assertThatThrownBy(() -> service.processOrder(orderId))
            .isInstanceOf(ProductProcessorNotFoundException.class)
            .hasMessageContaining("No processor found for product type: EXPIRABLE");
    }

    @Test
    void shouldProcessOrderWithEmptyItems() {
        Long orderId = 10L;
        Order order = createOrder(orderId);
        OrderDto orderDto = new OrderDto(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        service = new OrderProcessingService(
            orderRepository,
            List.of(productServiceNormal),
            orderMapper
        );

        OrderDto result = service.processOrder(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        verify(productServiceNormal, never()).processProduct(any());
    }

    private Product createProduct(ProductType type) {
        Product product = new Product();
        product.setId(1L);
        product.setType(type);
        product.setName("Test Product");
        product.setAvailable(10);
        product.setLeadTime(5);
        return product;
    }

    private Order createOrder(Long id, Product... products) {
        Order order = new Order();
        order.setId(id);
        Set<Product> items = new HashSet<>();
        items.addAll(Arrays.asList(products));
        order.setItems(items);
        return order;
    }
}