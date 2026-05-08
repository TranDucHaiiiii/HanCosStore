package com.example.demodatn2.service;

import com.example.demodatn2.dto.PosCartItemDTO;
import com.example.demodatn2.dto.PosInvoiceSummaryDTO;
import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.HinhAnhMauSac;
import com.example.demodatn2.entity.HinhAnhSanPham;
import com.example.demodatn2.entity.SanPham;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PosCartService {

    private static final String SESSION_CARTS = "POS_CARTS";
    private static final String SESSION_ACTIVE = "POS_ACTIVE_INVOICE";
    private static final String SESSION_COUNTER = "POS_INVOICE_COUNTER";
    private static final String SESSION_TRANSFER_REFS = "POS_TRANSFER_REFS";
    private static final String SESSION_INVOICE_CREATED_AT = "POS_INVOICE_CREATED_AT";
    private static final long INVOICE_TTL_MINUTES = 30;

    private final BienTheSanPhamRepository bienTheSanPhamRepository;

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> getCart(HttpSession session) {
        return new ArrayList<>(getMutableCart(session));
    }

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> addItem(HttpSession session, Integer variantId, Integer qty) {
        if (variantId == null) {
            throw new RuntimeException("variantId không hợp lệ");
        }
        int quantity = qty == null ? 1 : qty;
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        BienTheSanPham variant = bienTheSanPhamRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + variantId));

        if (variant.getSoLuongTon() == null || variant.getSoLuongTon() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        List<PosCartItemDTO> cart = getMutableCart(session);
        PosCartItemDTO existing = cart.stream()
                .filter(item -> Objects.equals(item.getVariantId(), variantId))
                .findFirst()
                .orElse(null);

        int currentQty = existing != null && existing.getQty() != null ? existing.getQty() : 0;
        int nextQty = currentQty + quantity;
        if (nextQty > variant.getSoLuongTon()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        if (existing != null) {
            existing.setQty(nextQty);
            existing.setStock(variant.getSoLuongTon());
            existing.setPrice(variant.getGia());
        } else {
            cart.add(toPosCartItem(variant, quantity));
        }

        return new ArrayList<>(cart);
    }

    @Transactional(readOnly = true)
    public List<PosCartItemDTO> updateQty(HttpSession session, Integer variantId, Integer qty) {
        if (variantId == null) {
            throw new RuntimeException("variantId không hợp lệ");
        }
        if (qty == null || qty <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        BienTheSanPham variant = bienTheSanPhamRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + variantId));

        if (qty > variant.getSoLuongTon()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        List<PosCartItemDTO> cart = getMutableCart(session);
        PosCartItemDTO existing = cart.stream()
                .filter(item -> Objects.equals(item.getVariantId(), variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong giỏ"));

        existing.setQty(qty);
        existing.setStock(variant.getSoLuongTon());
        existing.setPrice(variant.getGia());
        return new ArrayList<>(cart);
    }

    public List<PosCartItemDTO> removeItem(HttpSession session, Integer variantId) {
        List<PosCartItemDTO> cart = getMutableCart(session);
        cart.removeIf(item -> Objects.equals(item.getVariantId(), variantId));
        return new ArrayList<>(cart);
    }

    public void clear(HttpSession session) {
        String activeId = getActiveInvoiceId(session);
        getMutableCart(session).clear();
        getTransferRefs(session).remove(activeId);
    }

    public String ensureTransferReference(HttpSession session) {
        String activeId = getActiveInvoiceId(session);
        return getTransferRefs(session).computeIfAbsent(activeId, key -> generateTransferReference(session));
    }

    // ─── Invoice management ───────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, List<PosCartItemDTO>> getAllCarts(HttpSession session) {
        Object raw = session.getAttribute(SESSION_CARTS);
        if (raw instanceof LinkedHashMap) {
            return (LinkedHashMap<String, List<PosCartItemDTO>>) raw;
        }
        LinkedHashMap<String, List<PosCartItemDTO>> carts = new LinkedHashMap<>();
        session.setAttribute(SESSION_CARTS, carts);
        return carts;
    }

    public String getActiveInvoiceId(HttpSession session) {
        purgeExpiredInvoices(session);
        String activeId = (String) session.getAttribute(SESSION_ACTIVE);
        if (activeId == null || !getAllCarts(session).containsKey(activeId)) {
            activeId = createInvoice(session);
        }
        return activeId;
    }

    public String createInvoice(HttpSession session) {
        purgeExpiredInvoices(session);
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        
        // Giới hạn tối đa 5 hóa đơn
        if (carts.size() >= 5) {
            throw new RuntimeException("Tối đa 5 hóa đơn. Vui lòng thanh toán hoặc xóa hóa đơn cũ.");
        }
        
        Integer counter = (Integer) session.getAttribute(SESSION_COUNTER);
        if (counter == null) counter = 0;
        counter++;
        session.setAttribute(SESSION_COUNTER, counter);
        String invoiceId = "HD" + counter;
        carts.put(invoiceId, new ArrayList<>());
        getInvoiceCreatedAt(session).put(invoiceId, java.time.Instant.now());
        session.setAttribute(SESSION_ACTIVE, invoiceId);
        return invoiceId;
    }

    public String switchInvoice(HttpSession session, String invoiceId) {
        purgeExpiredInvoices(session);
        if (!getAllCarts(session).containsKey(invoiceId)) {
            throw new RuntimeException("Hóa đơn không tồn tại: " + invoiceId);
        }
        session.setAttribute(SESSION_ACTIVE, invoiceId);
        return invoiceId;
    }

    public String deleteInvoice(HttpSession session, String invoiceId) {
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        carts.remove(invoiceId);
        getInvoiceCreatedAt(session).remove(invoiceId);
        getTransferRefs(session).remove(invoiceId);
        String activeId = (String) session.getAttribute(SESSION_ACTIVE);
        if (invoiceId.equals(activeId)) {
            if (!carts.isEmpty()) {
                activeId = carts.keySet().iterator().next();
            } else {
                activeId = createInvoice(session);
            }
            session.setAttribute(SESSION_ACTIVE, activeId);
        }
        return (String) session.getAttribute(SESSION_ACTIVE);
    }

    public List<PosInvoiceSummaryDTO> listInvoices(HttpSession session) {
        purgeExpiredInvoices(session);
        String activeId = getActiveInvoiceId(session);
        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        List<PosInvoiceSummaryDTO> result = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, List<PosCartItemDTO>> entry : carts.entrySet()) {
            List<PosCartItemDTO> items = entry.getValue();
            int itemCount = items.stream().mapToInt(i -> i.getQty() != null ? i.getQty() : 0).sum();
            double total = items.stream().mapToDouble(i -> {
                double price = i.getPrice() != null ? i.getPrice().doubleValue() : 0;
                int qty = i.getQty() != null ? i.getQty() : 0;
                return price * qty;
            }).sum();
            result.add(new PosInvoiceSummaryDTO(
                    entry.getKey(), "HĐ " + index, itemCount, total, entry.getKey().equals(activeId)));
            index++;
        }
        return result;
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<PosCartItemDTO> getMutableCart(HttpSession session) {
        String activeId = getActiveInvoiceId(session);
        return getAllCarts(session).computeIfAbsent(activeId, k -> new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getTransferRefs(HttpSession session) {
        Object raw = session.getAttribute(SESSION_TRANSFER_REFS);
        if (raw instanceof Map<?, ?> refs) {
            return (Map<String, String>) refs;
        }
        Map<String, String> refs = new HashMap<>();
        session.setAttribute(SESSION_TRANSFER_REFS, refs);
        return refs;
    }

    @SuppressWarnings("unchecked")
    private Map<String, java.time.Instant> getInvoiceCreatedAt(HttpSession session) {
        Object raw = session.getAttribute(SESSION_INVOICE_CREATED_AT);
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, java.time.Instant>) map;
        }
        Map<String, java.time.Instant> createdAt = new HashMap<>();
        session.setAttribute(SESSION_INVOICE_CREATED_AT, createdAt);
        return createdAt;
    }

    private void purgeExpiredInvoices(HttpSession session) {
        Map<String, java.time.Instant> createdAt = getInvoiceCreatedAt(session);
        if (createdAt.isEmpty()) {
            return;
        }

        java.time.Instant cutoff = java.time.Instant.now().minus(java.time.Duration.ofMinutes(INVOICE_TTL_MINUTES));
        List<String> expiredIds = createdAt.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .toList();

        if (expiredIds.isEmpty()) {
            return;
        }

        LinkedHashMap<String, List<PosCartItemDTO>> carts = getAllCarts(session);
        String activeId = (String) session.getAttribute(SESSION_ACTIVE);
        expiredIds.forEach(invoiceId -> {
            carts.remove(invoiceId);
            createdAt.remove(invoiceId);
            getTransferRefs(session).remove(invoiceId);
        });

        if (activeId != null && expiredIds.contains(activeId)) {
            if (!carts.isEmpty()) {
                session.setAttribute(SESSION_ACTIVE, carts.keySet().iterator().next());
            } else {
                session.removeAttribute(SESSION_ACTIVE);
            }
        }
    }

    private String generateTransferReference(HttpSession session) {
        while (true) {
            String candidate = "DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (!isTransferReferenceUsed(session, candidate)) {
                return candidate;
            }
        }
    }

    private boolean isTransferReferenceUsed(HttpSession session, String code) {
        if (code == null || code.isBlank()) {
            return true;
        }
        if (getTransferRefs(session).containsValue(code)) {
            return true;
        }
        return false;
    }

    private PosCartItemDTO toPosCartItem(BienTheSanPham variant, int qty) {
        return PosCartItemDTO.builder()
                .variantId(variant.getId())
                .productName(variant.getSanPham().getTen())
                .color(variant.getMauSac())
                .size(variant.getKichCo())
                .price(variant.getGia())
                .qty(qty)
                .stock(variant.getSoLuongTon())
                .img(resolveImage(variant.getSanPham(), variant.getMauSac()))
                .build();
    }

    private String resolveImage(SanPham sanPham, String color) {
        List<HinhAnhMauSac> colorImages = sanPham.getHinhAnhMauSacs();
        if (colorImages != null) {
            String byColor = colorImages.stream()
                    .filter(img -> img.getMauSac() != null && img.getMauSac().equalsIgnoreCase(color))
                    .map(HinhAnhMauSac::getDuongDanAnh)
                    .filter(path -> path != null && !path.isBlank())
                    .findFirst()
                    .orElse(null);
            if (byColor != null) {
                return byColor;
            }
        }

        List<HinhAnhSanPham> productImages = sanPham.getHinhAnhSanPhams();
        if (productImages == null || productImages.isEmpty()) {
            return null;
        }

        return productImages.stream()
                .sorted(Comparator
                        .comparing((HinhAnhSanPham img) -> !Boolean.TRUE.equals(img.getLaAnhChinh()))
                        .thenComparing(HinhAnhSanPham::getThuTu, Comparator.nullsLast(Integer::compareTo)))
                .map(HinhAnhSanPham::getDuongDanAnh)
                .filter(path -> path != null && !path.isBlank())
                .findFirst()
                .orElse(null);
    }
}
