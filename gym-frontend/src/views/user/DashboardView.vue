<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>DASHBOARD</h2>
      <span class="mono" style="font-size:0.8rem;color:var(--c-text-inv2)">{{ today }}</span>
    </div>

    <div v-if="loading">
      <el-skeleton :rows="6" animated style="background:var(--c-card);padding:24px;border-radius:12px"/>
    </div>

    <template v-else>
      <!-- Stats -->
      <div class="grid-4" style="margin-bottom:24px">
        <div class="stat-card accent-card" style="cursor:pointer" @click="$router.push('/app/sessions')">
          <div class="label">BUỔI HOÀN THÀNH</div>
          <div class="value">{{ data.completedSessions || 0 }}</div>
          <div class="sub">/ {{ data.totalSessions || 0 }} đã đăng ký</div>
          <div class="icon">💪</div>
        </div>
        <div class="stat-card" style="cursor:pointer" @click="$router.push('/app/sessions')">
          <div class="label">CALORIES ĐÃ ĐỐT</div>
          <div class="value">{{ formatNum(data.totalCaloriesBurned) }}</div>
          <div class="sub">kcal tổng cộng</div>
          <div class="icon">🔥</div>
        </div>
        <div class="stat-card">
          <div class="label">STREAK</div>
          <div class="value">{{ data.currentStreak || 0 }}</div>
          <div class="sub">ngày · kỷ lục: {{ data.longestStreak || 0 }}</div>
          <div class="icon">⚡</div>
        </div>
        <div class="stat-card" style="cursor:pointer" @click="$router.push('/app/progress')">
          <div class="label">CÂN NẶNG</div>
          <div class="value">{{ data.currentWeight || '--' }}</div>
          <div class="sub" :style="{color:weightColor}">{{ weightText }}</div>
          <div class="icon">⚖️</div>
        </div>
      </div>

      <!-- Charts -->
      <div class="grid-2" style="margin-bottom:24px">
        <el-card>
          <template #header>CALORIES TUẦN NÀY (kcal)</template>
          <div style="height:200px;position:relative">
            <canvas ref="calChart"></canvas>
            <div v-if="noCalories" class="chart-empty">Chưa có dữ liệu</div>
          </div>
        </el-card>
        <el-card>
          <template #header>BUỔI TẬP THEO TUẦN</template>
          <div style="height:200px;position:relative">
            <canvas ref="wkChart"></canvas>
            <div v-if="noWorkouts" class="chart-empty">Chưa có dữ liệu</div>
          </div>
        </el-card>
      </div>

      <el-card style="margin-bottom:24px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>KHỐI LƯỢNG % HOÀN THÀNH TUẦN NÀY</span>
            <span v-if="volumeGapText" style="font-size:0.8rem;font-weight:600" :style="{color:volumeGapColor}">
              {{ volumeGapText }}
            </span>
          </div>
        </template>
        <div style="height:220px;position:relative">
          <canvas ref="volChart"></canvas>
          <div v-if="noVolume" class="chart-empty">Chưa có dữ liệu</div>
        </div>

        <!-- Danh sách bài tập còn thiếu / cần tập hôm nay -->
        <div v-if="todayMissingExercises.length" class="missing-box">
          <div class="missing-title">
            {{ todaySession?.status === 'CHECKED_IN' ? '📋 Bài tập hôm nay còn thiếu:' : '📋 Bài tập cần tập hôm nay:' }}
          </div>
          <div class="missing-tags">
            <el-tag v-for="ex in todayMissingExercises" :key="ex.exerciseId" type="warning" effect="plain" size="small">
              {{ ex.exerciseName }}
            </el-tag>
          </div>
        </div>
      </el-card>

      <!-- This week sessions -->
      <el-card>
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>LỊCH TẬP TUẦN NÀY</span>
            <div style="display:flex;gap:8px">
              <el-button size="small" plain @click="$router.push('/app/plan')">📋 Đăng ký buổi</el-button>
              <el-button type="primary" size="small" @click="$router.push('/app/sessions')">Tất cả →</el-button>
            </div>
          </div>
        </template>

        <div v-if="!weekSessions.length" class="empty-state" style="padding:24px">
          Chưa có lịch tập tuần này.
          <el-button text type="primary" @click="$router.push('/app/plan')" style="padding:0 4px">
            Đăng ký buổi tập →
          </el-button>
        </div>

        <div v-else class="session-grid">
          <div v-for="s in weekSessions" :key="s.id"
               class="session-card" :class="s.status.toLowerCase()"
               @click="$router.push('/app/sessions')">
            <div class="session-day">{{ s.dayName || s.customSessionName || 'Tự do' }}</div>
            <div class="session-date">{{ fmtDate(s.sessionDate) }}</div>
            <div v-if="s.scheduledTime" class="session-time">🕐 {{ s.scheduledTime.substring(0,5) }}</div>
            <div v-if="s.isLastSessionOfWeek" class="session-last">⭐ Cuối tuần</div>
            <div style="margin-top:6px">
              <span class="badge" :class="statusBadge(s.status)">{{ statusLabel(s.status) }}</span>
            </div>
            <div v-if="s.completionRate != null" style="font-size:0.75rem;margin-top:4px"
                 :style="{color: s.completionRate>=90?'var(--c-success)':s.completionRate<50?'var(--c-danger)':'var(--c-warning)'}">
              {{ s.completionRate }}% hoàn thành
            </div>

            <!-- Actions -->
            <div class="session-actions" @click.stop style="margin-top:8px">
              <el-button v-if="s.status==='SCHEDULED'"  type="primary" size="small" @click.stop="checkIn(s.id)">
                Check-in
              </el-button>
              <el-button v-if="s.status==='CHECKED_IN'" type="success" size="small" @click.stop="openCheckOut(s)">
                Check-out
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <!-- ── Check-out Dialog ──────────────────────────────────── -->
    <el-dialog v-model="checkOutDialog" title="CHECK-OUT BUỔI TẬP" width="520px" align-center>
      <el-form label-position="top">
        <div v-if="coExercises.length" style="margin-bottom:16px">
          <div style="font-weight:700;margin-bottom:10px">📋 Nhập kết quả từng bài tập</div>
          <div v-for="ex in coExercises" :key="ex.exerciseId" class="co-ex-row">
            <div class="co-ex-name">
              {{ ex.exerciseName }}
              <span class="co-ex-target">
                (mục tiêu: {{ ex.sets }}×{{ ex.reps != null ? ex.reps + ' reps' : (ex.durationSeconds + 's') }})
              </span>
            </div>
            <el-input-number
                v-if="ex.reps != null"
                v-model="ex.repsCompleted" :min="0" size="small" style="width:150px"
                controls-position="right"/>
            <el-input-number
                v-else-if="ex.durationSeconds != null"
                v-model="ex.durationCompleted" :min="0" size="small" style="width:150px"
                controls-position="right"/>
            <span v-else style="color:var(--c-text3);font-size:0.78rem">Không có mục tiêu</span>
          </div>

          <div class="rate-preview">
            Tỉ lệ hoàn thành ước tính:
            <span class="rate-badge" :class="previewRateClass">{{ previewCompletionRate }}%</span>
          </div>
        </div>
        <div v-else class="info-box" style="margin-bottom:16px">
          ⚠️ Buổi này không có danh sách bài tập từ giáo án (buổi tự do) nên không thể checkout chi tiết từng bài.
        </div>

        <!-- Cuối tuần → nhập cân nặng -->
        <template v-if="coSession?.isLastSessionOfWeek">
          <el-divider><span style="color:var(--c-accent);font-size:0.82rem">📊 TIẾN ĐỘ CUỐI TUẦN (BẮT BUỘC)</span></el-divider>
          <el-form-item label="Cân nặng hiện tại (kg) *">
            <el-input-number v-model="coForm.checkoutWeight" :min="30" :max="300"
                             :precision="1" style="width:100%" placeholder="Nhập cân nặng"/>
          </el-form-item>
          <div class="info-box">
            ℹ️ Dữ liệu dùng để điều chỉnh giáo án tuần tiếp theo.
          </div>
        </template>

        <el-form-item label="Ghi chú" style="margin-top:10px">
          <el-input v-model="coForm.notes" type="textarea" :rows="2" placeholder="Cảm giác hôm nay..."/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkOutDialog=false">Hủy</el-button>
        <el-button type="primary" @click="submitCheckOut" :loading="checkingOut">
          ✅ XÁC NHẬN CHECK-OUT
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { dashboardAPI, sessionAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

Chart.register(...registerables)

const router       = useRouter()
const data         = ref({})
const weekSessions = ref([])
const loading      = ref(true)
const calChart     = ref(null)
const wkChart      = ref(null)
let   calInst = null, wkInst = null
const volChart = ref(null)
let volInst = null

const checkOutDialog = ref(false)
const coSession      = ref(null)
const checkingOut    = ref(false)
const coForm      = reactive({ notes:'', checkoutWeight:null })
const coExercises = ref([])   // [{exerciseId, exerciseName, sets, reps, durationSeconds, repsCompleted, durationCompleted}]

const today = dayjs().format('dddd, DD/MM/YYYY')

const weightColor = computed(() => {
  const c = data.value.weightChange
  if (!c) return 'var(--c-text3)'
  return c < 0 ? 'var(--c-success)' : 'var(--c-warning)'
})
const weightText = computed(() => {
  const c = data.value.weightChange
  if (c == null) return 'kg'
  return `${c > 0 ? '+' : ''}${c} kg từ ban đầu`
})
const noCalories = computed(() => !Object.values(data.value.weeklyCalories || {}).some(v => v > 0))
const noWorkouts = computed(() => !Object.values(data.value.weeklyWorkouts || {}).some(v => v > 0))
const noVolume   = computed(() => !Object.values(data.value.dailyVolumePercent || {}).some(v => v > 0))

const dowKeys  = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']
const todayKey = computed(() => dowKeys[dayjs().day() === 0 ? 6 : dayjs().day() - 1])

const volumeGapText = computed(() => {
  const actual = data.value.dailyVolumePercent?.[todayKey.value]
  const target = data.value.dailyVolumeTarget?.[todayKey.value]
  if (!target) return ''
  if (!actual) return `Hôm nay cần đạt ${target}% — chưa tập`
  const gap = target - actual
  return gap > 0 ? `Còn thiếu ${gap}% so với mục tiêu hôm nay` : '✅ Đã đạt mục tiêu hôm nay!'
})
const volumeGapColor = computed(() => {
  const actual = data.value.dailyVolumePercent?.[todayKey.value]
  const target = data.value.dailyVolumeTarget?.[todayKey.value]
  if (!target) return 'var(--c-text3)'
  return actual >= target ? 'var(--c-success)' : 'var(--c-warning)'
})

// ── Buổi tập hôm nay + danh sách bài tập còn thiếu ──────────
const todaySession = computed(() => {
  const todayStr = dayjs().format('YYYY-MM-DD')
  return weekSessions.value.find(s => dayjs(s.sessionDate).format('YYYY-MM-DD') === todayStr) || null
})

const todayMissingExercises = computed(() => {
  const s = todaySession.value
  if (!s || s.status === 'COMPLETED' || s.status === 'SKIPPED') return []
  const planExs = s.planExercises || []
  const logs    = s.exerciseLogs || []
  return planExs.filter(pe => {
    const log = logs.find(l => l.exerciseId === pe.exerciseId)
    const done = log && (log.isCompleted || (log.completionPercent != null && log.completionPercent >= 100))
    return !done
  })
})

// ── Check-out: tính % hoàn thành từng bài (giống công thức backend) ──
function calcExPercent(ex) {
  let raw
  if (ex.reps != null) {
    if (!ex.sets || ex.repsCompleted == null) return null
    const planned = ex.sets * ex.reps
    if (planned <= 0) return null
    raw = (ex.repsCompleted / planned) * 100
  } else if (ex.durationSeconds != null) {
    if (!ex.sets || ex.durationCompleted == null) return null
    const planned = ex.sets * ex.durationSeconds
    if (planned <= 0) return null
    raw = (ex.durationCompleted / planned) * 100
  } else return null
  return Math.round(Math.max(0, Math.min(200, raw)))
}
const previewCompletionRate = computed(() => {
  const vals = coExercises.value.map(calcExPercent).filter(v => v != null)
  if (!vals.length) return 0
  return Math.round(vals.reduce((a, b) => a + b, 0) / vals.length)
})
const previewRateClass = computed(() => {
  const r = previewCompletionRate.value
  if (r >= 90) return 'rate-high'
  if (r >= 60) return 'rate-mid'
  return 'rate-low'
})

async function load() {
  loading.value = true
  try {
    const [dash, week] = await Promise.all([dashboardAPI.get(), sessionAPI.getWeek()])
    data.value         = dash.data || {}
    weekSessions.value = week.data || []
  } catch {} finally {
    loading.value = false
    nextTick(drawCharts)
  }
}

function drawCharts() {
  const GRID = 'rgba(196,154,108,0.3)', TICK = '#4A3728'
  const opts = (label, showLegend=false) => ({
    responsive:true, maintainAspectRatio:false,
    plugins:{ legend:{ display:showLegend, position:'top', labels:{color:TICK,boxWidth:12,font:{size:11}} } },
    scales:{
      x:{ grid:{color:GRID}, ticks:{color:TICK,font:{size:11}} },
      y:{ grid:{color:GRID}, ticks:{color:TICK,font:{size:11}}, beginAtZero:true,
        title:{display:!!label,text:label,color:TICK} }
    }
  })

  if (calChart.value && data.value.weeklyCalories) {
    if (calInst) calInst.destroy()
    calInst = new Chart(calChart.value, {
      type:'bar',
      data:{ labels:Object.keys(data.value.weeklyCalories),
        datasets:[{ label:'Calories', data:Object.values(data.value.weeklyCalories), backgroundColor:'#D4892A', borderRadius:6, borderSkipped:false }] },
      options: opts('kcal')
    })
  }
  if (wkChart.value && data.value.weeklyWorkouts) {
    if (wkInst) wkInst.destroy()
    wkInst = new Chart(wkChart.value, {
      type:'bar',
      data:{ labels:Object.keys(data.value.weeklyWorkouts),
        datasets:[{ label:'Buổi tập', data:Object.values(data.value.weeklyWorkouts), backgroundColor:'#6B4226', borderRadius:6, borderSkipped:false }] },
      options: opts('buổi')
    })
  }
  if (volChart.value && data.value.dailyVolumePercent) {
    if (volInst) volInst.destroy()
    volInst = new Chart(volChart.value, {
      type:'bar',
      data:{ labels:Object.keys(data.value.dailyVolumePercent),
        datasets:[
          { label:'Hiện tại', data:Object.values(data.value.dailyVolumePercent),     backgroundColor:'#1565C0', borderRadius:6, borderSkipped:false },
          { label:'Yêu cầu',  data:Object.values(data.value.dailyVolumeTarget||{}),  backgroundColor:'#90CAF9', borderRadius:6, borderSkipped:false }
        ] },
      options: opts('%', true)
    })
  }
}

async function checkIn(id) {
  await sessionAPI.checkIn(id); ElMessage.success('Check-in! 💪'); load()
}

function openCheckOut(s) {
  coSession.value = s
  coForm.notes = ''
  coForm.checkoutWeight = null
  coExercises.value = (s.planExercises || []).map(pe => ({
    exerciseId: pe.exerciseId,
    exerciseName: pe.exerciseName,
    sets: pe.sets,
    reps: pe.reps,
    durationSeconds: pe.durationSeconds,
    // Mặc định điền sẵn = hoàn thành đủ mục tiêu giáo án, user tự chỉnh xuống nếu tập chưa hết
    repsCompleted:     pe.reps != null ? (pe.sets || 1) * pe.reps : null,
    durationCompleted: pe.reps == null && pe.durationSeconds != null ? (pe.sets || 1) * pe.durationSeconds : null
  }))
  checkOutDialog.value = true
}

async function submitCheckOut() {
  if (coExercises.value.length === 0) {
    ElMessage.warning('Buổi này không có bài tập để checkout chi tiết.'); return
  }
  if (coSession.value?.isLastSessionOfWeek && !coForm.checkoutWeight) {
    ElMessage.warning('Đây là buổi cuối tuần! Vui lòng nhập cân nặng.'); return
  }
  checkingOut.value = true
  try {
    await sessionAPI.checkOut(coSession.value.id, {
      notes: coForm.notes,
      checkoutWeight: coForm.checkoutWeight || null,
      exerciseLogs: coExercises.value.map(ex => ({
        exerciseId: ex.exerciseId,
        repsCompleted: ex.repsCompleted,
        durationCompleted: ex.durationCompleted,
        weightUsedKg: null,
        notes: null
      }))
    })
    ElMessage.success('Check-out thành công! 🎉')
    checkOutDialog.value = false
    load()
  } catch {} finally { checkingOut.value = false }
}

function fmtDate(d)     { return dayjs(d).format('ddd DD/MM') }
function formatNum(n)   { return n ? Number(n).toLocaleString() : '0' }
function statusLabel(s) { return { SCHEDULED:'Chờ', CHECKED_IN:'Đang tập', COMPLETED:'Xong', SKIPPED:'Bỏ' }[s]||s }
function statusBadge(s) { return { SCHEDULED:'badge-info', CHECKED_IN:'badge-warning', COMPLETED:'badge-success', SKIPPED:'badge-danger' }[s]||'' }

onMounted(load)
</script>

<style scoped>
.session-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(155px,1fr)); gap:12px; }
.session-card {
  background:var(--c-card); border:1px solid var(--c-border2);
  border-radius:var(--radius-lg); padding:14px; cursor:pointer;
  transition:all var(--transition); display:flex; flex-direction:column; gap:3px;
}
.session-card:hover      { border-color:var(--c-accent); box-shadow:var(--shadow); }
.session-card.completed  { border-left:3px solid var(--c-success); }
.session-card.checked_in { border-left:3px solid var(--c-warning); }
.session-day   { font-family:var(--font-display); font-size:1rem; color:var(--c-accent); }
.session-date  { font-size:0.75rem; color:var(--c-text3); }
.session-time  { font-size:0.75rem; color:var(--c-info); font-weight:600; }
.session-last  { font-size:0.7rem; color:var(--c-warning); font-weight:600; }

.chart-empty { position:absolute; inset:0; display:flex; align-items:center; justify-content:center; color:var(--c-text3); font-size:0.85rem; }

.missing-box   { margin-top:14px; padding-top:14px; border-top:1px dashed var(--c-border2); }
.missing-title { font-size:0.82rem; font-weight:700; color:var(--c-text2); margin-bottom:8px; }
.missing-tags  { display:flex; flex-wrap:wrap; gap:6px; }

.co-ex-row {
  display:flex; justify-content:space-between; align-items:center;
  padding:8px 0; border-bottom:1px solid var(--c-border2); gap:12px;
}
.co-ex-name   { font-size:0.85rem; flex:1; }
.co-ex-target { font-size:0.75rem; color:var(--c-text3); display:block; }
.rate-preview { margin-top:12px; font-size:0.85rem; display:flex; align-items:center; gap:8px; }

.rate-badge { min-width:52px; text-align:center; padding:4px 10px; border-radius:20px; font-weight:700; font-size:0.9rem; }
.rate-high  { background:#E8F5E9; color:#2E7D32; }
.rate-mid   { background:#FFF3E0; color:#E65100; }
.rate-low   { background:#FFEBEE; color:#C62828; }
.info-box   { padding:10px 14px; background:#FFF8F0; border:1px solid var(--c-border); border-radius:var(--radius-lg); font-size:0.8rem; color:var(--c-text2); }
</style>