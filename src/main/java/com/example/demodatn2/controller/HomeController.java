package com.example.demodatn2.controller;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.HomeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;
    private final DanhMucService danhMucService;
    private final CartService cartService;
    /**
     * Xử lý hiển thị trang chủ (index.html).
     * @param danhMucId ID của danh mục lọc sản phẩm (nếu có).
     * @param q Từ khóa tìm kiếm (nếu có).
     * @param page Số thứ tự trang hiện tại (mặc định là 0).
     */
    @GetMapping({"/", "/index"})
    public String home(Model model, 
                       @RequestParam(required = false) Integer danhMucId, 
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session) {
        // Đảm bảo Session được tạo để lưu trữ ID giỏ hàng/thông tin người dùng
        session.getId(); // Force session creation
        // Cập nhật số lượng sản phẩm trong giỏ hàng hiển thị ở Badge trên Header
        if (session.getAttribute("CART_COUNT") == null) {
            session.setAttribute("CART_COUNT", cartService.getItemCount(session));
        }

        // 1. Lấy danh sách sản phẩm (phân trang)
        int pageSize = 12;
        PageRequest pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<HomeProductVM> productPage = homeService.getHomeProductsPage(danhMucId, q, pageable);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedDanhMucId", danhMucId);
        model.addAttribute("query", q);

        // 2. Lấy TẤT CẢ danh mục ACTIVE - QUAN TRỌNG: phải khai báo biến này
        List<DanhMuc> allCategories = danhMucService.getActive();
        model.addAttribute("categories", allCategories);

        // 3. Lọc ra danh mục CHA (không có parent)
        List<DanhMuc> parentCategories = allCategories.stream()
                .filter(dm -> dm.getDanhMucCha() == null)
                .collect(Collectors.toList());
        model.addAttribute("parentCategories", parentCategories);

        Map<Integer, List<HomeProductVM>> categoryPreviews = parentCategories.stream()
            .collect(Collectors.toMap(
                DanhMuc::getId,
                parent -> homeService.getHomeProducts(parent.getId(), null)
                    .stream()
                    .limit(3)
                    .collect(Collectors.toList())
            ));
        model.addAttribute("categoryPreviews", categoryPreviews);

        // 4. Tạo Map: key = ID danh mục cha, value = List danh mục con
        Map<Integer, List<DanhMuc>> childrenMap = allCategories.stream()
                .filter(dm -> dm.getDanhMucCha() != null)
                .collect(Collectors.groupingBy(dm -> dm.getDanhMucCha().getId()));
        model.addAttribute("childrenMap", childrenMap);

        return "index"; // templates/index.html
    }

    @GetMapping("/chinh-sach-doi-tra")
    public String returnPolicy(Model model, HttpSession session) {
        session.getId(); // Force session creation
        if (session.getAttribute("CART_COUNT") == null) {
            session.setAttribute("CART_COUNT", cartService.getItemCount(session));
        }

        List<DanhMuc> allCategories = danhMucService.getActive();
        model.addAttribute("categories", allCategories);

        List<DanhMuc> parentCategories = allCategories.stream()
                .filter(dm -> dm.getDanhMucCha() == null)
                .collect(Collectors.toList());

        Map<Integer, List<HomeProductVM>> categoryPreviews = parentCategories.stream()
                .collect(Collectors.toMap(
                        DanhMuc::getId,
                        parent -> homeService.getHomeProducts(parent.getId(), null)
                                .stream()
                                .limit(3)
                                .collect(Collectors.toList())
                ));
        model.addAttribute("categoryPreviews", categoryPreviews);

        return "Chinhsachdoitra";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Integer id, Model model, HttpSession session) {
        session.getId(); // Force session creation
        // Cập nhật số lượng giỏ hàng nếu chưa có
        if (session.getAttribute("CART_COUNT") == null) {
            session.setAttribute("CART_COUNT", cartService.getItemCount(session));
        }

        ProductDetailVM product = homeService.getProductDetail(id);
        model.addAttribute("p", product);

        // Vẫn cần danh mục cho menu
        List<DanhMuc> allCategories = danhMucService.getActive();
        model.addAttribute("categories", allCategories);

        return "product-detail";
    }
}