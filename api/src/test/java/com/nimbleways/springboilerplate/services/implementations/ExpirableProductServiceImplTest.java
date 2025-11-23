package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpirableProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExpirableProductServiceImpl service;

    @Test
    void shouldSupportExpirableProductType() {
        boolean result = service.supports(ProductType.EXPIRABLE);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDecrementStockWhenProductIsValid() {
        Product product = createExpirableProduct(10, LocalDate.now().plusDays(15));

        service.processProduct(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyExpirationWhenProductIsExpired() {
        LocalDate expirationDate = LocalDate.now().minusDays(2);
        Product product = createExpirableProduct(5, expirationDate);

        service.processProduct(product);

        verify(notificationService).sendExpirationNotification("fromage", expirationDate);
        verify(productRepository).save(product);
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldNotifyExpirationWhenProductHasNoStock() {
        Product product = createExpirableProduct(0, LocalDate.now().plusDays(10));

        service.processProduct(product);

        verify(notificationService).sendExpirationNotification(eq("fromage"), any());
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldHandleProductExpiringToday() {
        Product product = createExpirableProduct(10, LocalDate.now());

        service.processProduct(product);

        verify(notificationService).sendExpirationNotification(eq("fromage"), any());
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldHandleProductExpiringTomorrow() {
        Product product = createExpirableProduct(5, LocalDate.now().plusDays(1));

        service.processProduct(product);

        assertThat(product.getAvailable()).isEqualTo(4);
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldHandleLastItemInStock() {
        Product product = createExpirableProduct(1, LocalDate.now().plusDays(7));

        service.processProduct(product);

        assertThat(product.getAvailable()).isZero();
        verify(productRepository).save(product);
    }

    private Product createExpirableProduct(int stock, LocalDate expirationDate) {
        Product product = new Product();
        product.setId(1L);
        product.setType(ProductType.EXPIRABLE);
        product.setName("fromage");
        product.setAvailable(stock);
        product.setLeadTime(10);
        product.setExpiryDate(expirationDate);
        return product;
    }
}