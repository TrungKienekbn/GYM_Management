<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>THỰC ĐƠN MÓN ĂN</h2>
    </div>

    <el-alert v-if="nutritionTarget" type="success" :closable="false" show-icon style="margin-bottom:16px">
      <template #title>Gợi ý theo hồ sơ: khoảng {{ nutritionTarget.calories }} kcal và {{ nutritionTarget.protein }}g protein/ngày</template>
      Mục tiêu {{ goalLabel(profile?.goal) }} · đây là mức tham khảo, không thay thế tư vấn dinh dưỡng hoặc y tế.
    </el-alert>

    <el-card style="margin-bottom:20px">
      <div style="display:flex; gap:14px; flex-wrap:wrap; align-items:center">
        <el-radio-group v-model="goal" @change="load">
          <el-radio-button label="">Tất cả</el-radio-button>
          <el-radio-button label="WEIGHT_LOSS"> Giảm cân</el-radio-button>
          <el-radio-button label="MUSCLE_GAIN"> Tăng cân</el-radio-button>
          <el-radio-button label="MAINTENANCE"> Tổng hợp</el-radio-button>
          <el-radio-button label="ENDURANCE"> Sức bền</el-radio-button>
        </el-radio-group>

        <el-input v-model="keyword" placeholder="Tìm món ăn..." clearable
                  @input="load" style="max-width:260px; margin-left:auto">
          <template #prefix></template>
        </el-input>
      </div>
    </el-card>

    <div v-loading="loading">
      <div v-if="!foods.length" class="empty-state" style="padding:60px 0; text-align:center; color:var(--c-text3)">
        Chưa có món ăn nào phù hợp
      </div>

      <div v-else class="food-grid">
        <el-card v-for="f in foods" :key="f.id" class="food-card" shadow="hover" @click="openDetail(f)">
          <div class="food-thumb" :style="f.imageUrl ? {backgroundImage:`url(${f.imageUrl})`} : {}">
            <span v-if="!f.imageUrl"></span>
          </div>
          <div class="food-name display">{{ f.name }}</div>
          <div class="food-macros">
            <span> {{ f.calories ?? 0 }} kcal</span>
            <span> {{ f.proteinGrams ?? 0 }}g</span>
            <span> {{ f.fatGrams ?? 0 }}g</span>
          </div>
          <div v-if="f.weightGrams" class="food-per100g">
             1 phần {{ f.weightGrams }}g · {{ f.caloriesPer100g }} kcal/100g
          </div>
          <div class="food-goals">
            <span v-for="g in f.suitableGoalsList" :key="g" class="badge" :class="goalBadge(g)">
              {{ goalLabel(g) }}
            </span>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Detail dialog -->
    <el-dialog v-model="detailDialog" :title="selected?.name" width="520px" align-center append-to-body>
      <template v-if="selected">
        <div class="grid-3" style="margin-bottom:16px">
          <div class="stat-card">
            <div class="label">CALO</div>
            <div class="value">{{ selected.calories ?? 0 }}</div>
            <div class="sub">kcal</div>
          </div>
          <div class="stat-card">
            <div class="label">CHẤT ĐẠM</div>
            <div class="value">{{ selected.proteinGrams ?? 0 }}</div>
            <div class="sub">gram</div>
          </div>
          <div class="stat-card">
            <div class="label">CHẤT BÉO</div>
            <div class="value">{{ selected.fatGrams ?? 0 }}</div>
            <div class="sub">gram</div>
          </div>
        </div>

        <div v-if="selected.weightGrams" class="per100g-box">
          <div> Một khẩu phần: <strong>{{ selected.weightGrams }}g</strong></div>
          <div>Quy đổi trên 100g:  {{ selected.caloriesPer100g }} kcal ·
           {{ selected.proteinPer100g }}g protein ·  {{ selected.fatPer100g }}g chất béo</div>
        </div>

        <h4 style="margin-bottom:6px">Nguyên liệu</h4>
        <p class="recipe-content">{{ selected.ingredients || 'Chưa cập nhật nguyên liệu' }}</p>

        <h4 style="margin-bottom:6px">Công thức và các bước chế biến</h4>
        <p class="recipe-content">{{ selected.instructions || 'Chưa cập nhật công thức chế biến' }}</p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { foodAPI, profileAPI } from '@/api'
import { Search } from '@element-plus/icons-vue'

const foods        = ref([])
const loading       = ref(true)
const keyword       = ref('')
const goal          = ref('')
const detailDialog  = ref(false)
const selected      = ref(null)
const profile       = ref(null)
const nutritionTarget = computed(() => {
  const p = profile.value
  if (!p?.weight || !p?.height || !p?.dateOfBirth) return null
  const age = Math.max(15, new Date().getFullYear() - Number(String(p.dateOfBirth).slice(0, 4)))
  const base = 10 * p.weight + 6.25 * p.height - 5 * age + (p.gender === 'MALE' ? 5 : -161)
  const activity = Number(p.availableDaysPerWeek) >= 5 ? 1.55 : Number(p.availableDaysPerWeek) >= 3 ? 1.375 : 1.2
  const adjustment = p.goal === 'WEIGHT_LOSS' ? -400 : p.goal === 'MUSCLE_GAIN' ? 300 : 0
  return {
    calories: Math.max(1200, Math.round((base * activity + adjustment) / 50) * 50),
    protein: Math.round(p.weight * (p.goal === 'MUSCLE_GAIN' ? 1.8 : 1.6))
  }
})

async function load() {
  loading.value = true
  try {
    const r = await foodAPI.getAll({ keyword: keyword.value || undefined, goal: goal.value || undefined })
    foods.value = r.data || []
  } finally { loading.value = false }
}

function openDetail(f) { selected.value = f; detailDialog.value = true }

function goalLabel(g) { return { WEIGHT_LOSS:'Giảm cân', MUSCLE_GAIN:'Tăng cân', MAINTENANCE:'Tổng hợp', ENDURANCE:'Sức bền' }[g] || 'Khác' }
function goalBadge(g) { return { WEIGHT_LOSS:'badge-danger', MUSCLE_GAIN:'badge-success', MAINTENANCE:'badge-warning', ENDURANCE:'badge-info' }[g] || '' }

onMounted(async () => {
  try {
    const r = await profileAPI.get()
    profile.value = r.data
    if (r.data?.goal && ['WEIGHT_LOSS','MUSCLE_GAIN','MAINTENANCE','ENDURANCE'].includes(r.data.goal)) goal.value = r.data.goal
  } catch {}
  load()
})
</script>

<style scoped>
.food-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:16px; }
.food-card { cursor:pointer; transition:transform .15s; }
.food-card:hover { transform:translateY(-3px); }
.food-thumb {
  height:120px; border-radius:var(--radius-lg); background:var(--c-card2);
  background-size:cover; background-position:center;
  display:flex; align-items:center; justify-content:center; font-size:2.4rem; margin-bottom:10px;
}
.food-name { font-size:1rem; margin-bottom:8px; }
.food-macros { display:flex; gap:10px; font-size:0.8rem; color:var(--c-text2); margin-bottom:8px; flex-wrap:wrap; }
.food-per100g { font-size:0.72rem; color:var(--c-text3); margin-bottom:6px; }
.food-goals { display:flex; gap:6px; flex-wrap:wrap; }
.per100g-box  { font-size:0.82rem; color:var(--c-text2); background:var(--c-card2); line-height:1.7;
              padding:10px 14px; border-radius:8px; margin-bottom:16px; }
.recipe-content { white-space:pre-line; color:var(--c-text2); line-height:1.65; padding:12px 14px;
                  margin:0 0 16px; background:var(--c-card2); border:1px solid var(--c-border2);
                  border-radius:var(--radius-lg); }
</style>
