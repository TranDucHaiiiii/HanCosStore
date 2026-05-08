// document.addEventListener('DOMContentLoaded', function() {
//     const bankCode = 'MSB';
//     const container = document.querySelector('.success-container');
//     const statusEl = document.getElementById('payment-status');
//
//     const statusEndpoint = container?.dataset?.statusEndpoint;
//     const successUrl = container?.dataset?.successUrl;
//
//     fetch('https://api.vietqr.io/v2/banks')
//         .then(res => res.json())
//         .then(data => {
//             if (data.code === '00' && data.data) {
//                 // Tìm kiếm theo code hoặc shortName
//                 const bank = data.data.find(b => b.code === bankCode || b.shortName === bankCode);
//                 console.log('Tìm kiếm bank MSB:', bank); // Debug log
//
//                 if (bank) {
//                     const bankNameEl = document.getElementById('shop-bank-name');
//                     const bankLogoEl = document.getElementById('shop-bank-logo');
//
//                     bankNameEl.innerText = bank.name + ' (' + bank.shortName + ')';
//                     bankLogoEl.src = bank.logo;
//                     bankLogoEl.style.display = 'inline-block';
//                 } else {
//                     console.warn('Không tìm thấy bank MSB. Danh sách banks:', data.data.map(b => ({code: b.code, shortName: b.shortName})));
//                 }
//             }
//         })
//         .catch(err => console.error('Error fetching banks:', err));
//
//     if (!statusEndpoint) {
//         return;
//     }
//     async function loadShippingFee() {
//         try {
//             const res = await fetch('/api/ghtk/fee', {
//                 method: 'POST',
//                 headers: { 'Content-Type': 'application/json' },
//                 body: JSON.stringify({
//                     address: document.getElementById("address")?.value
//                 })
//             });
//
//             const data = await res.json();
//
//             if (data && data.fee) {
//                 // cập nhật input hidden để gửi về backend
//                 const input = document.getElementById("shippingFeeInput");
//                 if (input) input.value = data.fee;
//
//                 // update UI nếu có
//                 const feeEl = document.getElementById("shippingFee");
//                 if (feeEl) feeEl.innerText = data.fee + "₫";
//             }
//         } catch (err) {
//             console.error("Lỗi load phí ship:", err);
//         }
//     }
//
//     const hasPaidParam = window.location.search.includes('paid=1');
//
//     const pollStatus = () => {
//         fetch(statusEndpoint)
//             .then(res => res.json())
//             .then(data => {
//                 if (!data || !data.status) {
//                     return;
//                 }
//                 if (data.status === 'PAID') {
//                     if (statusEl) {
//                         statusEl.textContent = 'Thanh toán thành công!';
//                         statusEl.classList.remove('text-warning');
//                         statusEl.classList.add('text-success');
//                     }
//                     if (successUrl && !hasPaidParam) {
//                         window.location.href = successUrl;
//                     }
//                 } else if (statusEl) {
//                     statusEl.textContent = 'Đang chờ thanh toán...';
//                 }
//             })
//             .catch(err => console.error('Lỗi polling trạng thái:', err));
//     };
//
//     // Poll mỗi 3 giây
//     pollStatus();
//     setInterval(pollStatus, 3000);
// });
document.addEventListener('DOMContentLoaded', function () {

    const bankCode = 'MSB';

    const container = document.querySelector('.success-container');
    const statusEl = document.getElementById('payment-status');

    const statusEndpoint = container?.dataset?.statusEndpoint;
    const successUrl = container?.dataset?.successUrl;

    // =============================
    // 🔥 1. LOAD BANK (VIETQR)
    // =============================
    fetch('https://api.vietqr.io/v2/banks')
        .then(res => res.json())
        .then(data => {
            if (data.code === '00' && data.data) {
                const bank = data.data.find(b => b.code === bankCode || b.shortName === bankCode);

                if (bank) {
                    const bankNameEl = document.getElementById('shop-bank-name');
                    const bankLogoEl = document.getElementById('shop-bank-logo');

                    if (bankNameEl) bankNameEl.innerText = bank.name + ' (' + bank.shortName + ')';
                    if (bankLogoEl) {
                        bankLogoEl.src = bank.logo;
                        bankLogoEl.style.display = 'inline-block';
                    }
                }
            }
        })
        .catch(err => console.error('Error fetching banks:', err));

    // =============================
    // 🔁 4. POLL TRẠNG THÁI SEPAY
    // =============================
    const hasPaidParam = window.location.search.includes('paid=1');

    const pollStatus = () => {
        if (!statusEndpoint) return;

        fetch(statusEndpoint)
            .then(res => res.json())
            .then(data => {
                if (!data || !data.status) return;

                if (data.status === 'PAID') {
                    if (statusEl) {
                        statusEl.textContent = 'Thanh toán thành công!';
                        statusEl.classList.remove('text-warning');
                        statusEl.classList.add('text-success');
                    }
                    if (successUrl && !hasPaidParam) {
                        window.location.href = successUrl;
                    }
                } else if (statusEl) {
                    statusEl.textContent = 'Đang chờ thanh toán...';
                }
            })
            .catch(err => console.error('Lỗi polling:', err));
    };

    // =============================
    // 🎯 5. EVENT LISTENER
    // =============================


    // 👉 poll SePay
    if (statusEndpoint) {
        pollStatus();
        setInterval(pollStatus, 3000);
    }

});