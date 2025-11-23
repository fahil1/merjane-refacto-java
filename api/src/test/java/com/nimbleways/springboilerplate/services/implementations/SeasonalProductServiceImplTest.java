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
class SeasonalProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SeasonalProductServiceImpl service;

    @Test
    void shouldSupportSeasonalProductType() {
        boolean result = service.supports(ProductType.SEASONAL);

        assertThat(result).isTrue();
    }

    @Test
    void shouldDecrementStockWhenProductIsInSeason() {
        Product product = createSeasonalProduct(
            20,
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(50)
        );

        service.processProduct(product);

        assertThat(product.getAvailable()).isEqualTo(19);
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyOutOfStockWhenSeasonHasNotStartedYet() {
        Product product = createSeasonalProduct(
            20,
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(90)
        );

        service.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("pasteque");
        verify(productRepository).save(product);
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldNotifyOutOfStockWhenSeasonIsOver() {
        Product product = createSeasonalProduct(
            20,
            LocalDate.now().minusDays(90),
            LocalDate.now().minusDays(10)
        );

        service.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("pasteque");
        verify(productRepository).save(product);
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldDecrementStockWhenProductHasStockAndRestockArrivesBeforeSeasonEnd() {
        Product product = createSeasonalProduct(
            5,
            LocalDate.now().minusDays(5),
            LocalDate.now().plusDays(30)
        );
        product.setLeadTime(10);

        service.processProduct(product);

        assertThat(product.getAvailable()).isEqualTo(4);
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotifyOutOfStockWhenRestockExceedsSeasonEnd() {
        Product product = createSeasonalProduct(
            0,
            LocalDate.now().minusDays(5),
            LocalDate.now().plusDays(20)
        );
        product.setLeadTime(50);

        service.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("pasteque");
        verify(notificationService, never()).sendDelayNotification(anyInt(), any());
        assertThat(product.getAvailable()).isZero();
    }

    @Test
    void shouldNotifyOutOfStockWhenNoStockDuringActiveSeason() {
        Product product = createSeasonalProduct(
            0,
            LocalDate.now().minusDays(10),
            LocalDate.now().plusDays(50)
        );
        product.setLeadTime(5);

        service.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("pasteque");
        verify(notificationService, never()).sendDelayNotification(anyInt(), any());
        assertThat(product.getAvailable()).isZero();
    }

    private Product createSeasonalProduct(int stock, LocalDate startDate, LocalDate endDate) {
        Product product = new Product();
        product.setId(1L);
        product.setType(ProductType.SEASONAL);
        product.setName("pasteque");
        product.setAvailable(stock);
        product.setLeadTime(10);
        product.setSeasonStartDate(startDate);
        product.setSeasonEndDate(endDate);
        return product;
    }
}