<template>
  <el-card style="margin-bottom:16px">
    <template #header>Phân ca cho nhân viên</template>
    <el-form :inline="true">
      <el-form-item label="Nhân viên">
        <el-select v-model="form.staffUserId" style="width:200px">
          <el-option v-for="s in staffList" :key="s.id" :label="s.fullName" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="Ngày">
        <el-date-picker v-model="form.shiftDate" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="Bắt đầu">
        <el-time-picker v-model="form.startTime" value-format="HH:mm:ss" />
      </el-form-item>
      <el-form-item label="Kết thúc">
        <el-time-picker v-model="form.endTime" value-format="HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="assign">Phân ca</el-button>
      </el-form-item>
    </el-form>
  </el-card>

    <el-table :data="shiftsList" v-loading="loading">
    <el-table-column prop="userName" label="Nhân viên" />
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
    <el-table-column label="Tiền đầu ca" width="120">
      <template #default="{ row }">{{ row.cashAtStart != null ? formatVnd(row.cashAtStart) : '-' }}</template>
    </el-table-column>
    <el-table-column label="Dự kiến cuối ca" width="130">
      <template #default="{ row }">{{ row.expectedCash != null ? formatVnd(row.expectedCash) : '-' }}</template>
    </el-table-column>
    <el-table-column label="Đếm thực tế" width="120">
      <template #default="{ row }">{{ row.cashCounted != null ? formatVnd(row.cashCounted) : '-' }}</template>
    </el-table-column>
    <el-table-column label="Chênh lệch" width="120">
      <template #default="{ row }">
        <span v-if="row.cashDifference != null" :style="{ color: row.cashDifference === 0 ? 'green' : 'red' }">{{ formatVnd(row.cashDifference) }}</span>
        <span v-else>-</span>
      </template>
    </el-table-column>
    <el-table-column prop="handoverNote" label="Bàn giao" />
  </el-table>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { shiftAPI, adminAPI } from '@/api'
import { ElMessage } from 'element-plus'

const staffList = ref([])
const shiftsList = ref([])
const loading = ref(false)
const form = ref({ staffUserId: null, shiftDate: '', startTime: '', endTime: '' })
function formatVnd(v) { return (v || 0).toLocaleString('vi-VN') + ' đ' }
async function loadStaff() {
  const res = await adminAPI.getUsers()
  staffList.value = (res.data || []).filter(u => u.role === 'ROLE_STAFF')
}

async function loadShifts() {
  loading.value = true
  try {
    const res = await shiftAPI.all()
    shiftsList.value = res.data || []
  } finally { loading.value = false }
}

async function assign() {
  if (!form.value.staffUserId || !form.value.shiftDate || !form.value.startTime || !form.value.endTime) {
    ElMessage.error('Vui lòng nhập đủ thông tin ca làm')
    return
  }
  try {
    await shiftAPI.assign(form.value)
    ElMessage.success('Đã phân ca')
    loadShifts()
  } catch { }
}

onMounted(() => { loadStaff(); loadShifts() })
</script>