package com.example.demodatn2.controller;

import com.example.demodatn2.dto.HomeProductVM;
import com.example.demodatn2.dto.ProductDetailVM;
import com.example.demodatn2.entity.DanhMuc;
import com.example.demodatn2.service.CartService;
import com.example.demodatn2.service.DanhMucService;
import com.example.demodatn2.service.HomeService;
import com.example.demodatn2.service.ProductReviewService;
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

    private static final int HOME_PAGE_SIZE = 8;
    private static final int CATEGORY_PREVIEW_LIMIT = 3;
    private static final String CART_COUNT = "CART_COUNT";

    private final HomeService homeService;
    private final DanhMucService danhMucService;
    private final CartService cartService;
    private final ProductReviewService productReviewService;

    @GetMapping({"/", "/index"})
    public String home(Model model,
                       @RequestParam(required = false) Integer danhMucId,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session) {
        ensureCartCount(session);

        if (page < 1) {
            page = 1;
        }

        PageRequest pageable = PageRequest.of(page - 1, HOME_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "ngayTao"));
        Page<HomeProductVM> productPage = homeService.getHomeProductsPage(danhMucId, q, pageable);

        int totalPages = productPage.getTotalPages();
        int currentPage = productPage.getNumber() + 1;
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);

        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("selectedDanhMucId", danhMucId);
        model.addAttribute("query", q);
        addCategoryMenu(model, true);

        return "index";
    }

    @GetMapping("/chinh-sach-doi-tra")
    public String returnPolicy(Model model, HttpSession session) {
        ensureCartCount(session);
        addCategoryMenu(model, true);

        return "Chinhsachdoitra";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Integer id, Model model, HttpSession session) {
        ensureCartCount(session);

        ProductDetailVM product = homeService.getProductDetail(id);
        model.addAttribute("p", product);
        model.addAttribute("reviews", productReviewService.getReviews(id));
        model.addAttribute("reviewCount", productReviewService.getReviewCount(id));
        model.addAttribute("averageRating", productReviewService.getAverageRating(id));
        addCategoryMenu(model, false);

        return "product-detail";
    }

    private void ensureCartCount(HttpSession session) {
        session.getId();
        if (session.getAttribute(CART_COUNT) == null) {
            session.setAttribute(CART_COUNT, cartService.getItemCount(session));
        }
    }

    private void addCategoryMenu(Model model, boolean includePreviews) {
        List<DanhMuc> categories = danhMucService.getActive();
        List<DanhMuc> parentCategories = getParentCategories(categories);

        model.addAttribute("categories", categories);
        model.addAttribute("parentCategories", parentCategories);
        model.addAttribute("childrenMap", getChildrenMap(categories));

        if (includePreviews) {
            model.addAttribute("categoryPreviews", getCategoryPreviews(parentCategories));
        }
    }

    private List<DanhMuc> getParentCategories(List<DanhMuc> categories) {
        return categories.stream()
                .filter(category -> category.getDanhMucCha() == null)
                .toList();
    }
    

    private Map<Integer, List<DanhMuc>> getChildrenMap(List<DanhMuc> categories) {
        return categories.stream()
                .filter(category -> category.getDanhMucCha() != null)
                .collect(Collectors.groupingBy(category -> category.getDanhMucCha().getId()));
    }



    private Map<Integer, List<HomeProductVM>> getCategoryPreviews(List<DanhMuc> parentCategories) {
        return parentCategories.stream()
                .collect(Collectors.toMap(
                        DanhMuc::getId,
                parent -> homeService
                    .getHomeProductsPage(parent.getId(), null, PageRequest.of(0, CATEGORY_PREVIEW_LIMIT))
                    .getContent()
                ));
    }
}
