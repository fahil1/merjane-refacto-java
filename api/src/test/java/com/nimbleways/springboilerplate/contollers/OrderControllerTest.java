package com.nimbleways.springboilerplate.contollers;

import com.nimbleways.springboilerplate.dto.order.OrderDto;
import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.services.OrderProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderProcessingService orderProcessingService;

    @InjectMocks
    private OrderController controller;

    @Test
    void mustReturnProcessOrderResponse() {
        Long orderId = 123L;
        OrderDto orderDto = new OrderDto(orderId);
        when(orderProcessingService.processOrder(orderId)).thenReturn(orderDto);

        ProcessOrderResponse reponse = controller.processOrder(orderId);

        assertThat(reponse).isNotNull();
        assertThat(reponse.id()).isEqualTo(orderId);
        verify(orderProcessingService).processOrder(orderId);
    }

    @Test
    void mustCallProcessOrderService() {
        Long orderId = 999L;
        OrderDto orderDto = new OrderDto(orderId);
        when(orderProcessingService.processOrder(orderId)).thenReturn(orderDto);

        controller.processOrder(orderId);

        verify(orderProcessingService, times(1)).processOrder(orderId);
    }
}