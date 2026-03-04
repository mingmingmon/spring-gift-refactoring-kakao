package gift.order;

import gift.auth.AuthenticationException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
        @RequestHeader("Authorization") String authorization,
        Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrders(authorization, pageable));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @RequestHeader("Authorization") String authorization,
        @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(authorization, request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id()))
            .body(response);
    }

}
