<template>
  <div class="fade-in">
    <div v-if="!isVip" class="vip-lock-banner"><div><b> Thống kê gói thường: 4 tuần gần nhất</b><span>VIP mở toàn bộ lịch sử, thống kê dài hạn và tự động điều chỉnh giáo án mỗi tuần.</span></div><el-button type="warning" @click="$router.push('/app/membership')"> Mở khóa VIP</el-button></div>
    <div v-if="loading">
      <el-skeleton :rows="6" animated style="background:var(--c-card);padding:24px;border-radius:12px"/>
    </div>

    <template v-else>
      <!-- ── MỚI: banner Fitness Improvement ── -->
      <el-card v-if="plan && plan.isFitnessImprovement" style="margin-bottom:24px;border-left:4px solid #dc2626">
        <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
          <el-tag type="danger" effect="dark"> Đang tập giáo án nâng cao thể lực</el-tag>
          <span style="font-size:0.85rem;color:var(--c-text2)">
            Giáo án đang được tạm dừng và sẽ tự động tiếp tục khi đủ thể lực.
          </span>
        </div>
      </el-card>

      <section class="analytics-section" v-if="dashboardStats">
        <div class="analytics-heading">
          <div><h2> Thống kê tháng {{ dayjs(selectedMonth).format('MM/YYYY') }}</h2><p>Được tổng hợp từ lịch sử checkout thực tế</p></div>
          <div style="display:flex;gap:8px;align-items:center"><el-select v-model="selectedMonth" style="width:135px"><el-option v-for="m in dashboardMonths" :key="m" :label="dayjs(m).format('MM/YYYY')" :value="m" /></el-select><el-tag effect="plain">So với tháng trước</el-tag></div>
        </div>
        <div class="metric-grid">
          <div class="metric-card"><span>Buổi hoàn thành</span><strong>{{ dashboardStats.currentMonthCompleted || 0 }}</strong><small :class="changeClass(dashboardStats.sessionChangePercent)">{{ changeText(dashboardStats.sessionChangePercent) }}</small></div>
          <div class="metric-card"><span>Tỷ lệ duy trì</span><strong>{{ dashboardStats.currentMonthAdherencePercent || 0 }}%</strong><small :class="changeClass(dashboardStats.adherenceChangePercent)">{{ changeText(dashboardStats.adherenceChangePercent, ' điểm %') }}</small></div>
          <div class="metric-card"><span>Thời gian tập</span><strong>{{ formatDuration(dashboardStats.currentMonthDurationMinutes) }}</strong><small>Trong tháng này</small></div>
          <div class="metric-card"><span>Calo tiêu hao</span><strong>{{ (dashboardStats.currentMonthCalories || 0).toLocaleString('vi-VN') }}</strong><small>kcal đã ghi nhận</small></div>
          <div class="metric-card"><span>Thay đổi cân nặng</span><strong>{{ dashboardStats.currentMonthWeightChange == null ? '--' : signed(dashboardStats.currentMonthWeightChange) + ' kg' }}</strong><small>Từ lần ghi đầu đến cuối tháng</small></div>
        </div>

        <div class="analytics-grid">
          <el-card class="analytics-card wide"><template #header><b> Xu hướng 6 tháng</b></template><div class="analytics-chart"><canvas ref="monthlyChart"></canvas></div></el-card>
          <el-card class="analytics-card"><template #header><b> Chất lượng buổi tập</b></template><div class="analytics-chart"><canvas ref="qualityChart"></canvas><div v-if="!hasQualityData" class="chart-empty">Chưa có dữ liệu</div></div></el-card>
          <el-card class="analytics-card"><template #header><b> Phân bố nhóm cơ</b></template><div class="analytics-chart"><canvas ref="muscleChart"></canvas><div v-if="!hasMuscleData" class="chart-empty">Chưa có dữ liệu</div></div></el-card>
          <el-card class="analytics-card insight-card"><template #header><b> Nhận xét tự động</b></template><p class="insight-main">{{ dashboardStats.monthlyInsight }}</p><ul><li v-for="item in dashboardStats.recommendations || []" :key="item">{{ item }}</li></ul><small>Nhận xét mang tính hỗ trợ luyện tập, không thay thế tư vấn y tế.</small></el-card>
        </div>
      </section>

      <!-- Khối lượng hoàn thành theo tháng -->
      <el-card style="margin-bottom:24px">
        <template #header>
          <div class="volume-heading">
            <span style="font-weight:700">KHỐI LƯỢNG % HOÀN THÀNH THÁNG {{ dayjs(selectedMonth).format('MM/YYYY') }}</span>
            <div class="month-pagination" aria-label="Chọn tháng thống kê khối lượng">
              <el-button size="small" plain :disabled="volumeMonthPage === 0" @click="changeVolumeMonthPage(-1)">Trước</el-button>
              <el-button
                v-for="month in visibleVolumeMonths"
                :key="month"
                size="small"
                :type="month === selectedMonth ? 'primary' : 'default'"
                @click="selectVolumeMonth(month)">
                {{ dayjs(month).format('MM/YYYY') }}
              </el-button>
              <el-button size="small" plain :disabled="volumeMonthPage >= maxVolumeMonthPage" @click="changeVolumeMonthPage(1)">Sau</el-button>
            </div>
          </div>
        </template>
        <div style="height:220px;position:relative">
          <canvas ref="volChart"></canvas>
          <div v-if="noVolume" class="chart-empty">Chưa có dữ liệu</div>
        </div>
      </el-card>

      <!-- ===================== THỐNG KÊ CÁC BUỔI TẬP (MỚI) ===================== -->
      <el-card style="margin-top:24px" v-if="plan">
        <template #header>
          <span style="font-weight:700"> THỐNG KÊ CÁC BUỔI TẬP{{ buoiStatsTitleSuffix }}</span>
        </template>

        <div v-if="!hasSelectedWeekData" class="empty-state" style="padding:16px">
          Chưa có dữ liệu của tuần này.
        </div>
        <div v-else class="buoi-stats-list">
          <div v-for="b in buoiStats" :key="b.buoiNumber" class="buoi-stat-item">
            <div class="buoi-stat-row" @click="toggleExpand(b.buoiNumber)">
              <div class="buoi-stat-main">
                <div class="buoi-stat-title">
                  Buổi {{ b.buoiNumber }}
                  <span v-if="b.state === 'completed'" class="buoi-badge" :class="rateBadgeClass(b.completionRate)">
                    Hoàn thành {{ b.completionRate }}%
                  </span>
                  <span v-else-if="b.state === 'skipped'" class="buoi-badge badge-skip">Đã bỏ buổi</span>
                  <span v-else class="buoi-badge badge-empty">Chưa có thông tin tập</span>
                </div>
                <div class="buoi-stat-sub">{{ b.summary }}</div>
              </div>
              
            </div>

            <div v-if="expandedMap[b.buoiNumber] && b.state === 'completed'" class="buoi-detail">
              <div v-if="b.shortCount > 0" class="detail-group">
                <div class="detail-group-title short"> Cần cải thiện</div>
                <div v-for="ex in b.exercises.filter(e => e.status === 'short')" :key="ex.exerciseId" class="detail-ex-row">
                  <span class="detail-ex-name">{{ ex.exerciseName }}</span>
                  <span class="detail-ex-diff short">Thiếu {{ Math.abs(ex.diff) }} {{ ex.type === 'reps' ? 'reps' : 'giây' }}</span>
                </div>
              </div>

              <div v-if="b.excessReps > 0 || b.excessSeconds > 0" class="detail-group">
                <div class="detail-group-title excess"> Tập thừa</div>
                <div v-for="ex in b.exercises.filter(e => e.status === 'excess')" :key="ex.exerciseId" class="detail-ex-row">
                  <span class="detail-ex-name">{{ ex.exerciseName }}</span>
                  <span class="detail-ex-diff excess">+{{ ex.diff }} {{ ex.type === 'reps' ? 'reps' : 'giây' }}</span>
                </div>
              </div>

              <div class="detail-footer">
                 Đạt yêu cầu: {{ b.achievedCount }}/{{ b.totalExercises }} bài
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import { planAPI, sessionAPI, membershipAPI, dashboardAPI } from '@/api'
import dayjs from 'dayjs'

Chart.register(...registerables)

const plan         = ref(null)
const allSessions  = ref([])
const loading       = ref(true)
const isVip             = ref(false)
const dashboardStats    = ref(null)
const serverDashboardStats = ref(null)
const selectedMonth = ref(dayjs().startOf('month').format('YYYY-MM-DD'))
const dashboardMonths = computed(() => {
  const values = new Set([dayjs().startOf('month').format('YYYY-MM-DD')])
  allSessions.value.forEach(s => { if (s.sessionDate) values.add(dayjs(s.sessionDate).startOf('month').format('YYYY-MM-DD')) })
  return [...values].sort().reverse()
})
const monthlyChart      = ref(null)
const qualityChart      = ref(null)
const muscleChart       = ref(null)
let monthlyInst = null, qualityInst = null, muscleInst = null

const hasQualityData = computed(() => Object.values(dashboardStats.value?.sessionQualityDistribution || {}).some(v => v > 0))
const hasMuscleData = computed(() => Object.values(dashboardStats.value?.muscleGroupDistribution || {}).some(v => v > 0))
const signed = value => `${Number(value) > 0 ? '+' : ''}${value}`
const changeText = (value, suffix = '%') => value == null ? 'Chưa đủ dữ liệu so sánh' : `${signed(value)}${suffix} so với tháng trước`
const changeClass = value => Number(value) > 0 ? 'change-up' : Number(value) < 0 ? 'change-down' : ''
const formatDuration = minutes => `${Math.floor((minutes || 0) / 60)}h ${(minutes || 0) % 60}p`

function drawAnalyticsCharts() {
  if (!dashboardStats.value) return
  const text = '#4A3728', grid = 'rgba(196,154,108,.22)'
  const months = Object.keys(dashboardStats.value.monthlyCompletedSessions || {})
  monthlyInst?.destroy(); qualityInst?.destroy(); muscleInst?.destroy()
  if (monthlyChart.value) monthlyInst = new Chart(monthlyChart.value, { type:'bar', data:{ labels:months, datasets:[
    { label:'Số buổi hoàn thành', data:months.map(k => dashboardStats.value.monthlyCompletedSessions[k]), backgroundColor:'#F97316', borderRadius:6 }
  ]}, options:{ responsive:true, maintainAspectRatio:false, plugins:{legend:{display:false}}, scales:{x:{grid:{display:false},ticks:{color:text}},y:{beginAtZero:true,grid:{color:grid},ticks:{precision:0,color:text},title:{display:true,text:'Số buổi',color:text}}} } })
  const quality = dashboardStats.value.sessionQualityDistribution || {}
  if (qualityChart.value && hasQualityData.value) qualityInst = new Chart(qualityChart.value, { type:'doughnut', data:{labels:Object.keys(quality),datasets:[{data:Object.values(quality),backgroundColor:['#22c55e','#f59e0b','#ef4444','#94a3b8'],borderWidth:0}]},options:{responsive:true,maintainAspectRatio:false,cutout:'62%',plugins:{legend:{position:'bottom',labels:{color:text,boxWidth:10}}}} })
  const muscles = dashboardStats.value.muscleGroupDistribution || {}
  if (muscleChart.value && hasMuscleData.value) muscleInst = new Chart(muscleChart.value, { type:'bar', data:{labels:Object.keys(muscles).map(muscleLabel),datasets:[{label:'Số bài hoàn thành',data:Object.values(muscles),backgroundColor:'#1565c0',borderRadius:6}]},options:{indexAxis:'y',responsive:true,maintainAspectRatio:false,plugins:{legend:{display:false}},scales:{x:{beginAtZero:true,grid:{color:grid},ticks:{precision:0,color:text}},y:{grid:{display:false},ticks:{color:text}}}} })
}

function buildDashboardFallback(sessions, anchor = dayjs()) {
  const now = dayjs(anchor).endOf('month')
  const currentKey = now.format('YYYY-MM')
  const previousKey = now.subtract(1, 'month').format('YYYY-MM')
  const inMonth = key => sessions.filter(s => s.sessionDate && dayjs(s.sessionDate).format('YYYY-MM') === key)
  const current = inMonth(currentKey)
  const previous = inMonth(previousKey)
  const completed = list => list.filter(s => s.status === 'COMPLETED')
  const eligible = list => list.filter(s => !dayjs(s.sessionDate).isAfter(now, 'day'))
  const adherence = list => eligible(list).length ? Math.round(completed(eligible(list)).length * 100 / eligible(list).length) : 0
  const currentDone = completed(current), previousDone = completed(previous)
  const monthlyCompletedSessions = {}, monthlyCaloriesBurned = {}, monthlyDurationMinutes = {}, monthlyCompletionPercent = {}
  for (let i = 5; i >= 0; i--) {
    const d = now.subtract(i, 'month'), key = d.format('YYYY-MM'), label = d.format('MM/YYYY')
    const done = completed(inMonth(key))
    monthlyCompletedSessions[label] = done.length
    monthlyCaloriesBurned[label] = done.reduce((sum, s) => sum + (s.totalCaloriesBurned || 0), 0)
    monthlyDurationMinutes[label] = done.reduce((sum, s) => sum + (s.durationMinutes || 0), 0)
    monthlyCompletionPercent[label] = done.length ? Math.round(done.reduce((sum, s) => sum + (s.completionRate || 0), 0) / done.length) : null
  }
  const sessionQualityDistribution = { 'Hoàn thành tốt':0, 'Hoàn thành một phần':0, 'Bỏ buổi':0, 'Chưa hoàn thành':0 }
  eligible(current).forEach(s => {
    if (s.status === 'COMPLETED' && (s.completionRate || 0) >= 90) sessionQualityDistribution['Hoàn thành tốt']++
    else if (s.status === 'COMPLETED') sessionQualityDistribution['Hoàn thành một phần']++
    else if (s.status === 'SKIPPED') sessionQualityDistribution['Bỏ buổi']++
    else sessionQualityDistribution['Chưa hoàn thành']++
  })
  const muscleByExercise = {}
  ;(plan.value?.planDays || []).forEach(d => (d.exercises || []).forEach(e => { muscleByExercise[e.exerciseId] = e.muscleGroup }))
  const muscleGroupDistribution = {}
  currentDone.forEach(s => (s.exerciseLogs || []).filter(l => l.isCompleted !== false).forEach(l => {
    const muscle = muscleByExercise[l.exerciseId]
    if (muscle) muscleGroupDistribution[muscle] = (muscleGroupDistribution[muscle] || 0) + 1
  }))
  const currentRate = adherence(current), previousRate = adherence(previous)
  return {
    currentMonthSessions:current.length, previousMonthSessions:previous.length,
    currentMonthCompleted:currentDone.length,
    currentMonthCalories:currentDone.reduce((sum,s)=>sum+(s.totalCaloriesBurned||0),0),
    currentMonthDurationMinutes:currentDone.reduce((sum,s)=>sum+(s.durationMinutes||0),0),
    currentMonthAdherencePercent:currentRate, previousMonthAdherencePercent:previousRate,
    sessionChangePercent:previousDone.length ? Math.round((currentDone.length-previousDone.length)*1000/previousDone.length)/10 : (currentDone.length ? 100 : 0),
    adherenceChangePercent:currentRate-previousRate, currentMonthWeightChange:null,
    monthlyCompletedSessions, monthlyCaloriesBurned, monthlyDurationMinutes, monthlyCompletionPercent,
    muscleGroupDistribution, sessionQualityDistribution,
    monthlyInsight:`Tháng này bạn đã hoàn thành ${currentDone.length}/${eligible(current).length} buổi (${currentRate}%).`,
    recommendations: current.length ? ['Duy trì lịch tập đều và checkout đầy đủ để nhận phân tích chính xác hơn.'] : ['Bắt đầu và checkout buổi tập để hệ thống ghi nhận thống kê.']
  }
}

// Khối lượng % hoàn thành theo tháng được chọn.
const volChart = ref(null)
let volInst = null
const volumeMonthPage = ref(0)
const volumeMonthsPerPage = 4
const maxVolumeMonthPage = computed(() => Math.max(0, Math.ceil(dashboardMonths.value.length / volumeMonthsPerPage) - 1))
const visibleVolumeMonths = computed(() => {
  const start = volumeMonthPage.value * volumeMonthsPerPage
  return dashboardMonths.value.slice(start, start + volumeMonthsPerPage)
})

function selectVolumeMonth(month) {
  selectedMonth.value = month
}

function changeVolumeMonthPage(offset) {
  volumeMonthPage.value = Math.min(maxVolumeMonthPage.value, Math.max(0, volumeMonthPage.value + offset))
}

const sessionProgress = computed(() => {
  const monthKey = dayjs(selectedMonth.value).format('YYYY-MM')
  return allSessions.value
    .filter(s => s.sessionDate && dayjs(s.sessionDate).format('YYYY-MM') === monthKey)
    .sort((a, b) => dayjs(a.sessionDate).valueOf() - dayjs(b.sessionDate).valueOf() || Number(a.id || 0) - Number(b.id || 0))
    .map((session, index) => ({
      buoiNumber: index + 1,
      dateLabel: dayjs(session.sessionDate).format('DD/MM'),
      required: 100,
      actual: session.status === 'COMPLETED' ? Number(session.completionRate ?? 100) : 0
    }))
})

const noVolume = computed(() => sessionProgress.value.length === 0)

function drawVolumeChart() {
  const GRID = 'rgba(196,154,108,0.3)', TICK = '#4A3728'
  if (!volChart.value) return
  if (volInst) volInst.destroy()

  const labels       = sessionProgress.value.map(r => `Buổi ${r.buoiNumber} (${r.dateLabel})`)
  const actualData    = sessionProgress.value.map(r => r.actual)
  const requiredData  = sessionProgress.value.map(r => r.required)

  volInst = new Chart(volChart.value, {
    type:'bar',
    data:{
      labels,
      datasets:[
        { label:'Hiện tại', data:actualData,   backgroundColor:'#1565C0', borderRadius:6, borderSkipped:false },
        { label:'Yêu cầu',  data:requiredData, backgroundColor:'#90CAF9', borderRadius:6, borderSkipped:false }
      ]
    },
    options:{
      responsive:true, maintainAspectRatio:false,
      plugins:{ legend:{ display:true, position:'top', labels:{color:TICK,boxWidth:12,font:{size:11}} } },
      scales:{
        x:{ grid:{color:GRID}, ticks:{color:TICK,font:{size:11}} },
        y:{ grid:{color:GRID}, ticks:{color:TICK,font:{size:11}}, beginAtZero:true, title:{display:true,text:'%',color:TICK} }
      }
    }
  })
}

const selectedWeekNumber = ref(null) // null = chưa chọn -> dùng tuần hiện tại

// Tuần đang được xem trong " Thống kê các buổi tập"
const effectiveWeekNumber = computed(() => {
  return selectedWeekNumber.value != null ? selectedWeekNumber.value : plan.value?.currentWeek
})

const buoiStatsTitleSuffix = computed(() => {
  if (!plan.value || effectiveWeekNumber.value == null) return ''
  return effectiveWeekNumber.value === plan.value.currentWeek
      ? ' - Tuần hiện tại'
      : ` - Tuần ${effectiveWeekNumber.value}`
})

const hasSelectedWeekData = computed(() => {
  if (!plan.value || effectiveWeekNumber.value == null) return false
  return allSessions.value.some(s => s.planId === plan.value.id && s.weekNumber === effectiveWeekNumber.value)
})


// ═══════════════════ MỚI: THỐNG KÊ CÁC BUỔI TẬP ═══════════════════
const expandedMap = ref({})
function toggleExpand(buoiNumber) {
  expandedMap.value[buoiNumber] = !expandedMap.value[buoiNumber]
}

function rateBadgeClass(rate) {
  if (rate == null) return ''
  if (rate >= 90) return 'badge-high'
  if (rate >= 60) return 'badge-mid'
  return 'badge-low'
}

// MỚI — dùng riêng cho " Thống kê các buổi tập", lọc theo tuần được chọn.
// Khác findSessionForBuoi gốc (vốn cố định theo plan.currentWeek, phục vụ biểu đồ
// "Khối lượng % hoàn thành tuần này" — KHÔNG đổi hàm đó).
function findSessionForBuoiInSelectedWeek(planDay) {
  const weekNum = effectiveWeekNumber.value
  const planId  = plan.value?.id
  return allSessions.value.find(s =>
      s.planId === planId && s.weekNumber === weekNum && s.dayName === planDay.dayName
  )
}

// So sánh 1 bài tập: yêu cầu (sets × reps hoặc sets × durationSeconds) vs thực tế (log)
// Chỉ tính TỔNG khối lượng, không tách từng set (dữ liệu backend không lưu chi tiết từng set).
function computeExerciseDiff(pe, log) {
  if (!pe) return null
  if (pe.reps != null) {
    if (pe.sets == null || !log || log.repsCompleted == null) {
      return { type: 'reps', required: (pe.sets != null ? pe.sets * pe.reps : null), actual: null, diff: null }
    }
    const required = pe.sets * pe.reps
    const actual = log.repsCompleted
    return { type: 'reps', required, actual, diff: actual - required }
  } else if (pe.durationSeconds != null) {
    if (pe.sets == null || !log || log.durationSeconds == null) {
      return { type: 'duration', required: (pe.sets != null ? pe.sets * pe.durationSeconds : null), actual: null, diff: null }
    }
    const required = pe.sets * pe.durationSeconds
    const actual = log.durationSeconds
    return { type: 'duration', required, actual, diff: actual - required }
  }
  return null
}

const buoiStats = computed(() => {
  const planDays = plan.value?.planDays || []

  return planDays.map((planDay, idx) => {
    const buoiNumber = idx + 1
    const session = findSessionForBuoiInSelectedWeek(planDay)
    const totalExercises = (planDay.exercises || []).length

    if (!session || session.status !== 'COMPLETED') {
      const isSkipped = session?.status === 'SKIPPED'
      return {
        buoiNumber,
        state: isSkipped ? 'skipped' : 'not_started',
        totalExercises,
        summary: isSkipped ? `${totalExercises} bài tập · Đã bỏ` : `${totalExercises} bài tập · Chưa bắt đầu`,
        completionRate: null,
        exercises: []
      }
    }

    const logsByExId = {}
    ;(session.exerciseLogs || []).forEach(l => { logsByExId[l.exerciseId] = l })

    let shortCount = 0, achievedCount = 0
    let deficitReps = 0, excessReps = 0, deficitSeconds = 0, excessSeconds = 0
    const exerciseDetails = []

    ;(planDay.exercises || []).forEach(pe => {
      const log = logsByExId[pe.exerciseId]
      const d = computeExerciseDiff(pe, log)
      let status = 'unknown'
      if (d && d.diff != null) {
        if (d.diff < 0) {
          status = 'short'; shortCount++
          if (d.type === 'reps') deficitReps += -d.diff; else deficitSeconds += -d.diff
        } else if (d.diff > 0) {
          status = 'excess'; achievedCount++
          if (d.type === 'reps') excessReps += d.diff; else excessSeconds += d.diff
        } else {
          status = 'met'; achievedCount++
        }
      }
      exerciseDetails.push({ exerciseId: pe.exerciseId, exerciseName: pe.exerciseName, ...d, status })
    })

    const parts = [`${totalExercises} bài`]
    if (shortCount > 0) parts.push(`Thiếu ${shortCount} bài`)
    if (deficitReps > 0) parts.push(`Thiếu ${deficitReps} reps`)
    if (deficitSeconds > 0) parts.push(`Thiếu ${deficitSeconds} giây`)
    if (excessReps > 0) parts.push(`Tập thừa ${excessReps} reps`)
    if (excessSeconds > 0) parts.push(`Tập thừa ${excessSeconds} giây`)

    const summary = (shortCount === 0 && excessReps === 0 && excessSeconds === 0)
        ? `${totalExercises} bài · Đạt yêu cầu`
        : parts.join(' · ')

    return {
      buoiNumber,
      state: 'completed',
      totalExercises,
      completionRate: session.completionRate,
      summary,
      shortCount, achievedCount, deficitReps, excessReps, deficitSeconds, excessSeconds,
      exercises: exerciseDetails
    }
  })
})

async function load() {
  loading.value = true
  try {
    const [planRes, sessRes, dashboardRes] = await Promise.all([
      planAPI.getActive().catch(() => ({ data: null })),
      sessionAPI.getAll().catch(() => ({ data: [] })),
      dashboardAPI.get().catch(() => ({ data: null }))
    ])
    plan.value = planRes.data
    allSessions.value = sessRes.data || []
    serverDashboardStats.value = dashboardRes.data?.monthlyCompletedSessions ? dashboardRes.data : null
    dashboardStats.value = serverDashboardStats.value || { ...buildDashboardFallback(allSessions.value), ...(dashboardRes.data || {}) }

  } finally {
    loading.value = false
    nextTick(drawVolumeChart)
    nextTick(drawAnalyticsCharts)
  }
}

function muscleLabel(m) {
  return {
    CHEST: 'Ngực', BACK: 'Lưng', SHOULDERS: 'Vai', ARMS: 'Tay',
    LEGS: 'Chân', CORE: 'Cơ lõi', CARDIO: 'Cardio', FULL_BODY: 'Toàn thân'
  }[m] || m
}

watch(selectedMonth, async value => {
  const isCurrent = dayjs(value).isSame(dayjs(), 'month')
  dashboardStats.value = isCurrent && serverDashboardStats.value
    ? serverDashboardStats.value
    : buildDashboardFallback(allSessions.value, dayjs(value))
  await nextTick()
  drawAnalyticsCharts()
  drawVolumeChart()
})

onMounted(async () => {
  try { const r = await membershipAPI.getActive(); isVip.value = r.data?.membershipType === 'VIP' && r.data?.paymentStatus === 'PAID' } catch { isVip.value = false }
  load()
})
</script>

<style scoped>
.analytics-section{margin-bottom:24px}.analytics-heading{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:14px}.analytics-heading h2{margin:0;color:var(--c-text);font-size:1.15rem}.analytics-heading p{margin:4px 0 0;color:var(--c-text3);font-size:.8rem}.metric-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-bottom:16px}.metric-card{padding:16px;background:linear-gradient(145deg,var(--c-card),var(--c-card2));border:1px solid var(--c-border2);border-radius:12px;display:flex;flex-direction:column;gap:6px}.metric-card span{font-size:.76rem;color:var(--c-text2)}.metric-card strong{font-size:1.45rem;color:var(--c-text)}.metric-card small{color:var(--c-text3);font-size:.7rem}.metric-card .change-up{color:#15803d}.metric-card .change-down{color:#dc2626}.analytics-grid{display:grid;grid-template-columns:1.35fr 1fr;gap:16px}.analytics-card{min-width:0}.analytics-card.wide{grid-column:span 1}.analytics-chart{height:260px;position:relative}.insight-card{background:linear-gradient(145deg,#fffaf2,var(--c-card))}.insight-main{font-weight:600;line-height:1.55;color:var(--c-text)}.insight-card ul{padding-left:18px;color:var(--c-text2);font-size:.85rem;line-height:1.55}.insight-card li{margin-bottom:8px}.insight-card small{color:var(--c-text3)}@media(max-width:1050px){.metric-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:768px){.analytics-grid{grid-template-columns:1fr}.metric-grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:480px){.metric-grid{grid-template-columns:1fr}}
.vip-lock-banner{display:flex;justify-content:space-between;align-items:center;gap:20px;padding:14px 18px;margin-bottom:20px;border:1px solid #e7bd52;background:#fff8dc;border-radius:10px;color:#6b4b00}.vip-lock-banner span{display:block;font-size:.8rem;margin-top:4px}@media(max-width:650px){.vip-lock-banner{align-items:flex-start;flex-direction:column}}
.chart-empty { position:absolute; inset:0; display:flex; align-items:center; justify-content:center; color:var(--c-text3); font-size:0.85rem; }

.volume-heading{display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap}
.month-pagination{display:flex;align-items:center;gap:6px;flex-wrap:wrap}.month-pagination .el-button+.el-button{margin-left:0}
@media(max-width:700px){.volume-heading{align-items:flex-start;flex-direction:column}.month-pagination{width:100%}}

/* ── Thống kê các buổi tập ── */
.buoi-stats-list { display:flex; flex-direction:column; gap:8px; }
.buoi-stat-item { border:1px solid var(--c-border2); border-radius:10px; overflow:hidden; }
.buoi-stat-row {
  display:flex; justify-content:space-between; align-items:center; gap:12px;
  padding:12px 14px; cursor:pointer; background:var(--c-card2);
}
.buoi-stat-row:hover { background:#EDE0D0; }
.buoi-stat-main { flex:1; min-width:0; }
.buoi-stat-title { font-weight:700; font-size:0.9rem; color:var(--c-text); display:flex; align-items:center; gap:8px; flex-wrap:wrap; }
.buoi-stat-sub { font-size:0.78rem; color:var(--c-text3); margin-top:3px; }

.buoi-badge { font-size:0.72rem; font-weight:600; padding:2px 8px; border-radius:12px; }
.badge-high  { background:#E8F5E9; color:#2E7D32; }
.badge-mid   { background:#FFF3E0; color:#E65100; }
.badge-low   { background:#FFEBEE; color:#C62828; }
.badge-empty { background:#f1f5f9; color:#64748b; }
.badge-skip  { background:#fee2e2; color:#b91c1c; }

.buoi-expand-icon { transition:transform 0.2s; color:var(--c-text3); flex-shrink:0; }
.buoi-expand-icon.open { transform:rotate(180deg); }

.buoi-detail { padding:12px 14px; background:var(--c-card); border-top:1px dashed var(--c-border2); }
.detail-group { margin-bottom:10px; }
.detail-group-title { font-size:0.8rem; font-weight:700; margin-bottom:6px; }
.detail-group-title.short  { color:#c2410c; }
.detail-group-title.excess { color:#1565C0; }
.detail-ex-row { display:flex; justify-content:space-between; padding:4px 0; font-size:0.8rem; }
.detail-ex-name { color:var(--c-text); }
.detail-ex-diff.short  { color:#c2410c; font-weight:600; }
.detail-ex-diff.excess { color:#1565C0; font-weight:600; }
.detail-footer { font-size:0.82rem; font-weight:700; color:#2E7D32; margin-top:6px; }
</style>
