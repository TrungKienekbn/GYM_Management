<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>THEO DÕI TIẾN ĐỘ</h2>
      <el-button type="primary" @click="addDialog=true">+ GHI NHẬN</el-button>
    </div>
    <div v-if="!isVip" class="vip-lock-banner"><div><b> Gói thường đang hiển thị dữ liệu 4 tuần gần nhất</b><span>Nâng cấp VIP để xem toàn bộ lịch sử tiến độ và biểu đồ dài hạn.</span></div><el-button type="warning" @click="$router.push('/app/membership')"> Nâng cấp VIP</el-button></div>

    <!-- Stats row -->
    <div class="grid-4" style="margin-bottom:24px" v-if="progressList.length">
      <div class="stat-card accent-card">
        <div class="label">CÂN NẶNG HIỆN TẠI</div>
        <div class="value">{{ latest?.weight || '--' }}</div>
        <div class="sub">kg</div>
        <div class="icon"></div>
      </div>
      <div class="stat-card">
        <div class="label">BMI</div>
        <div class="value">{{ latest?.bmi || '--' }}</div>
        <div class="sub" :style="{color: bmiColor}">{{ bmiCat(latest?.bmi) }}</div>
        <div class="icon"></div>
      </div>
      <div class="stat-card" v-if="weightChange !== null">
        <div class="label">THAY ĐỔI</div>
        <div class="value" :style="{color: weightChange<0?'var(--c-success)':'var(--c-danger)'}">
          {{ weightChange > 0 ? '+' : '' }}{{ weightChange }}
        </div>
        <div class="sub">kg từ ban đầu</div>
        <div class="icon">{{ weightChange < 0 ? '' : '' }}</div>
      </div>
    </div>

    <!-- Weight chart -->
    <el-card style="margin-bottom:24px" v-if="selectedMonthProgress.length > 1">
      <template #header>BIỂU ĐỒ CÂN NẶNG · {{ selectedMonthLabel }}</template>
      <div style="height:240px;position:relative">
        <canvas ref="chartRef"></canvas>
      </div>
    </el-card>

    <!-- Measurements -->
    <el-card style="margin-bottom:24px" v-if="latest && hasBodyMeasure">
      <template #header>SỐ ĐO CƠ THỂ (cm)</template>
      <div class="measurements">
        <div class="meas-item" v-for="m in measurements" :key="m.key">
          <div class="meas-icon">{{ m.icon }}</div>
          <div class="meas-label">{{ m.label }}</div>
          <div class="meas-val">{{ latest?.[m.key] || '--' }} cm</div>
        </div>
      </div>
    </el-card>

    <!-- History table -->
    <el-card>
      <template #header>
        <div class="history-header">
          <span>LỊCH SỬ GHI NHẬN</span>
          <span class="history-count">{{ selectedMonthProgress.length }} lần ghi nhận</span>
        </div>
      </template>

      <div v-if="monthOptions.length" class="month-pagination" aria-label="Chọn tháng xem tiến độ">
        <button class="month-nav" type="button" :disabled="selectedMonthIndex >= monthOptions.length - 1"
                aria-label="Xem tháng trước" @click="moveMonth(1)">‹</button>
        <div class="month-list">
          <button v-for="month in monthOptions" :key="month.key" type="button"
                  class="month-button" :class="{ active: selectedMonth === month.key }"
                  @click="selectMonth(month.key)">
            <span>{{ month.label }}</span>
            <small>{{ month.count }} bản ghi</small>
          </button>
        </div>
        <button class="month-nav" type="button" :disabled="selectedMonthIndex <= 0"
                aria-label="Xem tháng sau" @click="moveMonth(-1)">›</button>
      </div>

      <el-table v-if="selectedMonthProgress.length" :data="selectedMonthProgress" stripe>
        <el-table-column label="Ngày" prop="recordedDate" width="110"/>
        <el-table-column label="Cân nặng (kg)" prop="weight" width="130" align="center"/>
        <el-table-column label="BMI" prop="bmi" width="80" align="center"/>
        <el-table-column label="Thay đổi" width="110" align="center">
          <template #default="{row}">
            <span v-if="row.weightChange != null"
                  :style="{color: row.weightChange<0?'var(--c-success)':'var(--c-danger)', fontWeight:700}">
              {{ row.weightChange > 0 ? '+' : '' }}{{ row.weightChange }} kg
            </span>
            <span v-else class="muted">--</span>
          </template>
        </el-table-column>
        <el-table-column label="Ghi chú" min-width="160">
          <template #default="{row}">{{ row.notes || '--' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="Tháng này chưa có dữ liệu tiến độ" :image-size="72"/>
    </el-card>

    <!-- Add dialog -->
    <el-dialog v-model="addDialog" title="GHI NHẬN TIẾN ĐỘ" width="480px" align-center append-to-body>
      <el-form :model="form" label-position="top">
        <el-form-item label="Cân nặng (kg)">
          <el-input-number v-model="form.weight" :min="30" :max="250" :precision="1" style="width:100%"/>
        </el-form-item>
        <el-form-item label="Ngày ghi nhận">
          <el-date-picker v-model="form.recordedDate" type="date"
                          format="DD/MM/YYYY" value-format="YYYY-MM-DD" style="width:100%"/>
        </el-form-item>
        <el-form-item label="Ghi chú">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="Cảm nhận, điều kiện đo..."/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog=false">Hủy</el-button>
        <el-button type="primary" @click="addProgress">LƯU TIẾN ĐỘ</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, nextTick, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import { progressAPI, membershipAPI } from '@/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

Chart.register(...registerables)

const progressList = ref([])
const addDialog    = ref(false)
const chartRef     = ref(null)
const isVip        = ref(false)
const selectedMonth = ref('')
let   chartInst    = null

const form = reactive({
  weight:null, recordedDate:dayjs().format('YYYY-MM-DD'), notes:''
})

const latest      = computed(() => progressList.value.at(-1))
const firstRecord = computed(() => progressList.value[0])
const weightChange = computed(() => {
  if (!latest.value?.weight || !firstRecord.value?.weight) return null
  return Math.round((latest.value.weight - firstRecord.value.weight) * 10) / 10
})
const hasBodyMeasure = computed(() =>
    latest.value?.chestCm || latest.value?.hipCm || latest.value?.armCm || latest.value?.thighCm
)

const bmiColor = computed(() => {
  const b = latest.value?.bmi
  if (!b) return 'var(--c-text3)'
  if (b < 18.5) return 'var(--c-info)'
  if (b < 25)   return 'var(--c-success)'
  if (b < 30)   return 'var(--c-warning)'
  return 'var(--c-danger)'
})

const measurements = [
  { key:'chestCm', label:'Ngực', icon:'' },
  { key:'hipCm',   label:'Hông', icon:'' },
  { key:'armCm',   label:'Tay',  icon:'' },
  { key:'thighCm', label:'Đùi',  icon:'' },
]

const monthOptions = computed(() => {
  const groups = new Map()
  progressList.value.forEach(item => {
    if (!item.recordedDate) return
    const date = dayjs(item.recordedDate)
    const key = date.format('YYYY-MM')
    if (!groups.has(key)) groups.set(key, { key, label: `Tháng ${date.format('MM/YYYY')}`, count: 0 })
    groups.get(key).count++
  })
  return Array.from(groups.values()).sort((a, b) => b.key.localeCompare(a.key))
})

const selectedMonthIndex = computed(() => monthOptions.value.findIndex(month => month.key === selectedMonth.value))
const selectedMonthLabel = computed(() =>
    monthOptions.value.find(month => month.key === selectedMonth.value)?.label || 'Chưa chọn tháng'
)
const selectedMonthProgress = computed(() => progressList.value
    .filter(item => item.recordedDate?.startsWith(selectedMonth.value))
    .slice()
    .sort((a, b) => b.recordedDate.localeCompare(a.recordedDate)))

function selectMonth(key) {
  selectedMonth.value = key
}

function moveMonth(offset) {
  const nextIndex = selectedMonthIndex.value + offset
  if (nextIndex >= 0 && nextIndex < monthOptions.value.length) {
    selectedMonth.value = monthOptions.value[nextIndex].key
  }
}

async function load() {
  try {
    const r = await progressAPI.getAll()
    // sort asc by date
    progressList.value = (r.data || []).sort((a,b) => a.recordedDate.localeCompare(b.recordedDate))
    if (!monthOptions.value.some(month => month.key === selectedMonth.value)) {
      selectedMonth.value = monthOptions.value[0]?.key || ''
    }
  } catch {}
  nextTick(drawChart)
}

function drawChart() {
  if (chartInst) {
    chartInst.destroy()
    chartInst = null
  }
  if (!chartRef.value || selectedMonthProgress.value.length < 2) return
  const chartData = selectedMonthProgress.value.slice().reverse()
  chartInst = new Chart(chartRef.value, {
    type: 'line',
    data: {
      labels: chartData.map(p => dayjs(p.recordedDate).format('DD/MM')),
      datasets: [{
        label: 'Cân nặng (kg)',
        data: chartData.map(p => p.weight),
        borderColor: '#F97316',
        backgroundColor: 'rgba(212,137,42,0.1)',
        tension: 0.4,
        pointBackgroundColor: '#F97316',
        pointRadius: 5,
        fill: true
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        x: { grid: { color:'rgba(196,154,108,0.3)' }, ticks: { color:'#4A3728', font:{size:11} } },
        y: { grid: { color:'rgba(196,154,108,0.3)' }, ticks: { color:'#4A3728', font:{size:11} }, beginAtZero:false }
      }
    }
  })
}

async function addProgress() {
  if (!form.weight || form.weight < 30 || form.weight > 250) { ElMessage.warning('Cân nặng phải từ 30 đến 250 kg'); return }
  await progressAPI.add(form)
  ElMessage.success('Đã ghi nhận tiến độ! ')
  addDialog.value = false
  Object.assign(form, { weight:null, notes:'' })
  load()
}

function bmiCat(b) {
  if (!b) return ''
  if (b < 18.5) return 'Thiếu cân'
  if (b < 25)   return 'Bình thường'
  if (b < 30)   return 'Thừa cân'
  return 'Béo phì'
}

onMounted(async () => {
  try { const r = await membershipAPI.getActive(); isVip.value = r.data?.membershipType === 'VIP' && r.data?.paymentStatus === 'PAID' } catch { isVip.value = false }
  load()
})

watch(selectedMonth, () => nextTick(drawChart))
</script>

<style scoped>
.measurements { display:grid; grid-template-columns:repeat(auto-fill,minmax(110px,1fr)); gap:12px; }
.meas-item {
  text-align:center; padding:14px; background:var(--c-card2);
  border:1px solid var(--c-border2); border-radius:var(--radius-lg);
}
.meas-icon  { font-size:1.5rem; margin-bottom:4px; }
.meas-label { font-size:0.72rem; color:var(--c-text3); text-transform:uppercase; letter-spacing:0.08em; margin-bottom:4px; }
.meas-val   { font-family:var(--font-display); font-size:1.1rem; color:var(--c-text); }
.vip-lock-banner{display:flex;justify-content:space-between;align-items:center;gap:20px;padding:14px 18px;margin-bottom:20px;border:1px solid #e7bd52;background:#fff8dc;border-radius:10px;color:#6b4b00}.vip-lock-banner span{display:block;font-size:.8rem;margin-top:4px}@media(max-width:650px){.vip-lock-banner{align-items:flex-start;flex-direction:column}}
.history-header{display:flex;align-items:center;justify-content:space-between;gap:16px}.history-count{font-size:.78rem;color:var(--c-text3);font-weight:500}.month-pagination{display:flex;align-items:center;gap:10px;margin-bottom:18px}.month-list{display:flex;gap:8px;overflow-x:auto;scrollbar-width:thin;flex:1;padding:2px}.month-button,.month-nav{border:1px solid var(--c-border2);background:var(--c-card2);color:var(--c-text2);border-radius:9px;cursor:pointer;transition:.2s}.month-button{min-width:124px;padding:9px 14px;text-align:left}.month-button span,.month-button small{display:block;white-space:nowrap}.month-button span{font-weight:700;font-size:.84rem}.month-button small{font-size:.7rem;margin-top:3px;color:var(--c-text3)}.month-button:hover,.month-button.active{border-color:var(--c-accent);color:var(--c-accent);background:#fff7eb}.month-button.active{box-shadow:0 0 0 1px var(--c-accent)}.month-nav{flex:0 0 38px;height:42px;font-size:1.45rem}.month-nav:disabled{opacity:.35;cursor:not-allowed}@media(max-width:650px){.history-header{align-items:flex-start;flex-direction:column;gap:4px}.month-pagination{gap:6px}.month-button{min-width:110px;padding:8px 10px}}
</style>
