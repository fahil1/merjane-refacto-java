package com.nimbleways.springboilerplate.mappers;

import com.nimbleways.springboilerplate.dto.order.OrderDto;
import com.nimbleways.springboilerplate.entities.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderDto(order.getId());
    }
}