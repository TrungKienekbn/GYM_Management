<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>THỰC ĐƠN MÓN ĂN</h2>
    </div>

    <el-card style="margin-bottom:20px">
      <div style="display:flex; gap:14px; flex-wrap:wrap; align-items:center">
        <el-radio-group v-model="goal" @change="load">
          <el-radio-button label="">Tất cả</el-radio-button>
          <el-radio-button label="WEIGHT_LOSS">🔥 Giảm cân</el-radio-button>
          <el-radio-button label="MUSCLE_GAIN">💪 Tăng cân</el-radio-button>
          <el-radio-button label="MAINTENANCE">⚖️ Duy trì</el-radio-button>
        </el-radio-group>

        <el-input v-model="keyword" placeholder="Tìm món ăn..." clearable
                  @input="load" style="max-width:260px; margin-left:auto">
          <template #prefix><el-icon><Search/></el-icon></template>
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
            <span v-if="!f.imageUrl">🍽️</span>
          </div>
          <div class="food-name display">{{ f.name }}</div>
          <div class="food-macros">
            <span>🔥 {{ f.calories ?? 0 }} kcal</span>
            <span>🥩 {{ f.proteinGrams ?? 0 }}g</span>
            <span>🥑 {{ f.fatGrams ?? 0 }}g</span>
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
    <el-dialog v-model="detailDialog" :title="selected?.name" width="520px" align-center>
      <template v-if="selected">
        <div class="grid-3" style="margin-bottom:16px">
          <div class="stat-card">
            <div class="label">CALO</div>
            <div class="value">{{ selected.calories ?? 0 }}</div>
            <div class="sub">kcal</div>
          </div>
          <div class="stat-card">
            <div class="label">PROTEIN</div>
            <div class="value">{{ selected.proteinGrams ?? 0 }}</div>
            <div class="sub">gram</div>
          </div>
          <div class="stat-card">
            <div class="label">CHẤT BÉO</div>
            <div class="value">{{ selected.fatGrams ?? 0 }}</div>
            <div class="sub">gram</div>
          </div>
        </div>

        <h4 style="margin-bottom:6px">🧂 Nguyên liệu</h4>
        <p style="white-space:pre-line; color:var(--c-text2); margin-bottom:16px">{{ selected.ingredients || 'Chưa cập nhật' }}</p>

        <h4 style="margin-bottom:6px">👨‍🍳 Cách nấu</h4>
        <p style="white-space:pre-line; color:var(--c-text2)">{{ selected.instructions || 'Chưa cập nhật' }}</p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { foodAPI } from '@/api'
import { Search } from '@element-plus/icons-vue'

const foods        = ref([])
const loading       = ref(true)
const keyword       = ref('')
const goal          = ref('')
const detailDialog  = ref(false)
const selected      = ref(null)

async function load() {
  loading.value = true
  try {
    const r = await foodAPI.getAll({ keyword: keyword.value || undefined, goal: goal.value || undefined })
    foods.value = r.data || []
  } finally { loading.value = false }
}

function openDetail(f) { selected.value = f; detailDialog.value = true }

function goalLabel(g) { return { WEIGHT_LOSS:'Giảm cân', MUSCLE_GAIN:'Tăng cân', MAINTENANCE:'Duy trì' }[g] || g }
function goalBadge(g) { return { WEIGHT_LOSS:'badge-danger', MUSCLE_GAIN:'badge-success', MAINTENANCE:'badge-warning' }[g] || '' }

onMounted(load)
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
.food-goals { display:flex; gap:6px; flex-wrap:wrap; }
</style>