<template>
  <div>
    <h2>Giỏ hàng</h2>

    <div v-for="item in cart" :key="item.id">
      <p>{{ item.name }}</p>

      <input type="number" v-model.number="item.quantity" @change="updateQuantity(item)" />

      <button @click="removeItem(item.id)">Xóa</button>
    </div>

    <hr />

    <p>Tạm tính: {{ format(subtotal) }} ₫</p>

    <p v-if="discount > 0">
      Giảm giá: -{{ format(discount) }} ₫
    </p>

    <p>
      Phí ship:
      <span :style="{ color: shippingFee === 0 ? 'green' : '' }">
        {{ shippingText }}
      </span>
    </p>

    <h3>Tổng: {{ format(total) }} ₫</h3>

    <hr />

    <input v-model="voucherCode" placeholder="Nhập mã giảm giá" />
    <button @click="applyVoucher">Áp dụng</button>

    <p :class="voucherMessageClass">{{ voucherMessage }}</p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const cart = ref([])
const subtotal = ref(0)
const discount = ref(0)
const shippingFee = ref(0)
const total = ref(0)

const voucherCode = ref('')
const voucherMessage = ref('')
const voucherMessageClass = ref('')

const format = (num) => new Intl.NumberFormat('vi-VN').format(num)

const shippingText = computed(() =>
    shippingFee.value === 0 ? 'Miễn phí' : format(shippingFee.value) + '₫'
)

function updateQuantity(item) {
  axios.post(`/cart/update`, {
    itemId: item.id,
    soLuong: item.quantity
  }).then(res => {
    const data = res.data

    subtotal.value = data.total
    discount.value = data.discount
    total.value = data.totalAfterDiscount
    shippingFee.value = data.shippingFee
  })
}

function removeItem(id) {
  axios.post(`/cart/remove`, { itemId: id })
      .then(res => {
        const data = res.data

        cart.value = cart.value.filter(i => i.id !== id)
        subtotal.value = data.total
        discount.value = data.discount
        total.value = data.totalAfterDiscount
        shippingFee.value = data.shippingFee
      })
}

function applyVoucher() {
  axios.post(`/cart/apply-voucher`, { code: voucherCode.value })
      .then(res => {
        const data = res.data

        if (data.success) {
          voucherMessage.value = data.message
          voucherMessageClass.value = 'text-success'

          discount.value = data.discount
          total.value = data.totalAfterDiscount
          shippingFee.value = data.shippingFee
        } else {
          voucherMessage.value = data.message
          voucherMessageClass.value = 'text-danger'
        }
      })
}
</script>