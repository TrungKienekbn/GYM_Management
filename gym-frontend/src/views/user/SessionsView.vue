<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>BUỔI TẬP</h2>
    </div>

    <PetWidget/>

    <!-- Số liệu tổng quan -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-number">{{ totalCompleted }}</span>
        <span class="stat-label">buổi đã hoàn thành</span>
      </div>
      <div class="stat-item">
        <span class="stat-number">{{ sessions.length }}</span>
        <span class="stat-label">tổng số buổi</span>
      </div>
    </div>

    <el-card class="filters">
      <el-input v-model="keyword" placeholder="Tìm tên buổi tập..." clearable prefix-icon="Search" />
      <el-select v-model="statusFilter" placeholder="Trạng thái" clearable><el-option v-for="s in statuses" :key="s" :value="s" :label="statusLabel(s)"/></el-select>
      <el-input-number v-model="weekFilter" :min="1" placeholder="Tuần" controls-position="right" />
    </el-card>

    <el-table :data="pagedSessions" v-loading="loading" stripe>
      <el-table-column label="Ngày" width="110">
        <template #default="{row}">{{ fmtDate(row.sessionDate) }}</template>
      </el-table-column>
      <el-table-column label="Giờ" width="80" align="center">
        <template #default="{row}">{{ row.scheduledTime ? row.scheduledTime.substring(0,5) : '--' }}</template>
      </el-table-column>
      <el-table-column label="Buổi tập" min-width="180">
        <template #default="{row}">{{ row.customSessionName || row.planName || 'Buổi tập' }}</template>
      </el-table-column>
      <el-table-column label="Ngày trong tuần" width="130">
        <template #default="{row}">{{ fmtDayOfWeek(row.sessionDate) }}</template>
      </el-table-column>
      <el-table-column label="Tuần" width="65" align="center">
        <template #default="{row}">{{ row.weekNumber ? 'W' + row.weekNumber : '--' }}</template>
      </el-table-column>
      <el-table-column label="Trạng thái" width="140" align="center">
        <template #default="{row}">
          <span class="status-pill" :class="statusClass(row.status)">
            {{ statusIcon(row.status) }} {{ statusLabel(row.status) }}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-if="filteredSessions.length" v-model:current-page="page" v-model:page-size="pageSize" :total="filteredSessions.length" :page-sizes="[5,10,20]" layout="total, sizes, prev, pager, next" style="margin-top:18px;justify-content:flex-end" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { sessionAPI } from '@/api'
import PetWidget from '@/components/pet/PetWidget.vue'
import dayjs from 'dayjs'
import 'dayjs/locale/vi'
dayjs.locale('vi')

const sessions = ref([])
const loading  = ref(true)
const keyword = ref('')
const statusFilter = ref('')
const weekFilter = ref(null)
const page = ref(1)
const pageSize = ref(10)
const statuses = ['SCHEDULED','CHECKED_IN','COMPLETED','SKIPPED']
const filteredSessions = computed(() => sessions.value.filter(s => {
  const name = (s.customSessionName || s.planName || '').toLowerCase()
  return (!keyword.value || name.includes(keyword.value.toLowerCase())) &&
    (!statusFilter.value || s.status === statusFilter.value) &&
    (!weekFilter.value || s.weekNumber === weekFilter.value)
}))
const pagedSessions = computed(() => filteredSessions.value.slice((page.value-1)*pageSize.value, page.value*pageSize.value))

// Không filter — luôn hiển thị toàn bộ lịch sử buổi tập, kể cả SKIPPED, CHECKED_IN dở dang...
const totalCompleted = computed(() =>
  sessions.value.filter(s => s.status === 'COMPLETED').length
)

async function load() {
  loading.value = true
  try {
    const r = await sessionAPI.getAll()
    // Sắp xếp mới nhất lên trên; nếu BE đã sort thì bỏ dòng dưới cũng không sao
    sessions.value = (r.data || []).sort((a, b) => (a.sessionDate < b.sessionDate ? 1 : -1))
  } finally {
    loading.value = false
  }
}

function fmtDate(d) { return dayjs(d).format('DD/MM/YYYY') }
function fmtDayOfWeek(d) { return d ? dayjs(d).format('dddd') : '--' }

function statusIcon(s) {
  return { SCHEDULED: '⏳', CHECKED_IN: '🏃', COMPLETED: '✅', SKIPPED: '❌' }[s] || '❔'
}
function statusLabel(s) {
  return { SCHEDULED: 'Chờ', CHECKED_IN: 'Đang tập', COMPLETED: 'Hoàn thành', SKIPPED: 'Bỏ qua' }[s] || s
}
function statusClass(s) {
  return {
    SCHEDULED: 'status-scheduled',
    CHECKED_IN: 'status-checkedin',
    COMPLETED: 'status-completed',
    SKIPPED: 'status-skipped'
  }[s] || ''
}

onMounted(load)
</script>

<style scoped>
.stats-bar {
  display: flex;
  gap: 32px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--c-surface, #fff);
  border-radius: 10px;
}
.stat-item { display: flex; flex-direction: column; align-items: flex-start; }
.stat-number { font-size: 1.6rem; font-weight: 800; line-height: 1; }
.stat-label { font-size: 0.8rem; color: var(--c-text3, #888); margin-top: 4px; }

.status-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.82rem;
  font-weight: 600;
}
.status-scheduled  { background: #e8f0fe; color: #1a56db; }
.status-checkedin  { background: #fff4e0; color: #b45309; }
.status-completed  { background: #e6f7ea; color: #15803d; }
.status-skipped    { background: #fdecec; color: #b91c1c; }
.filters{margin-bottom:16px}.filters :deep(.el-card__body){display:grid;grid-template-columns:2fr 1fr 1fr;gap:12px}@media(max-width:700px){.filters :deep(.el-card__body){grid-template-columns:1fr}}
</style>
