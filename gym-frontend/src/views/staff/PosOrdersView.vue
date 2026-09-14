<template>
  <div>
    <el-input v-model="keyword" placeholder="Tìm theo tên/SĐT khách..." clearable style="margin-bottom:12px;width:320px" />
    <el-table :data="filtered" v-loading="loading">
      <el-table-column prop="id" label="Mã HĐ" width="90" />
      <el-table-column prop="receiverName" label="Khách hàng" />
      <el-table-column prop="phone" label="SĐT" width="130" />
      <el-table-column prop="paymentMethod" label="Thanh toán" width="120" />
      <el-table-column label="Tổng tiền" width="120">
        <template #default="{ row }">{{ formatVnd(row.total) }}</template>
      </el-table-column>
      <el-table-column label="Thời gian" width="170">
        <template #default="{ row }">{{ new Date(row.createdAt).toLocaleString('vi-VN') }}</template>
      </el-table-column>
      <el-table-column width="100">
        <template #default="{ row }">
          <el-button size="small" @click="view(row)">Xem</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" title="Chi tiết hóa đơn" width="480px">
      <div v-if="detail">
        <p>Mã hóa đơn: <b>#{{ detail.id }}</b></p>
        <p>Khách hàng: {{ detail.receiverName }} - {{ detail.phone }}</p>
        <p>Phương thức: {{ detail.paymentMethod }}</p>
        <el-table :data="detail.items" size="small" style="margin:12px 0">
          <el-table-column prop="productName" label="Sản phẩm" />
          <el-table-column prop="quantity" label="SL" width="60" />
          <el-table-column label="Thành tiền" width="110">
            <template #default="{ row }">{{ formatVnd(row.lineTotal) }}</template>
          </el-table-column>
        </el-table>
        <div style="text-align:right"><b>Tổng: {{ formatVnd(detail.total) }}</b></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { posAPI } from '@/api'

const orders = ref([])
const loading = ref(false)
const keyword = ref('')
const visible = ref(false)
const detail = ref(null)

const filtered = computed(() => {
  if (!keyword.value.trim()) return orders.value
  const k = keyword.value.toLowerCase()
  return orders.value.filter(o => (o.receiverName || '').toLowerCase().includes(k) || (o.phone || '').includes(k))
})

function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

async function load() {
  loading.value = true
  try {
    const res = await posAPI.orders()
    orders.value = res.data || []
  } finally { loading.value = false }
}

async function view(row) {
  const res = await posAPI.order(row.id)
  detail.value = res.data
  visible.value = true
}

onMounted(load)
</script>