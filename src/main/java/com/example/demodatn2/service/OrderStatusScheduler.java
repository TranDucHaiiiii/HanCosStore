package com.example.demodatn2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// Job định kỳ cập nhật trạng thái đơn hàng theo quy tắc thời gian của hệ thống.
public class OrderStatusScheduler {

    private final OrderService orderService;

    @Scheduled(cron = "0 0 2 * * *")
    public void autoCompleteDeliveredOrders() {
        orderService.autoCompleteDeliveredOrders(7);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void cleanupExpiredPendingPosOrders() {
        orderService.cleanupExpiredPendingPosOrders(30);
    }
}
