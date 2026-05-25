package com.example.demodatn2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// Job định kỳ cập nhật trạng thái đơn hàng theo quy tắc thời gian của hệ thống.
public class OrderStatusScheduler {

    private final OrderService orderService;

    @Value("${app.order.pending-transfer-timeout-minutes:30}")
    private int pendingTransferTimeoutMinutes;

    @Scheduled(cron = "0 0 2 * * *")
    public void autoCompleteDeliveredOrders() {
        orderService.autoCompleteDeliveredOrders(7);
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void cleanupExpiredPendingTransferOrders() {
        orderService.cleanupExpiredPendingTransferOrders(pendingTransferTimeoutMinutes);
    }
}
