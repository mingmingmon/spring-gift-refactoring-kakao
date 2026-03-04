package gift.order;

import gift.auth.AuthenticationResolver;
import gift.kakao.KakaoMessageClient;
import gift.member.Member;
import gift.member.MemberService;
import gift.option.Option;
import gift.option.OptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionService optionService;
    private final MemberService memberService;
    private final AuthenticationResolver authenticationResolver;
    private final KakaoMessageClient kakaoMessageClient;

    public OrderService(
        OrderRepository orderRepository,
        OptionService optionService,
        MemberService memberService,
        AuthenticationResolver authenticationResolver,
        KakaoMessageClient kakaoMessageClient
    ) {
        this.orderRepository = orderRepository;
        this.optionService = optionService;
        this.memberService = memberService;
        this.authenticationResolver = authenticationResolver;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    public Page<OrderResponse> getOrders(String authorization, Pageable pageable) {
        Member member = authenticationResolver.extractMemberOrThrow(authorization);
        return orderRepository.findByMemberId(member.getId(), pageable).map(OrderResponse::from);
    }

    public OrderResponse createOrder(String authorization, OrderRequest request) {
        Member member = authenticationResolver.extractMemberOrThrow(authorization);

        Option option = optionService.findById(request.optionId());

        optionService.subtractQuantity(option.getId(), request.quantity());

        int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberService.save(member);

        Order saved = orderRepository.save(request.toEntity(option, member.getId()));

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

}
