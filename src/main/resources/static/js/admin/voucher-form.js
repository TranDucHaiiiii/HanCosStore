// Bật/tắt bắt buộc nhập giá trị giảm tối đa theo loại voucher.
function toggleGiaTriToiDa() {
    const loai = document.getElementById('loaiGiam').value;
    const input = document.getElementById('giaTriToiDa');
    const star = document.getElementById('giaTriToiDaStar');
    if (loai === 'PERCENT') {
        input.required = true;
        star.style.display = 'inline';
    } else {
        input.required = false;
        star.style.display = 'none';
    }
}

// Thiết lập trạng thái ban đầu của ô giá trị giảm tối đa khi trang tải xong.
document.addEventListener('DOMContentLoaded', toggleGiaTriToiDa);

// Chặn submit nếu mức giảm tối đa vượt quá 30% giá trị đơn tối thiểu.
document.querySelector('form').addEventListener('submit', function(e) {
    const giaTriToiDa = parseFloat(document.getElementById('giaTriToiDa').value);
    const donToiThieu = parseFloat(document.getElementById('donToiThieu').value);
    if (giaTriToiDa && donToiThieu && giaTriToiDa > donToiThieu * 0.3) {
        e.preventDefault();
        alert('Giá trị giảm tối đa không được vượt quá 30% đơn tối thiểu (' + Math.floor(donToiThieu * 0.3) + '₫).');
    }
});
