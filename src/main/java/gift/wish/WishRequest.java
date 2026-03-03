package gift.wish;

import gift.product.Product;
import jakarta.validation.constraints.NotNull;

public record WishRequest(@NotNull Long productId) {
    public Wish toEntity(Long memberId, Product product) {
        return new Wish(memberId, product);
    }
}
