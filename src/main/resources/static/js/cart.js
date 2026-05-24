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
    if (qty < 1) return;

    fetch(`/cart/update?itemId=${itemId}&soLuong=${qty}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(parseCartResponse)
        .then(data => {
            if (data.success) {
                const total = new Intl.NumberFormat('vi-VN').format(data.total) + '₫';
                document.getElementById('cart-subtotal').innerText = total;
                document.getElementById('cart-total').innerText = total;

                location.reload();
            } else {
                Swal.fire('Lỗi', data.message || 'Không thể cập nhật số lượng', 'error').then(() => location.reload());
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
        });
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
                        const total = new Intl.NumberFormat('vi-VN').format(data.total) + '₫';
                        document.getElementById('cart-subtotal').innerText = total;
                        document.getElementById('cart-total').innerText = total;
                        location.reload();
                    }
                })
            .catch(err => {
                console.error(err);
                Swal.fire('Lỗi', err.message || 'Không thể kết nối với máy chủ', 'error').then(() => location.reload());
            });
        }
    });
}
