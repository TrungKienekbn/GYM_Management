<template>
  <el-table :data="shiftsList" v-loading="loading">
    <el-table-column prop="shiftDate" label="Ngày" width="120" />
    <el-table-column label="Giờ ca" width="150">
      <template #default="{ row }">{{ row.plannedStart }} - {{ row.plannedEnd }}</template>
    </el-table-column>
    <el-table-column prop="status" label="Trạng thái" width="130" />
    <el-table-column label="Check-in" width="160">
      <template #default="{ row }">{{ row.checkInAt ? new Date(row.checkInAt).toLocaleString('vi-VN') : '-' }}</template>
    </el-table-column>
    <el-table-column label="Check-out" width="160">
      <template #default="{ row }">{{ row.checkOutAt ? new Date(row.checkOutAt).toLocaleString('vi-VN') : '-' }}</template>
    </el-table-column>
    <el-table-column label="Hành động" width="220">
      <template #default="{ row }">
        <el-button v-if="row.status === 'SCHEDULED'" size="small" type="primary" @click="doCheckIn(row)">Check-in</el-button>
        <el-button v-if="row.status === 'CHECKED_IN'" size="small" type="warning" @click="openCheckout(row)">Check-out</el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog v-model="dialogVisible" title="Báo cáo bàn giao ca" width="420px">
    <el-input v-model="handoverNote" type="textarea" :rows="4" placeholder="VD: Tiền mặt bàn giao 2.500.000đ, còn 3 đơn chưa xử lý..." />
    <template #footer>
      <el-button @click="dialogVisible = false">Hủy</el-button>
      <el-button type="primary" @click="submitCheckout">Xác nhận check-out</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shiftAPI } from '@/api'
import { ElMessage } from 'element-plus'

const shiftsList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const handoverNote = ref('')
const activeShiftId = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await shiftAPI.mine()
    shiftsList.value = res.data || []
  } finally { loading.value = false }
}

async function doCheckIn(row) {
  try { await shiftAPI.checkIn(row.id); ElMessage.success('Đã check-in'); load() } catch { }
}

function openCheckout(row) {
  activeShiftId.value = row.id
  handoverNote.value = ''
  dialogVisible.value = true
}

async function submitCheckout() {
  try {
    await shiftAPI.checkOut(activeShiftId.value, handoverNote.value)
    ElMessage.success('Đã check-out')
    dialogVisible.value = false
    load()
  } catch { }
}

onMounted(load)
</script>