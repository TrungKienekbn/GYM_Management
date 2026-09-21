<template>
  <div class="lookup-page">
    <header class="lookup-header">
      <router-link to="/" class="brand">GYM<span>PRO</span></router-link>
      <router-link to="/shop">Về cửa hàng</router-link>
    </header>

    <el-card class="lookup-card">
      <template #header>Tra cứu đơn hàng</template>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="Mã đơn hàng">
          <el-input v-model="orderId" placeholder="VD: 12" />
        </el-form-item>
        <el-form-item label="Số điện thoại đặt hàng">
          <el-input v-model="phone" placeholder="Số điện thoại đã dùng khi đặt/mua hàng" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="search">Tra cứu</el-button>
      </el-form>
    </el-card>

    <el-card v-if="order" class="lookup-result">
      <template #header>Đơn hàng #{{ order.id }}</template>
      <p>Trạng thái: <b>{{ statusLabel(order.status) }}</b></p>
      <p>Kênh: {{ order.channel === 'POS' ? 'Mua tại quầy' : 'Mua online' }}</p>
      <p>Người nhận: {{ order.receiverName }} - {{ order.phone }}</p>
      <p v-if="order.shippingAddress">Địa chỉ: {{ order.shippingAddress }}</p>
      <el-table :data="order.items" size="small" style="margin:12px 0">
        <el-table-column prop="productName" label="Sản phẩm" />
        <el-table-column prop="variantLabel" label="Phân loại"/><el-table-column prop="quantity" label="SL" width="60" />
        <el-table-column label="Thành tiền" width="120">
          <template #default="{ row }">{{ formatVnd(row.lineTotal) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="canGuestCancel" class="cancel-box"><el-input v-model="cancelEmail" placeholder="Email dùng khi mua hàng"/><el-button @click="requestOtp">Gửi OTP</el-button><el-input v-if="otpSent" v-model="otp" placeholder="Nhập OTP"/><el-button v-if="otpSent" type="danger" @click="cancelGuest">Xác nhận hủy đơn</el-button></div><OrderTimeline :history="order.history"/><div class="total-row">Tổng cộng: <b>{{ formatVnd(order.total) }}</b></div>
    </el-card>
  </div>
</template>

<script setup>
import OrderTimeline from '@/components/shop/OrderTimeline.vue'
import { ref, computed } from 'vue'
import { shopAPI } from '@/api'

const orderId = ref('')
const phone = ref('')
const loading = ref(false)
const order = ref(null)\nconst cancelEmail = ref(''), otp = ref(''), otpSent = ref(false)\nconst canGuestCancel = computed(() => order.value && !order.value.userEmail && ['PENDING_PAYMENT','CONFIRMED'].includes(order.value.status))

const statusMap = {
  CONFIRMED: 'Chờ xử lý (COD)',
  PENDING_PAYMENT: 'Chờ thanh toán',
  PAID: 'Đã thanh toán',
  PREPARING: 'Đang chuẩn bị',
  SHIPPING: 'Đang giao hàng',
  DELIVERED: 'Đã giao hàng',
  COMPLETED: 'Hoàn thành',
  CANCELLED: 'Đã hủy',
  EXPIRED: 'Hết hạn thanh toán'
}
function statusLabel(s) { return statusMap[s] || s }
function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

async function requestOtp(){await shopAPI.requestGuestCancelOtp(orderId.value,cancelEmail.value);otpSent.value=true}\nasync function cancelGuest(){await shopAPI.cancelGuestOrder(orderId.value,cancelEmail.value,otp.value);await search();otpSent.value=false}\n\nasync function search() {
  if (!orderId.value || !phone.value) return
  loading.value = true
  order.value = null
  try {
    const res = await shopAPI.lookupOrder(orderId.value, phone.value)
    order.value = res.data
  } catch (e) {
    // lỗi đã được toast tự động qua interceptor chung của api/index.js
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.lookup-page { max-width: 560px; margin: 0 auto; padding: 24px; }
.lookup-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.brand { font-weight: 700; text-decoration: none; color: inherit; }
.lookup-card { margin-bottom: 20px; }
.total-row { text-align: right; font-size: 1.05rem; }
</style>