function deleteVoucher(id) {
    Swal.fire({
        title: 'Xác nhận vô hiệu hóa?',
        text: "Bạn muốn chuyển voucher này sang trạng thái Inactive?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#f0ad4e',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/admin/vouchers/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => res.text()).then(data => {
                if (data === 'SUCCESS') {
                    Swal.fire('Thành công!', 'Voucher đã được chuyển sang Inactive.', 'success')
                        .then(() => location.reload());
                } else {
                    Swal.fire('Lỗi!', 'Không thể vô hiệu hóa voucher: ' + data, 'error');
                }
            });
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('voucherFilterForm');
    if (!form) return;

    let searchTimer = null;
    const submitFilters = () => {
        form.requestSubmit ? form.requestSubmit() : form.submit();
    };

    form.querySelectorAll('select').forEach(select => {
        select.addEventListener('change', submitFilters);
    });

    const keywordInput = form.querySelector('input[name="q"]');
    if (keywordInput) {
        keywordInput.addEventListener('input', function() {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(submitFilters, 450);
        });
    }
});
