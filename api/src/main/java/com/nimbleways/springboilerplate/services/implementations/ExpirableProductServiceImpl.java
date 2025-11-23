package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
/*
 * RG: Les produits "EXPIRABLE" ont une date d'expiration.
 *  Ils peuvent être vendus normalement tant qu'ils n'ont pas expiré, mais ne sont plus disponibles une fois la date d'expiration passée
 */
public class ExpirableProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void processProduct(Product product) {
        if (isProductValid(product)) {
            decrementStock(product);
        } else {
            handleExpiredProduct(product);
        }
    }

    @Override
    public boolean supports(ProductType type) {
        return ProductType.EXPIRABLE == type;
    }

    /**
     * Un produit est considéré valide, s'il est en stock et il n'est pas expiré
     * @param product
     * @return
     */
    private boolean isProductValid(Product product) {
        return hasStock(product) && isNotExpired(product);
    }

    private boolean hasStock(Product product) {
        return product.getAvailable() > 0;
    }

    private boolean isNotExpired(Product product) {
        return product.getExpiryDate().isAfter(LocalDate.now());
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
        log.debug("product with id: {} decremented", product.getId());
    }

    /**
     * envoie une notification pour alerter l'expiration du produit..
     * @param product
     */
    private void handleExpiredProduct(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
        log.debug("product with id: {} expired", product.getId());
    }
}