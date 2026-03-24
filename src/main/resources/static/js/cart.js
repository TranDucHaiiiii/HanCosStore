function updateShippingDisplay(fee) {
    const el = document.getElementById('shipping-value');
    if (!el) return;
    el.dataset.fee = fee ?? 0;
    if (fee === 0) {
        el.textContent = 'Miễn phí';
        el.style.color = '#198754';
    } else {
        el.textContent = new Intl.NumberFormat('vi-VN').format(fee) + '₫';
        el.style.color = '';
    }
}

function getShippingFeeValue() {
    const el = document.getElementById('shipping-value');
    if (!el) return 0;
    const attr = parseInt(el.dataset.fee, 10);
    if (!Number.isNaN(attr)) return attr;
    const text = el.textContent || '';
    const num = parseInt(text.replace(/\D/g, ''), 10);
    return Number.isNaN(num) ? 0 : num;
}

function updateQuantity(itemId, qty) {
    if (qty < 1) return;

    fetch(`/cart/update?itemId=${itemId}&soLuong=${qty}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById('cart-subtotal').innerText = new Intl.NumberFormat('vi-VN').format(data.total) + 'â‚«';

                if (data.discount > 0) {
                    document.getElementById('discount-row').style.display = 'flex';
                    document.getElementById('discount-value').innerText = '-' + new Intl.NumberFormat('vi-VN').format(data.discount) + 'â‚«';
                } else {
                    document.getElementById('discount-row').style.display = 'none';
                }
                document.getElementById('cart-total').innerText = new Intl.NumberFormat('vi-VN').format(data.totalAfterDiscount) + '₫';
                updateShippingDisplay(data.shippingFee);

                location.reload();
            } else {
                Swal.fire('Lá»—i', data.message, 'error').then(() => location.reload());
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lá»—i', 'KhÃ´ng thá»ƒ káº¿t ná»‘i vá»›i mÃ¡y chá»§', 'error');
        });
}

function removeItem(itemId) {
    Swal.fire({
        title: 'XÃ¡c nháº­n xÃ³a?',
        text: "Báº¡n muá»‘n xÃ³a sáº£n pháº©m nÃ y khá»i giá» hÃ ng?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#1a1a1a',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Äá»“ng Ã½',
        cancelButtonText: 'Há»§y'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch(`/cart/remove?itemId=${itemId}`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('cart-subtotal').innerText = new Intl.NumberFormat('vi-VN').format(data.total) + 'â‚«';
                        if (data.discount > 0) {
                            document.getElementById('discount-row').style.display = 'flex';
                            document.getElementById('discount-value').innerText = '-' + new Intl.NumberFormat('vi-VN').format(data.discount) + 'â‚«';
                        } else {
                            document.getElementById('discount-row').style.display = 'none';
                        }
                        document.getElementById('cart-total').innerText = new Intl.NumberFormat('vi-VN').format(data.totalAfterDiscount) + '₫';
                        updateShippingDisplay(data.shippingFee);
                        location.reload();
                    }
                })
                .catch(err => {
                    console.error(err);
                    Swal.fire('Lá»—i', 'KhÃ´ng thá»ƒ káº¿t ná»‘i vá»›i mÃ¡y chá»§', 'error');
                });
        }
    });
}

function applyVoucher() {
    const code = document.getElementById('voucher-code').value;
    if (!code) return;

    fetch(`/cart/apply-voucher?code=${code}`, {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.json())
        .then(data => {
            const msg = document.getElementById('voucher-message');
            if (data.success) {
                msg.className = 'small mt-2 text-success';
                msg.innerText = data.message;

                document.getElementById('discount-row').style.display = 'flex';
                document.getElementById('discount-value').innerText = '-' + new Intl.NumberFormat('vi-VN').format(data.discount) + '₫';
                document.getElementById('cart-total').innerText = new Intl.NumberFormat('vi-VN').format(data.totalAfterDiscount) + '₫';
                updateShippingDisplay(data.shippingFee);
            } else {
                msg.className = 'small mt-2 text-danger';
                msg.innerText = data.message;

                document.getElementById('discount-row').style.display = 'none';
                const subtotal = parseInt(document.getElementById('cart-subtotal').innerText.replace(/\D/g, ''));
                const shippingFee = getShippingFeeValue();
                document.getElementById('cart-total').innerText = new Intl.NumberFormat('vi-VN').format(subtotal + shippingFee) + '₫';
            }
        })
        .catch(err => {
            console.error(err);
            Swal.fire('Lá»—i', 'KhÃ´ng thá»ƒ káº¿t ná»‘i vá»›i mÃ¡y chá»§', 'error');
        });
}

function useSuggestedVoucher(code) {
    document.getElementById('voucher-code').value = code;
    applyVoucher();
}
