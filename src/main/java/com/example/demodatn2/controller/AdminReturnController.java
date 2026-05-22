package com.example.demodatn2.controller;

import com.example.demodatn2.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController {
    private final ReturnRequestService returnRequestService;
    private final com.example.demodatn2.repository.ChiTietDoiTraRepository chiTietDoiTraRepository;
    private final com.example.demodatn2.repository.HinhAnhDoiTraRepository hinhAnhDoiTraRepository;
    private final com.example.demodatn2.repository.LichSuXuLyDoiTraRepository lichSuXuLyDoiTraRepository;
    private final com.example.demodatn2.repository.RefundTransactionRepository refundTransactionRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "ALL") String status, Model model) {
        model.addAttribute("requests", returnRequestService.findAll(status));
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", java.util.List.of("ALL", "CHO_DUYET", "DA_DUYET", "TU_CHOI", "CHO_KIEM_DINH", "DA_HOAN_TIEN", "HOAN_TAT"));
        return "admin/returns";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var request = returnRequestService.getById(id);
        var details = withDeadlockRetry(() -> chiTietDoiTraRepository.findByYeuCauDoiTraId(id));
        var nextStatuses = new java.util.ArrayList<>(returnRequestService.getNextValidStatuses(request.getTrangThai()));
        boolean hasPendingInspection = details.stream()
                .anyMatch(detail -> "PENDING_INSPECTION".equals(detail.getInspectionStatus()));
        if (hasPendingInspection) {
            nextStatuses.remove("DA_HOAN_TIEN");
        }
        model.addAttribute("request", request);
        model.addAttribute("details", details);
        model.addAttribute("images", withDeadlockRetry(() -> hinhAnhDoiTraRepository.findByYeuCauDoiTraIdOrderByIdAsc(id)));
        model.addAttribute("histories", withDeadlockRetry(() -> lichSuXuLyDoiTraRepository.findByYeuCauDoiTraIdOrderByThoiGianAsc(id)));
        model.addAttribute("refundTransactions", withDeadlockRetry(() -> refundTransactionRepository.findByYeuCauDoiTraIdOrderByThoiGianHoanDesc(id)));
        model.addAttribute("maxRefundAmount", returnRequestService.calculateMaxRefundAmount(id));
        model.addAttribute("nextStatuses", nextStatuses);
        return "admin/return-detail";
    }

    private <T> T withDeadlockRetry(java.util.function.Supplier<T> action) {
        RuntimeException last = null;
        for (int i = 0; i < 3; i++) {
            try {
                return action.get();
            } catch (CannotAcquireLockException | JpaSystemException e) {
                last = e;
                if (!isDeadlock(e) || i == 2) {
                    throw e;
                }
                try {
                    Thread.sleep(80L * (i + 1));
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }

    private boolean isDeadlock(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("deadlocked")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
