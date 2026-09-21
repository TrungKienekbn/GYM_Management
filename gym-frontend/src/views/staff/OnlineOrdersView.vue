<template>
  <el-table :data="onlineOrders" v-loading="loading">
    <el-table-column type="expand"><template #default="{row}"><p v-for="i in row.items" :key="i.productId+':'+i.variantId">{{i.productName}} · {{i.variantLabel}} × {{i.quantity}}</p><OrderTimeline :history="row.history" show-actor/></template></el-table-column><el-table-column prop="id" label="Mã đơn" width="90" />
    <el-table-column prop="receiverName" label="Khách hàng" />
    <el-table-column prop="phone" label="SĐT" width="130" />
    <el-table-column prop="status" label="Trạng thái" width="140" />
    <el-table-column label="Tổng tiền" width="120">
      <template #default="{ row }">{{ formatVnd(row.total) }}</template>
    </el-table-column>
    <el-table-column label="Hành động" width="220">
      <template #default="{ row }">
        <el-button v-if="nextStatus(row.status)" size="small" type="primary"
          @click="advance(row)">
          Chuyển sang: {{ nextStatus(row.status) }}
        </el-button>
        <span v-else style="color:#999">Không thể chuyển tiếp</span>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import OrderTimeline from '@/components/shop/OrderTimeline.vue'
import { ref, computed, onMounted } from 'vue'
import { adminShopAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const allOrders = ref([])
const loading = ref(false)

const onlineOrders = computed(() => allOrders.value.filter(o => o.channel === 'ONLINE'))

const flow = { CONFIRMED: 'PREPARING',PAID: 'PREPARING', PREPARING: 'SHIPPING', SHIPPING: 'DELIVERED', DELIVERED: 'COMPLETED' }
function nextStatus(status) { return flow[status] || null }

async function load() {
  loading.value = true
  try {
    const res = await adminShopAPI.orders()
    allOrders.value = res.data || []
  } finally { loading.value = false }
}

async function advance(row) {
  if(row.paymentMethod==='COD'&&row.status==='SHIPPING'){try{await ElMessageBox.confirm('Xác nhận đã giao hàng và thu đủ tiền COD?','Thu tiền COD')}catch{return}}
  const next = nextStatus(row.status)
  if (!next) return
  try {
    await adminShopAPI.status(row.id, next)
    ElMessage.success('Đã cập nhật trạng thái')
    load()
  } catch { }
}

function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

onMounted(load)
</script>