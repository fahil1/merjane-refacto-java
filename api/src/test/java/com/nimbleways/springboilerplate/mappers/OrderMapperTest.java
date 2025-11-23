package com.nimbleways.springboilerplate.mappers;

import com.nimbleways.springboilerplate.dto.order.OrderDto;
import com.nimbleways.springboilerplate.entities.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private OrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderMapper();
    }

    @Test
    void convertOrderToDto() {
        Order commande = new Order();
        commande.setId(42L);

        OrderDto resultat = mapper.toDto(commande);

        assertThat(resultat).isNotNull();
        assertThat(resultat.getId()).isEqualTo(42L);
    }

    @Test
    void returnNullWhenOrderIsNull() {
        OrderDto resultat = mapper.toDto(null);

        assertThat(resultat).isNull();
    }

    @Test
    void handleNullId() {
        Order commande = new Order();
        commande.setId(null);

        OrderDto resultat = mapper.toDto(commande);

        assertThat(resultat).isNotNull();
        assertThat(resultat.getId()).isNull();
    }
}