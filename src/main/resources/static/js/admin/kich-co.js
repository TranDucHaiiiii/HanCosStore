function deleteKichCo(id) {
    Swal.fire({
        title: 'Ngưng hoạt động size này?',
        text: 'Size sẽ không còn xuất hiện khi thêm biến thể mới.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/admin/kich-co/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã cập nhật!', 'Size đã chuyển sang ngưng hoạt động.', 'success')
                        .then(() => location.reload());
                } else {
                    res.text().then(text => {
                        Swal.fire('Lỗi!', text || 'Không thể cập nhật size này.', 'error');
                    });
                }
            });
        }
    });
}
