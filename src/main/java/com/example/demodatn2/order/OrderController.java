package com.example.demodatn2.order;

import com.example.demodatn2.order.dto.CancelOrderRequest;
import com.example.demodatn2.order.dto.RefundRequest;
import com.example.demodatn2.order.dto.ShippingErrorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("orderApiController")
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Order> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @PostMapping("/{id}/shipping")
    public ResponseEntity<Order> markShipping(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markShipping(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Order> complete(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.completeOrder(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancel(@PathVariable Long id, @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(id, request.getReason()));
    }

    @PostMapping("/{id}/shipping-error")
    public ResponseEntity<Order> shippingError(@PathVariable Long id, @RequestBody ShippingErrorRequest request) {
        return ResponseEntity.ok(orderService.markShippingError(id, request.getReason()));
    }

    @PostMapping("/{id}/shipping-error/reship")
    public ResponseEntity<Order> reship(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.adminReship(id));
    }

    @PostMapping("/{id}/shipping-error/refund")
    public ResponseEntity<Order> refund(@PathVariable Long id, @RequestBody RefundRequest request) {
        return ResponseEntity.ok(orderService.adminRefund(id, request.getReason()));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Order> returnOrder(@PathVariable Long id, @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.returnOrder(id, request.getReason()));
    }
}
