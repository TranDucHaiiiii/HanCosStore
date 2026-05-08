package com.example.demodatn2.dto;

import lombok.Data;
import java.util.List;

@Data
public class GenerateQuickVariantsRequest {
    private List<String> colors;        // ["Đen", "Trắng", ...]
    private List<String> sizes;         // ["XS", "S", "M", ...]
    private Integer stock;              // số lượng tồn mặc định
    private Integer price;              // giá bán mặc định
    private Integer weight;             // khối lượng (gram)
    private String mode;                // "replace_all" hoặc "add_missing"
    private String maSanPham;           // mã sản phẩm để sinh SKU
}
