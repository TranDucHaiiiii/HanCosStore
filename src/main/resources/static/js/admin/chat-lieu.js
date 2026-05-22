function deleteChatLieu(id) {
    Swal.fire({
        title: 'Xác nhận xóa?',
        text: 'Bạn chắc chắn muốn xóa chất liệu này?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Xóa ngay',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/admin/chat-lieu/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã xóa!', 'Chất liệu đã được xóa.', 'success')
                        .then(() => location.reload());
                } else {
                    res.text().then(text => {
                        Swal.fire('Lỗi!', text || 'Không thể xóa chất liệu này.', 'error');
                    });
                }
            });
        }
    });
}
