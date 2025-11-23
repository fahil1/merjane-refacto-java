package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/*
 * RG: Les produits "NORMAL" ne présentent aucune particularité.
 *  Lorsqu'ils sont en rupture de stock, un délai est simplement annoncé aux clients.
 */
public class NormalProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void processProduct(Product product) {
        if (isProductAvailable(product)) {
            decrementStock(product);
        } else {
            handleOutOfStock(product);
        }
    }

    @Override
    public boolean supports(ProductType type) {
        return ProductType.NORMAL == type;
    }

    private boolean isProductAvailable(Product product) {
        return product.getAvailable() > 0;
    }

    private void decrementStock(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

    private void handleOutOfStock(Product product) {
        int leadTime = product.getLeadTime();
        notificationService.sendDelayNotification(leadTime, product.getName());
    }
}