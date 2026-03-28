package com.example.demodatn2.controller;

import com.example.demodatn2.dto.PaymentReturnResultDTO;
import com.example.demodatn2.service.VnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params,
                              RedirectAttributes redirectAttributes) {
        PaymentReturnResultDTO result = vnPayService.processReturn(params);

        if (result.getOrderId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
            return "redirect:/order/checkout";
        }

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }

        return "redirect:/order/success?id=" + result.getOrderId();
    }
}
