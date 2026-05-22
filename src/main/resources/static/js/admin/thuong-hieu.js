function deleteThuongHieu(id) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: 'Bạn chắc chắn muốn xóa thương hiệu này?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa ngay',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/admin/thuong-hieu/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã xóa!', 'Thương hiệu đã được xóa.', 'success')
                        .then(() => location.reload());
                } else {
                    res.text().then(text => {
                        Swal.fire('Lỗi!', text || 'Không thể xóa thương hiệu này.', 'error');
                    });
                }
            });
        }
    });
}
