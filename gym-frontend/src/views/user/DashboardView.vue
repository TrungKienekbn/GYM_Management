<template>
  <div class="fade-in">
    <div v-if="loading">
      <el-skeleton :rows="6" animated style="background:var(--c-card);padding:24px;border-radius:12px"/>
    </div>

    <template v-else>
      <!-- ===================== KHỐI LƯỢNG % HOÀN THÀNH TUẦN NÀY (không đổi) ===================== -->
      <el-card style="margin-bottom:24px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span style="font-weight:700">📊 KHỐI LƯỢNG % HOÀN THÀNH TUẦN NÀY</span>
            <span v-if="volumeGapText" style="font-size:0.8rem;font-weight:600" :style="{color:volumeGapColor}">
              {{ volumeGapText }}
            </span>
          </div>
        </template>
        <div style="height:220px;position:relative">
          <canvas ref="volChart"></canvas>
          <div v-if="noVolume" class="chart-empty">Chưa có dữ liệu</div>
        </div>
      </el-card>

      <!-- ===================== LỊCH TẬP HÔM NAY | MÓN ĂN ĐỀ XUẤT (không đổi) ===================== -->
      <div class="today-row">
        <el-card>
          <template #header>
            <span style="font-weight:700">🏋️ LỊCH TẬP HÔM NAY</span>
          </template>

          <div v-if="!plan" class="empty-state" style="padding:16px">
            Bạn chưa có giáo án nào.
          </div>
          <div v-else-if="!todayPlanDay" class="empty-state" style="padding:16px">
            Hôm nay không có lịch tập theo kế hoạch.
          </div>
          <div v-else>

            <!--<div style="font-weight:700;color:var(--c-accent);margin-bottom:10px">
                              {{ todayPlanDay.dayName }} · {{ todayPlanDay.exercises?.length || 0 }} bài tập
                            </div>-->
            <div class="exercise-list">
              <div v-for="ex in todayPlanDay.exercises" :key="ex.id" class="ex-row">
                <div class="ex-info">
                  <div class="ex-name">{{ ex.exerciseName }}</div>
                  <div class="ex-sub">{{ muscleLabel(ex.muscleGroup) }}</div>
                </div>
                <div class="ex-meta">
                  <div class="ex-sets">
                    <span v-if="ex.reps">{{ ex.sets }}×{{ ex.reps }}</span>
                    <span v-else-if="ex.durationSeconds">{{ ex.sets }}×{{ ex.durationSeconds }}s</span>
                  </div>
                  <div v-if="ex.restSeconds" style="font-size:0.7rem;color:var(--c-text3)">nghỉ {{ ex.restSeconds }}s</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card>
          <template #header>
            <span style="font-weight:700">🍽️ MÓN ĂN ĐỀ XUẤT</span>
          </template>

          <div v-if="!plan" class="empty-state" style="padding:16px">
            Bạn cần có giáo án để nhận đề xuất món ăn.
          </div>
          <template v-else>
            <div style="font-size:0.82rem;color:var(--c-text2);margin-bottom:10px">
              Mục tiêu: {{ goalLabel(foodGoalFor(plan.goal)) }}
            </div>

            <div v-if="loadingFoods" style="font-size:0.85rem;color:var(--c-text3)">Đang tải món ăn...</div>
            <div v-else-if="foodError" style="font-size:0.85rem;color:var(--c-text3)">Không thể tải dữ liệu món ăn.</div>
            <div v-else-if="!recommendedFoods.length" style="font-size:0.85rem;color:var(--c-text3)">
              Chưa có món ăn đề xuất cho mục tiêu này.
            </div>
            <div v-else class="food-suggest-list">
              <div v-for="f in recommendedFoods" :key="f.id" class="food-suggest-card">
                <img v-if="f.imageUrl" :src="f.imageUrl" class="food-suggest-img" alt="" />
                <div class="food-suggest-info">
                  <div class="food-suggest-name">{{ f.name }}</div>
                  <div class="food-suggest-meta">
                    <span v-if="f.calories != null">🔥 {{ f.calories }} kcal</span>
                    <span v-if="f.proteinGrams != null">🥩 Protein {{ f.proteinGrams }}g</span>
                    <span v-if="f.fatGrams != null">🥑 Chất béo {{ f.fatGrams }}g</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-card>
      </div>

      <!-- ===================== THỐNG KÊ CÁC BUỔI TẬP (MỚI) ===================== -->
      <el-card style="margin-top:24px" v-if="plan">
        <template #header>
          <span style="font-weight:700">📋 THỐNG KÊ CÁC BUỔI TẬP</span>
        </template>

        <div class="buoi-stats-list">
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
              <el-icon class="buoi-expand-icon" :class="{ open: expandedMap[b.buoiNumber] }"><ArrowDown/></el-icon>
            </div>

            <div v-if="expandedMap[b.buoiNumber] && b.state === 'completed'" class="buoi-detail">
              <div v-if="b.shortCount > 0" class="detail-group">
                <div class="detail-group-title short">⚠️ Cần cải thiện</div>
                <div v-for="ex in b.exercises.filter(e => e.status === 'short')" :key="ex.exerciseId" class="detail-ex-row">
                  <span class="detail-ex-name">{{ ex.exerciseName }}</span>
                  <span class="detail-ex-diff short">Thiếu {{ Math.abs(ex.diff) }} {{ ex.type === 'reps' ? 'reps' : 'giây' }}</span>
                </div>
              </div>

              <div v-if="b.excessReps > 0 || b.excessSeconds > 0" class="detail-group">
                <div class="detail-group-title excess">📈 Tập thừa</div>
                <div v-for="ex in b.exercises.filter(e => e.status === 'excess')" :key="ex.exerciseId" class="detail-ex-row">
                  <span class="detail-ex-name">{{ ex.exerciseName }}</span>
                  <span class="detail-ex-diff excess">+{{ ex.diff }} {{ ex.type === 'reps' ? 'reps' : 'giây' }}</span>
                </div>
              </div>

              <div class="detail-footer">
                ✅ Đạt yêu cầu: {{ b.achievedCount }}/{{ b.totalExercises }} bài
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { planAPI, sessionAPI, foodAPI } from '@/api'
import { ArrowDown } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

Chart.register(...registerables)

const plan         = ref(null)
const allSessions  = ref([])
const loading       = ref(true)
const recommendedFoods = ref([])
const loadingFoods      = ref(false)
const foodError         = ref(false)

// ── Khối lượng % hoàn thành tuần này — theo THỨ TỰ BUỔI trong giáo án (không đổi) ──
const volChart = ref(null)
let volInst = null

function findSessionForBuoi(planDay) {
  const weekNum = plan.value?.currentWeek
  const planId  = plan.value?.id
  return allSessions.value.find(s =>
      s.planId === planId && s.weekNumber === weekNum && s.dayName === planDay.dayName
  )
}

const sessionProgress = computed(() => {
  const planDays = plan.value?.planDays || []
  const result = []
  for (let i = 0; i < 7; i++) {
    const buoiNumber = i + 1
    const planDay = planDays[i]

    if (!planDay) {
      result.push({ buoiNumber, required: 0, actual: 0 })
      continue
    }

    const session = findSessionForBuoi(planDay)
    const actual = (session && session.status === 'COMPLETED' && session.completionRate != null)
        ? session.completionRate
        : 0

    result.push({ buoiNumber, required: 100, actual })
  }
  return result
})

const noVolume = computed(() => !plan.value || (plan.value.planDays || []).length === 0)

const todayDow = computed(() => {
  const d = dayjs().day()
  return d === 0 ? 7 : d
})

const todayBuoiNumber = computed(() => {
  const planDays = plan.value?.planDays || []
  const idx = planDays.findIndex(d => d.dayOfWeek === todayDow.value)
  return idx >= 0 ? idx + 1 : null
})


const volumeGapColor = computed(() => {
  if (!todayBuoiNumber.value) return 'var(--c-text3)'
  const item = sessionProgress.value[todayBuoiNumber.value - 1]
  if (!item || !item.required) return 'var(--c-text3)'
  return item.actual >= item.required ? 'var(--c-success)' : 'var(--c-warning)'
})

function drawVolumeChart() {
  const GRID = 'rgba(196,154,108,0.3)', TICK = '#4A3728'
  if (!volChart.value) return
  if (volInst) volInst.destroy()

  const labels       = sessionProgress.value.map(r => 'Buổi ' + r.buoiNumber)
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

// ── Lịch tập hôm nay (không đổi) ──
const todayPlanDay = computed(() => {
  return (plan.value?.planDays || []).find(d => d.dayOfWeek === todayDow.value) || null
})

function foodGoalFor(goal) {
  return goal === 'ENDURANCE' ? 'MAINTENANCE' : goal
}

async function loadRecommendedFoods() {
  if (!plan.value?.goal) return
  loadingFoods.value = true
  foodError.value = false
  try {
    const res = await foodAPI.getAll({ goal: foodGoalFor(plan.value.goal) })
    recommendedFoods.value = (res.data || []).slice(0, 3)
  } catch (err) {
    foodError.value = true
    recommendedFoods.value = []
  } finally {
    loadingFoods.value = false
  }
}

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
    const session = findSessionForBuoi(planDay)
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
    const [planRes, sessRes] = await Promise.all([
      planAPI.getActive().catch(() => ({ data: null })),
      sessionAPI.getAll().catch(() => ({ data: [] }))
    ])
    plan.value = planRes.data
    allSessions.value = sessRes.data || []

    if (plan.value?.goal) {
      await loadRecommendedFoods()
    }
  } finally {
    loading.value = false
    nextTick(drawVolumeChart)
  }
}

function goalLabel(g) {
  return {
    WEIGHT_LOSS: '🔥 Giảm cân',
    MUSCLE_GAIN: '💪 Tăng cơ',
    ENDURANCE: '🏃 Sức bền',
    MAINTENANCE: '⚖️ Duy trì'
  }[g] || g
}

function muscleLabel(m) {
  return {
    CHEST: 'Ngực', BACK: 'Lưng', SHOULDERS: 'Vai', ARMS: 'Tay',
    LEGS: 'Chân', CORE: 'Cơ lõi', CARDIO: 'Cardio', FULL_BODY: 'Toàn thân'
  }[m] || m
}

onMounted(load)
</script>

<style scoped>
.chart-empty { position:absolute; inset:0; display:flex; align-items:center; justify-content:center; color:var(--c-text3); font-size:0.85rem; }

.today-row { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
@media (max-width:768px) { .today-row { grid-template-columns:1fr; } }

.exercise-list { display:flex; flex-direction:column; gap:6px; }
.ex-row {
  display:flex; align-items:center; gap:10px; padding:8px 10px;
  background:var(--c-card2); border-radius:var(--radius);
}
.ex-info { flex:1; min-width:0; }
.ex-name { font-size:0.875rem; font-weight:600; color:var(--c-text); }
.ex-sub  { font-size:0.72rem; color:var(--c-text3); margin-top:1px; }
.ex-meta { text-align:right; flex-shrink:0; }
.ex-sets { font-size:0.82rem; color:var(--c-accent); font-family:var(--font-mono); font-weight:700; }

.food-suggest-list { display:flex; flex-direction:column; gap:8px; }
.food-suggest-card { display:flex; gap:10px; align-items:center; background:var(--c-card2); border-radius:8px; padding:8px 10px; }
.food-suggest-img { width:44px; height:44px; object-fit:cover; border-radius:8px; flex-shrink:0; }
.food-suggest-info { flex:1; min-width:0; }
.food-suggest-name { font-size:0.85rem; font-weight:600; color:var(--c-text); }
.food-suggest-meta { display:flex; gap:10px; flex-wrap:wrap; font-size:0.72rem; color:var(--c-text3); margin-top:3px; }

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