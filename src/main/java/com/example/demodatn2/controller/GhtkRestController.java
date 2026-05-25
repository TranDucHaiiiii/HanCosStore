package com.example.demodatn2.controller;

import com.example.demodatn2.dto.CartItemDTO;
import com.example.demodatn2.dto.GhtkFeeRequest;
import com.example.demodatn2.dto.GhtkFeeResponse;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.DiaChiGiaoHang;
import com.example.demodatn2.repository.DiaChiGiaoHangRepository;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DvhcvnLocationService;
import com.example.demodatn2.service.GhtkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ghtk")
@RequiredArgsConstructor
public class GhtkRestController {

    private final GhtkService ghtkService;
    private final CartService cartService;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;
    private final DvhcvnLocationService locationService;

    @PostMapping("/fee")
    public ResponseEntity<?> calculateFee(@RequestParam(required = false) Integer addressId,
                                          @RequestParam(required = false) String province,
                                          @RequestParam(required = false) String district,
                                          @RequestParam(required = false) String ward,
                                          @RequestParam(required = false) String address,
                                          @RequestParam(required = false) String transport,
                                          HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Ban can dang nhap de tu dong tinh phi giao hang."));
        }

        DiaChiGiaoHang addressEntity = null;
        if (addressId != null) {
            addressEntity = resolveAddress(loginUser.getId(), addressId);
        }
        if (addressEntity == null && (province == null || district == null || ward == null)) {
            addressEntity = resolveAddress(loginUser.getId(), null);
        }
        if (addressEntity == null && (province == null || district == null || ward == null)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Chua co dia chi giao hang trong tai khoan."));
        }

        int totalWeight = cartService.getSelectedTotalWeightGram(session);
        if (totalWeight <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Vui long chon san pham de tinh phi giao hang."));
        }

        List<CartItemDTO> items = cartService.getSelectedCartItems(session);
        BigDecimal totalAmount = cartService.getTotalAmount(items);
        Integer value = toIntegerValue(totalAmount);

        DvhcvnLocationService.LocationNames names;
        try {
            if (addressEntity != null) {
                names = locationService.resolveFromNames(
                        addressEntity.getTinhThanh(),
                        addressEntity.getQuanHuyen(),
                        addressEntity.getPhuongXa()
                );
                address = addressEntity.getDiaChiChiTiet();
            } else {
                names = locationService.resolveFromNames(province, district, ward);
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }

        GhtkFeeRequest request = new GhtkFeeRequest();
        request.setProvince(names.province());
        request.setDistrict(names.district());
        request.setWard(names.ward());
        request.setAddress(address);
        request.setWeight(totalWeight);
        request.setValue(value);
        request.setTransport(transport);

        GhtkFeeResponse response;
        try {
            response = ghtkService.calculateFeeWithDefaultPick(request);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", "Khong the tinh phi. Vui long thu lai."));
        }

        if (response != null && response.getFee() != null) {
            Integer fee = response.getFee().getFee();
            if (fee != null) {
                session.setAttribute("CURRENT_SHIPPING_FEE", BigDecimal.valueOf(fee.longValue()));
            }
        }
        return ResponseEntity.ok(response);
    }

    private DiaChiGiaoHang resolveAddress(Integer accountId, Integer addressId) {
        if (accountId == null) {
            return null;
        }
        if (addressId != null) {
            return diaChiGiaoHangRepository.findById(addressId)
                    .filter(a -> a.getTaiKhoan() != null && accountId.equals(a.getTaiKhoan().getId()))
                    .orElse(null);
        }
        List<DiaChiGiaoHang> addresses =
                diaChiGiaoHangRepository.findByTaiKhoanIdOrderByLaMacDinhDescNgayTaoDesc(accountId);
        return addresses.isEmpty() ? null : addresses.getFirst();
    }

    private Integer toIntegerValue(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return null;
        }
        try {
            return totalAmount.setScale(0, RoundingMode.HALF_UP).intValueExact();
        } catch (ArithmeticException ex) {
            return totalAmount.intValue();
        }
    }
}
