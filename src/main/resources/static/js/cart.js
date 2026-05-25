function parseCartResponse(res) {
    return res.text().then(text => {
        let data = {};
        try {
            data = text ? JSON.parse(text) : {};
        } catch (err) {
            throw new Error('Máy chủ trả về dữ liệu không hợp lệ');
        }

        if (!res.ok) {
            throw new Error(data.message || 'Không thể xử lý yêu cầu');
        }

        return data;
    });
}

// Gửi yêu cầu cập nhật số lượng sản phẩm trong giỏ hàng.
function updateQuantity(itemId, qty) {
    const normalizedQty = Math.max(1, Number(qty) || 1);
    const qtyInput = document.getElementById(`qty-${itemId}`);
    if (qtyInput) qtyInput.value = normalizedQty;

    fetch(`/cart/update?itemId=${itemId}&soLuong=${normalizedQty}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(parseCartResponse)
        .then(data => {
            if (data.success) {
                updateRowQuantity(itemId, normalizedQty);
                updateSummary(data.total, data.selectedCount);
            } else {
                Swal.fire('Lỗi', data.message || 'Không thể cập nhật số lượng', 'error').then(() => location.reload());
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
        });
}

function changeQuantity(itemId, delta) {
    const qtyInput = document.getElementById(`qty-${itemId}`);
    if (!qtyInput) return;
    const nextQty = Math.max(1, (Number(qtyInput.value) || 1) + delta);
    updateQuantity(itemId, nextQty);
}

// Xác nhận và gửi yêu cầu xóa sản phẩm khỏi giỏ hàng.
function removeItem(itemId) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: "Bạn muốn xóa sản phẩm này khỏi giỏ hàng?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#1a1a1a',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/cart/remove?itemId=${itemId}`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(parseCartResponse)
                .then(data => {
                    if (data.success) {
                        const row = document.getElementById(`item-${itemId}`);
                        if (row) row.remove();
                        updateSummary(data.total, data.selectedCount);
                        syncSelectAllState();
                        if (!document.querySelector('.cart-item-row')) {
                            location.reload();
                        }
                    }
                })
            .catch(err => {
                console.error(err);
                Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
            });
        }
    });
}

function removeSelectedItems() {
    const selectedChecks = Array.from(document.querySelectorAll('.cart-item-check:checked'));
    if (selectedChecks.length === 0) {
        Swal.fire('Chưa chọn sản phẩm', 'Vui lòng tick sản phẩm cần xóa.', 'warning');
        return;
    }

    Swal.fire({
        title: 'Xóa sản phẩm đã chọn?',
        text: `Bạn muốn xóa ${selectedChecks.length} sản phẩm khỏi giỏ hàng?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#1a1a1a',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Xóa đã chọn',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (!result.isConfirmed) return;

        fetch('/cart/remove-selected', {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(parseCartResponse)
            .then(data => {
                if (!data.success) {
                    Swal.fire('Lỗi', data.message || 'Không thể xóa sản phẩm đã chọn', 'error').then(() => location.reload());
                    return;
                }

                (data.removedIds || []).forEach(itemId => {
                    const row = document.getElementById(`item-${itemId}`);
                    if (row) row.remove();
                });
                updateSummary(data.total, data.selectedCount);
                updateSelectedActions(0);
                syncSelectAllState(false);

                if (!document.querySelector('.cart-item-row')) {
                    location.reload();
                }
            })
            .catch(err => {
                console.error(err);
                Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
            });
    });
}

function selectCartItem(itemId, selected) {
    fetch(`/cart/select?itemId=${itemId}&selected=${selected}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(parseCartResponse)
        .then(data => {
            if (!data.success) {
                Swal.fire('Lỗi', data.message || 'Không thể chọn sản phẩm', 'error').then(() => location.reload());
                return;
            }
            const row = document.getElementById(`item-${itemId}`);
            if (row) row.classList.toggle('selected', selected);
            updateSummary(data.total, data.selectedCount);
            syncSelectAllState(data.allSelected);
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
        });
}

function selectAllCartItems(selected) {
    fetch(`/cart/select-all?selected=${selected}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(parseCartResponse)
        .then(data => {
            if (!data.success) {
                Swal.fire('Lỗi', data.message || 'Không thể chọn sản phẩm', 'error').then(() => location.reload());
                return;
            }
            document.querySelectorAll('.cart-item-check').forEach(input => {
                input.checked = selected;
                const row = document.getElementById(`item-${input.dataset.itemId}`);
                if (row) row.classList.toggle('selected', selected);
            });
            updateSummary(data.total, data.selectedCount);
            syncSelectAllState(data.allSelected);
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
        });
}

function updateRowQuantity(itemId, qty) {
    const row = document.getElementById(`item-${itemId}`);
    if (!row) return;
    row.dataset.qty = qty;
    const price = Number(row.dataset.price) || 0;
    const rowTotal = price * qty;
    const totalCol = row.querySelector('.total-col');
    if (totalCol) totalCol.innerText = formatMoney(rowTotal);
}

function updateSummary(total, selectedCount) {
    const formattedTotal = formatMoney(total);
    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl = document.getElementById('cart-total');
    const checkoutBtn = document.getElementById('checkout-btn');

    if (subtotalEl) subtotalEl.innerText = formattedTotal;
    if (totalEl) totalEl.innerText = formattedTotal;

    const hasSelection = Number(selectedCount) > 0;
    if (checkoutBtn) {
        checkoutBtn.classList.toggle('disabled', !hasSelection);
        checkoutBtn.setAttribute('href', hasSelection ? '/order/checkout' : '#');
    }
    updateSelectedActions(Number(selectedCount) || 0);
}

function updateSelectedActions(selectedCount) {
    const bar = document.getElementById('selected-actions-bar');
    const countEl = document.getElementById('selected-count');
    if (countEl) countEl.textContent = String(selectedCount);
    if (bar) bar.classList.toggle('d-none', selectedCount <= 0);
}

function syncSelectAllState(forcedState) {
    const selectAll = document.getElementById('select-all-cart');
    if (!selectAll) return;
    if (typeof forcedState === 'boolean') {
        selectAll.checked = forcedState;
        return;
    }
    const checks = Array.from(document.querySelectorAll('.cart-item-check'));
    selectAll.checked = checks.length > 0 && checks.every(input => input.checked);
}

function formatMoney(value) {
    return new Intl.NumberFormat('vi-VN').format(Number(value) || 0) + '₫';
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.cart-item-check').forEach(input => {
        input.addEventListener('change', () => selectCartItem(input.dataset.itemId, input.checked));
    });

    const selectAll = document.getElementById('select-all-cart');
    if (selectAll) {
        selectAll.addEventListener('change', () => selectAllCartItems(selectAll.checked));
    }

    const checkoutBtn = document.getElementById('checkout-btn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', event => {
            if (checkoutBtn.classList.contains('disabled')) {
                event.preventDefault();
                Swal.fire('Chưa chọn sản phẩm', 'Vui lòng tick ít nhất một sản phẩm để thanh toán.', 'warning');
            }
        });
    }
});
