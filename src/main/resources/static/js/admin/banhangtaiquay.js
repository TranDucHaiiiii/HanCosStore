/* ══════════════════════════════════════════
   STATE
   ══════════════════════════════════════════ */
let cart = Array.isArray(window.POS_INITIAL_CART) ? window.POS_INITIAL_CART : [];
let selectedCustomer = null;
let appliedVoucher = null;
let paymentMethod = 'cash';
let currentCategory = 'all';
let customerMode = 'guest';
let invoices = Array.isArray(window.POS_INVOICES) ? window.POS_INVOICES : [];
let activeInvoiceId = window.POS_ACTIVE_INVOICE || '';
let tempOrderCode = null; // Mã đơn hàng tạm cho transfer QR
const invoiceMetas = {};
const MAX_PENDING_INVOICES = 5;

/* ══════════════════════════════════════════
   FORMAT
   ══════════════════════════════════════════ */
// Định dạng số tiền theo chuẩn tiền Việt Nam.
function fmt(n) {
    return Number(n || 0).toLocaleString('vi-VN') + '₫';
}

// Gọi API giỏ hàng POS, đồng bộ lại state giỏ hàng và cập nhật giao diện.
async function requestPosCart(url, options, silent) {
    try {
        const res = await fetch(url, options);
        const data = await res.json();
        if (data.success) {
            cart = Array.isArray(data.cart) ? data.cart : [];
            renderCart();
            recalc();
            return data;
        }
        if (!silent) {
            showToast(data.message || 'Không thể cập nhật giỏ hàng', 'error');
        }
        return data;
    } catch (e) {
        if (!silent) {
            showToast('Lỗi kết nối giỏ hàng: ' + e.message, 'error');
        }
        return {success: false, message: e.message};
    }
}

// Lấy giỏ hàng hiện tại từ server.
function syncPosCart(silent) {
    return requestPosCart('/admin/pos/api/cart', {method: 'GET'}, !!silent);
}

// Thêm một biến thể sản phẩm vào giỏ POS.
function addPosCartItem(variantId, qty) {
    return requestPosCart('/admin/pos/api/cart/items', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({variantId, qty})
    }, false);
}

// Cập nhật số lượng của một biến thể trong giỏ POS.
function updatePosCartItem(variantId, qty) {
    return requestPosCart('/admin/pos/api/cart/items/' + variantId, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({qty})
    }, false);
}

// Xóa một biến thể khỏi giỏ POS.
function removePosCartItem(variantId) {
    return requestPosCart('/admin/pos/api/cart/items/' + variantId, {
        method: 'DELETE'
    }, false);
}

// Xóa toàn bộ giỏ POS hiện tại.
function clearPosCart(silent) {
    return requestPosCart('/admin/pos/api/cart', {
        method: 'DELETE'
    }, !!silent);
}


// Render danh sách tab hóa đơn chờ và nút tạo hóa đơn mới.
function renderInvoiceTabs() {
    const bar = document.getElementById('invoiceBar');
    if (!bar) return;
    const canAddInvoice = invoices.length < MAX_PENDING_INVOICES;
    bar.innerHTML = invoices.map(inv => `
        <div class="pos-invoice-tab${inv.active ? ' active' : ''}" onclick="activateInvoice('${inv.invoiceId}')">
            <span class="pos-inv-label">${inv.label}</span>
            ${inv.itemCount > 0 ? `<span class="pos-inv-badge">${inv.itemCount}</span>` : ''}
            ${invoices.length > 1 ? `<button class="pos-inv-close" onclick="event.stopPropagation();removeInvoice('${inv.invoiceId}')" title="Dong"><i class="fas fa-times"></i></button>` : ''}
        </div>
    `).join('') + `<button class="pos-invoice-add${canAddInvoice ? '' : ' disabled'}" onclick="addInvoice()" title="${canAddInvoice ? 'Them hoa don moi' : 'Toi da 5 hoa don cho'}"><i class="fas fa-plus"></i></button>`;
}
// Lưu tạm thông tin khách hàng, voucher và thanh toán của hóa đơn đang mở.
function saveCurrentMeta() {
    if (!activeInvoiceId) return;
    invoiceMetas[activeInvoiceId] = {
        customerMode,
        selectedCustomer,
        appliedVoucher,
        paymentMethod,
        custName: document.getElementById('custName').value,
        custPhone: document.getElementById('custPhone').value,
        note: document.getElementById('orderNote').value,
        cashGiven: document.getElementById('cashGiven').value
    };
}

// Khôi phục thông tin khách hàng, voucher và thanh toán khi chuyển hóa đơn.
function restoreMeta(invoiceId) {
    const meta = invoiceMetas[invoiceId];
    const mode = meta ? meta.customerMode : 'guest';
    customerMode = mode;
    document.querySelectorAll('.cust-mode-btn').forEach(b => b.classList.toggle('active', b.dataset.mode === mode));
    document.getElementById('custPanelGuest').style.display = mode === 'guest' ? '' : 'none';
    document.getElementById('custPanelExisting').style.display = mode === 'existing' ? '' : 'none';
    document.getElementById('custPanelNew').style.display = mode === 'new' ? '' : 'none';

    selectedCustomer = meta ? meta.selectedCustomer : null;
    if (selectedCustomer) {
        document.getElementById('custComboText').textContent = selectedCustomer.hoTen + ' — ' + (selectedCustomer.soDienThoai || '');
        document.getElementById('custComboText').classList.add('has-value');
        document.getElementById('custComboClear').style.display = '';
    } else {
        clearCustomer();
    }

    appliedVoucher = meta ? meta.appliedVoucher : null;
    if (appliedVoucher) {
        document.getElementById('voucherInputArea').style.display = 'none';
        document.getElementById('voucherApplied').style.display = '';
        document.getElementById('voucherLabel').textContent = appliedVoucher.code + ' (-' + fmt(appliedVoucher.discount) + ')';
    } else {
        document.getElementById('voucherInputArea').style.display = '';
        document.getElementById('voucherApplied').style.display = 'none';
        document.getElementById('voucherCode').value = '';
    }

    paymentMethod = meta ? (meta.paymentMethod || 'cash') : 'cash';
    document.querySelectorAll('.pay-method-btn').forEach(b => b.classList.toggle('active', b.dataset.method === paymentMethod));
    document.getElementById('cashRow').style.display = paymentMethod === 'cash' ? '' : 'none';
    document.getElementById('changeRow').style.display = 'none';
    document.getElementById('transferRow').style.display = paymentMethod === 'cash' ? 'none' : '';
    document.getElementById('custName').value = meta ? meta.custName || '' : '';
    document.getElementById('custPhone').value = meta ? meta.custPhone || '' : '';
    document.getElementById('orderNote').value = meta ? meta.note || '' : '';
    document.getElementById('cashGiven').value = meta ? meta.cashGiven || '' : '';
    document.getElementById('transferConfirm').checked = false;
}

// Tạo hóa đơn chờ mới nếu chưa vượt quá giới hạn.
async function addInvoice() {
    if (invoices.length >= MAX_PENDING_INVOICES) {
        showToast('Chi duoc tao toi da 5 hoa don cho', 'error');
        return;
    }
    saveCurrentMeta();
    const res = await fetch('/admin/pos/api/invoices', { method: 'POST' });
    const data = await res.json();
    if (data.success) {
        invoices = data.invoices;
        activeInvoiceId = data.activeInvoiceId;
        cart = Array.isArray(data.cart) ? data.cart : [];
        renderInvoiceTabs();
        restoreMeta(activeInvoiceId);
        renderCart();
        recalc();
    }
}

// Chuyển sang hóa đơn chờ được chọn và tải lại giỏ tương ứng.
async function activateInvoice(invoiceId) {
    if (invoiceId === activeInvoiceId) return;
    saveCurrentMeta();
    const res = await fetch('/admin/pos/api/invoices/' + invoiceId + '/activate', { method: 'PUT' });
    const data = await res.json();
    if (data.success) {
        invoices = data.invoices;
        activeInvoiceId = data.activeInvoiceId;
        cart = Array.isArray(data.cart) ? data.cart : [];
        renderInvoiceTabs();
        restoreMeta(activeInvoiceId);
        renderCart();
        recalc();
    }
}

// Xóa một hóa đơn chờ và chuyển giao diện sang hóa đơn còn hoạt động.
async function removeInvoice(invoiceId) {
    const switching = invoiceId === activeInvoiceId;
    delete invoiceMetas[invoiceId];
    const res = await fetch('/admin/pos/api/invoices/' + invoiceId, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
        invoices = data.invoices;
        activeInvoiceId = data.activeInvoiceId;
        cart = Array.isArray(data.cart) ? data.cart : [];
        renderInvoiceTabs();
        if (switching) restoreMeta(activeInvoiceId);
        renderCart();
        recalc();
    }
}

/* ══════════════════════════════════════════
   FILTER SẢN PHẨM (show/hide DOM)
   ══════════════════════════════════════════ */
// Chọn danh mục sản phẩm và kích hoạt lọc danh sách sản phẩm.
function filterCategory(catId, el) {
    currentCategory = catId;
    document.querySelectorAll('.pos-cat-link, .pos-cat-sub').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    const parentItem = el.closest('.pos-cat-item');
    if (parentItem) {
        const parentLink = parentItem.querySelector('.pos-cat-link');
        if (parentLink && parentLink !== el) parentLink.classList.add('active');
    }
    filterProducts();
}

// Lọc sản phẩm theo danh mục hiện tại và từ khóa tìm kiếm.
function filterProducts() {
    const keyword = document.getElementById('searchProduct').value.trim().toLowerCase();
    const cards = document.querySelectorAll('.pos-product-card');
    let visible = 0;

    cards.forEach(card => {
        const catId = parseInt(card.dataset.cat) || 0;
        const parentCatId = parseInt(card.dataset.pcat) || 0;
        const name = card.dataset.name || '';
        const code = card.dataset.code || '';

        const matchCat = currentCategory === 'all' || catId == currentCategory || parentCatId == currentCategory;
        const matchSearch = !keyword || name.includes(keyword) || code.includes(keyword);

        if (matchCat && matchSearch) {
            card.style.display = '';
            visible++;
        } else {
            card.style.display = 'none';
        }
    });

    document.getElementById('noProductMsg').style.display = visible === 0 ? '' : 'none';
}

let searchTimer = null;
// Debounce ô tìm kiếm sản phẩm để không lọc lại DOM ở mỗi ký tự quá nhanh.
document.getElementById('searchProduct').addEventListener('input', function () {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(filterProducts, 300);
});

/* ══════════════════════════════════════════
   VARIANT MODAL (đọc data từ DOM — Thymeleaf render)
   ══════════════════════════════════════════ */
let modalCard = null;
let modalSelectedColor = null;
let modalSelectedSize = null;
let modalSelectedVariant = null;

// Đọc danh sách biến thể sản phẩm từ dữ liệu DOM của card sản phẩm.
function getVariantsFromCard(card) {
    return Array.from(card.querySelectorAll('.vd')).map(el => ({
        id: parseInt(el.dataset.id),
        mauSac: el.dataset.color,
        kichCo: el.dataset.size,
        gia: parseFloat(el.dataset.price),
        soLuongTon: parseInt(el.dataset.stock)
    }));
}

// Mở modal chọn màu, size và số lượng cho sản phẩm.
function openVariantModal(productId) {
    const card = document.querySelector('.pos-product-card[data-id="' + productId + '"]');
    if (!card) return;
    modalCard = card;

    const variants = getVariantsFromCard(card);
    if (variants.length === 0) return;

    document.getElementById('modalProductName').textContent = card.querySelector('.prod-name').textContent;

    const imgEl = card.querySelector('.prod-img-wrap img');
    const modalImg = document.getElementById('modalProductImg');
    if (imgEl) { modalImg.src = imgEl.src; modalImg.style.display = ''; }
    else { modalImg.style.display = 'none'; }

    const colors = [...new Set(variants.map(v => v.mauSac))];
    const sizes = [...new Set(variants.map(v => v.kichCo))];

    document.getElementById('modalColors').innerHTML = colors.map(c =>
        `<button class="variant-color-btn" onclick="selectColor('${c}',this)">${c}</button>`
    ).join('');
    document.getElementById('modalSizes').innerHTML = sizes.map(s =>
        `<button class="variant-size-btn" onclick="selectSize('${s}',this)">${s}</button>`
    ).join('');

    modalSelectedColor = null;
    modalSelectedSize = null;
    modalSelectedVariant = null;
    document.getElementById('modalQty').value = 1;
    document.getElementById('modalPrice').textContent = fmt(Math.min(...variants.map(v => v.gia)));
    document.getElementById('modalStock').textContent = '';

    if (colors.length === 1) {
        modalSelectedColor = colors[0];
        document.querySelector('.variant-color-btn').classList.add('active');
    }
    if (sizes.length === 1) {
        modalSelectedSize = sizes[0];
        document.querySelector('.variant-size-btn').classList.add('active');
    }
    updateModalVariant();

    new bootstrap.Modal(document.getElementById('variantModal')).show();
}

// Xử lý chọn màu, đồng thời giới hạn danh sách size còn hợp lệ.
function selectColor(color, el) {
    document.querySelectorAll('.variant-color-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    modalSelectedColor = color;

    const variants = getVariantsFromCard(modalCard);
    const availableSizes = new Set(variants.filter(v => v.mauSac === color).map(v => v.kichCo));
    document.querySelectorAll('.variant-size-btn').forEach(btn => {
        const size = btn.textContent;
        if (availableSizes.has(size)) {
            btn.disabled = false;
            btn.style.opacity = '1';
        } else {
            btn.disabled = true;
            btn.style.opacity = '0.4';
            if (modalSelectedSize === size) {
                modalSelectedSize = null;
                btn.classList.remove('active');
            }
        }
    });

    const colorImgEl = modalCard.querySelector('.vcid[data-color="' + color + '"]');
    if (colorImgEl) document.getElementById('modalProductImg').src = colorImgEl.dataset.url;

    updateModalVariant();
}

// Xử lý chọn size trong modal biến thể.
function selectSize(size, el) {
    document.querySelectorAll('.variant-size-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    modalSelectedSize = size;
    updateModalVariant();
}

// Xác định biến thể đang chọn và cập nhật giá, tồn kho trong modal.
function updateModalVariant() {
    if (modalSelectedColor && modalSelectedSize) {
        const variants = getVariantsFromCard(modalCard);
        const v = variants.find(b => b.mauSac === modalSelectedColor && b.kichCo === modalSelectedSize);
        modalSelectedVariant = v || null;
        if (v) {
            document.getElementById('modalPrice').textContent = fmt(v.gia);
            document.getElementById('modalStock').textContent = 'Tồn kho: ' + v.soLuongTon;
            const qtyEl = document.getElementById('modalQty');
            if (parseInt(qtyEl.value) > v.soLuongTon) qtyEl.value = v.soLuongTon > 0 ? v.soLuongTon : 1;
        }
    } else {
        modalSelectedVariant = null;
    }
}

// Tăng hoặc giảm số lượng trong modal, có kiểm tra tồn kho.
function modalQtyChange(delta) {
    const qtyEl = document.getElementById('modalQty');
    let val = parseInt(qtyEl.value) + delta;
    if (val < 1) val = 1;
    if (modalSelectedVariant && val > modalSelectedVariant.soLuongTon) {
        showToast('Số lượng vượt quá tồn kho!', 'error');
        return;
    }
    qtyEl.value = val;
}

// Chuẩn hóa số lượng nhập tay trong modal và không cho vượt tồn kho.
function modalQtyInput(el) {
    let val = parseInt(el.value) || 1;
    if (val < 1) val = 1;
    if (modalSelectedVariant && val > modalSelectedVariant.soLuongTon) {
        showToast('Số lượng vượt quá tồn kho! (Tồn: ' + modalSelectedVariant.soLuongTon + ')', 'error');
        val = modalSelectedVariant.soLuongTon;
    }
    el.value = val;
}

// Thêm biến thể đã chọn từ modal vào giỏ hàng.
async function addToCartFromModal() {
    if (!modalSelectedVariant) {
        showToast('Vui lòng chọn màu sắc và kích cỡ', 'error');
        return;
    }

    const variant = modalSelectedVariant;
    const qty = parseInt(document.getElementById('modalQty').value) || 1;

    const existingItem = cart.find(c => c.variantId === variant.id);
    const currentInCart = existingItem ? existingItem.qty : 0;
    if (currentInCart + qty > variant.soLuongTon) {
        showToast('Sản phẩm không đủ số lượng! (Tồn kho: ' + variant.soLuongTon + ', trong giỏ: ' + currentInCart + ')', 'error');
        return;
    }

    const syncResult = await addPosCartItem(variant.id, qty);
    if (!syncResult.success) {
        return;
    }

    bootstrap.Modal.getInstance(document.getElementById('variantModal')).hide();
    showToast('Đã thêm vào giỏ hàng', 'success');
}

/* ══════════════════════════════════════════
   CART
   ══════════════════════════════════════════ */
// Render các sản phẩm trong giỏ hàng và tổng số dòng sản phẩm.
function renderCart() {
    const wrap = document.getElementById('cartItems');
    document.getElementById('cartCount').textContent = cart.length;

    if (cart.length === 0) {
        wrap.innerHTML = '<div class="cart-empty"><i class="fas fa-shopping-basket"></i><p>Chưa có sản phẩm nào</p></div>';
        return;
    }

    wrap.innerHTML = cart.map((item, idx) => `
        <div class="cart-item">
            ${item.img ? `<img class="cart-item-img" src="${item.img}" alt="">` : '<div class="cart-item-img d-flex align-items-center justify-content-center"><i class="fas fa-image text-muted"></i></div>'}
            <div class="cart-item-info">
                <div class="cart-item-name" title="${item.productName}">${item.productName}</div>
                <div class="cart-item-variant">${item.color} / ${item.size}</div>
                <div class="cart-item-price">${fmt(item.price)}</div>
                <div class="qty-control">
                    <button onclick="changeQty(${idx},-1)">−</button>
                    <input type="number" class="qty-val" value="${item.qty}" min="1" onchange="changeQtyDirect(${idx}, this.value)">
                    <button onclick="changeQty(${idx},1)">+</button>
                </div>
                <div class="cart-item-subtotal mt-1">= ${fmt(item.price * item.qty)}</div>
            </div>
            <i class="fas fa-times cart-item-remove" onclick="removeItem(${idx})" title="Xóa"></i>
        </div>
    `).join('');
}

// Tăng hoặc giảm số lượng sản phẩm trong giỏ theo nút bấm.
async function changeQty(idx, delta) {
    const item = cart[idx];
    if (!item) return;
    const newQty = item.qty + delta;
    if (newQty < 1) {
        await removeItem(idx);
        return;
    }
    if (newQty > item.stock) {
        showToast('Sản phẩm không đủ số lượng! Tồn kho: ' + item.stock, 'error');
        return;
    }
    await updatePosCartItem(item.variantId, newQty);
}

// Cập nhật số lượng sản phẩm trong giỏ từ ô nhập trực tiếp.
async function changeQtyDirect(idx, value) {
    const item = cart[idx];
    if (!item) return;
    let newQty = parseInt(value) || 1;
    if (newQty < 1) newQty = 1;
    if (newQty > item.stock) {
        showToast('Sản phẩm không đủ số lượng! Tồn kho: ' + item.stock, 'error');
        newQty = item.stock;
    }
    await updatePosCartItem(item.variantId, newQty);
}

// Xóa một dòng sản phẩm khỏi giỏ hàng.
async function removeItem(idx) {
    const item = cart[idx];
    if (!item) return;
    await removePosCartItem(item.variantId);
}

// Xóa toàn bộ sản phẩm trong giỏ hàng hiện tại.
async function clearCart() {
    await clearPosCart(false);
}

/* ══════════════════════════════════════════
   CUSTOMER MODE
   ══════════════════════════════════════════ */
// Chuyển chế độ khách hàng: khách lẻ, khách đã có hoặc khách mới.
function switchCustMode(mode, btn) {
    customerMode = mode;
    // toggle tab buttons
    document.querySelectorAll('.cust-mode-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    // toggle panels
    document.getElementById('custPanelGuest').style.display = mode === 'guest' ? '' : 'none';
    document.getElementById('custPanelExisting').style.display = mode === 'existing' ? '' : 'none';
    document.getElementById('custPanelNew').style.display = mode === 'new' ? '' : 'none';
    // reset state when switching
    if (mode !== 'existing') clearCustomer();
    if (mode !== 'new') {
        document.getElementById('custName').value = '';
        document.getElementById('custPhone').value = '';
    }
}

/* ══════════════════════════════════════════
   CUSTOMER COMBOBOX (Thymeleaf render, JS filter DOM)
   ══════════════════════════════════════════ */
let custDropdownOpen = false;

// Đóng hoặc mở combobox chọn khách hàng.
function toggleCustDropdown() {
    custDropdownOpen ? closeCustDropdown() : openCustDropdown();
}

// Mở dropdown chọn khách hàng và focus vào ô tìm kiếm.
function openCustDropdown() {
    custDropdownOpen = true;
    document.getElementById('custComboDropdown').classList.add('show');
    document.getElementById('custComboSearch').value = '';
    filterCustList('');
    setTimeout(() => document.getElementById('custComboSearch').focus(), 50);
}

// Đóng dropdown chọn khách hàng.
function closeCustDropdown() {
    custDropdownOpen = false;
    document.getElementById('custComboDropdown').classList.remove('show');
}

// Lọc danh sách khách hàng theo tên, số điện thoại hoặc email.
function filterCustList(keyword) {
    const kw = keyword.toLowerCase();
    const items = document.querySelectorAll('#custComboList .cust-combobox-item');
    let visible = 0;
    items.forEach(item => {
        const name = item.dataset.name || '';
        const phone = (item.dataset.phone || '').toLowerCase();
        const email = item.dataset.email || '';
        const match = !kw || name.includes(kw) || phone.includes(kw) || email.includes(kw);
        item.style.display = match ? '' : 'none';
        if (match) visible++;
        if (selectedCustomer && parseInt(item.dataset.id) === selectedCustomer.id) {
            item.classList.add('selected');
        } else {
            item.classList.remove('selected');
        }
    });
    document.getElementById('custNoResult').style.display = visible === 0 ? '' : 'none';
}

// Gắn sự kiện tìm kiếm trong combobox khách hàng.
(function() {
    const searchInput = document.getElementById('custComboSearch');
    searchInput.addEventListener('input', function() {
        filterCustList(this.value.trim());
    });
})();

// Đóng dropdown khách hàng khi click ra ngoài combobox.
document.addEventListener('click', function(e) {
    if (custDropdownOpen && !document.getElementById('custCombobox').contains(e.target)) {
        closeCustDropdown();
    }
});

// Chọn một khách hàng từ danh sách và hiển thị lên combobox.
function pickCustomer(el) {
    selectedCustomer = {
        id: parseInt(el.dataset.id),
        hoTen: el.dataset.hoten,
        soDienThoai: el.dataset.phone
    };
    document.getElementById('custComboText').textContent = selectedCustomer.hoTen + ' — ' + (selectedCustomer.soDienThoai || '');
    document.getElementById('custComboText').classList.add('has-value');
    document.getElementById('custComboClear').style.display = '';
    closeCustDropdown();
}

// Xóa khách hàng đang chọn và đưa combobox về trạng thái mặc định.
function clearCustomer() {
    selectedCustomer = null;
    document.getElementById('custComboText').textContent = '-- Chọn khách hàng --';
    document.getElementById('custComboText').classList.remove('has-value');
    document.getElementById('custComboClear').style.display = 'none';
}

/* ══════════════════════════════════════════
   VOUCHER
   ══════════════════════════════════════════ */
// Kiểm tra và áp dụng mã giảm giá cho giỏ hàng hiện tại.
async function applyVoucher() {
    const code = document.getElementById('voucherCode').value.trim();
    if (!code) { showToast('Vui lòng nhập mã giảm giá', 'error'); return; }

    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    if (subtotal === 0) { showToast('Giỏ hàng trống', 'error'); return; }

    try {
        const res = await fetch('/admin/pos/api/voucher/validate', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({code, amount: subtotal})
        });
        const data = await res.json();
        if (data.success) {
            appliedVoucher = {code: data.code, discount: Number(data.discount)};
            document.getElementById('voucherInputArea').style.display = 'none';
            document.getElementById('voucherApplied').style.display = '';
            document.getElementById('voucherLabel').textContent = data.code + ' (-' + fmt(data.discount) + ')';
            recalc();
            showToast('Áp dụng mã giảm giá thành công!', 'success');
        } else {
            showToast(data.message || 'Mã giảm giá không hợp lệ', 'error');
        }
    } catch (e) {
        showToast('Lỗi kiểm tra mã giảm giá', 'error');
    }
}

// Gỡ mã giảm giá đang áp dụng và tính lại tổng tiền.
function removeVoucher() {
    appliedVoucher = null;
    document.getElementById('voucherInputArea').style.display = '';
    document.getElementById('voucherApplied').style.display = 'none';
    document.getElementById('voucherCode').value = '';
    recalc();
}

// Hiển thị các mã giảm giá gợi ý phù hợp với giá trị giỏ hàng.
function renderVoucherSuggestions(list) {
    const wrap = document.getElementById('voucherSuggestList');
    const suggestWrap = document.getElementById('voucherSuggestWrap');
    if (!wrap || !suggestWrap) return;

    const suggestions = Array.isArray(list) ? list.filter(Boolean).slice(0, 8) : [];
    if (suggestions.length === 0) {
        suggestWrap.style.display = 'none';
        return;
    }

    suggestWrap.style.display = '';
    wrap.innerHTML = suggestions.map(item => {
        const code = item.code || '';
        const desc = item.label || item.description || item.name || '';
        const label = desc ? `${code} - ${desc}` : code;
        const safeCode = (code || '').replace(/"/g, '&quot;');
        return `<button type="button" class="voucher-suggest-chip" onclick="applySuggestedVoucher('${safeCode}')">${label}</button>`;
    }).join('');
}

// Điền mã giảm giá được gợi ý vào ô nhập và áp dụng ngay.
function applySuggestedVoucher(code) {
    const input = document.getElementById('voucherCode');
    if (!input) return;
    input.value = code;
    applyVoucher();
}

// Tải danh sách voucher đủ điều kiện từ server theo tổng tiền giỏ hàng.
async function loadVoucherSuggestions() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    try {
        const res = await fetch('/admin/pos/api/vouchers/eligible?amount=' + encodeURIComponent(subtotal));
        if (!res.ok) throw new Error('no-api');
        const data = await res.json();
        const list = Array.isArray(data.vouchers) ? data.vouchers : [];
        renderVoucherSuggestions(list);
        return;
    } catch (e) {
        renderVoucherSuggestions([]);
    }
}

/* ══════════════════════════════════════════
   RECALC
   ══════════════════════════════════════════ */
// Tính lại tạm tính, giảm giá, tổng thanh toán và các trạng thái liên quan.
function recalc() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);

    document.getElementById('subtotal').textContent = fmt(subtotal);
    if (discount > 0) {
        document.getElementById('discountRow').style.display = '';
        document.getElementById('discountAmount').textContent = '-' + fmt(discount);
    } else {
        document.getElementById('discountRow').style.display = 'none';
    }
    document.getElementById('totalAmount').textContent = fmt(total);
    loadVoucherSuggestions();

    // Enable/disable checkout
    document.getElementById('btnCheckout').disabled = cart.length === 0;

    if (paymentMethod === 'transfer') updateTransferQR();
    calcChange();
}

/* ══════════════════════════════════════════
   PAYMENT
   ══════════════════════════════════════════ */
/* ── Cấu hình ngân hàng ── */
const BANK_CODE  = '970426'; // BIN MSB
const BANK_ACC   = '96886693011616';
const BANK_OWNER = 'TRAN DUC HAI';

let bankInfo = null;

// Tải thông tin ngân hàng từ VietQR để hiển thị tên và logo khi chuyển khoản.
(async function loadBankInfo() {
    try {
        const res = await fetch('https://api.vietqr.io/v2/banks');
        const json = await res.json();
        if (json.data) {
            bankInfo = json.data.find(b => b.code === BANK_CODE || b.shortName === BANK_CODE);
            if (bankInfo) {
                document.getElementById('transferBankName').textContent = bankInfo.shortName + ' - ' + bankInfo.name;
                const logoEl = document.getElementById('transferBankLogo');
                if (bankInfo.logo) { logoEl.src = bankInfo.logo; logoEl.style.display = ''; }
            }
        }
    } catch (e) { /* fallback giữ nguyên text mặc định */ }
})();

// Chọn phương thức thanh toán và cập nhật vùng nhập tiền hoặc QR chuyển khoản.
function selectPayment(method, el) {
    paymentMethod = method;
    document.querySelectorAll('.pay-method-btn').forEach(b => b.classList.remove('active'));
    el.classList.add('active');

    const isCash = method === 'cash';
    document.getElementById('cashRow').style.display = isCash ? '' : 'none';
    document.getElementById('changeRow').style.display = 'none';
    document.getElementById('transferRow').style.display = isCash ? 'none' : '';

    if (!isCash) {
        updateTransferQR();
        openTransferModal();
    }
}

// Mở modal QR chuyển khoản sau khi cập nhật thông tin thanh toán.
function openTransferModal() {
    updateTransferQR();
    new bootstrap.Modal(document.getElementById('transferModal')).show();
}

// Tạo lại QR chuyển khoản theo tổng tiền hiện tại và thông tin ngân hàng.
function updateTransferQR() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);

    document.getElementById('transferSTK').textContent = BANK_ACC;
    document.getElementById('transferName').textContent = BANK_OWNER;
    document.getElementById('transferAmount').textContent = fmt(total);

    const bankBin = bankInfo ? bankInfo.bin : BANK_CODE;
    const info = 'HANCOS ' + Date.now();
    const qrUrl = 'https://img.vietqr.io/image/' + bankBin + '-' + BANK_ACC + '-compact2.png?amount=' + total + '&addInfo=' + encodeURIComponent(info) + '&accountName=' + encodeURIComponent(BANK_OWNER);
    document.getElementById('transferQR').src = qrUrl;
}

// Sao chép số tài khoản ngân hàng vào clipboard.
function copySTK() {
    navigator.clipboard.writeText(BANK_ACC).then(() => showToast('Đã sao chép số tài khoản', 'success'));
}

// Tính tiền thừa hoặc thiếu khi khách thanh toán bằng tiền mặt.
function calcChange() {
    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const discount = appliedVoucher ? appliedVoucher.discount : 0;
    const total = Math.max(0, subtotal - discount);
    const given = parseFloat(document.getElementById('cashGiven').value.replace(/[^0-9]/g, '')) || 0;

    if (given > 0 && paymentMethod === 'cash') {
        const change = given - total;
        document.getElementById('changeRow').style.display = '';
        document.getElementById('changeAmount').textContent = fmt(Math.max(0, change));
        document.getElementById('changeAmount').style.color = change >= 0 ? '#28a745' : '#dc3545';
    } else {
        document.getElementById('changeRow').style.display = 'none';
    }
}

/* ══════════════════════════════════════════
   CHECKOUT
   ══════════════════════════════════════════ */
// Xác thực dữ liệu bán hàng, gửi yêu cầu thanh toán và hiển thị kết quả.
async function checkout() {
    if (cart.length === 0) { showToast('Giỏ hàng trống', 'error'); return; }

    let custName, custPhone, custId = null;

    if (customerMode === 'guest') {
        custName = 'Khách lẻ';
        custPhone = 'N/A';
    } else if (customerMode === 'existing') {
        if (!selectedCustomer) { showToast('Vui lòng chọn khách hàng', 'error'); return; }
        custId = selectedCustomer.id;
        custName = selectedCustomer.hoTen || selectedCustomer.tenDangNhap;
        custPhone = selectedCustomer.soDienThoai || 'N/A';
    } else { // 'new'
        custName = document.getElementById('custName').value.trim();
        custPhone = document.getElementById('custPhone').value.trim();
        if (!custName) { showToast('Vui lòng nhập tên khách hàng', 'error'); return; }
        if (!custPhone) { showToast('Vui lòng nhập số điện thoại', 'error'); return; }
    }

    // Validate payment
    if (paymentMethod === 'cash') {
        const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
        const discount = appliedVoucher ? appliedVoucher.discount : 0;
        const total = Math.max(0, subtotal - discount);
        const given = parseFloat(document.getElementById('cashGiven').value.replace(/[^0-9]/g, '')) || 0;
        if (given < total) {
            showToast('Tiền khách đưa chưa đủ!', 'error');
            return;
        }
    } else if (paymentMethod === 'transfer') {
        if (!document.getElementById('transferConfirm').checked) {
            showToast('Vui lòng xác nhận đã nhận được tiền chuyển khoản!', 'error');
            return;
        }
    }

    const payload = {
        customerId: custId,
        customerName: custName,
        customerPhone: custPhone,
        paymentMethod: paymentMethod,
        voucherCode: appliedVoucher ? appliedVoucher.code : null,
        note: document.getElementById('orderNote').value.trim() || null
    };

    document.getElementById('btnCheckout').disabled = true;
    document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Đang xử lý...';

    try {
        const res = await fetch('/admin/pos/api/checkout', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            cart = [];
            if (Array.isArray(data.invoices)) { invoices = data.invoices; renderInvoiceTabs(); }

            // Setup success overlay
            const successOverlay = document.getElementById('successOverlay');
            const successLoading = document.getElementById('successLoading');
            const successContent = document.getElementById('successContent');
            const successOrderCode = document.getElementById('successOrderCode');
            const successTotal = document.getElementById('successTotal');
            const successInvoiceLink = document.getElementById('successInvoiceLink');

            // Debug log
            console.log('Checkout success. OrderCode:', data.orderCode, 'Total:', data.total, 'PaymentMethod:', paymentMethod);

            // Set data FIRST (before showing)
            if (successOrderCode) { successOrderCode.textContent = data.orderCode || ''; }
            if (successTotal) { successTotal.textContent = fmt(data.total) || '0₫'; }

            if (successInvoiceLink && data.orderId) {
                successInvoiceLink.href = '/admin/orders/' + data.orderId + '/invoice';
            }

            // If transfer payment, show QR code with order code
            if (paymentMethod === 'transfer') {
                const qrSection = document.getElementById('successQRSection');
                const qrImg = document.getElementById('successQR');
                if (qrSection && qrImg && data.orderCode) {
                    // Generate QR with order code
                    const orderCode = data.orderCode;
                    const total = data.total || 0;
                    const bankBin = bankInfo ? bankInfo.bin : '970426';
                    const qrUrl = 'https://img.vietqr.io/image/' + bankBin + '-' + BANK_ACC + '-compact2.png?amount=' + total + '&addInfo=' + encodeURIComponent(orderCode) + '&accountName=' + encodeURIComponent(BANK_OWNER);
                    qrImg.src = qrUrl;
                    qrSection.style.display = '';
                }
            } else {
                document.getElementById('successQRSection').style.display = 'none';
            }

            // Show loading state and overlay
            successLoading.style.display = 'flex';
            successContent.style.display = 'none';
            successOverlay.classList.add('show');

            // After 1.5 seconds, show success message
            setTimeout(() => {
                successLoading.style.display = 'none';
                successContent.style.display = 'block';
                if (data.orderId) {
                    window.open('/admin/orders/' + data.orderId + '/invoice', '_blank');
                }
            }, 1500);
        } else {
            showToast(data.message || 'Thanh toán thất bại', 'error');
            document.getElementById('btnCheckout').disabled = false;
            document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';
        }
    } catch (e) {
        showToast('Lỗi kết nối: ' + e.message, 'error');
        document.getElementById('btnCheckout').disabled = false;
        document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';
    }
}

/* ══════════════════════════════════════════
   NEW ORDER / RESET
   ══════════════════════════════════════════ */
// Reset giao diện và giỏ hàng để bắt đầu đơn hàng mới.
async function newOrder() {
    await clearPosCart(true);

    cart = [];
    selectedCustomer = null;
    appliedVoucher = null;
    paymentMethod = 'cash';

    document.getElementById('successOverlay').classList.remove('show');
    // Reset customer to guest mode
    customerMode = 'guest';
    document.querySelectorAll('.cust-mode-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('[data-mode="guest"]').classList.add('active');
    document.getElementById('custPanelGuest').style.display = '';
    document.getElementById('custPanelExisting').style.display = 'none';
    document.getElementById('custPanelNew').style.display = 'none';
    clearCustomer();
    document.getElementById('custName').value = '';
    document.getElementById('custPhone').value = '';
    document.getElementById('orderNote').value = '';
    document.getElementById('voucherCode').value = '';
    document.getElementById('voucherInputArea').style.display = '';
    document.getElementById('voucherApplied').style.display = 'none';
    document.getElementById('cashGiven').value = '';
    document.getElementById('changeRow').style.display = 'none';
    document.getElementById('cashRow').style.display = '';
    document.getElementById('transferRow').style.display = 'none';
    document.getElementById('transferConfirm').checked = false;
    document.getElementById('btnCheckout').disabled = true;
    document.getElementById('btnCheckout').innerHTML = '<i class="fas fa-check-circle me-2"></i>Thanh toán';

    document.querySelectorAll('.pay-method-btn').forEach(b => b.classList.remove('active'));
    document.querySelector('[data-method="cash"]').classList.add('active');

    renderCart();
    recalc();

    // Reload trang để lấy dữ liệu tồn kho mới từ server
    window.location.reload();
}

/* ══════════════════════════════════════════
   TOAST
   ══════════════════════════════════════════ */
// Hiển thị thông báo nổi ngắn hạn cho thao tác thành công hoặc lỗi.
function showToast(msg, type) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = 'pos-toast ' + (type || 'success');
    toast.innerHTML = `<i class="fas fa-${type === 'error' ? 'exclamation-circle text-danger' : 'check-circle text-success'}"></i> ${msg}`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 3000);
}

// Khởi tạo màn hình bán hàng tại quầy sau khi DOM đã sẵn sàng.
document.addEventListener('DOMContentLoaded', async function() {
    renderInvoiceTabs();
    renderCart();
    recalc();
    filterProducts();
    loadVoucherSuggestions();
    await syncPosCart(true);
});





