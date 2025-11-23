package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NormalProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NormalProductServiceImpl service;

    @Test
    void shouldSupportNormalProductType() {
        boolean result = service.supports(ProductType.NORMAL);

        assertThat(result).isTrue();
    }

    @Test
    void shouldNotSupportOtherProductTypes() {
        assertThat(service.supports(ProductType.SEASONAL)).isFalse();
        assertThat(service.supports(ProductType.EXPIRABLE)).isFalse();
    }

    @Test
    void shouldDecrementStockWhenProductIsAvailable() {
        Product product = createProduct(10);

        service.processProduct(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyDelayWhenProductIsOutOfStock() {
        Product product = createProduct(0);
        product.setLeadTime(15);

        service.processProduct(product);

        verify(notificationService).sendDelayNotification(15, "cable usb");
        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldNotNotifyWhenLeadTimeIsZero() {
        Product product = createProduct(0);
        product.setLeadTime(0);

        service.processProduct(product);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldHandleStockOfOne() {
        Product product = createProduct(1);

        service.processProduct(product);

        assertThat(product.getAvailable()).isZero();
        verify(productRepository).save(product);
    }

    private Product createProduct(int availableQuantity) {
        Product product = new Product();
        product.setId(1L);
        product.setType(ProductType.NORMAL);
        product.setName("cable usb");
        product.setAvailable(availableQuantity);
        product.setLeadTime(10);
        return product;
    }
}