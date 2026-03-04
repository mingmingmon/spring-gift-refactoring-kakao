package gift.wish;

import gift.auth.AuthenticationResolver;
import gift.auth.ForbiddenException;
import gift.member.Member;
import gift.product.Product;
import gift.product.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductService productService;
    private final AuthenticationResolver authenticationResolver;

    public WishService(
        WishRepository wishRepository,
        ProductService productService,
        AuthenticationResolver authenticationResolver
    ) {
        this.wishRepository = wishRepository;
        this.productService = productService;
        this.authenticationResolver = authenticationResolver;
    }

    public Page<WishResponse> getWishes(String authorization, Pageable pageable) {
        Member member = authenticationResolver.extractMemberOrThrow(authorization);
        return wishRepository.findByMemberId(member.getId(), pageable).map(WishResponse::from);
    }

    public record AddWishResult(WishResponse wish, boolean created) {
    }

    public AddWishResult addWish(String authorization, WishRequest request) {
        Member member = authenticationResolver.extractMemberOrThrow(authorization);
        Product product = productService.findById(request.productId());
        return wishRepository.findByMemberIdAndProductId(member.getId(), product.getId())
            .map(existing -> new AddWishResult(WishResponse.from(existing), false))
            .orElseGet(() -> new AddWishResult(
                WishResponse.from(wishRepository.save(request.toEntity(member.getId(), product))), true));
    }

    public void removeWish(String authorization, Long id) {
        Member member = authenticationResolver.extractMemberOrThrow(authorization);
        Wish wish = wishRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("위시가 존재하지 않습니다."));
        if (!wish.getMemberId().equals(member.getId())) {
            throw new ForbiddenException();
        }
        wishRepository.delete(wish);
    }

}
