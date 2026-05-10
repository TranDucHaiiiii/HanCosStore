const initialProduct = window.initialProduct || {};
const API_URL = '/api';
const pendingUploads = new Set();
let activeGalleryImageIndex = null;
let activeColorImageIndex = null;

const COLORS = ["Đen","Trắng","Đỏ","Xanh dương","Xanh lá","Vàng","Cam","Tím","Hồng","Nâu","Xám","Be"];
const SIZE_LETTERS = ["XS","S","M","L","XL","XXL"];
const SIZE_PANTS = ["28","29","30","31","32","33","34","35","36","37","38"];

// Khởi tạo dữ liệu form sửa sản phẩm, biến thể, gallery và sự kiện UI.
document.addEventListener('DOMContentLoaded', async () => {
    // Load danh mục con
    if (document.getElementById('parentDanhMucId').value) {
        await loadChildren();
        if (initialProduct && initialProduct.danhMucId) {
            document.getElementById('danhMucId').value = initialProduct.danhMucId;
        }
    }

    // Populate existing variants/images/color-images
    if (initialProduct && initialProduct.bienThes && initialProduct.bienThes.length) {
        initialProduct.bienThes.forEach(bt => addVariant(bt));
    } else {
        addVariant();
    }

    updateAllSizeSelects();

    if (initialProduct && initialProduct.hinhAnhSanPhams && initialProduct.hinhAnhSanPhams.length) {
        initialProduct.hinhAnhSanPhams.forEach(img => addImage(img));
    } else {
        addImage();
    }

    if (initialProduct && initialProduct.hinhAnhMauSacs && initialProduct.hinhAnhMauSacs.length) {
        initialProduct.hinhAnhMauSacs.forEach(cimg => addColorImage(cimg));
    }

    const parentDanhMucEl = document.getElementById('parentDanhMucId');
    const danhMucEl = document.getElementById('danhMucId');

    if (parentDanhMucEl) {
        parentDanhMucEl.addEventListener('change', () => setTimeout(updateAllSizeSelects, 0));
    }
    if (danhMucEl) {
        danhMucEl.addEventListener('change', updateAllSizeSelects);
    }

    const pasteArea = document.getElementById('imagePasteArea');
    if (pasteArea) {
        pasteArea.addEventListener('click', () => pasteArea.focus());
        pasteArea.addEventListener('paste', handleGalleryPaste);
    }

    const imagesListEl = document.getElementById('imagesList');
    if (imagesListEl) {
        imagesListEl.addEventListener('click', (e) => {
            const item = e.target.closest('.image-item');
            if (item) {
                setActiveGalleryImage(item);
            }
        });
    }

    const colorPasteArea = document.getElementById('colorImagePasteArea');
    if (colorPasteArea) {
        colorPasteArea.addEventListener('click', () => colorPasteArea.focus());
        colorPasteArea.addEventListener('paste', handleColorImagePaste);
    }

    const colorImagesListEl = document.getElementById('colorImagesList');
    if (colorImagesListEl) {
        colorImagesListEl.addEventListener('click', (e) => {
            const item = e.target.closest('.image-item');
            if (item) {
                setActiveColorImage(item);
            }
        });
    }
});

// Tải danh mục con theo danh mục cha đang chọn.
async function loadChildren() {
    const parentId = document.getElementById('parentDanhMucId').value;
    const select = document.getElementById('danhMucId');
    select.innerHTML = '<option value="">-- Chọn danh mục con --</option>';
    if (!parentId) return;

    try {
        const res = await fetch(`${API_URL}/danh-muc/${parentId}/children`, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        if (res.ok) {
            const children = await res.json();
            children.forEach(dm => {
                const opt = document.createElement('option');
                opt.value = dm.id;
                opt.textContent = dm.ten;
                select.appendChild(opt);
            });
            updateAllSizeSelects();
        }
    } catch (e) {
        console.error('loadChildren error:', e);
    }
}

// ===== Helpers =====
// Loại bỏ dấu tiếng Việt để phục vụ tạo mã/SKU ổn định.
function removeVietnameseTones(str) {
    if (!str) return "";
    str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ|À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ/g, "a");
    str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ|È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ/g, "e");
    str = str.replace(/ì|í|ị|ỉ|ĩ|Ì|Í|Ị|Ỉ|Ĩ/g, "i");
    str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ|Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ/g, "o");
    str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ|Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ/g, "u");
    str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ|Ỳ|Ý|Ỵ|Ỷ|Ỹ/g, "y");
    str = str.replace(/đ|Đ/g, "d");
    return str;
}

// Chuyển chuỗi thành token viết hoa dùng trong mã SKU.
function toSkuToken(s) {
    return removeVietnameseTones(s)
        .trim()
        .toUpperCase()
        .replace(/\s+/g, "_");
}

// Chuẩn hóa tên danh mục để suy luận loại size.
function normalizeCategoryName(name) {
    return removeVietnameseTones((name || "").toLowerCase());
}

// Lấy tên danh mục con hoặc danh mục cha đang được chọn.
function getSelectedCategoryName() {
    const child = document.getElementById('danhMucId');
    const parent = document.getElementById('parentDanhMucId');

    if (child && child.value && child.selectedIndex >= 0) {
        return child.options[child.selectedIndex]?.text || "";
    }

    if (parent && parent.value && parent.selectedIndex >= 0) {
        return parent.options[parent.selectedIndex]?.text || "";
    }

    return "";
}

// Chọn danh sách size phù hợp theo danh mục sản phẩm.
function getSizeOptions() {
    const name = normalizeCategoryName(getSelectedCategoryName());

    if (name.includes('quan')) {
        return SIZE_PANTS;
    }

    if (name.includes('ao')) {
        return SIZE_LETTERS;
    }

    return SIZE_LETTERS;
}

// Cập nhật option size cho một select và giữ lại giá trị cũ nếu còn hợp lệ.
function updateSizeSelect(selectEl) {
    if (!selectEl) return;
    const current = selectEl.value;
    const options = getSizeOptions();
    selectEl.innerHTML = `<option value="">-- Chọn size --</option>${renderOptions(options, current)}`;
    if (options.includes(current)) {
        selectEl.value = current;
    } else {
        selectEl.value = "";
    }
}

// Cập nhật toàn bộ select size của các biến thể hiện có.
function updateAllSizeSelects() {
    document.querySelectorAll('#variantsList select[name="kichCo"]').forEach(selectEl => {
        updateSizeSelect(selectEl);
        updateSKUFromEl(selectEl);
    });
}

// Tìm dòng biến thể từ input/select vừa đổi và cập nhật SKU của dòng đó.
function updateSKUFromEl(el) {
    const item = el.closest('.variant-item');
    if (item) updateSKUItem(item);
}

// Tự sinh mã SKU cho một dòng biến thể từ mã sản phẩm, màu và size.
function updateSKUItem(item) {
    const maSP = (document.getElementById('maSanPham').value || '').trim();
    const mauEl = item.querySelector('select[name="mauSac"]');
    const sizeEl = item.querySelector('select[name="kichCo"]');
    const skuEl = item.querySelector('input[name="maSKU"]');

    if (!mauEl || !sizeEl || !skuEl) return;

    const mau = mauEl.value || '';
    const size = sizeEl.value || '';

    if (maSP && mau && size) {
        const newSku = `${toSkuToken(maSP)}_${toSkuToken(mau)}_${toSkuToken(size)}`;
        if (skuEl.value !== newSku) {
            skuEl.value = newSku;
        }
    }
}

// Render danh sách option HTML với giá trị được chọn sẵn.
function renderOptions(arr, selected) {
    return arr.map(v => `<option value="${v}" ${selected === v ? 'selected' : ''}>${v}</option>`).join('');
}

// Đánh lại số thứ tự hiển thị cho danh sách biến thể.
function reindexVariantsUI() {
    const list = document.getElementById('variantsList');
    list.querySelectorAll('.variant-item').forEach((item, i) => {
        item.setAttribute('data-index', i);
        const num = item.querySelector('.item-number');
        if (num) num.innerHTML = `<i class="fas fa-tags"></i> Biến thể #${i + 1}`;
    });
}

// Đánh lại index, name input và preview id cho gallery ảnh sản phẩm.
function reindexImages() {
    const list = document.getElementById('imagesList');
    const items = list.querySelectorAll('.image-item');

    items.forEach((item, i) => {
        item.setAttribute('data-index', i);

        const num = item.querySelector('.item-number');
        if (num) num.innerHTML = `<i class="fas fa-image"></i> Hình ảnh #${i + 1}`;

        const file = item.querySelector('input[type="file"]');
        const hidden = item.querySelector('input[type="hidden"]');
        const thuTu = item.querySelector('input[name*="thuTu"]');
        const cb = item.querySelector('input[type="checkbox"]');
        const img = item.querySelector('img.preview-img');

        const previewId = `preview_img_${i}`;

        if (hidden) hidden.name = `images[${i}].duongDanAnh`;
        if (thuTu) thuTu.name = `images[${i}].thuTu`;
        if (cb) {
            cb.name = `images[${i}].laAnhChinh`;
            cb.id = `primary_${i}`;
            cb.setAttribute('onchange', `handlePrimaryChange(${i})`);
            const lb = item.querySelector('label[for^="primary_"]');
            if (lb) lb.setAttribute('for', `primary_${i}`);
        }
        if (img) img.id = previewId;
        if (file) file.setAttribute('onchange', `uploadFile(this, 'images[${i}].duongDanAnh', '${previewId}')`);
    });

    if (activeGalleryImageIndex !== null) {
        const selected = document.querySelector(`#imagesList .image-item[data-index="${activeGalleryImageIndex}"]`);
        if (!selected) {
            activeGalleryImageIndex = null;
        }
    }
}

// Đánh lại index, name input và preview id cho ảnh theo màu.
function reindexColorImages() {
    const list = document.getElementById('colorImagesList');
    const items = list.querySelectorAll('.image-item');

    items.forEach((item, i) => {
        item.setAttribute('data-index', i);

        const num = item.querySelector('.item-number');
        if (num) num.innerHTML = `<i class="fas fa-palette"></i> Hình màu #${i + 1}`;

        const sel = item.querySelector('select[name*=".mauSac"]');
        const file = item.querySelector('input[type="file"]');
        const hidden = item.querySelector('input[type="hidden"]');
        const img = item.querySelector('img.preview-img');

        const previewId = `preview_color_img_${i}`;

        if (sel) sel.name = `colorImages[${i}].mauSac`;
        if (hidden) hidden.name = `colorImages[${i}].duongDanAnh`;
        if (img) img.id = previewId;
        if (file) file.setAttribute('onchange', `uploadFile(this, 'colorImages[${i}].duongDanAnh', '${previewId}')`);
    });

    if (activeColorImageIndex !== null) {
        const selected = document.querySelector(`#colorImagesList .image-item[data-index="${activeColorImageIndex}"]`);
        if (!selected) {
            activeColorImageIndex = null;
        }
    }
}

// ===== Variants =====
// Thêm một dòng biến thể mới hoặc render biến thể đã có từ dữ liệu ban đầu.
function addVariant(data = null) {
    const list = document.getElementById('variantsList');
    const index = list.children.length;

    const html = `
      <div class="variant-item" data-index="${index}">
        <input type="hidden" name="id" value="${data ? (data.id ?? '') : ''}">

        <div class="item-header">
          <span class="item-number"><i class="fas fa-tags"></i> Biến thể #${index + 1}</span>
          <button type="button" class="btn btn-danger" onclick="removeVariant(this)">
            <i class="fas fa-trash"></i> Xóa
          </button>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Màu Sắc <span class="required">*</span></label>
            <select name="mauSac" onchange="updateSKUFromEl(this)">
              <option value="">-- Chọn màu --</option>
              ${renderOptions(COLORS, data ? data.mauSac : '')}
            </select>
          </div>

          <div class="form-group">
            <label>Kích Cỡ <span class="required">*</span></label>
                        <select name="kichCo" onchange="updateSKUFromEl(this)">
                            <option value="">-- Chọn size --</option>
                            ${renderOptions(getSizeOptions(), data ? data.kichCo : '')}
                        </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Mã SKU <span class="required">*</span></label>
            <input type="text" name="maSKU" readonly
                   value="${data ? (data.maSKU ?? '') : ''}"
                   placeholder="AO_PHAO_AKP_DEN_S">
          </div>

          <div class="form-group">
            <label>Số Lượng Tồn <span class="required">*</span></label>
            <input type="number" name="soLuongTon"
                   value="${data ? (data.soLuongTon ?? 0) : 0}">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Giá Bán <span class="required">*</span></label>
            <input type="number" name="gia"
                   value="${data ? (data.gia ?? 0) : 0}">
          </div>

          <div class="form-group">
            <label>Giá Gốc</label>
            <input type="number" name="giaGoc"
                   value="${data && data.giaGoc != null ? data.giaGoc : ''}">
          </div>
        </div>
      </div>
    `;

    list.insertAdjacentHTML('beforeend', html);

    // auto generate sku for new item if has enough
    const item = list.lastElementChild;
    if (item) updateSKUItem(item);
}

// Xóa dòng biến thể khỏi giao diện và đánh lại số thứ tự.
function removeVariant(btn) {
    const item = btn.closest('.variant-item');
    if (item) item.remove();
    reindexVariantsUI();
}

// Theo dõi các upload đang chạy để chờ hoàn tất trước khi submit.
function trackUpload(promise) {
    pendingUploads.add(promise);
    promise.finally(() => pendingUploads.delete(promise));
    return promise;
}

// ===== Upload (shared) =====
// Upload file ảnh, cập nhật preview và ghi đường dẫn trả về vào input hidden.
async function uploadFile(input, hiddenInputName, previewId) {
    const uploadPromise = (async () => {
        const file = input.files && input.files[0];
        if (!file) return;

        // local preview
        const preview = document.getElementById(previewId);
        if (preview) {
            preview.src = URL.createObjectURL(file);
            preview.classList.add('show');
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            const res = await fetch(`${API_URL}/upload`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                body: formData
            });

            if (res.ok) {
                const path = await res.text();
                const item = input.closest('.image-item');
                const hidden = item
                    ? item.querySelector('input[type="hidden"]')
                    : document.querySelector(`input[name="${hiddenInputName}"]`);
                if (hidden) hidden.value = path;
            } else {
                const msg = await res.text();
                Swal.fire('Lỗi!', `Upload thất bại: ${msg}`, 'error');
            }
        } catch (e) {
            Swal.fire('Lỗi!', `Upload lỗi kết nối: ${e.message}`, 'error');
        }
    })();

    return trackUpload(uploadPromise);
}

// ===== Images Gallery =====
// Thêm một dòng ảnh gallery mới hoặc render ảnh gallery đã có.
function addImage(data = null) {
    const list = document.getElementById('imagesList');
    const index = list.children.length;
    const previewId = `preview_img_${index}`;

    const html = `
      <div class="image-item" data-index="${index}">
        <div class="item-header">
          <span class="item-number"><i class="fas fa-image"></i> Hình ảnh #${index + 1}</span>
          <button type="button" class="btn btn-danger" onclick="removeImage(this)">
            <i class="fas fa-trash"></i> Xóa
          </button>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Chọn Ảnh <span class="required">*</span></label>

            <input type="file"
                   class="file-upload"
                   accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,.tif,.tiff,.heic,.heif,image/*"
                   onchange="uploadFile(this, 'images[${index}].duongDanAnh', '${previewId}')">

            <input type="hidden" name="images[${index}].duongDanAnh" value="${data ? (data.duongDanAnh ?? '') : ''}">

            <img id="${previewId}" class="preview-img ${data && data.duongDanAnh ? 'show' : ''}"
                 src="${data && data.duongDanAnh ? data.duongDanAnh : ''}" alt="Preview">
          </div>

          <div class="form-group">
            <label>Thứ Tự</label>
            <input type="number" name="images[${index}].thuTu"
                   value="${data ? (data.thuTu ?? (index + 1)) : (index + 1)}">

            <div class="checkbox-group">
              <input type="checkbox" name="images[${index}].laAnhChinh" id="primary_${index}"
                     ${data && data.laAnhChinh ? 'checked' : (index === 0 ? 'checked' : '')}
                     onchange="handlePrimaryChange(${index})">
              <label for="primary_${index}">Đặt làm ảnh chính</label>
            </div>
          </div>
        </div>
      </div>
    `;

    list.insertAdjacentHTML('beforeend', html);
    return index;
}

// Xóa một ảnh gallery khỏi giao diện và đánh lại index.
function removeImage(btn) {
    const item = btn.closest('.image-item');
    if (item) item.remove();
    reindexImages();
}

// Đánh dấu ảnh gallery đang được chọn để dán ảnh từ clipboard vào đúng vị trí.
function setActiveGalleryImage(item) {
    const allItems = document.querySelectorAll('#imagesList .image-item');
    allItems.forEach((el) => {
        el.style.outline = 'none';
        el.style.boxShadow = 'none';
    });
    item.style.outline = '2px solid #8b4d4d';
    item.style.boxShadow = '0 0 0 3px rgba(139,77,77,0.15)';

    const idx = parseInt(item.getAttribute('data-index'), 10);
    activeGalleryImageIndex = Number.isNaN(idx) ? null : idx;
}

// Lấy ảnh gallery mục tiêu khi paste, ưu tiên ảnh đang được chọn.
function getTargetGalleryItem() {
    if (activeGalleryImageIndex !== null) {
        const selected = document.querySelector(`#imagesList .image-item[data-index="${activeGalleryImageIndex}"]`);
        if (selected) return selected;
    }
    return document.querySelector('#imagesList .image-item');
}

// Gán file được tạo từ clipboard vào input file.
function setFileToInput(input, file) {
    const dt = new DataTransfer();
    dt.items.add(file);
    input.files = dt.files;
}

// Chuẩn hóa tên file ảnh paste để có tên file hợp lệ khi upload.
function normalizePastedImage(file, seq) {
    const ext = (file.type && file.type.includes('/')) ? file.type.split('/')[1] : 'png';
    const safeExt = (ext || 'png').replace(/[^a-zA-Z0-9]/g, '').toLowerCase() || 'png';
    return new File([file], `pasted_${Date.now()}_${seq}.${safeExt}`, { type: file.type || 'image/png' });
}

// Xử lý paste ảnh vào gallery và tự upload ảnh đó.
function handleGalleryPaste(event) {
    const items = event.clipboardData?.items || [];
    const imageFiles = [];

    for (const item of items) {
        if (item.kind !== 'file') continue;
        const file = item.getAsFile();
        if (file && file.type && file.type.startsWith('image/')) {
            imageFiles.push(file);
        }
    }

    if (imageFiles.length === 0) return;
    event.preventDefault();

    const file = normalizePastedImage(imageFiles[0], 1);

    let imageItem = getTargetGalleryItem();
    if (!imageItem) {
        const imageIndex = addImage();
        imageItem = document.querySelector(`#imagesList .image-item[data-index="${imageIndex}"]`);
    }
    if (!imageItem) return;

    setActiveGalleryImage(imageItem);

    const imageIndex = parseInt(imageItem.getAttribute('data-index'), 10);
    const fileInput = imageItem.querySelector('input[type="file"].file-upload');
    if (!fileInput || Number.isNaN(imageIndex)) return;

    setFileToInput(fileInput, file);
    uploadFile(fileInput, `images[${imageIndex}].duongDanAnh`, `preview_img_${imageIndex}`);
}

// Đảm bảo chỉ một ảnh gallery được chọn làm ảnh chính.
function handlePrimaryChange(index) {
    const checkboxes = document.querySelectorAll('#imagesList input[type="checkbox"]');
    checkboxes.forEach((cb, i) => { if (i !== index) cb.checked = false; });
}

// ===== Color Images =====
// Thêm một dòng ảnh theo màu mới hoặc render ảnh màu đã có.
function addColorImage(data = null) {
    const list = document.getElementById('colorImagesList');
    const index = list.children.length;
    const previewId = `preview_color_img_${index}`;

    const html = `
      <div class="image-item" data-index="${index}">
        <div class="item-header">
          <span class="item-number"><i class="fas fa-palette"></i> Hình màu #${index + 1}</span>
          <button type="button" class="btn btn-danger" onclick="removeColorImage(this)">
            <i class="fas fa-trash"></i> Xóa
          </button>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Màu Sắc <span class="required">*</span></label>
            <select name="colorImages[${index}].mauSac">
              <option value="">-- Chọn màu --</option>
              ${renderOptions(COLORS, data ? data.mauSac : '')}
            </select>
          </div>

          <div class="form-group">
            <label>Chọn Ảnh <span class="required">*</span></label>

            <input type="file"
                   class="file-upload"
                   accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,.tif,.tiff,.heic,.heif,image/*"
                   onchange="uploadFile(this, 'colorImages[${index}].duongDanAnh', '${previewId}')">

            <input type="hidden" name="colorImages[${index}].duongDanAnh" value="${data ? (data.duongDanAnh ?? '') : ''}">

            <img id="${previewId}" class="preview-img ${data && data.duongDanAnh ? 'show' : ''}"
                 src="${data && data.duongDanAnh ? data.duongDanAnh : ''}" alt="Preview">
          </div>
        </div>
      </div>
    `;

    list.insertAdjacentHTML('beforeend', html);
    return index;
}

// Xóa một ảnh theo màu khỏi giao diện và đánh lại index.
function removeColorImage(btn) {
    const item = btn.closest('.image-item');
    if (item) item.remove();
    reindexColorImages();
}

// Đánh dấu ảnh màu đang được chọn để dán ảnh từ clipboard vào đúng vị trí.
function setActiveColorImage(item) {
    const allItems = document.querySelectorAll('#colorImagesList .image-item');
    allItems.forEach((el) => {
        el.style.outline = 'none';
        el.style.boxShadow = 'none';
    });
    item.style.outline = '2px solid #8b4d4d';
    item.style.boxShadow = '0 0 0 3px rgba(139,77,77,0.15)';

    const idx = parseInt(item.getAttribute('data-index'), 10);
    activeColorImageIndex = Number.isNaN(idx) ? null : idx;
}

// Lấy ảnh màu mục tiêu khi paste, ưu tiên ảnh màu đang được chọn.
function getTargetColorImageItem() {
    if (activeColorImageIndex !== null) {
        const selected = document.querySelector(`#colorImagesList .image-item[data-index="${activeColorImageIndex}"]`);
        if (selected) return selected;
    }
    return document.querySelector('#colorImagesList .image-item');
}

// Xử lý paste ảnh vào danh sách ảnh theo màu và tự upload ảnh đó.
function handleColorImagePaste(event) {
    const items = event.clipboardData?.items || [];
    const imageFiles = [];

    for (const item of items) {
        if (item.kind !== 'file') continue;
        const file = item.getAsFile();
        if (file && file.type && file.type.startsWith('image/')) {
            imageFiles.push(file);
        }
    }

    if (imageFiles.length === 0) return;
    event.preventDefault();

    const file = normalizePastedImage(imageFiles[0], 1);

    let colorItem = getTargetColorImageItem();
    if (!colorItem) {
        const colorIndex = addColorImage();
        colorItem = document.querySelector(`#colorImagesList .image-item[data-index="${colorIndex}"]`);
    }
    if (!colorItem) return;

    setActiveColorImage(colorItem);

    const colorIndex = parseInt(colorItem.getAttribute('data-index'), 10);
    const fileInput = colorItem.querySelector('input[type="file"].file-upload');
    if (!fileInput || Number.isNaN(colorIndex)) return;

    setFileToInput(fileInput, file);
    uploadFile(fileInput, `colorImages[${colorIndex}].duongDanAnh`, `preview_color_img_${colorIndex}`);
}

// ===== Sync Colors (from variants -> colorImages) =====
// Chuẩn hóa màu thành khóa so sánh để tránh trùng khi đồng bộ ảnh màu.
function normalizeColorKey(color) {
    return toSkuToken(color);
}

// Lấy danh sách màu duy nhất từ các biến thể hiện có.
function getVariantColors() {
    const colors = Array.from(document.querySelectorAll('#variantsList select[name="mauSac"]'))
        .map(el => (el.value || '').trim())
        .filter(v => v !== "");

    const seen = new Set();
    const unique = [];
    for (const c of colors) {
        const key = normalizeColorKey(c);
        if (!seen.has(key)) {
            seen.add(key);
            unique.push(c);
        }
    }
    return unique;
}

// Lấy tập màu đã có ảnh màu để tránh thêm trùng khi đồng bộ.
function getExistingColorImageKeys() {
    const keys = new Set();
    document.querySelectorAll('#colorImagesList select[name^="colorImages["][name$="].mauSac"]').forEach(sel => {
        const v = (sel.value || '').trim();
        if (v) keys.add(normalizeColorKey(v));
    });
    return keys;
}

// Đồng bộ màu từ biến thể sang danh sách ảnh màu.
function syncColors() {
    const variantColors = getVariantColors();
    if (variantColors.length === 0) {
        Swal.fire('Thông báo', 'Chưa có màu trong biến thể để đồng bộ.', 'info');
        return;
    }

    const existingKeys = getExistingColorImageKeys();
    let added = 0;

    variantColors.forEach(color => {
        const key = normalizeColorKey(color);
        if (!existingKeys.has(key)) {
            addColorImage({ mauSac: color, duongDanAnh: "" });
            existingKeys.add(key);
            added++;
        }
    });

    if (added === 0) {
        Swal.fire('OK', 'Tất cả màu trong biến thể đã có Hình màu rồi.', 'success');
    } else {
        Swal.fire('Thành công', `Đã thêm ${added} Hình màu mới.`, 'success');
    }
}

// ===== Submit =====
// Gom dữ liệu form, chờ upload hoàn tất và gửi request cập nhật sản phẩm.
document.getElementById('editForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    document.getElementById('loading').classList.add('show');

    if (pendingUploads.size > 0) {
        await Promise.allSettled(Array.from(pendingUploads));
    }

    // Ensure latest SKU update for all variants before submit
    document.querySelectorAll('#variantsList .variant-item').forEach(item => updateSKUItem(item));

    const dto = {
        id: parseInt(document.getElementById('productId').value),
        ten: document.getElementById('ten').value.trim(),
        chatLieu: document.getElementById('chatLieu').value.trim(),
        gioiTinh: document.getElementById('gioiTinh').value,
        danhMucId: document.getElementById('danhMucId').value
            ? parseInt(document.getElementById('danhMucId').value)
            : (document.getElementById('parentDanhMucId').value ? parseInt(document.getElementById('parentDanhMucId').value) : null),
        moTaNgan: document.getElementById('moTaNgan').value.trim(),
        moTa: document.getElementById('moTa').value.trim(),

        bienThes: Array.from(document.querySelectorAll('#variantsList .variant-item')).map(el => ({
            id: el.querySelector('input[name="id"]').value ? parseInt(el.querySelector('input[name="id"]').value) : null,
            mauSac: el.querySelector('select[name="mauSac"]').value,
            kichCo: el.querySelector('select[name="kichCo"]').value,
            maSKU: el.querySelector('input[name="maSKU"]').value,
            soLuongTon: parseInt(el.querySelector('input[name="soLuongTon"]').value) || 0,
            gia: parseFloat(el.querySelector('input[name="gia"]').value) || 0,
            giaGoc: el.querySelector('input[name="giaGoc"]').value ? parseFloat(el.querySelector('input[name="giaGoc"]').value) : null,
            trangThai: 'ACTIVE'
        })),

        hinhAnhSanPhams: Array.from(document.querySelectorAll('#imagesList .image-item')).map((el, i) => ({
            duongDanAnh: el.querySelector('input[type="hidden"]').value,
            thuTu: parseInt(el.querySelector('input[name*=".thuTu"]').value) || (i + 1),
            laAnhChinh: !!el.querySelector('input[type="checkbox"]')?.checked
        })).filter(img => img.duongDanAnh && img.duongDanAnh.trim() !== ''),

        hinhAnhMauSacs: Array.from(document.querySelectorAll('#colorImagesList .image-item')).map(el => ({
            mauSac: el.querySelector('select[name*=".mauSac"]').value,
            duongDanAnh: el.querySelector('input[type="hidden"]').value
        })).filter(img => img.mauSac && img.mauSac.trim() !== '' && img.duongDanAnh && img.duongDanAnh.trim() !== '')
    };

    try {
        const res = await fetch(`${API_URL}/san-pham/${dto.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(dto)
        });

        document.getElementById('loading').classList.remove('show');

        if (res.ok) {
            Swal.fire('Thành công!', 'Đã cập nhật sản phẩm.', 'success')
                .then(() => window.location.href = '/san-pham');
        } else {
            const contentType = res.headers.get('content-type') || '';
            const msg = contentType.includes('application/json')
                ? ((await res.json()).message || 'Cập nhật sản phẩm thất bại.')
                : await res.text();
            Swal.fire('Lỗi!', msg, 'error');
        }
    } catch (e2) {
        document.getElementById('loading').classList.remove('show');
        Swal.fire('Lỗi!', e2.message, 'error');
    }
});
