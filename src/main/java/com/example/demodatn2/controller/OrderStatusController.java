package com.example.demodatn2.controller;

import com.example.demodatn2.dto.OrderStatusResponse;
import com.example.demodatn2.service.SepayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderStatusController {
    private final SepayService sepayService;

    @GetMapping("/status/{code}")
    public ResponseEntity<OrderStatusResponse> getStatus(@PathVariable String code) {
        try {
            return ResponseEntity.ok(sepayService.getOrderStatus(code));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
