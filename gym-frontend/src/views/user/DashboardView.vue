<template>
  <div class="fade-in">
    <div v-if="loading">
      <el-skeleton :rows="6" animated style="background:var(--c-card);padding:24px;border-radius:12px"/>
    </div>

    <template v-else>
      <!-- ===================== KHỐI LƯỢNG % HOÀN THÀNH TUẦN NÀY (theo Buổi) ===================== -->
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

      <!-- ===================== LỊCH TẬP HÔM NAY | MÓN ĂN ĐỀ XUẤT ===================== -->
      <div class="today-row">
        <!-- Lịch tập hôm nay -->
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
            <div style="font-weight:700;color:var(--c-accent);margin-bottom:10px">
              {{ todayPlanDay.dayName }} · {{ todayPlanDay.exercises?.length || 0 }} bài tập
            </div>
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

        <!-- Món ăn đề xuất -->
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
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { planAPI, sessionAPI, foodAPI } from '@/api'
import dayjs from 'dayjs'

Chart.register(...registerables)

const plan         = ref(null)
const allSessions  = ref([])
const loading       = ref(true)
const recommendedFoods = ref([])
const loadingFoods      = ref(false)
const foodError         = ref(false)

// ── Khối lượng % hoàn thành tuần này — theo THỨ TỰ BUỔI trong giáo án ──
const volChart = ref(null)
let volInst = null

// plan.planDays đã được backend trả về sắp xếp tăng dần theo dayOfWeek
// (dayRepo.findByWorkoutPlanIdOrderByDayOfWeek) => vị trí i trong mảng = Buổi i+1.
const sessionProgress = computed(() => {
  const planDays = plan.value?.planDays || []
  const weekNum  = plan.value?.currentWeek
  const planId   = plan.value?.id

  const result = []
  for (let i = 0; i < 7; i++) {
    const buoiNumber = i + 1
    const planDay = planDays[i]

    if (!planDay) {
      // Giáo án không có buổi thứ i+1 này => không tính là buổi cần hoàn thành
      result.push({ buoiNumber, required: 0, actual: 0 })
      continue
    }

    // Khớp session thực tế với buổi này qua planId + weekNumber + dayName
    // (đúng cách khớp đã dùng sẵn trong Plan.vue, không tạo field mới)
    const session = allSessions.value.find(s =>
        s.planId === planId &&
        s.weekNumber === weekNum &&
        s.dayName === planDay.dayName
    )

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

// Buổi số mấy tương ứng với hôm nay (nếu có), dựa trên vị trí trong planDays
const todayBuoiNumber = computed(() => {
  const planDays = plan.value?.planDays || []
  const idx = planDays.findIndex(d => d.dayOfWeek === todayDow.value)
  return idx >= 0 ? idx + 1 : null
})

const volumeGapText = computed(() => {
  if (!todayBuoiNumber.value) return ''
  const item = sessionProgress.value[todayBuoiNumber.value - 1]
  if (!item || !item.required) return ''
  if (!item.actual) return `Buổi ${todayBuoiNumber.value} hôm nay cần đạt ${item.required}% — chưa tập`
  const gap = item.required - item.actual
  return gap > 0 ? `Buổi ${todayBuoiNumber.value}: còn thiếu ${gap}% so với yêu cầu` : '✅ Buổi hôm nay đã đạt yêu cầu!'
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
    MAINTENANCE: '⚖️ Duy trì , Sức bền'
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
</style>