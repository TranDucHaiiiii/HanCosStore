function cancelOrder() {
    const headRow = document.querySelector('.head-row');
    const status = (headRow?.dataset.orderStatus || '').toUpperCase();
    const paymentMethod = (headRow?.dataset.paymentMethod || '').toUpperCase();
    const isConfirmed = status === 'DA_XAC_NHAN' || status === 'CONFIRMED' || status === 'PAID';
    const isBankTransfer = paymentMethod.includes('SEPAY')
        || paymentMethod.includes('TRANSFER')
        || paymentMethod.includes('CHUYEN_KHOAN')
        || paymentMethod.includes('CHUYENKHOAN')
        || paymentMethod.includes('CHUYEN KHOAN');
    const refundNotice = isConfirmed && isBankTransfer
        ? `
            <div style="text-align:left;background:#fff3cd;border:1px solid #ffe69c;color:#664d03;padding:10px 12px;margin:0 0 12px 0;font-size:13px;line-height:1.45;">
                Đơn hàng đã xác nhận và đã thanh toán chuyển khoản. Để được hoàn tiền, vui lòng liên hệ Zalo shop:
                <a href="https://zalo.me/0559105153" target="_blank" rel="noopener noreferrer" style="font-weight:800;color:#0d6efd;">0559105153</a>
            </div>
        `
        : '';

    Swal.fire({
        title: 'Hủy đơn hàng',
        html: refundNotice + '<div style="text-align:left;font-size:14px;margin-bottom:6px;">Vui lòng cho biết lý do hủy đơn hàng này:</div>',
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
            const reason = result.value;
            document.getElementById('cancelReason').value = reason;
            document.getElementById('cancelOrderForm').submit();
        }
    });
}

function requestReturn() {
    Swal.fire({
        title: 'Yêu cầu trả hàng',
        html: `
            <select id="returnReasonSelect" class="swal2-select" style="display:block;width:100%;margin:0 0 12px 0;">
                <option value="">Chọn lý do trả hàng</option>
                <option value="Sản phẩm lỗi / hư hỏng">Sản phẩm lỗi / hư hỏng</option>
                <option value="Giao sai sản phẩm">Giao sai sản phẩm</option>
                <option value="Sản phẩm không đúng mô tả">Sản phẩm không đúng mô tả</option>
                <option value="Không vừa size">Không vừa size</option>
                <option value="Khác">Khác</option>
            </select>
            <input id="returnImageInput" class="swal2-file" type="file" accept="image/*" style="display:block;width:100%;margin:0;">
        `,
        showCancelButton: true,
        confirmButtonColor: '#fd7e14',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Gửi yêu cầu',
        cancelButtonText: 'Hủy',
        preConfirm: () => {
            const reason = document.getElementById('returnReasonSelect').value;
            const image = document.getElementById('returnImageInput').files[0];
            if (!reason) {
                Swal.showValidationMessage('Vui lòng chọn lý do trả hàng!');
                return false;
            }
            if (!image) {
                Swal.showValidationMessage('Vui lòng upload ảnh sản phẩm!');
                return false;
            }
            if (!image.type || !image.type.startsWith('image/')) {
                Swal.showValidationMessage('File minh chứng phải là ảnh sản phẩm!');
                return false;
            }
            return { reason, image };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const headRow = document.querySelector('.head-row');
            const orderId = headRow ? headRow.dataset.orderId : null;
            const formData = new FormData();
            formData.append('reason', result.value.reason);
            formData.append('image', result.value.image);

            fetch('/order/return/' + orderId, {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Thành công', 'Yêu cầu trả hàng đã được gửi. Vui lòng chờ admin xử lý.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi', data, 'error');
                }
            });
        }
    });
}

let locationData = [];
const editAddressModal = new bootstrap.Modal(document.getElementById('editAddressModal'));

function showEditAddressModal() {
    if (locationData.length === 0) {
        fetch('/data/dvhcvn.json')
            .then(response => response.json())
            .then(res => {
                locationData = res.data;
                initLocationDropdowns();
                editAddressModal.show();
            })
            .catch(error => console.error('Lỗi tải dữ liệu địa chính:', error));
    } else {
        editAddressModal.show();
    }
}

function initLocationDropdowns() {
    const provinceSelect = document.getElementById('province');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const detailAddressInput = document.getElementById('detailAddress');

    provinceSelect.innerHTML = '<option value="">Chọn Tỉnh/Thành</option>';
    locationData.forEach(p => {
        provinceSelect.add(new Option(p.name, p.level1_id));
    });

    const headRow = document.querySelector('.head-row');
    const currentAddress = headRow ? (headRow.dataset.currentAddress || '') : '';

    if (currentAddress) {
        const parts = currentAddress.split(',').map(s => s.trim());

        if (parts.length >= 4) {
            const detail = parts.slice(0, parts.length - 3).join(', ');
            const wardName = parts[parts.length - 3];
            const districtName = parts[parts.length - 2];
            const provinceName = parts[parts.length - 1];

            detailAddressInput.value = detail;

            const prov = locationData.find(p => p.name === provinceName);
            if (prov) {
                provinceSelect.value = prov.level1_id;
                updateDistricts(prov.level1_id);

                setTimeout(() => {
                    const dist = prov.level2s.find(d => d.name === districtName);
                    if (dist) {
                        districtSelect.value = dist.level2_id;
                        updateWards(prov.level1_id, dist.level2_id);

                        setTimeout(() => {
                            const ward = dist.level3s.find(w => w.name === wardName);
                            if (ward) {
                                wardSelect.value = ward.level3_id;
                            }
                        }, 100);
                    }
                }, 100);
            }
        } else {
            detailAddressInput.value = currentAddress;
        }
    }

    provinceSelect.addEventListener('change', function() {
        updateDistricts(this.value);
        wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
        wardSelect.disabled = true;
    });

    districtSelect.addEventListener('change', function() {
        updateWards(provinceSelect.value, this.value);
    });
}

function updateDistricts(provinceId) {
    const districtSelect = document.getElementById('district');
    districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';

    if (!provinceId) {
        districtSelect.disabled = true;
        return;
    }

    const province = locationData.find(p => p.level1_id === provinceId);
    if (province && province.level2s) {
        province.level2s.forEach(d => {
            districtSelect.add(new Option(d.name, d.level2_id));
        });
        districtSelect.disabled = false;
    } else {
        districtSelect.disabled = true;
    }
}

function updateWards(provinceId, districtId) {
    const wardSelect = document.getElementById('ward');
    wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';

    if (!provinceId || !districtId) {
        wardSelect.disabled = true;
        return;
    }

    const province = locationData.find(p => p.level1_id === provinceId);
    if (province && province.level2s) {
        const district = province.level2s.find(d => d.level2_id === districtId);
        if (district && district.level3s) {
            district.level3s.forEach(w => {
                wardSelect.add(new Option(w.name, w.level3_id));
            });
            wardSelect.disabled = false;
        } else {
            wardSelect.disabled = true;
        }
    } else {
        wardSelect.disabled = true;
    }
}

function saveAddress() {
    const hoTen = document.getElementById('editHoTen').value;
    const soDienThoai = document.getElementById('editSoDienThoai').value;
    const provinceSelect = document.getElementById('province');
    const districtSelect = document.getElementById('district');
    const wardSelect = document.getElementById('ward');
    const detail = document.getElementById('detailAddress').value;

    if (!hoTen || !soDienThoai || !provinceSelect.value || !districtSelect.value || !wardSelect.value || !detail) {
        Swal.fire('Lỗi', 'Vui lòng điền đầy đủ thông tin giao hàng!', 'warning');
        return;
    }

    const provinceName = provinceSelect.options[provinceSelect.selectedIndex].text;
    const districtName = districtSelect.options[districtSelect.selectedIndex].text;
    const wardName = wardSelect.options[wardSelect.selectedIndex].text;

    const fullAddress = `${detail}, ${wardName}, ${districtName}, ${provinceName}`;

    document.getElementById('fullAddress').value = fullAddress;
    document.getElementById('selectedProvinceName').value = provinceName;
    document.getElementById('selectedDistrictName').value = districtName;
    document.getElementById('selectedWardName').value = wardName;
    document.getElementById('selectedDetailAddress').value = detail;

    Swal.fire({
        title: 'Xác nhận đổi địa chỉ',
        text: 'Phí Ship sẽ thay đổi khi đổi địa chỉ. Bạn có muốn tiếp tục?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#111',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            document.getElementById('editAddressForm').submit();
        }
    });
}

function requestReturn() {
    const headRow = document.querySelector('.head-row');
    const orderId = headRow ? headRow.dataset.orderId : null;
    if (!orderId) {
        Swal.fire('Lỗi', 'Không xác định được đơn hàng.', 'error');
        return;
    }

    fetch('/api/returns/orders/' + orderId + '/items', {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
        .then(res => res.json())
        .then(payload => {
            if (!payload.success) {
                Swal.fire('Lỗi', payload.message || 'Không tải được sản phẩm trả hàng.', 'error');
                return;
            }
            openReturnDetailDialog(orderId, payload.data || []);
        })
        .catch(() => Swal.fire('Lỗi', 'Không tải được sản phẩm trả hàng.', 'error'));
}

function openReturnDetailDialog(orderId, items) {
    const itemRows = items.map(item => `
        <div style="display:grid;grid-template-columns:1fr 92px;gap:10px;align-items:center;text-align:left;margin-bottom:10px;">
            <div>
                <div style="font-weight:700;font-size:13px;">${escapeReturnHtml(item.productName)}</div>
                <div style="font-size:12px;color:#777;">${escapeReturnHtml(item.color)} / ${escapeReturnHtml(item.size)} - Đã mua: ${item.orderedQuantity}</div>
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
            if (!reason || !selectedItems.length || !refundMethod || !images.length) {
                Swal.showValidationMessage('Vui lòng nhập đủ lý do, số lượng trả, phương thức hoàn tiền và ảnh minh chứng!');
                return false;
            }
            if (images.some(file => !file.type || !file.type.startsWith('image/'))) {
                Swal.showValidationMessage('Tất cả file minh chứng phải là ảnh!');
                return false;
            }
            return { reason, description, refundMethod, selectedItems, images };
        }
    }).then((result) => {
        if (!result.isConfirmed) return;
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

function cancelReturnRequest(button) {
    const requestId = button?.dataset?.returnRequestId;
    if (!requestId) {
        Swal.fire('Lỗi', 'Không xác định được yêu cầu trả hàng.', 'error');
        return;
    }

    Swal.fire({
        title: 'Hủy yêu cầu trả hàng',
        text: 'Bạn có chắc muốn hủy yêu cầu trả hàng này?',
        input: 'text',
        inputPlaceholder: 'Lý do hủy (không bắt buộc)',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Xác nhận hủy',
        cancelButtonText: 'Quay lại'
    }).then((result) => {
        if (!result.isConfirmed) {
            return;
        }

        const formData = new URLSearchParams();
        if (result.value) {
            formData.append('reason', result.value);
        }

        fetch('/api/returns/' + requestId + '/cancel', {
            method: 'POST',
            body: formData,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    Swal.fire('Thành công', data.message || 'Đã hủy yêu cầu trả hàng.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi', data.message || 'Không hủy được yêu cầu trả hàng.', 'error');
                }
            })
            .catch(() => Swal.fire('Lỗi', 'Không thể kết nối với máy chủ.', 'error'));
    });
}

function escapeReturnHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (char) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    }[char]));
}

document.addEventListener('DOMContentLoaded', () => {
    startPaymentCountdowns();
});

function startPaymentCountdowns() {
    const countdownEls = Array.from(document.querySelectorAll('.payment-countdown[data-expire-at], .success-container[data-expire-at] .detail-countdown'));
    if (!countdownEls.length) {
        return;
    }

    const resolveExpireAt = (el) => {
        const ownValue = Number(el.dataset.expireAt);
        if (Number.isFinite(ownValue)) {
            return ownValue;
        }
        const parentValue = Number(el.closest('.success-container')?.dataset.expireAt);
        return Number.isFinite(parentValue) ? parentValue : null;
    };

    const formatRemaining = (ms) => {
        const totalSeconds = Math.max(Math.floor(ms / 1000), 0);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    };

    const tick = () => {
        countdownEls.forEach((el) => {
            const expireAt = resolveExpireAt(el);
            if (expireAt === null) {
                return;
            }

            const valueEl = el.querySelector('strong') || el;
            const remaining = expireAt - Date.now();
            if (remaining <= 0) {
                el.classList.add('is-expired');
                el.innerHTML = '<strong>00:00</strong> đã quá hạn thanh toán';
                const statusEl = document.getElementById('payment-status');
                if (statusEl) {
                    statusEl.textContent = 'Đã quá hạn thanh toán, hệ thống sẽ tự hủy đơn.';
                    statusEl.classList.remove('text-warning');
                    statusEl.classList.add('text-danger');
                }
                return;
            }

            valueEl.textContent = formatRemaining(remaining);
        });
    };

    tick();
    setInterval(tick, 1000);
}
