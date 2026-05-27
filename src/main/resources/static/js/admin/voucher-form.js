// Bật/tắt bắt buộc nhập giá trị giảm tối đa theo loại voucher.
function toggleGiaTriToiDa() {
    const loai = document.getElementById('loaiGiam').value;
    const input = document.getElementById('giaTriToiDa');
    const group = document.getElementById('giaTriToiDaGroup');
    const star = document.getElementById('giaTriToiDaStar');
    const giaTriGroup = document.getElementById('giaTriGroup');
    const donToiThieuGroup = document.getElementById('donToiThieuGroup');
    const giaTriInput = document.getElementById('giaTri');
    const giaTriLabel = document.getElementById('giaTriLabel');
    const giaTriUnit = document.getElementById('giaTriUnit');

    if (loai === 'PERCENT') {
        input.required = true;
        input.disabled = false;
        group.hidden = false;
        star.style.display = 'inline';
        giaTriGroup.classList.remove('col-md-6');
        giaTriGroup.classList.add('col-md-4');
        donToiThieuGroup.classList.remove('col-md-6');
        donToiThieuGroup.classList.add('col-md-4');
        giaTriLabel.innerHTML = 'Giá trị giảm (%) <span class="text-danger">*</span>';
        giaTriInput.placeholder = 'VD: 10';
        giaTriInput.max = '100';
        giaTriUnit.textContent = '%';
    } else {
        input.required = false;
        input.disabled = true;
        group.hidden = true;
        star.style.display = 'none';
        giaTriGroup.classList.remove('col-md-4');
        giaTriGroup.classList.add('col-md-6');
        donToiThieuGroup.classList.remove('col-md-4');
        donToiThieuGroup.classList.add('col-md-6');
        giaTriLabel.innerHTML = 'Giá trị giảm (₫) <span class="text-danger">*</span>';
        giaTriInput.placeholder = 'VD: 30000';
        giaTriInput.removeAttribute('max');
        giaTriUnit.textContent = '₫';
    }
    updateVoucherPreview();
}

function formatCurrency(value) {
    const number = Number(value);
    if (!Number.isFinite(number) || number <= 0) {
        return '0₫';
    }
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0
    }).format(number);
}

function formatDateTime(value) {
    if (!value) {
        return '';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return '';
    }

    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

function getValidityMessage(startValue, endValue) {
    if (!startValue && !endValue) {
        return {
            text: 'Chọn thời gian hiệu lực để tính số ngày còn lại.',
            state: ''
        };
    }

    const now = new Date();
    const startDate = startValue ? new Date(startValue) : null;
    const endDate = endValue ? new Date(endValue) : null;
    const dayMs = 24 * 60 * 60 * 1000;

    if (endDate && Number.isNaN(endDate.getTime())) {
        return {
            text: 'Thời gian kết thúc chưa hợp lệ.',
            state: 'is-expired'
        };
    }

    if (startDate && !Number.isNaN(startDate.getTime()) && now < startDate) {
        const daysUntilStart = Math.max(1, Math.ceil((startDate - now) / dayMs));
        return {
            text: `Chưa hiệu lực, bắt đầu sau ${daysUntilStart} ngày.`,
            state: 'is-waiting'
        };
    }

    if (!endDate) {
        return {
            text: 'Chưa chọn ngày kết thúc.',
            state: ''
        };
    }

    if (now > endDate) {
        return {
            text: 'Voucher đã hết hạn.',
            state: 'is-expired'
        };
    }

    const daysLeft = Math.max(1, Math.ceil((endDate - now) / dayMs));
    return {
        text: `Còn ${daysLeft} ngày hiệu lực.`,
        state: ''
    };
}

function setText(id, text) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = text;
    }
}

function setVoucherFormError(message) {
    const errorBox = document.getElementById('voucherFormError');
    if (!errorBox) {
        return;
    }

    errorBox.textContent = message || '';
    errorBox.classList.toggle('d-none', !message);
}

function clearVoucherFieldErrors() {
    document.querySelector('[name="giaTri"]')?.classList.remove('is-invalid');
    document.getElementById('giaTriToiDa')?.classList.remove('is-invalid');
    document.getElementById('donToiThieu')?.classList.remove('is-invalid');
    setVoucherFormError('');
}

function getVoucherForm() {
    return document.querySelector('.voucher-editor-layout');
}

function updateVoucherPreview() {
    const form = getVoucherForm();
    if (!form) {
        return;
    }

    const ma = form.elements.ma?.value.trim().toUpperCase() || 'VOUCHER10';
    const loai = form.elements.loai?.value || 'PERCENT';
    const giaTri = form.elements.giaTri?.value;
    const giaTriToiDa = form.elements.giaTriToiDa?.value;
    const donToiThieu = form.elements.donToiThieu?.value;
    const soLuong = form.elements.soLuongToiDa?.value;
    const trangThai = form.elements.trangThai?.value || 'ACTIVE';
    const batDau = form.elements.batDauStr?.value;
    const ketThuc = form.elements.ketThucStr?.value;

    const discountText = loai === 'PERCENT'
        ? `Giảm ${giaTri || 0}%${giaTriToiDa ? `, tối đa ${formatCurrency(giaTriToiDa)}` : ''}`
        : `Giảm ${formatCurrency(giaTri)}`;
    const dateText = batDau || ketThuc
        ? `${formatDateTime(batDau) || 'Chưa chọn'} - ${formatDateTime(ketThuc) || 'Chưa chọn'}`
        : 'Chưa chọn thời hạn';
    const isActive = trangThai === 'ACTIVE';
    const statusText = isActive ? 'Hoạt động' : 'Ngưng hoạt động';
    const statusBadge = document.getElementById('previewStatusBadge');
    const validityMessage = getValidityMessage(batDau, ketThuc);
    const remainingNotice = document.getElementById('remainingNotice');

    setText('previewCode', ma);
    setText('previewDiscount', discountText);
    setText('previewMinOrder', `Đơn từ ${formatCurrency(donToiThieu)}`);
    setText('previewQuantity', `${soLuong || 0} mã`);
    setText('previewDates', dateText);
    setText('previewRemaining', validityMessage.text);
    setText('previewStatusText', statusText);

    if (remainingNotice) {
        remainingNotice.textContent = validityMessage.text;
        remainingNotice.classList.toggle('is-expired', validityMessage.state === 'is-expired');
        remainingNotice.classList.toggle('is-waiting', validityMessage.state === 'is-waiting');
    }

    if (statusBadge) {
        statusBadge.textContent = statusText;
        statusBadge.classList.toggle('status-active', isActive);
        statusBadge.classList.toggle('status-inactive', !isActive);
    }
}

// Thiết lập trạng thái ban đầu và lắng nghe thay đổi để cập nhật panel xem trước.
document.addEventListener('DOMContentLoaded', function() {
    const form = getVoucherForm();
    toggleGiaTriToiDa();

    if (form) {
        form.addEventListener('input', function() {
            clearVoucherFieldErrors();
            updateVoucherPreview();
        });
        form.addEventListener('change', function() {
            clearVoucherFieldErrors();
            updateVoucherPreview();
        });
    }
});

// Chặn submit nếu đơn tối thiểu không đủ lớn so với tiền giảm.
const voucherForm = getVoucherForm();
if (voucherForm) {
    voucherForm.addEventListener('submit', function(e) {
    clearVoucherFieldErrors();
    const loai = document.getElementById('loaiGiam').value;
    const giaTriInput = document.querySelector('[name="giaTri"]');
    const giaTriToiDaInput = document.getElementById('giaTriToiDa');
    const donToiThieuInput = document.getElementById('donToiThieu');
    const giaTri = parseFloat(giaTriInput.value);
    const giaTriToiDa = parseFloat(giaTriToiDaInput.value);
    const donToiThieu = parseFloat(donToiThieuInput.value);

    if (loai === 'FIXED' && giaTri && donToiThieu && donToiThieu < giaTri * 5) {
        e.preventDefault();
        const minOrder = formatCurrency(giaTri * 5);
        giaTriInput.classList.add('is-invalid');
        donToiThieuInput.classList.add('is-invalid');
        setVoucherFormError('Đơn tối thiểu phải ít nhất gấp 5 lần giá trị giảm (' + minOrder + ').');
        donToiThieuInput.focus();
        return;
    }

        if (loai === 'PERCENT' && giaTriToiDa && donToiThieu && giaTriToiDa > donToiThieu * 0.3) {
            e.preventDefault();
            const limit = formatCurrency(Math.floor(donToiThieu * 0.3));
            giaTriToiDaInput.classList.add('is-invalid');
            donToiThieuInput.classList.add('is-invalid');
            setVoucherFormError('Giá trị giảm tối đa không được vượt quá 30% đơn tối thiểu (' + limit + ').');
            giaTriToiDaInput.focus();
        }
    });
}
