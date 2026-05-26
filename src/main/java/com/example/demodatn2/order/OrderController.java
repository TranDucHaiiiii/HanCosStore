package com.example.demodatn2.order;

import com.example.demodatn2.order.dto.CancelOrderRequest;
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

    @PostMapping("/{id}/return")
    public ResponseEntity<Order> returnOrder(@PathVariable Long id, @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(orderService.returnOrder(id, request.getReason()));
    }
}
