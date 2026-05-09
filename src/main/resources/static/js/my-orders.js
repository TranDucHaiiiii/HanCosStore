function cancelOrder(orderId) {
    Swal.fire({
        title: 'Hủy đơn hàng',
        text: 'Vui lòng cho biết lý do hủy đơn hàng này:',
        input: 'text',
        inputPlaceholder: 'Nhập lý do tại đây...',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Xác nhận hủy',
        cancelButtonText: 'Quay lại',
        inputValidator: (value) => {
            if (!value) {
                return 'Bạn cần nhập lý do hủy!';
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const formData = new FormData();
            formData.append('reason', result.value);

            fetch('/order/cancel/' + orderId, {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Thành công', 'Đơn hàng của bạn đã được hủy.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi', data, 'error');
                }
            });
        }
    });
}

function requestReturn(orderId) {
    fetch('/api/returns/orders/' + orderId + '/items', {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.json())
        .then(payload => {
            if (!payload.success) {
                Swal.fire('Lỗi', payload.message || 'Không tải được sản phẩm trả hàng.', 'error');
                return;
            }
            openReturnDialog(orderId, payload.data || []);
        })
        .catch(() => Swal.fire('Lỗi', 'Không tải được sản phẩm trả hàng.', 'error'));
}

function openReturnDialog(orderId, items) {
    const itemRows = items.map(item => `
        <div style="display:grid;grid-template-columns:1fr 92px;gap:10px;align-items:center;text-align:left;margin-bottom:10px;">
            <div>
                <div style="font-weight:700;font-size:13px;">${escapeHtml(item.productName)}</div>
                <div style="font-size:12px;color:#777;">${escapeHtml(item.color)} / ${escapeHtml(item.size)} - Đã mua: ${item.orderedQuantity}</div>
            </div>
            <input class="swal2-input return-qty" data-item-id="${item.orderItemId}" type="number" min="0" max="${item.orderedQuantity}" value="0" style="width:92px;margin:0;">
        </div>
    `).join('');

    Swal.fire({
        title: 'Yêu cầu trả hàng',
        width: 720,
        html: `
            <select id="returnReasonSelect" class="swal2-select" style="display:block;width:100%;margin:0 0 10px 0;">
                <option value="">Chọn lý do trả hàng</option>
                <option value="Sản phẩm lỗi / hư hỏng">Sản phẩm lỗi / hư hỏng</option>
                <option value="Giao sai sản phẩm">Giao sai sản phẩm</option>
                <option value="Sản phẩm không đúng mô tả">Sản phẩm không đúng mô tả</option>
                <option value="Không vừa size">Không vừa size</option>
                <option value="Khác">Khác</option>
            </select>
            <textarea id="returnDescription" class="swal2-textarea" placeholder="Mô tả chi tiết tình trạng sản phẩm" style="height:84px;margin:0 0 10px 0;width:100%;"></textarea>
            <select id="refundMethodSelect" class="swal2-select" style="display:block;width:100%;margin:0 0 12px 0;">
                <option value="">Chọn phương thức hoàn tiền</option>
                <option value="BANK_TRANSFER">Chuyển khoản ngân hàng</option>
                <option value="STORE_CREDIT">Hoàn vào ví/tài khoản mua hàng</option>
                <option value="CASH">Tiền mặt</option>
            </select>
            <div style="text-align:left;background:#eef6ff;border:1px solid #b9d7ff;color:#0b4a8f;padding:10px 12px;margin:0 0 12px 0;font-size:13px;line-height:1.45;">
                Để nhận tiền hoàn hàng, vui lòng liên hệ Zalo shop:
                <a href="https://zalo.me/0559105153" target="_blank" rel="noopener noreferrer" style="font-weight:800;color:#0068ff;">0559105153</a>
            </div>
            <div style="text-align:left;font-weight:800;font-size:12px;text-transform:uppercase;margin-bottom:8px;">Số lượng trả</div>
            <div>${itemRows}</div>
            <input id="returnImageInput" class="swal2-file" type="file" accept="image/*" multiple style="display:block;width:100%;margin:8px 0 0 0;">
        `,
        showCancelButton: true,
        confirmButtonColor: '#fd7e14',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Gửi yêu cầu',
        cancelButtonText: 'Hủy',
        preConfirm: () => {
            const reason = document.getElementById('returnReasonSelect').value;
            const description = document.getElementById('returnDescription').value;
            const refundMethod = document.getElementById('refundMethodSelect').value;
            const images = Array.from(document.getElementById('returnImageInput').files);
            const selectedItems = Array.from(document.querySelectorAll('.return-qty'))
                .map(input => ({ orderItemId: Number(input.dataset.itemId), quantity: Number(input.value) }))
                .filter(item => item.quantity > 0);

            if (!reason) {
                Swal.showValidationMessage('Vui lòng chọn lý do trả hàng!');
                return false;
            }
            if (!selectedItems.length) {
                Swal.showValidationMessage('Vui lòng nhập số lượng trả cho ít nhất một sản phẩm!');
                return false;
            }
            if (!refundMethod) {
                Swal.showValidationMessage('Vui lòng chọn phương thức hoàn tiền!');
                return false;
            }
            if (!images.length) {
                Swal.showValidationMessage('Vui lòng upload ảnh minh chứng!');
                return false;
            }
            if (images.some(file => !file.type || !file.type.startsWith('image/'))) {
                Swal.showValidationMessage('Tất cả file minh chứng phải là ảnh!');
                return false;
            }
            return { reason, description, refundMethod, selectedItems, images };
        }
    }).then((result) => {
        if (!result.isConfirmed) {
            return;
        }

        const formData = new FormData();
        formData.append('orderId', orderId);
        formData.append('reason', result.value.reason);
        formData.append('description', result.value.description);
        formData.append('refundMethod', result.value.refundMethod);
        formData.append('itemsJson', JSON.stringify(result.value.selectedItems));
        result.value.images.forEach(image => formData.append('images', image));

        fetch('/api/returns', {
            method: 'POST',
            body: formData,
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        }).then(res => res.json()).then(data => {
            if (data.success) {
                Swal.fire('Thành công', data.message || 'Yêu cầu trả hàng đã được gửi.', 'success')
                    .then(() => location.reload());
            } else {
                Swal.fire('Lỗi', data.message || 'Không gửi được yêu cầu.', 'error');
            }
        });
    });
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    }[char]));
}
