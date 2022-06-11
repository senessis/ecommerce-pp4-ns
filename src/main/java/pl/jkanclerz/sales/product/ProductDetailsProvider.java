package pl.jkanclerz.sales.product;

import java.util.Optional;

public interface ProductDetailsProvider {
    Optional<ProductDetails> getById(String productId);
}
