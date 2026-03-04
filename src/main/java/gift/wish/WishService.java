package gift.wish;

import gift.auth.AuthenticationException;
import gift.auth.AuthenticationResolver;
import gift.auth.ForbiddenException;
import gift.member.Member;
import gift.product.Product;
import gift.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;
    private final AuthenticationResolver authenticationResolver;

    public WishService(
        WishRepository wishRepository,
        ProductRepository productRepository,
        AuthenticationResolver authenticationResolver
    ) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
        this.authenticationResolver = authenticationResolver;
    }

    public Page<WishResponse> getWishes(String authorization, Pageable pageable) {
        Member member = extractMember(authorization);
        return wishRepository.findByMemberId(member.getId(), pageable).map(WishResponse::from);
    }

    public record AddWishResult(WishResponse wish, boolean created) {
    }

    public AddWishResult addWish(String authorization, WishRequest request) {
        Member member = extractMember(authorization);
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
        return wishRepository.findByMemberIdAndProductId(member.getId(), product.getId())
            .map(existing -> new AddWishResult(WishResponse.from(existing), false))
            .orElseGet(() -> new AddWishResult(
                WishResponse.from(wishRepository.save(request.toEntity(member.getId(), product))), true));
    }

    public void removeWish(String authorization, Long id) {
        Member member = extractMember(authorization);
        Wish wish = wishRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Wish not found."));
        if (!wish.getMemberId().equals(member.getId())) {
            throw new ForbiddenException();
        }
        wishRepository.delete(wish);
    }

    private Member extractMember(String authorization) {
        Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            throw new AuthenticationException();
        }
        return member;
    }
}
