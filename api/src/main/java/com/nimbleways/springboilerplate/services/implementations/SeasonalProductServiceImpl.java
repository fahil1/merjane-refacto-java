package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
/*
 * RG: Les produits "SEASONAL" ne sont disponibles qu'à certaines périodes de l'année.
 *  Lorsqu'ils sont en rupture de stock, un délai est annoncé aux clients, mais si ce délai dépasse la saison de disponibilité, le produit est considéré comme non disponible.
 *  Quand le produit est considéré comme non disponible, les client sont notifiés de cette indisponibilité.
 */
public class SeasonalProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void processProduct(Product product) {
        if (isProductAvailableInSeason(product)) {
            decrementStock(product);
        } else {
            handleUnavailableProduct(product);
        }
    }

    @Override
    public boolean supports(ProductType type) {
        return ProductType.SEASONAL == type;
    }

    /**
     * un produit est considéré DISPO si il est en stock et qu'il est disponible dans la saison actuelle
     * @param product
     * @return
     */
    private boolean isProductAvailableInSeason(Product product) {
        LocalDate now = LocalDate.now();
        return isInSeason(product, now) && hasStock(product);
    }

    private boolean isInSeason(Product product, LocalDate date) {
        return date.isAfter(product.getSeasonStartDate())
                && date.isBefore(product.getSeasonEndDate());
    }

    private boolean hasStock(Product product) {
        return product.getAvailable() > 0;
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void handleUnavailableProduct(Product product) {
        LocalDate now = LocalDate.now();
        boolean isOutOfSeason = isBeforeSeason(product, now) || isAfterSeason(product, now);

        if (isOutOfSeason) {
            notifyOutOfStock(product);
            return;
        }

        if (hasStock(product) && willBeAvailableBeforeSeasonEnds(product, now)) {
            notifyDelay(product);
        } else {
            notifyOutOfStock(product);
        }
    }

    private boolean isBeforeSeason(Product product, LocalDate date) {
        return product.getSeasonStartDate().isAfter(date);
    }

    private boolean isAfterSeason(Product product, LocalDate date) {
        return date.isAfter(product.getSeasonEndDate());
    }

    private boolean willBeAvailableBeforeSeasonEnds(Product product, LocalDate date) {
        LocalDate restockDate = date.plusDays(product.getLeadTime());
        return restockDate.isBefore(product.getSeasonEndDate())
                || restockDate.isEqual(product.getSeasonEndDate());
    }

    private void notifyDelay(Product product) {
        notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
    }

    private void notifyOutOfStock(Product product) {
        notificationService.sendOutOfStockNotification(product.getName());
        product.setAvailable(0);
        productRepository.save(product);
    }
}