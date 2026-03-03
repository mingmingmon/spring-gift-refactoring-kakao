package gift.order;

import gift.auth.AuthenticationException;
import gift.auth.AuthenticationResolver;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.wish.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final WishRepository wishRepository;
    private final MemberRepository memberRepository;
    private final AuthenticationResolver authenticationResolver;
    private final KakaoMessageClient kakaoMessageClient;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        WishRepository wishRepository,
        MemberRepository memberRepository,
        AuthenticationResolver authenticationResolver,
        KakaoMessageClient kakaoMessageClient
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.wishRepository = wishRepository;
        this.memberRepository = memberRepository;
        this.authenticationResolver = authenticationResolver;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    public Page<OrderResponse> getOrders(String authorization, Pageable pageable) {
        Member member = extractMember(authorization);
        return orderRepository.findByMemberId(member.getId(), pageable).map(OrderResponse::from);
    }

    public OrderResponse createOrder(String authorization, OrderRequest request) {
        Member member = extractMember(authorization);

        Option option = optionRepository.findById(request.optionId())
            .orElseThrow(() -> new NoSuchElementException("Option not found."));

        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        Order saved = orderRepository.save(new Order(option, member.getId(), request.quantity(), request.message()));

        sendKakaoMessageIfPossible(member, saved, option);

        return OrderResponse.from(saved);
    }

    private void sendKakaoMessageIfPossible(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }

    private Member extractMember(String authorization) {
        Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            throw new AuthenticationException();
        }
        return member;
    }
}
