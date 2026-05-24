// Gửi yêu cầu cập nhật số lượng sản phẩm trong giỏ hàng.
function updateQuantity(itemId, qty) {
    if (qty < 1) return;

    fetch(`/cart/update?itemId=${itemId}&soLuong=${qty}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const total = new Intl.NumberFormat('vi-VN').format(data.total) + '₫';
                document.getElementById('cart-subtotal').innerText = total;
                document.getElementById('cart-total').innerText = total;

                location.reload();
            } else {
                Swal.fire('Lỗi', data.message, 'error').then(() => location.reload());
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lỗi', 'Không thể kết nối với máy chủ', 'error');
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
                .then(res => res.json())
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
                Swal.fire('Lỗi', 'Không thể kết nối với máy chủ', 'error');
            });
        }
    });
}
