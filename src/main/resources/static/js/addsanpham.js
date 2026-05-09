const API_URL = '/api';
const SIZE_LETTERS = ["XS", "S", "M", "L", "XL", "XXL"];
const SIZE_PANTS = ["28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38"];
const COLORS = [
    "\u0110en", "Tr\u1eafng", "\u0110\u1ecf", "Xanh d\u01b0\u01a1ng", "Xanh l\u00e1", "V\u00e0ng",
    "Cam", "T\u00edm", "H\u1ed3ng", "N\u00e2u", "X\u00e1m", "Be"
];
const PARENT_DANH_MUC_TREE = window.PARENT_DANH_MUC_TREE || [];
const skuPreviewCache = new Map();
//
async function ensureMaSanPham() {
    const input = document.getElementById('maSanPham');
    if (!input) return;
    if (input.value.trim()) return;

    try {
        const ten = (document.getElementById('ten')?.value || '').trim();
        const response = await fetch('/api/san-pham/generate-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ ten })
        });
        const data = await response.json();
        if (response.ok && data.success && data.code) {
            input.value = data.code;
            updateAllSKUs();
        }
    } catch (error) {
        // Keep quiet to avoid blocking form usage when code API is temporarily unavailable
    }
}

function normalizeCategoryName(name) {
    return removeVietnameseTones((name || "").toLowerCase());
}

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

function updateSelectedDanhMucId() {
    const child = document.getElementById('danhMucId');
    const parent = document.getElementById('parentDanhMucId');
    const target = document.getElementById('selectedDanhMucId');

    if (!target) return;

    if (child && child.value) {
        target.value = child.value;
        return;
    }

    target.value = parent && parent.value ? parent.value : '';
}

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

function buildSizeOptionsHtml(selected = '') {
    const options = getSizeOptions();
    const optionHtml = options.map(size => `<option value="${size}" ${selected === size ? 'selected' : ''}>${size}</option>`).join('');
    return `<option value="">-- Chọn size --</option>${optionHtml}`;
}

function updateSizeSelect(selectEl) {
    if (!selectEl) return;
    const current = selectEl.value;
    selectEl.innerHTML = buildSizeOptionsHtml();
    if (getSizeOptions().includes(current)) {
        selectEl.value = current;
    }
}

function updateAllSizeSelects() {
    const variants = document.querySelectorAll('.variant-item');
    variants.forEach((item) => {
        const sizeEl = item.querySelector('select[name*=".kichCo"]');
        updateSizeSelect(sizeEl);
        const index = item.getAttribute('data-index');
        if (index !== null) {
            updateSKU(index);
        }
    });
    renderQuickSizeCheckboxes();
}

async function loadChildren() {
    const parentId = document.getElementById('parentDanhMucId').value;
    const select = document.getElementById('danhMucId');

    select.innerHTML = '<option value="">-- Chọn danh mục con --</option>';

    if (!parentId) {
        select.disabled = true;
        updateAllSizeSelects();
        updateSelectedDanhMucId();
        return;
    }
    select.disabled = false;

    const parent = PARENT_DANH_MUC_TREE.find(dm => String(dm.id) === String(parentId));
    const children = Array.isArray(parent?.danhMucCon) ? parent.danhMucCon : [];

    children.forEach(dm => {
        const option = document.createElement('option');
        option.value = dm.id;
        option.textContent = dm.ten;
        select.appendChild(option);
    });

    updateAllSizeSelects();
    updateSelectedDanhMucId();
}

function getLastVariantData() {
    const lastItem = document.querySelector('#variantsList .variant-item:last-child');
    if (!lastItem) return null;

    return {
        mauSac: lastItem.querySelector('select[name*=".mauSac"]')?.value || '',
        kichCo: lastItem.querySelector('select[name*=".kichCo"]')?.value || '',
        soLuongTon: lastItem.querySelector('input[name*=".soLuongTon"]')?.value || 0,
        gia: lastItem.querySelector('input[name*=".gia"]')?.value || 0,
        khoiLuongGram: lastItem.querySelector('input[name*=".khoiLuongGram"]')?.value || 300,
    };
}

function cloneVariant(btn) {
    const item = btn.closest('.variant-item');
    if (!item) return;

    addVariant({
        mauSac: item.querySelector('select[name*=".mauSac"]')?.value || '',
        kichCo: item.querySelector('select[name*=".kichCo"]')?.value || '',
        soLuongTon: item.querySelector('input[name*=".soLuongTon"]')?.value || 0,
        gia: item.querySelector('input[name*=".gia"]')?.value || 0,
        khoiLuongGram: item.querySelector('input[name*=".khoiLuongGram"]')?.value || 300,
    });
}

function addVariant(data = null) {
    const variantsList = document.getElementById('variantsList');
    const index = variantsList.children.length;
    const seed = data || getLastVariantData();

    const html = `
        <div class="variant-item variant-row" data-index="${index}">
            <div class="variant-row-info">
                <span class="item-number"><i class="fas fa-tags"></i> #${index + 1}</span>
                <span class="variant-badge">
                    <span class="color-badge"></span>
                    <span class="size-badge">-</span>
                </span>
            </div>
            <div class="form-group variant-field variant-color-field">
                <label>Mau Sac <span class="required">*</span></label>
                <select name="bienThes[${index}].mauSac" required onchange="updateSKUFromEl(this)">
                    <option value="">-- Chon mau --</option>
                    <option value="\u0110en" ${seed && seed.mauSac === '\u0110en' ? 'selected' : ''}>\u0110en</option>
                    <option value="Tr\u1eafng" ${seed && seed.mauSac === 'Tr\u1eafng' ? 'selected' : ''}>Tr\u1eafng</option>
                    <option value="\u0110\u1ecf" ${seed && seed.mauSac === '\u0110\u1ecf' ? 'selected' : ''}>\u0110\u1ecf</option>
                    <option value="Xanh d\u01b0\u01a1ng" ${seed && seed.mauSac === 'Xanh d\u01b0\u01a1ng' ? 'selected' : ''}>Xanh d\u01b0\u01a1ng</option>
                    <option value="Xanh l\u00e1" ${seed && seed.mauSac === 'Xanh l\u00e1' ? 'selected' : ''}>Xanh l\u00e1</option>
                    <option value="V\u00e0ng" ${seed && seed.mauSac === 'V\u00e0ng' ? 'selected' : ''}>V\u00e0ng</option>
                    <option value="Cam" ${seed && seed.mauSac === 'Cam' ? 'selected' : ''}>Cam</option>
                    <option value="T\u00edm" ${seed && seed.mauSac === 'T\u00edm' ? 'selected' : ''}>T\u00edm</option>
                    <option value="H\u1ed3ng" ${seed && seed.mauSac === 'H\u1ed3ng' ? 'selected' : ''}>H\u1ed3ng</option>
                    <option value="N\u00e2u" ${seed && seed.mauSac === 'N\u00e2u' ? 'selected' : ''}>N\u00e2u</option>
                    <option value="X\u00e1m" ${seed && seed.mauSac === 'X\u00e1m' ? 'selected' : ''}>X\u00e1m</option>
                    <option value="Be" ${seed && seed.mauSac === 'Be' ? 'selected' : ''}>Be</option>
                </select>
            </div>
            <div class="form-group variant-field variant-size-field">
                <label>Kich Co <span class="required">*</span></label>
                <select name="bienThes[${index}].kichCo" required onchange="updateSKUFromEl(this)">
                    ${buildSizeOptionsHtml(seed ? seed.kichCo : '')}
                </select>
            </div>
            <div class="form-group variant-field variant-sku-field">
                <label>Ma SKU <span class="required">*</span></label>
                <input type="text" name="bienThes[${index}].maSKU" required readonly value="" placeholder="AO_PHAO_AKP_DEN_S">
            </div>
            <div class="form-group variant-field variant-stock-field">
                <label>So Luong Ton <span class="required">*</span></label>
                <input type="number" name="bienThes[${index}].soLuongTon" required min="0" value="${seed ? seed.soLuongTon : ''}" placeholder="100">
            </div>
            <div class="form-group variant-field variant-price-field">
                <label>Gia Ban <span class="required">*</span></label>
                <input type="number" name="bienThes[${index}].gia" required min="0" value="${seed ? seed.gia : ''}" placeholder="850000">
            </div>
            <div class="form-group variant-field variant-weight-field">
                <label>Khoi Luong (gram) <span class="required">*</span></label>
                <input type="number" name="bienThes[${index}].khoiLuongGram" required min="1" value="${seed ? seed.khoiLuongGram : 300}" placeholder="500">
            </div>
            <div class="variant-row-actions">
                <button type="button" class="btn btn-secondary btn-icon-text" onclick="cloneVariant(this)">
                    <i class="fas fa-copy"></i> Nhan ban
                </button>
                <button type="button" class="btn btn-danger btn-icon-text" onclick="removeVariant(this)">
                    <i class="fas fa-trash"></i> Xoa
                </button>
            </div>
        </div>
    `;

    variantsList.insertAdjacentHTML('beforeend', html);

    const item = variantsList.lastElementChild;
    if (item) {
        updateSKU(index);
        updateVariantBadge(item);
    }
    updateVariantSummary();
}

function themBienThe(data = null) {
    addVariant(data);
}

function updateSKUFromEl(el) {
    const item = el.closest('.variant-item');
    if (!item) return;
    const index = item.getAttribute('data-index');
    if (index !== null) updateSKU(index);
}

function updateSKU(index) {
    const item = document.querySelector(`.variant-item[data-index="${index}"]`);
    if (!item) return;

    const maSP = document.getElementById('maSanPham').value.trim();
    const mauSacEl = item.querySelector('select[name*=".mauSac"]');
    const kichCoEl = item.querySelector('select[name*=".kichCo"]');
    const skuEl = item.querySelector('input[name*=".maSKU"]');

    if (!mauSacEl || !kichCoEl || !skuEl) return;

    const mauSac = mauSacEl.value;
    const kichCo = kichCoEl.value;

    if (mauSac && kichCo) {
        const requestToken = `${Date.now()}_${Math.random().toString(36).slice(2)}`;
        item.dataset.skuReqToken = requestToken;
        requestSkuPreview(maSP, mauSac, kichCo).then((sku) => {
            if (!sku) return;
            if (item.dataset.skuReqToken !== requestToken) return;
            if (skuEl.value !== sku) {
                skuEl.value = sku;
            }
        });
    }

    updateVariantBadge(item);
}

async function requestSkuPreview(maSanPham, mauSac, kichCo) {
    const cacheKey = `${maSanPham || ''}__${mauSac || ''}__${kichCo || ''}`;
    if (skuPreviewCache.has(cacheKey)) {
        return skuPreviewCache.get(cacheKey);
    }

    try {
        const response = await fetch('/api/san-pham/preview-sku', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({ maSanPham, mauSac, kichCo })
        });
        const data = await response.json();
        if (!response.ok || !data.success || !data.sku) {
            return '';
        }
        skuPreviewCache.set(cacheKey, data.sku);
        return data.sku;
    } catch (error) {
        return '';
    }
}

function updateVariantBadge(item) {
    if (!item) return;

    const colorMap = {
        "\u0110en": "#000000", "Tr\u1eafng": "#ffffff", "\u0110\u1ecf": "#ff0000",
        "Xanh d\u01b0\u01a1ng": "#0066ff", "Xanh l\u00e1": "#00aa00", "V\u00e0ng": "#ffff00",
        "Cam": "#ff8800", "T\u00edm": "#aa00ff", "H\u1ed3ng": "#ff66cc",
        "N\u00e2u": "#8b4513", "X\u00e1m": "#888888", "Be": "#d4a574"
    };

    const mauSac = item.querySelector('select[name*=".mauSac"]')?.value || '';
    const kichCo = item.querySelector('select[name*=".kichCo"]')?.value || '';
    const colorBadge = item.querySelector('.color-badge');
    const sizeBadge = item.querySelector('.size-badge');

    if (colorBadge) colorBadge.style.backgroundColor = colorMap[mauSac] || '#ccc';
    if (sizeBadge) sizeBadge.textContent = kichCo || '-';
}

function updateVariantSummary() {
    const summary = document.getElementById('variantSummary');
    if (!summary) return;

    const items = Array.from(document.querySelectorAll('#variantsList .variant-item'));
    const colors = new Set();
    const sizes = new Set();
    items.forEach(item => {
        const color = item.querySelector('select[name*=".mauSac"]')?.value || '';
        const size = item.querySelector('select[name*=".kichCo"]')?.value || '';
        if (color) colors.add(color);
        if (size) sizes.add(size);
    });

    summary.innerHTML = `
        <span><i class="fas fa-layer-group"></i> ${items.length} bien the</span>
        <span>${colors.size} mau / ${sizes.size} size</span>
    `;
}
function toSkuToken(s) {
    return removeVietnameseTones(s)
        .trim()
        .toUpperCase()
        .replace(/\s+/g, "_");
}

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

function updateAllSKUs() {
    const variants = document.querySelectorAll('.variant-item');
    variants.forEach((v) => {
        updateSKU(v.getAttribute('data-index'));
    });
}

function syncColors() {
    const variantColors = Array.from(document.querySelectorAll('#variantsList select[name*=".mauSac"]'))
        .map(el => el.value.trim())
        .filter((value, index, self) => value !== "" && self.indexOf(value) === index);

    const colorImageSections = document.querySelectorAll('#colorImagesList .image-item');

    if (variantColors.length > 0 && colorImageSections.length === 0) {
        variantColors.forEach(color => addColorImageWithColor(color));
    }
}

function getQuickVariantColors() {
    const checkboxes = Array.from(document.querySelectorAll('.color-checkbox:checked'));
    return checkboxes.length > 0 ? checkboxes.map(cb => cb.value) : ["\u0110en", "Tr\u1eafng", "X\u00e1m"];
}

function getQuickVariantSizes() {
    const checkboxes = Array.from(document.querySelectorAll('.size-checkbox:checked'));
    return checkboxes.length > 0 ? checkboxes.map(cb => cb.value) : getSizeOptions().slice();
}

function getQuickVariantMode() {
    const mode = document.getElementById('quickVariantMode')?.value || 'replace_all';
    return mode === 'add_missing' ? 'add_missing' : 'replace_all';
}

function getNumberValue(id, fallback) {
    const el = document.getElementById(id);
    const value = Number(el?.value);
    return Number.isFinite(value) ? value : fallback;
}

function getExistingVariantKeys() {
    const keys = new Set();
    document.querySelectorAll('#variantsList .variant-item').forEach((item) => {
        const color = (item.querySelector('select[name*=".mauSac"]')?.value || '').trim();
        const size = (item.querySelector('select[name*=".kichCo"]')?.value || '').trim();
        if (color && size) {
            keys.add(`${toSkuToken(color)}__${toSkuToken(size)}`);
        }
    });
    return keys;
}

function renderQuickSizeCheckboxes() {
    const sizeList = document.getElementById('sizeCheckboxList');
    if (!sizeList) return;

    const selectedSizes = new Set(
        Array.from(sizeList.querySelectorAll('.size-checkbox:checked')).map(cb => cb.value)
    );

    sizeList.innerHTML = '';
    getSizeOptions().forEach(size => {
        const label = document.createElement('label');
        label.style.cssText = 'display:flex; align-items:center; gap:8px; cursor:pointer; padding:8px; border-radius:8px; background:#fff; border:1px solid #ddd; transition:all 0.2s;';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = size;
        checkbox.className = 'size-checkbox';
        checkbox.checked = selectedSizes.has(size);

        const text = document.createElement('span');
        text.textContent = size;
        text.style.cssText = 'font-size:13px; font-weight:600; flex:1;';

        label.appendChild(checkbox);
        label.appendChild(text);

        const syncLabelState = () => {
            label.style.background = checkbox.checked ? '#f0e6e0' : '#fff';
            label.style.borderColor = checkbox.checked ? '#8b4d4d' : '#ddd';
        };
        checkbox.addEventListener('change', syncLabelState);
        syncLabelState();

        sizeList.appendChild(label);
    });
}

function dongBoMau() {
    syncColors();
}

function initQuickVariantDropdowns() {
    const colorList = document.getElementById('colorCheckboxList');
    if (colorList && colorList.children.length === 0) {
        const colorMap = {
            "\u0110en": "#000000", "Tr\u1eafng": "#ffffff", "\u0110\u1ecf": "#ff0000",
            "Xanh d\u01b0\u01a1ng": "#0066ff", "Xanh l\u00e1": "#00aa00", "V\u00e0ng": "#ffff00",
            "Cam": "#ff8800", "T\u00edm": "#aa00ff", "H\u1ed3ng": "#ff66cc",
            "N\u00e2u": "#8b4513", "X\u00e1m": "#888888", "Be": "#d4a574"
        };

        COLORS.forEach(color => {
            const label = document.createElement('label');
            label.style.cssText = 'display:flex; align-items:center; gap:8px; cursor:pointer; padding:8px; border-radius:8px; background:#fff; border:1px solid #ddd; transition:all 0.2s;';

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.value = color;
            checkbox.className = 'color-checkbox';

            const dot = document.createElement('span');
            dot.style.cssText = `width:16px; height:16px; border-radius:50%; background-color:${colorMap[color]}; border:1px solid rgba(0,0,0,0.2); flex-shrink:0;`;

            const text = document.createElement('span');
            text.textContent = color;
            text.style.cssText = 'font-size:13px; flex:1;';

            label.appendChild(checkbox);
            label.appendChild(dot);
            label.appendChild(text);

            checkbox.addEventListener('change', () => {
                label.style.background = checkbox.checked ? '#f0e6e0' : '#fff';
                label.style.borderColor = checkbox.checked ? '#8b4d4d' : '#ddd';
            });

            colorList.appendChild(label);
        });
    }

    renderQuickSizeCheckboxes();
}

function toggleQuickPanel() {
    const content = document.getElementById('quickPanelContent');
    const toggle = document.getElementById('quickPanelToggle');
    if (!content || !toggle) return;

    const isHidden = content.style.display === 'none';
    if (isHidden) {
        content.style.display = 'block';
        toggle.style.transform = 'rotate(180deg)';
    } else {
        content.style.display = 'none';
        toggle.style.transform = 'rotate(0deg)';
    }
}

function dongMoBangTaoNhanh() {
    toggleQuickPanel();
}

async function generateQuickVariants() {
    const colors = getQuickVariantColors();
    const sizes = getQuickVariantSizes();
    const mode = getQuickVariantMode();

    const result = await Swal.fire({
        title: 'Sinh nhiều biến thể?',
        text: mode === 'replace_all'
            ? 'Hệ thống sẽ thay thế toàn bộ biến thể hiện tại.'
            : 'Hệ thống chỉ thêm các biến thể còn thiếu.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sinh ngay',
        cancelButtonText: 'Huy'
    });

    if (!result.isConfirmed) return;

    try {
        const maSanPham = document.getElementById('maSanPham')?.value || '';
        const stock = getNumberValue('quickVariantStock', 0);
        const price = getNumberValue('quickVariantPrice', 0);
        const weight = getNumberValue('quickVariantWeight', 300);

        const response = await fetch('/api/san-pham/generate-variants', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                colors: colors,
                sizes: sizes,
                stock: stock,
                price: price,
                weight: weight,
                mode: mode,
                maSanPham: maSanPham
            })
        });

        if (!response.ok) {
            const err = await response.text();
            showAlert('error', `API Error: ${err}`);
            return;
        }

        const variants = await response.json();
        const list = document.getElementById('variantsList');
        if (mode === 'replace_all') list.innerHTML = '';

        const existingKeys = mode === 'add_missing' ? getExistingVariantKeys() : new Set();
        const createdKeys = new Set();
        let added = 0;

        for (const variant of variants) {
            const key = `${toSkuToken(variant.mauSac)}__${toSkuToken(variant.kichCo)}`;
            if (createdKeys.has(key) || existingKeys.has(key)) continue;
            createdKeys.add(key);
            added++;

            addVariant({
                mauSac: variant.mauSac,
                kichCo: variant.kichCo,
                soLuongTon: variant.soLuongTon,
                gia: variant.gia,
                khoiLuongGram: variant.khoiLuongGram
            });
        }

        if (added === 0) showAlert('success', 'Khong co bien the moi can them.');
        else showAlert('success', `Da them ${added} bien the.`);
    } catch (error) {
        showAlert('error', `Loi sinh bien the: ${error.message}`);
    }
}

async function taoNhanhBienThe() {
    await generateQuickVariants();
}

function addColorImageWithColor(color) {
    const colorImagesList = document.getElementById('colorImagesList');
    const index = colorImagesList.children.length;

    const html = `
        <div class="image-item" data-index="${index}">
            <div class="item-header">
                <span class="item-number"><i class="fas fa-palette"></i> Hình màu #${index + 1}</span>
                <button type="button" class="btn btn-danger" onclick="removeColorImage(${index})">
                    <i class="fas fa-trash"></i> Xóa
                </button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Màu Sắc <span class="required">*</span></label>
                    <input type="text" name="hinhAnhMauSacs[${index}].mauSac" required value="${color}" placeholder="Đen">
                </div>
                <div class="form-group">
                    <label>Chọn Ảnh <span class="required">*</span></label>
                    <input type="file"
                           accept=".jpg,.jpeg,.png,.webp,.gif,.bmp,.tif,.tiff,.heic,.heif,image/*"
                           class="file-upload"
                           onchange="handleFileUpload(this, 'hinhAnhMauSacs[${index}].duongDanAnh', 'preview_color_img_${index}')">
                    <input type="hidden" name="hinhAnhMauSacs[${index}].duongDanAnh" required>
                    <img id="preview_color_img_${index}" class="preview-img" src="" alt="Preview">
                </div>
            </div>
        </div>
    `;

    colorImagesList.insertAdjacentHTML('beforeend', html);
}

function removeVariant(target) {
    const variant = typeof target === 'number'
        ? document.querySelector(`#variantsList .variant-item[data-index="${target}"]`)
        : target.closest('.variant-item');
    if (!variant) return;
    variant.remove();
    updateVariantNumbers();
}

function updateVariantNumbers() {
    const variants = document.querySelectorAll('#variantsList .variant-item');
    variants.forEach((variant, idx) => {
        variant.setAttribute('data-index', idx);
        const mauSacEl = variant.querySelector('select[name*=".mauSac"]');
        const kichCoEl = variant.querySelector('select[name*=".kichCo"]');
        const maSkuEl = variant.querySelector('input[name*=".maSKU"]');
        const soLuongTonEl = variant.querySelector('input[name*=".soLuongTon"]');
        const giaEl = variant.querySelector('input[name*=".gia"]');
        const khoiLuongEl = variant.querySelector('input[name*=".khoiLuongGram"]');

        variant.querySelector('.item-number').innerHTML = `<i class="fas fa-tags"></i> #${idx + 1}`;

        if (mauSacEl) mauSacEl.name = `bienThes[${idx}].mauSac`;
        if (kichCoEl) kichCoEl.name = `bienThes[${idx}].kichCo`;
        if (maSkuEl) maSkuEl.name = `bienThes[${idx}].maSKU`;
        if (soLuongTonEl) soLuongTonEl.name = `bienThes[${idx}].soLuongTon`;
        if (giaEl) giaEl.name = `bienThes[${idx}].gia`;
        if (khoiLuongEl) khoiLuongEl.name = `bienThes[${idx}].khoiLuongGram`;
        updateVariantBadge(variant);
    });
    updateVariantSummary();
}
function validateBaseFields() {
    const maSanPham = (document.getElementById('maSanPham')?.value || '').trim();
    const ten = (document.getElementById('ten')?.value || '').trim();
    const parentDanhMucId = document.getElementById('parentDanhMucId')?.value || '';
    const childDanhMucId = document.getElementById('danhMucId')?.value || '';

    if (!maSanPham) {
        showAlert('error', 'Vui lòng nhập Mã sản phẩm.');
        return false;
    }
    if (!ten) {
        showAlert('error', 'Vui lòng nhập Tên sản phẩm.');
        return false;
    }
    if (!parentDanhMucId && !childDanhMucId) {
        showAlert('error', 'Vui lòng chọn danh mục.');
        return false;
    }
    return true;
}

function validateVariants() {
    const variants = Array.from(document.querySelectorAll('#variantsList .variant-item'));
    if (variants.length === 0) {
        showAlert('error', 'Vui lòng thêm ít nhất 1 biến thể!');
        return false;
    }

    const seen = new Set();
    for (const variant of variants) {
        const mau = (variant.querySelector('select[name*=".mauSac"]')?.value || '').trim();
        const size = (variant.querySelector('select[name*=".kichCo"]')?.value || '').trim();
        const sku = (variant.querySelector('input[name*=".maSKU"]')?.value || '').trim();
        const soLuongRaw = variant.querySelector('input[name*=".soLuongTon"]')?.value;
        const giaRaw = variant.querySelector('input[name*=".gia"]')?.value;
        const khoiLuongRaw = variant.querySelector('input[name*=".khoiLuongGram"]')?.value;

        if (!mau || !size) {
            showAlert('error', 'Vui lòng chọn đầy đủ màu sắc và kích cỡ cho biến thể.');
            return false;
        }
        if (!sku) {
            showAlert('error', 'Vui lòng đảm bảo mã SKU được tạo cho biến thể.');
            return false;
        }

        const soLuong = Number(soLuongRaw);
        if (!Number.isFinite(soLuong) || soLuong < 0) {
            showAlert('error', 'Số lượng tồn phải là số >= 0.');
            return false;
        }

        const gia = Number(giaRaw);
        if (!Number.isFinite(gia) || gia < 0) {
            showAlert('error', 'Giá bán phải là số >= 0.');
            return false;
        }

        const khoiLuong = Number(khoiLuongRaw);
        if (!Number.isFinite(khoiLuong) || khoiLuong <= 0) {
            showAlert('error', 'Khối lượng phải là số > 0.');
            return false;
        }

        const key = `${mau}__${size}`.toLowerCase();
        if (seen.has(key)) {
            showAlert('error', 'Không được trùng màu sắc + kích cỡ.');
            return false;
        }
        seen.add(key);
    }
    return true;
}

function collectVariantPayload() {
    return Array.from(document.querySelectorAll('#variantsList .variant-item')).map((variant) => ({
        maSKU: (variant.querySelector('input[name*=".maSKU"]')?.value || '').trim(),
        mauSac: (variant.querySelector('select[name*=".mauSac"]')?.value || '').trim(),
        kichCo: (variant.querySelector('select[name*=".kichCo"]')?.value || '').trim(),
        soLuongTon: Number(variant.querySelector('input[name*=".soLuongTon"]')?.value || 0),
        gia: Number(variant.querySelector('input[name*=".gia"]')?.value || 0),
        khoiLuongGram: Number(variant.querySelector('input[name*=".khoiLuongGram"]')?.value || 0)
    }));
}

async function validateVariantsWithServer() {
    const payload = collectVariantPayload();
    try {
        const response = await fetch('/api/san-pham/validate-variants', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok || !data.success) {
            showAlert('error', data.message || 'Du lieu bien the khong hop le.');
            return false;
        }
        return true;
    } catch (error) {
        showAlert('error', `Khong the validate bien the: ${error.message}`);
        return false;
    }
}

async function validateBaseWithServer() {
    const payload = {
        maSanPham: (document.getElementById('maSanPham')?.value || '').trim(),
        ten: (document.getElementById('ten')?.value || '').trim(),
        danhMucId: (document.getElementById('selectedDanhMucId')?.value || '').trim()
    };

    try {
        const response = await fetch('/api/san-pham/validate-base', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok || !data.success) {
            showAlert('error', data.message || 'Thong tin co ban khong hop le.');
            return false;
        }
        return true;
    } catch (error) {
        showAlert('error', `Khong the validate thong tin co ban: ${error.message}`);
        return false;
    }
}

function getNextImageIndex() {
    const images = document.querySelectorAll('#imagesList .image-item');
    if (images.length === 0) return 0;

    let maxIndex = -1;
    images.forEach((item) => {
        const idx = parseInt(item.getAttribute('data-index'), 10);
        if (!Number.isNaN(idx) && idx > maxIndex) {
            maxIndex = idx;
        }
    });
    return maxIndex + 1;
}

function addImage() {
    const imagesList = document.getElementById('imagesList');
    const index = getNextImageIndex();

    const html = `
        <div class="image-item" data-index="${index}">
            <div class="item-header">
                <span class="item-number"><i class="fas fa-image"></i> Hình ảnh #${index + 1}</span>
                <button type="button" class="btn btn-danger" onclick="removeImage(${index})">
                    <i class="fas fa-trash"></i> Xóa
                </button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Chọn Ảnh <span class="required">*</span></label>
                    <input type="file" accept="image/*" class="file-upload"
                           onchange="handleFileUpload(this, 'hinhAnhSanPhams[${index}].duongDanAnh', 'preview_img_${index}')">
                    <input type="hidden" name="hinhAnhSanPhams[${index}].duongDanAnh" required>
                    <img id="preview_img_${index}" class="preview-img" src="" alt="Preview">
                </div>
                <div class="form-group">
                    <label>Thứ Tự</label>
                    <input type="number" name="hinhAnhSanPhams[${index}].thuTu" value="${index + 1}" min="1">
                </div>
            </div>
            <div class="checkbox-group">
                <input type="checkbox" name="hinhAnhSanPhams[${index}].laAnhChinh" id="primary_${index}" ${index === 0 ? 'checked' : ''} onchange="handlePrimaryChange(${index})">
                <label for="primary_${index}">Đặt làm ảnh chính</label>
            </div>
        </div>
    `;

    imagesList.insertAdjacentHTML('beforeend', html);
    return index;
}

function themAnh() {
    addImage();
}

async function handleFileUpload(fileInput, hiddenInputName, previewId) {
    const file = fileInput.files[0];
    if (!file) return;

    const preview = document.getElementById(previewId);
    preview.src = URL.createObjectURL(file);
    preview.classList.add('show');

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`${API_URL}/upload`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: formData
        });

        if (response.ok) {
            const relativePath = await response.text();
            const hiddenInput = document.querySelector(`input[name="${hiddenInputName}"]`);
            if (hiddenInput) {
                hiddenInput.value = relativePath;
            }
        } else {
            const error = await response.text();
            showAlert('error', `Lỗi upload: ${error}`);
        }
    } catch (error) {
        showAlert('error', `Lỗi kết nối upload: ${error.message}`);
    }
}

function removeImage(index) {
    const image = document.querySelector(`#imagesList .image-item[data-index="${index}"]`);
    if (image) {
        image.remove();
        updateImageNumbers();
    }
}

function updateImageNumbers() {
    const images = document.querySelectorAll('#imagesList .image-item');
    images.forEach((image, idx) => {
        const removeBtn = image.querySelector('.item-header .btn-danger');
        const fileInput = image.querySelector('input.file-upload');
        const hiddenInput = image.querySelector('input[type="hidden"][name*=".duongDanAnh"]');
        const previewImg = image.querySelector('img.preview-img');
        const thuTuInput = image.querySelector('input[name*=".thuTu"]');
        const primaryCheckbox = image.querySelector('input[type="checkbox"][name*=".laAnhChinh"]');
        const primaryLabel = image.querySelector('.checkbox-group label');

        image.setAttribute('data-index', idx);
        image.querySelector('.item-number').innerHTML = `<i class="fas fa-image"></i> Hình ảnh #${idx + 1}`;

        if (removeBtn) {
            removeBtn.setAttribute('onclick', `removeImage(${idx})`);
        }
        if (fileInput) {
            fileInput.setAttribute('onchange', `handleFileUpload(this, 'hinhAnhSanPhams[${idx}].duongDanAnh', 'preview_img_${idx}')`);
        }
        if (hiddenInput) {
            hiddenInput.name = `hinhAnhSanPhams[${idx}].duongDanAnh`;
        }
        if (previewImg) {
            previewImg.id = `preview_img_${idx}`;
        }
        if (thuTuInput) {
            thuTuInput.name = `hinhAnhSanPhams[${idx}].thuTu`;
        }
        if (primaryCheckbox) {
            primaryCheckbox.name = `hinhAnhSanPhams[${idx}].laAnhChinh`;
            primaryCheckbox.id = `primary_${idx}`;
            primaryCheckbox.setAttribute('onchange', `handlePrimaryChange(${idx})`);
        }
        if (primaryLabel) {
            primaryLabel.setAttribute('for', `primary_${idx}`);
        }
    });
}

function setFileToInput(fileInput, file) {
    const dt = new DataTransfer();
    dt.items.add(file);
    fileInput.files = dt.files;
}

function normalizePastedFile(file, seq) {
    const ext = (file.type && file.type.includes('/')) ? file.type.split('/')[1] : 'png';
    const safeExt = (ext || 'png').replace(/[^a-zA-Z0-9]/g, '').toLowerCase() || 'png';
    const fileName = `pasted_${Date.now()}_${seq}.${safeExt}`;
    return new File([file], fileName, { type: file.type || 'image/png' });
}

function handleImagePaste(event) {
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

    imageFiles.forEach((rawFile, i) => {
        const file = normalizePastedFile(rawFile, i + 1);
        const index = addImage();
        const input = document.querySelector(`#imagesList .image-item[data-index="${index}"] input.file-upload`);
        if (!input) return;

        setFileToInput(input, file);
        handleFileUpload(input, `hinhAnhSanPhams[${index}].duongDanAnh`, `preview_img_${index}`);
    });
}

function handlePrimaryChange(index) {
    const checkboxes = document.querySelectorAll('input[name*="laAnhChinh"]');
    checkboxes.forEach((cb, i) => {
        if (i !== index) cb.checked = false;
    });
}

function addColorImage() {
    const colorImagesList = document.getElementById('colorImagesList');
    const index = colorImagesList.children.length;

    const html = `
        <div class="image-item" data-index="${index}">
            <div class="item-header">
                <span class="item-number"><i class="fas fa-palette"></i> Hình màu #${index + 1}</span>
                <button type="button" class="btn btn-danger" onclick="removeColorImage(${index})">
                    <i class="fas fa-trash"></i> Xóa
                </button>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Màu Sắc <span class="required">*</span></label>
                    <input type="text" name="hinhAnhMauSacs[${index}].mauSac" required placeholder="Đen">
                </div>
                <div class="form-group">
                    <label>Chọn Ảnh <span class="required">*</span></label>
                    <input type="file" accept="image/*" class="file-upload"
                           onchange="handleFileUpload(this, 'hinhAnhMauSacs[${index}].duongDanAnh', 'preview_color_img_${index}')">
                    <input type="hidden" name="hinhAnhMauSacs[${index}].duongDanAnh" required>
                    <img id="preview_color_img_${index}" class="preview-img" src="" alt="Preview">
                </div>
            </div>
        </div>
    `;

    colorImagesList.insertAdjacentHTML('beforeend', html);
}

function themAnhTheoMau() {
    addColorImage();
}

function removeColorImage(index) {
    const image = document.querySelector(`#colorImagesList .image-item[data-index="${index}"]`);
    if (image) {
        image.remove();
        updateColorImageNumbers();
    }
}

function updateColorImageNumbers() {
    const colorImages = document.querySelectorAll('#colorImagesList .image-item');
    colorImages.forEach((image, idx) => {
        const removeBtn = image.querySelector('.item-header .btn-danger');
        const mauSacInput = image.querySelector('input[name*=".mauSac"]');
        const fileInput = image.querySelector('input.file-upload');
        const hiddenInput = image.querySelector('input[type="hidden"][name*=".duongDanAnh"]');
        const previewImg = image.querySelector('img.preview-img');

        image.setAttribute('data-index', idx);
        image.querySelector('.item-number').innerHTML = `<i class="fas fa-palette"></i> Hình màu #${idx + 1}`;

        if (removeBtn) {
            removeBtn.setAttribute('onclick', `removeColorImage(${idx})`);
        }
        if (mauSacInput) {
            mauSacInput.name = `hinhAnhMauSacs[${idx}].mauSac`;
        }
        if (fileInput) {
            fileInput.setAttribute('onchange', `handleFileUpload(this, 'hinhAnhMauSacs[${idx}].duongDanAnh', 'preview_color_img_${idx}')`);
        }
        if (hiddenInput) {
            hiddenInput.name = `hinhAnhMauSacs[${idx}].duongDanAnh`;
        }
        if (previewImg) {
            previewImg.id = `preview_color_img_${idx}`;
        }
    });
}

function showAlert(type, message) {
    Swal.fire({
        icon: type,
        title: type === 'success' ? 'Thành công!' : 'Lỗi!',
        text: message,
        confirmButtonColor: '#8b4d4d'
    });
}

async function resetForm() {
    const result = await Swal.fire({
        title: 'Bạn có chắc chắn?',
        text: 'Tất cả dữ liệu đã nhập sẽ bị xóa!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#8b4d4d',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        document.getElementById('productForm').reset();
        document.getElementById('variantsList').innerHTML = '';
        document.getElementById('imagesList').innerHTML = '';
        document.getElementById('colorImagesList').innerHTML = '';

        addVariant();
        addImage();
        addColorImage();
        updateSelectedDanhMucId();
    }
}

async function datLaiForm() {
    await resetForm();
}

document.getElementById('productForm').addEventListener('submit', async (e) => {
    updateSelectedDanhMucId();

    const variants = document.querySelectorAll('#variantsList .variant-item');
    const images = document.querySelectorAll('#imagesList .image-item');

    if (!validateBaseFields() || !validateVariants()) {
        e.preventDefault();
        return;
    }

    const baseValid = await validateBaseWithServer();
    if (!baseValid) {
        e.preventDefault();
        return;
    }

    const serverValid = await validateVariantsWithServer();
    if (!serverValid) {
        e.preventDefault();
        return;
    }

    if (images.length === 0) {
        e.preventDefault();
        showAlert('error', 'Vui lòng thêm ít nhất 1 hình ảnh!');
        return;
    }

    document.getElementById('loading').classList.add('show');
});

window.addEventListener('DOMContentLoaded', async () => {
    await ensureMaSanPham();
    initQuickVariantDropdowns();
    addVariant();
    addImage();

    const parentDanhMucEl = document.getElementById('parentDanhMucId');
    const danhMucEl = document.getElementById('danhMucId');

    if (parentDanhMucEl) {
        parentDanhMucEl.addEventListener('change', () => {
            setTimeout(updateAllSizeSelects, 0);
            updateSelectedDanhMucId();
        });
    }

    if (danhMucEl) {
        danhMucEl.addEventListener('change', () => {
            updateAllSizeSelects();
            updateSelectedDanhMucId();
        });
    }

    const tenEl = document.getElementById('ten');
    if (tenEl) {
        tenEl.addEventListener('input', async () => {
            const maSanPhamEl = document.getElementById('maSanPham');
            if (maSanPhamEl && !maSanPhamEl.value.trim()) {
                await ensureMaSanPham();
            }
        });
    }

    const pasteArea = document.getElementById('imagePasteArea');
    if (pasteArea) {
        pasteArea.addEventListener('click', () => pasteArea.focus());
        pasteArea.addEventListener('paste', handleImagePaste);
    }

    const successAlert = document.getElementById('successAlert');
    if (successAlert && successAlert.textContent.trim()) {
        showAlert('success', successAlert.textContent.trim());
    }

    const errorAlert = document.getElementById('errorAlert');
    if (errorAlert && errorAlert.textContent.trim()) {
        showAlert('error', errorAlert.textContent.trim());
    }

    updateSelectedDanhMucId();
});




