package com.example.demodatn2.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("orderManagementService")
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + id));
    }

    @Transactional
    public Order confirmOrder(Long id) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.CHO_XAC_NHAN);
        order.setTrangThai(OrderStatus.DA_XAC_NHAN);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markShipping(Long id) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.DA_XAC_NHAN);
        order.setTrangThai(OrderStatus.DANG_GIAO);
        return orderRepository.save(order);
    }

    @Transactional
    public Order completeOrder(Long id) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.DANG_GIAO);
        if (!order.isDaDieuChinhTonKho()) {
            decreaseStock(order.getItems());
            order.setDaDieuChinhTonKho(true);
        }
        order.setTrangThai(OrderStatus.HOAN_THANH);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long id, String reason) {
        Order order = getById(id);
        ensureStatus(order, List.of(
                OrderStatus.CHO_XAC_NHAN,
                OrderStatus.DA_XAC_NHAN,
                OrderStatus.DANG_GIAO,
                OrderStatus.LOI_VAN_CHUYEN
        ));
        if (order.isDaDieuChinhTonKho()) {
            increaseStock(order.getItems());
            order.setDaDieuChinhTonKho(false);
        }
        order.setLyDoHuy(reason);
        order.setTrangThai(OrderStatus.DA_HUY);
        return orderRepository.save(order);
    }

    @Transactional
    public Order markShippingError(Long id, String reason) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.DANG_GIAO);
        order.setLyDoLoiVanChuyen(reason);
        order.setTrangThai(OrderStatus.LOI_VAN_CHUYEN);
        return orderRepository.save(order);
    }

    @Transactional
    public Order adminReship(Long id) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.LOI_VAN_CHUYEN);
        order.setLyDoLoiVanChuyen(null);
        order.setTrangThai(OrderStatus.DANG_GIAO);
        return orderRepository.save(order);
    }

    @Transactional
    public Order adminRefund(Long id, String reason) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.LOI_VAN_CHUYEN);
        if (order.isDaDieuChinhTonKho()) {
            increaseStock(order.getItems());
            order.setDaDieuChinhTonKho(false);
        }
        order.setLyDoHuy(reason != null && !reason.isBlank() ? reason : "Hoàn tiền do lỗi vận chuyển");
        order.setTrangThai(OrderStatus.DA_HUY);
        return orderRepository.save(order);
    }

    @Transactional
    public Order returnOrder(Long id, String reason) {
        Order order = getById(id);
        ensureStatus(order, OrderStatus.HOAN_THANH);
        if (order.isDaDieuChinhTonKho()) {
            increaseStock(order.getItems());
            order.setDaDieuChinhTonKho(false);
        }
        order.setLyDoHuy(reason);
        order.setTrangThai(OrderStatus.TRA_HANG);
        return orderRepository.save(order);
    }

    private void ensureStatus(Order order, OrderStatus expected) {
        if (order.getTrangThai() != expected) {
            throw new IllegalStateException("Trạng thái không hợp lệ: " + order.getTrangThai());
        }
    }

    private void ensureStatus(Order order, List<OrderStatus> allowed) {
        if (!allowed.contains(order.getTrangThai())) {
            throw new IllegalStateException("Trạng thái không hợp lệ: " + order.getTrangThai());
        }
    }

    private void decreaseStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = item.getProduct();
            int newQty = product.getSoLuongTon() - item.getSoLuong();
            if (newQty < 0) {
                throw new IllegalStateException("Không đủ tồn kho cho sản phẩm: " + product.getTen());
            }
            product.setSoLuongTon(newQty);
            productRepository.save(product);
        }
    }

    private void increaseStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setSoLuongTon(product.getSoLuongTon() + item.getSoLuong());
            productRepository.save(product);
        }
    }
}
