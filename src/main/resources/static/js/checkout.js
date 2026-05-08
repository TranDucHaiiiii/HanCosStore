function toggleVoucherList() {
        const list = document.getElementById('voucherList');
        const arrow = document.getElementById('voucherArrow');
        list.classList.toggle('show');
        arrow.classList.toggle('fa-chevron-down');
        arrow.classList.toggle('fa-chevron-up');
    }

    function formatVND(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.') + '₫';
    }

    let currentShippingFee = null;
    let isShippingFeeLoading = false;
    let subtotalValue = 0;
    let discountValue = 0;

    function parseIntSafe(value, fallback) {
        const num = parseInt(value, 10);
        return Number.isNaN(num) ? fallback : num;
    }

    function updateFinalTotal() {
        const total = Math.max(0, subtotalValue - discountValue + (currentShippingFee || 0));
        const totalEl = document.getElementById('finalTotalDisplay');
        if (totalEl) {
            totalEl.textContent = formatVND(total);
        }
    }

    function setDiscountValue(value) {
        discountValue = parseIntSafe(value, 0);
        const discountInput = document.getElementById('discountValue');
        if (discountInput) discountInput.value = discountValue;
        updateFinalTotal();
    }

    function updateShippingDisplay(fee) {
        const el = document.getElementById('shippingDisplay');
        if (!el) return;
        if (fee === 0) {
            el.textContent = 'Miễn phí';
            el.style.color = '#198754';
        } else {
            el.textContent = formatVND(fee);
            el.style.color = '';
        }
    }

    function setShippingText(text, color) {
        const el = document.getElementById('shippingDisplay');
        if (!el) return;
        el.textContent = text;
        el.style.color = color || '';
    }

    function setShippingFee(fee) {
        currentShippingFee = parseIntSafe(fee, 0);
        const shippingInput = document.getElementById('shippingValue');
        if (shippingInput) shippingInput.value = currentShippingFee;
        updateShippingDisplay(currentShippingFee);
        updateFinalTotal();
    }

    function syncSelectedVoucherCode(code) {
        const voucherInput = document.getElementById('selectedVoucherCode');
        if (voucherInput) {
            voucherInput.value = code || '';
        }
    }

    function buildFeeParams() {
        const savedSelect = document.getElementById('savedAddressSelect');
        if (savedSelect && savedSelect.value) {
            return { addressId: savedSelect.value };
        }

        const provinceSelect = document.getElementById('province');
        const districtSelect = document.getElementById('district');
        const wardSelect = document.getElementById('ward');
        const detailInput = document.getElementById('detailAddress');

        const provinceText = provinceSelect?.options[provinceSelect.selectedIndex]?.text || '';
        const districtText = districtSelect?.options[districtSelect.selectedIndex]?.text || '';
        const wardText = wardSelect?.options[wardSelect.selectedIndex]?.text || '';
        const detail = detailInput?.value?.trim() || '';

        if (!provinceSelect?.value || !districtSelect?.value || !wardSelect?.value || !detail) {
            return null;
        }

        return {
            province: provinceText,
            district: districtText,
            ward: wardText,
            address: detail
        };
    }

    function refreshGhtkFee() {
        const params = buildFeeParams();
        isShippingFeeLoading = true;
        if (!params) {
            isShippingFeeLoading = false;
            setShippingText('Chọn địa chỉ để tính phí', '');
            return;
        }

        setShippingText('Đang tính phí...', '');
        const body = new URLSearchParams(params).toString();
        fetch('/api/ghtk/fee', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
            body
        })
        .then(r => r.json())
        .then(data => {
            if (data && data.fee && typeof data.fee.fee === 'number') {
                setShippingFee(data.fee.fee);
                return;
            }
            if (data && data.success && data.fee && data.fee.fee != null) {
                setShippingFee(data.fee.fee);
                return;
            }
            const message = (data && data.message) ? data.message : 'Không thể tính phí';
            setShippingText(message, '#dc3545');
        })
        .catch(() => {
            setShippingText('Không thể tính phí', '#dc3545');
        })
        .finally(() => {
            isShippingFeeLoading = false;
        });
    }

    function selectVoucher(el) {
        const code = el.dataset.code;
        // Nếu đang active thì bỏ chọn
        if (el.classList.contains('active')) {
            removeVoucher();
            return;
        }
        fetch('/order/checkout/apply-voucher', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'X-Requested-With': 'XMLHttpRequest' },
            body: 'code=' + encodeURIComponent(code)
        })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                // Update UI
                document.querySelectorAll('.voucher-item').forEach(i => i.classList.remove('active'));
                el.classList.add('active');
                document.getElementById('appliedBadge').textContent = data.code;
                document.getElementById('appliedBadge').style.display = 'inline-block';
                syncSelectedVoucherCode(data.code);

                // Show discount row
                let discountRow = document.getElementById('discountRow');
                if (!discountRow) {
                    const shippingRow = document.querySelector('.font-size-0-9:last-of-type');
                    discountRow = document.createElement('div');
                    discountRow.id = 'discountRow';
                    discountRow.className = 'd-flex justify-content-between mb-3 font-size-0-9';
                    discountRow.innerHTML = '<span class="text-muted">Giảm giá</span><span id="discountAmount"></span>';
                    shippingRow.parentNode.insertBefore(discountRow, shippingRow);
                }
                document.getElementById('discountAmount').textContent = '-' + formatVND(data.discount);
                discountRow.style.display = 'flex';
                setDiscountValue(data.discount);

                // Show bỏ chọn link
                let removeLink = document.querySelector('.voucher-remove');
                if (!removeLink) {
                    const div = document.createElement('div');
                    div.className = 'text-end mt-2';
                    div.innerHTML = '<span class="voucher-remove" onclick="removeVoucher()">Bỏ chọn voucher</span>';
                    document.getElementById('voucherList').appendChild(div);
                }
            } else {
                alert(data.message);
            }
        });
    }

    function removeVoucher() {
        fetch('/order/checkout/remove-voucher', {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                document.querySelectorAll('.voucher-item').forEach(i => i.classList.remove('active'));
                document.getElementById('appliedBadge').style.display = 'none';
                syncSelectedVoucherCode('');
                const discountRow = document.getElementById('discountRow');
                if (discountRow) discountRow.style.display = 'none';
                setDiscountValue(0);
                const removeLink = document.querySelector('.voucher-remove');
                if (removeLink) removeLink.parentElement.remove();
            }
        });
    }

let locationData = [];

    document.addEventListener('DOMContentLoaded', function() {
        // Tải dữ liệu hành chính
        fetch('/data/dvhcvn.json')
            .then(response => response.json())
            .then(res => {
                locationData = res.data;
                const provinceSelect = document.getElementById('province');
                
                locationData.forEach(p => {
                    const option = new Option(p.name, p.level1_id);
                    provinceSelect.add(option);
                });

                // Auto-fill default address after location data loads
                const savedSelect = document.getElementById('savedAddressSelect');
                if (savedSelect && savedSelect.value) {
                    applySavedAddress(savedSelect);
                }
            })
            .catch(error => console.error('Lỗi tải dữ liệu địa chính:', error));

        const provinceSelect = document.getElementById('province');
        const districtSelect = document.getElementById('district');
        const wardSelect = document.getElementById('ward');
        const detailAddressInput = document.getElementById('detailAddress');
        const fullAddressInput = document.getElementById('fullAddress');
        const subtotalInput = document.getElementById('subtotalValue');
        const discountInput = document.getElementById('discountValue');
        const shippingInput = document.getElementById('shippingValue');

        subtotalValue = parseIntSafe(subtotalInput ? subtotalInput.value : 0, 0);
        discountValue = parseIntSafe(discountInput ? discountInput.value : 0, 0);
        currentShippingFee = parseIntSafe(shippingInput ? shippingInput.value : 0, 0);
        syncSelectedVoucherCode((document.getElementById('appliedBadge')?.textContent || '').trim());
        updateFinalTotal();

        // Khi chọn Tỉnh
        provinceSelect.addEventListener('change', function() {
            const provinceId = this.value;
            districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
            wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            wardSelect.disabled = true;
            
            if (provinceId) {
                const province = locationData.find(p => p.level1_id === provinceId);
                if (province && province.level2s) {
                    province.level2s.forEach(d => {document.addEventListener('DOMContentLoaded', function() {
    const bankCode = 'MB';
    fetch('https://api.vietqr.io/v2/banks')
        .then(res => res.json())
        .then(data => {
            if (data.code === '00') {
                const bank = data.data.find(b => b.code === bankCode);
                if (bank) {
                    const bankNameEl = document.getElementById('shop-bank-name');
                    const bankLogoEl = document.getElementById('shop-bank-logo');

                    bankNameEl.innerText = bank.name + ' (' + bank.shortName + ')';
                    bankLogoEl.src = bank.logo;
                    bankLogoEl.style.display = 'inline-block';
                }
            }
        })
        .catch(err => console.error('Error fetching banks:', err));
});
                        const option = new Option(d.name, d.level2_id);
                        districtSelect.add(option);
                    });
                    districtSelect.disabled = false;
                }
            } else {
                districtSelect.disabled = true;
            }
            updateFullAddress();
            refreshGhtkFee();
        });

        // Khi chọn Quận
        districtSelect.addEventListener('change', function() {
            const provinceId = provinceSelect.value;
            const districtId = this.value;
            wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            
            if (districtId) {
                const province = locationData.find(p => p.level1_id === provinceId);
                const district = province.level2s.find(d => d.level2_id === districtId);
                
                if (district && district.level3s) {
                    district.level3s.forEach(w => {
                        const option = new Option(w.name, w.level3_id);
                        wardSelect.add(option);
                    });
                    wardSelect.disabled = false;
                }
            } else {
                wardSelect.disabled = true;
            }
            updateFullAddress();
            refreshGhtkFee();
        });

        // Khi chọn Xã
        wardSelect.addEventListener('change', function() {
            updateFullAddress();
            refreshGhtkFee();
        });
        
        // Khi nhập địa chỉ chi tiết
        detailAddressInput.addEventListener('input', function() {
            updateFullAddress();
            refreshGhtkFee();
        });

        function updateFullAddress() {
            const pText = provinceSelect.options[provinceSelect.selectedIndex]?.text || '';
            const dText = districtSelect.options[districtSelect.selectedIndex]?.text || '';
            const wText = wardSelect.options[wardSelect.selectedIndex]?.text || '';
            const detail = detailAddressInput.value.trim();

            let full = detail;
            if (wText && wText !== 'Chọn Phường/Xã') full += (full ? ', ' : '') + wText;
            if (dText && dText !== 'Chọn Quận/Huyện') full += (full ? ', ' : '') + dText;
            if (pText && pText !== 'Chọn Tỉnh/Thành') full += (full ? ', ' : '') + pText;

            fullAddressInput.value = full;
        }

        // Trước khi submit, kiểm tra lại địa chỉ
        document.querySelector('form').addEventListener('submit', function(e) {
            updateFullAddress();
            if (!fullAddressInput.value || provinceSelect.value === '' || districtSelect.value === '' || wardSelect.value === '') {
                e.preventDefault();
                alert('Vui lòng chọn đầy đủ thông tin địa chỉ!');
            }
        });

        document.querySelector('form').addEventListener('submit', function(e) {
            if (isShippingFeeLoading) {
                e.preventDefault();
                alert('Phi van chuyen dang duoc tinh, vui long thu lai sau vai giay.');
            }
        });
    });

    function applySavedAddress(selectEl) {
        const opt = selectEl.options[selectEl.selectedIndex];
        const hoTenInput = document.querySelector('input[name="hoTen"]');
        const sdtInput = document.querySelector('input[name="soDienThoai"]');
        const provinceSelect = document.getElementById('province');
        const districtSelect = document.getElementById('district');
        const wardSelect = document.getElementById('ward');
        const detailInput = document.getElementById('detailAddress');

        if (!opt.value) {
            // Reset to user defaults
            provinceSelect.value = '';
            provinceSelect.dispatchEvent(new Event('change'));
            detailInput.value = '';
            refreshGhtkFee();
            return;
        }

        const hoTen = opt.dataset.hoten;
        const sdt = opt.dataset.sdt;
        const tinh = opt.dataset.tinh;
        const huyen = opt.dataset.huyen;
        const xa = opt.dataset.xa;
        const chiTiet = opt.dataset.chitiet;

        hoTenInput.value = hoTen;
        sdtInput.value = sdt;
        detailInput.value = chiTiet;

        // Match province by name
        const matchName = (full, short) => full === short || full.endsWith(short) || short.endsWith(full);

        const prov = locationData.find(p => matchName(p.name, tinh));
        if (prov) {
            provinceSelect.value = prov.level1_id;
            // Populate districts
            districtSelect.innerHTML = '<option value="">Chọn Quận/Huyện</option>';
            wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
            wardSelect.disabled = true;
            prov.level2s.forEach(d => districtSelect.add(new Option(d.name, d.level2_id)));
            districtSelect.disabled = false;

            const dist = prov.level2s.find(d => matchName(d.name, huyen));
            if (dist) {
                districtSelect.value = dist.level2_id;
                // Populate wards
                wardSelect.innerHTML = '<option value="">Chọn Phường/Xã</option>';
                dist.level3s.forEach(w => wardSelect.add(new Option(w.name, w.level3_id)));
                wardSelect.disabled = false;

                const ward = dist.level3s.find(w => matchName(w.name, xa));
                if (ward) wardSelect.value = ward.level3_id;
            }
        }

        // Update hidden full address
        const fullAddressInput = document.getElementById('fullAddress');
        let full = chiTiet;
        const wText = wardSelect.options[wardSelect.selectedIndex]?.text || '';
        const dText = districtSelect.options[districtSelect.selectedIndex]?.text || '';
        const pText = provinceSelect.options[provinceSelect.selectedIndex]?.text || '';
        if (wText && wText !== 'Chọn Phường/Xã') full += ', ' + wText;
        if (dText && dText !== 'Chọn Quận/Huyện') full += ', ' + dText;
        if (pText && pText !== 'Chọn Tỉnh/Thành') full += ', ' + pText;
        fullAddressInput.value = full;

        refreshGhtkFee();
    }
