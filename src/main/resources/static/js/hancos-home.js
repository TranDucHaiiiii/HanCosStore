document.addEventListener("DOMContentLoaded", () => {
    // normalize: bỏ dấu + lower + trim
    const normalize = (s) =>
        (s ?? "")
            .toString()
            .trim()
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");

    // Ánh xạ tên màu (VN/biến thể) -> class CSS
    const colorMap = new Map([
        ["den",          "black"],
        ["đen",          "black"],
        ["trang",        "white"],
        ["xam",          "gray"],
        ["xam tro",      "gray"],
        ["be",           "beige"],
        ["beige",        "beige"],
        ["kem",          "cream"],
        ["nau",          "brown"],
        ["nau dat",      "brown"],
        ["xanh navy",    "navy"],
        ["navy",         "navy"],
        ["xanh lam",     "blue"],
        ["xanh duong",   "blue"],
        ["xanh la",      "green"],
        ["xanh la cay",  "green"],
        ["xanh",         "navy"],
        ["teal",         "teal"],
        ["olive",        "olive"],
        ["do",           "red"],
        ["do tuoi",      "red"],
        ["hong",         "pink"],
        ["hong pastel",  "pink"],
        ["tim",          "purple"],
        ["vang",         "yellow"],
        ["vang dong",    "yellow"],
        ["cam",          "orange"],
    ]);

    const getCssColorName = (name) => {
        const key = normalize(name);
        if (colorMap.has(key)) return colorMap.get(key);
        for (const [k, v] of colorMap.entries()) {
            if (key.startsWith(k)) return v;
        }
        return "gray";
    };

    // Update color swatches
    document.querySelectorAll(".color-swatch").forEach((swatch) => {
        const colorName = swatch.dataset.colorName || swatch.getAttribute("data-color-name") || "";
        const key = normalize(colorName);

        const cssColor = getCssColorName(colorName);

        // reset class về đúng 2 class cần thiết
        swatch.className = `color-swatch color-${cssColor}`;
    });

    // Filter tabs (hiện tại chỉ set active + log)
    const filterBtns = document.querySelectorAll(".filter-btn");
    filterBtns.forEach((btn) => {
        btn.addEventListener("click", () => {
            filterBtns.forEach((b) => b.classList.remove("active"));
            btn.classList.add("active");

            const filter = btn.getAttribute("data-filter");
            console.log("Filter by:", filter);
            // Thêm logic filter nếu cần
        });
    });

    const productCards = Array.from(document.querySelectorAll(".product-card"));
    const colorSelectEl = document.getElementById("colorFilterSelect");
    const brandSelectEl = document.getElementById("brandFilterSelect");
    const sizeSelectEl = document.getElementById("sizeFilterSelect");
    const priceSelectEl = document.getElementById("priceFilterSelect");
    const resetBtn = document.getElementById("resetFilters");
    const filterEmptyState = document.getElementById("filterEmptyState");
    const paginationWrapEl = document.querySelector(".home-pagination-wrap");

    const splitValues = (value) =>
        (value || "")
            .split(",")
            .map((v) => v.trim())
            .filter(Boolean);

    const toNumber = (value) => {
        const num = parseFloat(value);
        return Number.isFinite(num) ? num : null;
    };

    const uniqueFromProducts = (attr) => {
        const set = new Map();
        productCards.forEach((card) => {
            splitValues(card.getAttribute(attr)).forEach((val) => {
                const key = normalize(val);
                if (!set.has(key)) {
                    set.set(key, val);
                }
            });
        });
        return Array.from(set.values());
    };

    const renderSelectOptions = (selectEl, values) => {
        if (!selectEl) return;
        selectEl.querySelectorAll("option:not(:first-child)").forEach((opt) => opt.remove());
        values.forEach((val) => {
            const option = document.createElement("option");
            option.value = val;
            option.textContent = val;
            selectEl.appendChild(option);
        });
    };

    const parsePriceRange = (value) => {
        if (!value) return { min: null, max: null };
        const [minStr, maxStr] = value.split("-");
        const min = minStr ? toNumber(minStr) : null;
        const max = maxStr ? toNumber(maxStr) : null;
        return { min, max };
    };

    const applyFilters = () => {
        if (productCards.length === 0) {
            if (filterEmptyState) filterEmptyState.classList.add("d-none");
            return;
        }

        const selectedColor = normalize(colorSelectEl?.value || "");
        const selectedBrand = normalize(brandSelectEl?.value || "");
        const selectedSize = normalize(sizeSelectEl?.value || "");
        const { min: minPrice, max: maxPrice } = parsePriceRange(priceSelectEl?.value || "");

        const hasFilter = Boolean(selectedColor || selectedBrand || selectedSize || minPrice !== null || maxPrice !== null);

        let visibleCount = 0;
        productCards.forEach((card) => {
            const colors = splitValues(card.getAttribute("data-colors")).map(normalize);
            const brands = splitValues(card.getAttribute("data-brands")).map(normalize);
            const sizes = splitValues(card.getAttribute("data-sizes")).map(normalize);
            const cardMin = toNumber(card.getAttribute("data-min-price")) ?? 0;
            const cardMax = toNumber(card.getAttribute("data-max-price")) ?? cardMin;

            const matchColor = !selectedColor || colors.includes(selectedColor);
            const matchBrand = !selectedBrand || brands.includes(selectedBrand);
            const matchSize = !selectedSize || sizes.includes(selectedSize);

            let matchPrice = true;
            if (minPrice !== null || maxPrice !== null) {
                const min = minPrice ?? 0;
                const max = maxPrice ?? Number.MAX_SAFE_INTEGER;
                matchPrice = cardMax >= min && cardMin <= max;
            }

            const isMatch = matchColor && matchBrand && matchSize && matchPrice;
            card.style.display = isMatch ? "" : "none";
            if (isMatch) visibleCount++;
        });

        if (filterEmptyState) {
            filterEmptyState.classList.toggle("d-none", visibleCount > 0);
        }
        if (paginationWrapEl) {
            paginationWrapEl.classList.toggle("d-none", hasFilter && visibleCount === 0);
        }
    };

    const resetFilters = () => {
        if (colorSelectEl) colorSelectEl.value = "";
        if (brandSelectEl) brandSelectEl.value = "";
        if (sizeSelectEl) sizeSelectEl.value = "";
        if (priceSelectEl) priceSelectEl.value = "";
        applyFilters();
    };

    const initFilters = () => {
        const colors = uniqueFromProducts("data-colors").sort((a, b) => a.localeCompare(b, "vi"));
        const brands = uniqueFromProducts("data-brands").sort((a, b) => a.localeCompare(b, "vi"));
        const sizes = uniqueFromProducts("data-sizes").sort((a, b) => a.localeCompare(b, "vi"));
        renderSelectOptions(colorSelectEl, colors);
        renderSelectOptions(brandSelectEl, brands);
        renderSelectOptions(sizeSelectEl, sizes);

        colorSelectEl?.addEventListener("change", applyFilters);
        brandSelectEl?.addEventListener("change", applyFilters);
        sizeSelectEl?.addEventListener("change", applyFilters);
        priceSelectEl?.addEventListener("change", applyFilters);
        resetBtn?.addEventListener("click", resetFilters);
        applyFilters();
    };

    initFilters();

    const quickBuyModalEl = document.getElementById("quickBuyModal");
    const quickBuyImageEl = document.getElementById("quickBuyImage");
    const quickBuyProductNameEl = document.getElementById("quickBuyProductName");
    const quickBuyProductPriceEl = document.getElementById("quickBuyProductPrice");
    const quickBuyColorEl = document.getElementById("quickBuyColor");
    const quickBuyColorOptionsEl = document.getElementById("quickBuyColorOptions");
    const quickBuyColorNameEl = document.getElementById("quickBuyColorName");
    const quickBuySizeEl = document.getElementById("quickBuySize");
    const quickBuySizeOptionsEl = document.getElementById("quickBuySizeOptions");
    const quickBuySizeNameEl = document.getElementById("quickBuySizeName");
    const quickBuyQtyEl = document.getElementById("quickBuyQty");
    const quickBuyQtyMinusEl = document.getElementById("quickBuyQtyMinus");
    const quickBuyQtyPlusEl = document.getElementById("quickBuyQtyPlus");
    const quickBuyStockTextEl = document.getElementById("quickBuyStockText");
    const quickBuyNoVariantHintEl = document.getElementById("quickBuyNoVariantHint");
    const quickBuyDetailLinkEl = document.getElementById("quickBuyDetailLink");
    const quickBuyConfirmBtn = document.getElementById("quickBuyConfirmBtn");
    const quickBuyAlertEl = document.getElementById("quickBuyAlert");
    const quickBuyModal = quickBuyModalEl && window.bootstrap ? new bootstrap.Modal(quickBuyModalEl) : null;
    let activeVariants = [];

    const showQuickBuyError = (message) => {
        if (!quickBuyAlertEl) return;
        quickBuyAlertEl.textContent = message;
        quickBuyAlertEl.classList.remove("d-none");
    };

    const clearQuickBuyError = () => {
        if (!quickBuyAlertEl) return;
        quickBuyAlertEl.classList.add("d-none");
        quickBuyAlertEl.textContent = "";
    };

    const createDefaultOption = (text) => {
        const option = document.createElement("option");
        option.value = "";
        option.textContent = text;
        return option;
    };

    const normalizeColorKey = (value) => normalize(value).replace(/\s+/g, " ");

    const fillSelect = (selectEl, values, defaultText) => {
        if (!selectEl) return;
        selectEl.innerHTML = "";
        selectEl.appendChild(createDefaultOption(defaultText));
        values.forEach((value) => {
            const option = document.createElement("option");
            option.value = value;
            option.textContent = value;
            selectEl.appendChild(option);
        });
    };

    const getCurrentVariant = () => {
        const selectedColor = normalizeColorKey(quickBuyColorEl?.value || "");
        const selectedSize = quickBuySizeEl?.value || "";
        return activeVariants.find((variant) => normalizeColorKey(variant.color) === selectedColor && variant.size === selectedSize) || null;
    };

    const updateStockInfo = () => {
        const variant = getCurrentVariant();
        const maxQty = Math.max(variant?.stock || 1, 1);
        if (quickBuyQtyEl) {
            quickBuyQtyEl.max = String(maxQty);
            let currentQty = parseInt(quickBuyQtyEl.value || "1", 10) || 1;
            if (currentQty < 1) currentQty = 1;
            if (currentQty > maxQty) {
                quickBuyQtyEl.value = String(maxQty);
            } else {
                quickBuyQtyEl.value = String(currentQty);
            }
        }
        if (quickBuyQtyMinusEl) {
            quickBuyQtyMinusEl.disabled = !variant || (parseInt(quickBuyQtyEl?.value || "1", 10) || 1) <= 1;
        }
        if (quickBuyQtyPlusEl) {
            const currentQty = parseInt(quickBuyQtyEl?.value || "1", 10) || 1;
            quickBuyQtyPlusEl.disabled = !variant || currentQty >= maxQty;
        }
        if (quickBuyStockTextEl) {
            quickBuyStockTextEl.textContent = variant ? `Còn ${variant.stock} sản phẩm` : "Vui lòng chọn màu và size";
        }
    };

    const changeQuickBuyQty = (delta) => {
        if (!quickBuyQtyEl) return;
        const variant = getCurrentVariant();
        if (!variant) {
            showQuickBuyError("Vui lòng chọn màu và kích cỡ trước");
            return;
        }
        clearQuickBuyError();
        const maxQty = Math.max(variant.stock || 1, 1);
        const current = parseInt(quickBuyQtyEl.value || "1", 10) || 1;
        const next = Math.min(maxQty, Math.max(1, current + delta));
        quickBuyQtyEl.value = String(next);
        updateStockInfo();
    };

    const setActiveSizeOption = (sizeName) => {
        if (!quickBuySizeOptionsEl) return;
        quickBuySizeOptionsEl.querySelectorAll(".quick-buy-size-option").forEach((btn) => {
            btn.classList.toggle("active", btn.dataset.size === sizeName);
        });
    };

    const renderSizeButtons = (sizes, activeSize) => {
        if (!quickBuySizeOptionsEl) return;
        quickBuySizeOptionsEl.innerHTML = "";
        if (sizes.length === 0) {
            const hint = document.createElement("span");
            hint.className = "text-muted small";
            hint.textContent = "Chọn màu trước";
            quickBuySizeOptionsEl.appendChild(hint);
            return;
        }
        sizes.forEach((size) => {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "quick-buy-size-option";
            btn.dataset.size = size;
            btn.textContent = size;
            btn.setAttribute("aria-label", `Size ${size}`);
            if (size === activeSize) btn.classList.add("active");
            btn.addEventListener("click", () => {
                if (!quickBuySizeEl) return;
                quickBuySizeEl.value = size;
                quickBuySizeEl.dispatchEvent(new Event("change"));
            });
            quickBuySizeOptionsEl.appendChild(btn);
        });
    };

    const refreshSizeOptions = () => {
        const selectedColor = normalizeColorKey(quickBuyColorEl?.value || "");
        const availableSizes = Array.from(new Set(
            activeVariants
                .filter((variant) => !selectedColor || normalizeColorKey(variant.color) === selectedColor)
                .map((variant) => variant.size)
        ));
        const currentSize = quickBuySizeEl?.value || "";

        let newSize = "";
        if (availableSizes.length === 1) {
            newSize = availableSizes[0];
        } else if (availableSizes.includes(currentSize)) {
            newSize = currentSize;
        }

        if (quickBuySizeEl) quickBuySizeEl.value = newSize;
        renderSizeButtons(availableSizes, newSize);
        if (quickBuySizeNameEl) quickBuySizeNameEl.textContent = newSize ? `— ${newSize}` : "";
        updateStockInfo();
    };

    const parseVariantsFromCard = (cardEl) => {
        return Array.from(cardEl.querySelectorAll(".variant-item"))
            .map((item) => ({
                variantId: parseInt(item.dataset.variantId || "0", 10),
                color: (item.dataset.color || "").trim(),
                size: (item.dataset.size || "").trim(),
                stock: parseInt(item.dataset.stock || "0", 10) || 0,
                colorImage: (item.dataset.colorImage || "").trim(),
            }))
            .filter((variant) => variant.variantId > 0 && variant.color && variant.size && variant.stock > 0);
    };

    const setActiveColorOption = (colorName) => {
        if (!quickBuyColorOptionsEl) return;
        const normalizedName = normalizeColorKey(colorName);
        quickBuyColorOptionsEl.querySelectorAll(".quick-buy-color-option").forEach((button) => {
            button.classList.toggle("active", normalizeColorKey(button.dataset.color || "") === normalizedName);
        });
    };

    const renderColorOptions = () => {
        if (!quickBuyColorOptionsEl) return;
        quickBuyColorOptionsEl.innerHTML = "";

        const colorMap = new Map();
        activeVariants.forEach((variant) => {
            const normalizedColor = normalizeColorKey(variant.color);
            if (!normalizedColor || colorMap.has(normalizedColor)) {
                return;
            }
            colorMap.set(normalizedColor, {
                colorName: variant.color,
                imagePath: variant.colorImage || quickBuyImageEl?.getAttribute("src") || "/images/no-image.png",
            });
        });

        colorMap.forEach(({ imagePath, colorName }) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "quick-buy-color-option";
            button.dataset.color = colorName;
            button.title = colorName;
            button.setAttribute("aria-label", `Màu ${colorName}`);

            const img = document.createElement("img");
            img.src = imagePath || "/images/no-image.png";
            img.alt = colorName;
            button.appendChild(img);

            button.addEventListener("click", () => {
                if (!quickBuyColorEl) return;
                quickBuyColorEl.value = colorName;
                quickBuyColorEl.dispatchEvent(new Event("change"));
            });

            quickBuyColorOptionsEl.appendChild(button);
        });
    };

    const openQuickBuyModal = (cardEl, productName) => {
        activeVariants = parseVariantsFromCard(cardEl);
        clearQuickBuyError();

        if (!quickBuyModal) {
            return;
        }

        const productLink = cardEl.querySelector(".product-main-link")?.getAttribute("href") || "/";
        const productImage = cardEl.querySelector(".product-img-container img")?.getAttribute("src") || "/images/no-image.png";
        const productPrice = (cardEl.querySelector(".product-price")?.textContent || "").replace(/\s+/g, " ").trim();

        if (quickBuyImageEl) {
            quickBuyImageEl.src = productImage;
            quickBuyImageEl.alt = productName || "Sản phẩm";
        }
        if (quickBuyProductPriceEl) {
            quickBuyProductPriceEl.textContent = productPrice || "Liên hệ";
        }
        if (quickBuyDetailLinkEl) {
            quickBuyDetailLinkEl.href = productLink;
        }

        if (quickBuyProductNameEl) {
            quickBuyProductNameEl.textContent = productName || "Sản phẩm";
        }

        const colors = Array.from(
            activeVariants.reduce((map, variant) => {
                const normalizedColor = normalizeColorKey(variant.color);
                if (normalizedColor && !map.has(normalizedColor)) {
                    map.set(normalizedColor, variant.color);
                }
                return map;
            }, new Map()).values()
        );
        if (quickBuyColorNameEl) quickBuyColorNameEl.textContent = "";
        fillSelect(quickBuyColorEl, colors, "Chọn màu");
        renderColorOptions();
        setActiveColorOption("");
        renderSizeButtons([], "");
        if (quickBuySizeNameEl) quickBuySizeNameEl.textContent = "";
        if (quickBuyQtyEl) quickBuyQtyEl.value = "1";
        if (quickBuyStockTextEl) quickBuyStockTextEl.textContent = "Vui lòng chọn màu và size";

        const hasVariant = activeVariants.length > 0;
        quickBuyColorEl?.toggleAttribute("disabled", !hasVariant);
        quickBuyQtyEl?.toggleAttribute("disabled", !hasVariant);
        if (quickBuyConfirmBtn) quickBuyConfirmBtn.disabled = !hasVariant;
        if (quickBuyNoVariantHintEl) {
            quickBuyNoVariantHintEl.classList.toggle("d-none", hasVariant);
        }
        if (!hasVariant && quickBuyStockTextEl) {
            quickBuyStockTextEl.textContent = "";
        }

        if (colors.length === 1 && quickBuyColorEl) {
            quickBuyColorEl.value = colors[0];
            if (quickBuyColorNameEl) quickBuyColorNameEl.textContent = `— ${colors[0]}`;
            setActiveColorOption(colors[0]);
            refreshSizeOptions();
        } else if (hasVariant) {
            updateStockInfo();
        }

        quickBuyModal.show();
    };

    quickBuyColorEl?.addEventListener("change", () => {
        const rawColor = quickBuyColorEl.value || "";
        setActiveColorOption(rawColor);
        if (quickBuyColorNameEl) {
            quickBuyColorNameEl.textContent = rawColor ? `— ${rawColor}` : "";
        }
        const selectedColor = normalizeColorKey(rawColor);
        const selectedVariant = activeVariants.find((variant) => normalizeColorKey(variant.color) === selectedColor);
        if (quickBuyImageEl && selectedVariant?.colorImage) {
            quickBuyImageEl.src = selectedVariant.colorImage;
        }
        refreshSizeOptions();
    });

    quickBuySizeEl?.addEventListener("change", () => {
        const size = quickBuySizeEl.value || "";
        setActiveSizeOption(size);
        if (quickBuySizeNameEl) quickBuySizeNameEl.textContent = size ? `— ${size}` : "";
        updateStockInfo();
    });

    quickBuyQtyEl?.addEventListener("input", () => {
        clearQuickBuyError();
        updateStockInfo();
    });

    quickBuyQtyMinusEl?.addEventListener("click", () => {
        changeQuickBuyQty(-1);
    });

    quickBuyQtyPlusEl?.addEventListener("click", () => {
        changeQuickBuyQty(1);
    });

    quickBuyConfirmBtn?.addEventListener("click", async () => {
        clearQuickBuyError();
        const variant = getCurrentVariant();
        const qty = parseInt(quickBuyQtyEl?.value || "1", 10);

        if (!variant) {
            showQuickBuyError("Vui lòng chọn đầy đủ màu sắc và kích cỡ");
            return;
        }

        if (!Number.isFinite(qty) || qty < 1) {
            showQuickBuyError("Số lượng phải lớn hơn 0");
            return;
        }

        if (qty > variant.stock) {
            showQuickBuyError("Số lượng vượt quá tồn kho");
            return;
        }

        quickBuyConfirmBtn.disabled = true;
        const originalText = quickBuyConfirmBtn.textContent;
        quickBuyConfirmBtn.textContent = "Đang xử lý...";

        try {
            const body = new URLSearchParams({
                bienTheId: String(variant.variantId),
                soLuong: String(qty),
                checkoutNow: "true",
            });
            const response = await fetch("/cart/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
                    "X-Requested-With": "XMLHttpRequest",
                },
                body: body.toString(),
            });
            const data = await response.json();

            if (data?.requireLogin && data?.loginUrl) {
                window.location.href = data.loginUrl;
                return;
            }

            if (!response.ok || !data?.success) {
                showQuickBuyError(data?.message || "Không thể thêm sản phẩm vào giỏ hàng");
                return;
            }

            const cartBadge = document.getElementById("cart-badge");
            if (cartBadge && typeof data.count !== "undefined") {
                cartBadge.textContent = String(data.count);
                cartBadge.style.display = data.count > 0 ? "inline-block" : "none";
            }

            window.location.assign(data?.checkoutUrl || "/order/checkout");
            return;
        } catch (error) {
            showQuickBuyError("Đã có lỗi xảy ra, vui lòng thử lại");
        } finally {
            quickBuyConfirmBtn.disabled = false;
            quickBuyConfirmBtn.textContent = originalText;
        }
    });

    document.querySelectorAll(".js-buy-now").forEach((buttonEl) => {
        buttonEl.addEventListener("click", (event) => {
            const cardEl = event.currentTarget.closest(".product-card");
            if (!cardEl) return;
            const productName = event.currentTarget.dataset.productName || cardEl.querySelector(".product-name")?.textContent || "Sản phẩm";
            openQuickBuyModal(cardEl, productName.trim());
        });
    });
})
;
document.addEventListener("click", (e) => {
    const link = e.target.closest(".dropdown-submenu > a");
    if (!link) return;

    if (window.matchMedia("(max-width: 992px)").matches) {
        e.preventDefault(); // chặn chuyển trang khi bấm cha trên mobile
        const submenu = link.nextElementSibling;
        if (submenu) submenu.classList.toggle("show");
    }
});
