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
    <el-table-column label="Chênh lệch" width="120">
      <template #default="{ row }">
        <span v-if="row.cashDifference != null" :style="{ color: row.cashDifference === 0 ? 'green' : 'red' }">
          {{ formatVnd(row.cashDifference) }}
        </span>
        <span v-else>-</span>
      </template>
    </el-table-column>
    <el-table-column label="Hành động" width="240">
      <template #default="{ row }">
        <el-button v-if="row.status === 'SCHEDULED'" size="small" type="primary" @click="openCheckin(row)">Check-in</el-button>
        <el-button v-if="row.status === 'CHECKED_IN'" size="small" type="warning" @click="openCheckout(row)">Check-out</el-button>
        <el-button v-if="row.status === 'COMPLETED'" size="small" @click="viewReport(row)">Xem báo cáo</el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog v-model="checkinVisible" title="Check-in ca làm việc" width="380px">
    <el-form label-position="top">
      <el-form-item label="Tiền mặt đầu ca (tiền quỹ bàn giao từ ca trước)">
        <el-input-number v-model="cashAtStart" :min="0" style="width:100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="checkinVisible = false">Hủy</el-button>
      <el-button type="primary" @click="submitCheckin">Xác nhận check-in</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="checkoutVisible" title="Check-out & bàn giao ca" width="420px">
    <el-form label-position="top">
      <el-form-item label="Tiền mặt đếm được cuối ca">
        <el-input-number v-model="cashCounted" :min="0" style="width:100%" />
      </el-form-item>
      <el-form-item label="Ghi chú bàn giao">
        <el-input v-model="handoverNote" type="textarea" :rows="3" placeholder="VD: còn 2 đơn online chưa xử lý..." />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="checkoutVisible = false">Hủy</el-button>
      <el-button type="primary" @click="submitCheckout">Xác nhận check-out</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="reportVisible" title="Báo cáo bàn giao ca" width="420px">
    <div v-if="reportRow">
      <p>Tiền mặt đầu ca: <b>{{ formatVnd(reportRow.cashAtStart) }}</b></p>
      <p>Doanh thu tiền mặt (POS) trong ca: <b>{{ formatVnd(reportRow.posCashRevenue) }}</b></p>
      <p>Doanh thu chuyển khoản (POS) trong ca: <b>{{ formatVnd(reportRow.posBankRevenue) }}</b></p>
      <p>Tiền mặt dự kiến cuối ca: <b>{{ formatVnd(reportRow.expectedCash) }}</b></p>
      <p>Tiền mặt đếm được thực tế: <b>{{ formatVnd(reportRow.cashCounted) }}</b></p>
      <p>Chênh lệch:
        <b :style="{ color: reportRow.cashDifference === 0 ? 'green' : 'red' }">{{ formatVnd(reportRow.cashDifference) }}</b>
        <span v-if="reportRow.cashDifference > 0"> (dư quỹ)</span>
        <span v-else-if="reportRow.cashDifference < 0"> (thiếu quỹ)</span>
      </p>
      <p v-if="reportRow.handoverNote">Ghi chú: {{ reportRow.handoverNote }}</p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shiftAPI } from '@/api'
import { ElMessage } from 'element-plus'

const shiftsList = ref([])
const loading = ref(false)

const checkinVisible = ref(false)
const cashAtStart = ref(0)
const activeShiftId = ref(null)

const checkoutVisible = ref(false)
const cashCounted = ref(0)
const handoverNote = ref('')

const reportVisible = ref(false)
const reportRow = ref(null)

function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }

async function load() {
  loading.value = true
  try {
    const res = await shiftAPI.mine()
    shiftsList.value = res.data || []
  } finally { loading.value = false }
}

function openCheckin(row) {
  activeShiftId.value = row.id
  cashAtStart.value = 0
  checkinVisible.value = true
}

async function submitCheckin() {
  try {
    await shiftAPI.checkIn(activeShiftId.value, cashAtStart.value)
    ElMessage.success('Đã check-in')
    checkinVisible.value = false
    load()
  } catch { }
}

function openCheckout(row) {
  activeShiftId.value = row.id
  cashCounted.value = 0
  handoverNote.value = ''
  checkoutVisible.value = true
}

async function submitCheckout() {
  try {
    const res = await shiftAPI.checkOut(activeShiftId.value, cashCounted.value, handoverNote.value)
    ElMessage.success('Đã check-out')
    checkoutVisible.value = false
    load()
    reportRow.value = res.data
    reportVisible.value = true
  } catch { }
}

function viewReport(row) {
  reportRow.value = row
  reportVisible.value = true
}

onMounted(load)
</script>