function deleteCategory(id) {
    Swal.fire({
        title: 'Ngừng hoạt động danh mục?',
        text: "Danh mục sẽ chuyển sang trạng thái INACTIVE và không hiển thị ở các form chọn.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Ngừng hoạt động',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            fetch('/api/danh-muc/' + id, {
                method: 'DELETE',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            }).then(res => {
                if (res.ok) {
                    Swal.fire('Đã cập nhật!', 'Danh mục đã chuyển sang INACTIVE.', 'success')
                        .then(() => location.reload());
                } else {
                    res.text().then(text => {
                        Swal.fire('Lỗi!', text || 'Không thể cập nhật danh mục này.', 'error');
                    });
                }
            });
        }
    });
}
// Thêm sự kiện cho các nút xóa
document.addEventListener('DOMContentLoaded', function() {
    // Page size change
    var pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', function() {
            var url = new URL(window.location.href);
            url.searchParams.set('size', this.value);
            url.searchParams.set('page', '1');
            window.location.href = url.toString();
        });
    }

    // Go to page
    var goInput = document.getElementById('goToPageInput');
    if (goInput) {
        goInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                var page = parseInt(this.value);
                var max = parseInt(this.max);
                if (page >= 1 && page <= max) {
                    var url = new URL(window.location.href);
                    url.searchParams.set('page', page);
                    window.location.href = url.toString();
                }
            }
        });
    }
});
