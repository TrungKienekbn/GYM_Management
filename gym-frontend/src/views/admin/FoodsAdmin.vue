<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>QUẢN LÝ MÓN ĂN</h2>
      <el-button type="primary" @click="openAdd">+ THÊM MÓN ĂN</el-button>
    </div>

    <el-card style="margin-bottom:16px">
      <el-input v-model="keyword" placeholder="Tìm theo tên món ăn..." clearable
                @input="load" style="max-width:320px">
        <template #prefix><el-icon><Search/></el-icon></template>
      </el-input>
    </el-card>

    <el-card>
      <el-table :data="foods" v-loading="loading" stripe>
        <el-table-column label="ID" prop="id" width="55" align="center"/>
        <el-table-column label="Tên món" prop="name" min-width="160"/>
        <el-table-column label="Calo" width="90" align="center">
          <template #default="{row}">{{ row.calories ?? 0 }} kcal</template>
        </el-table-column>
        <el-table-column label="Protein" width="90" align="center">
          <template #default="{row}">{{ row.proteinGrams ?? 0 }}g</template>
        </el-table-column>
        <el-table-column label="Chất béo" width="90" align="center">
          <template #default="{row}">{{ row.fatGrams ?? 0 }}g</template>
        </el-table-column>
        <el-table-column label="Khối lượng" width="100" align="center">
          <template #default="{row}">{{ row.weightGrams ? row.weightGrams + 'g' : '—' }}</template>
        </el-table-column>
        <el-table-column label="Trên 1kg" width="140" align="center">
          <template #default="{row}">
            <div v-if="row.weightGrams" style="font-size:0.72rem;line-height:1.5;text-align:left">
              🔥 {{ row.caloriesPerKg ?? 0 }} kcal<br/>
              🥩 {{ row.proteinPerKg ?? 0 }}g · 🥑 {{ row.fatPerKg ?? 0 }}g
            </div>
            <span v-else style="color:var(--c-text3)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="Phù hợp mục tiêu" min-width="220">
          <template #default="{row}">
            <span v-for="g in row.suitableGoalsList" :key="g" class="badge" :class="goalBadge(g)" style="margin-right:6px">
              {{ goalLabel(g) }}
            </span>
            <span v-if="!row.suitableGoalsList?.length" style="color:var(--c-text3)">—</span>
          </template>
        </el-table-column>
        <el-table-column label="Thao tác" width="150" align="center" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">Sửa</el-button>
            <el-button size="small" type="danger" plain @click="remove(row.id)">Xóa</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="formDialog" :title="editId ? 'SỬA MÓN ĂN' : 'THÊM MÓN ĂN'" width="620px" align-center>
      <el-form :model="form" label-position="top">
        <el-form-item label="Tên món ăn">
          <el-input v-model="form.name" placeholder="VD: Ức gà áp chảo sốt chanh"/>
        </el-form-item>

        <div class="grid-4">
          <el-form-item label="Calo (kcal)">
            <el-input-number v-model="form.calories" :min="0" :max="5000" style="width:100%"/>
          </el-form-item>
          <el-form-item label="Protein (g)">
            <el-input-number v-model="form.proteinGrams" :min="0" :max="500" :step="0.5" style="width:100%"/>
          </el-form-item>
          <el-form-item label="Chất béo (g)">
            <el-input-number v-model="form.fatGrams" :min="0" :max="500" :step="0.5" style="width:100%"/>
          </el-form-item>
          <el-form-item label="Khối lượng (g)">
            <el-input-number v-model="form.weightGrams" :min="0" :max="5000" :step="10" style="width:100%"
                              placeholder="VD: 250"/>
          </el-form-item>
        </div>
        <div v-if="previewPerKg" style="font-size:0.78rem;color:var(--c-text2);margin:-8px 0 14px">
          → Quy đổi trên 1kg: 🔥 {{ previewPerKg.cal }} kcal · 🥩 {{ previewPerKg.pro }}g protein · 🥑 {{ previewPerKg.fat }}g béo
        </div>

        <el-form-item label="Phù hợp cho mục tiêu">
          <el-checkbox-group v-model="form.suitableGoals">
            <el-checkbox label="WEIGHT_LOSS">🔥 Giảm cân</el-checkbox>
            <el-checkbox label="MUSCLE_GAIN">💪 Tăng cân</el-checkbox>
            <el-checkbox label="MAINTENANCE">⚖️ Duy trì</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="Nguyên liệu">
          <el-input v-model="form.ingredients" type="textarea" :rows="3"
                     placeholder="VD: 200g ức gà, 1 quả chanh, 1 thìa dầu ô liu, tỏi, muối, tiêu..."/>
        </el-form-item>

        <el-form-item label="Cách nấu">
          <el-input v-model="form.instructions" type="textarea" :rows="4"
                     placeholder="VD: Bước 1... Bước 2..."/>
        </el-form-item>

        <el-form-item label="Ảnh minh họa (URL - tùy chọn)">
          <el-input v-model="form.imageUrl" placeholder="https://..."/>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog=false">Hủy</el-button>
        <el-button type="primary" @click="submit">{{ editId ? 'CẬP NHẬT' : 'THÊM MỚI' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { foodAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const foods       = ref([])
const loading      = ref(true)
const keyword      = ref('')
const formDialog   = ref(false)
const editId       = ref(null)

const defaultForm = () => ({
  name:'', calories:0, proteinGrams:0, fatGrams:0, weightGrams:null,
  ingredients:'', instructions:'', imageUrl:'',
  suitableGoals: []
})
const form = reactive(defaultForm())

const previewPerKg = computed(() => {
  if (!form.weightGrams || form.weightGrams <= 0) return null
  const k = 1000 / form.weightGrams
  return {
    cal: Math.round((form.calories || 0) * k),
    pro: Math.round((form.proteinGrams || 0) * k * 10) / 10,
    fat: Math.round((form.fatGrams || 0) * k * 10) / 10
  }
})

async function load() {
  loading.value = true
  try {
    const r = await foodAPI.getAll({ keyword: keyword.value || undefined })
    foods.value = r.data || []
  } finally { loading.value = false }
}

function openAdd() {
  editId.value = null
  Object.assign(form, defaultForm())
  formDialog.value = true
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(form, {
    name: row.name, calories: row.calories ?? 0,
    proteinGrams: row.proteinGrams ?? 0, fatGrams: row.fatGrams ?? 0,
    weightGrams: row.weightGrams ?? null,
    ingredients: row.ingredients || '', instructions: row.instructions || '',
    imageUrl: row.imageUrl || '',
    suitableGoals: [...(row.suitableGoalsList || [])]
  })
  formDialog.value = true
}

async function submit() {
  if (!form.name) { ElMessage.warning('Nhập tên món ăn'); return }
  if (editId.value) { await foodAPI.update(editId.value, { ...form }); ElMessage.success('Đã cập nhật!') }
  else              { await foodAPI.create({ ...form }); ElMessage.success('Đã thêm món ăn!') }
  formDialog.value = false; load()
}

async function remove(id) {
  await ElMessageBox.confirm('Xóa món ăn này?', 'Xác nhận', { type:'warning' })
  await foodAPI.delete(id); ElMessage.success('Đã xóa món ăn'); load()
}

function goalLabel(g) { return { WEIGHT_LOSS:'Giảm cân', MUSCLE_GAIN:'Tăng cân', MAINTENANCE:'Duy trì' }[g] || g }
function goalBadge(g) { return { WEIGHT_LOSS:'badge-danger', MUSCLE_GAIN:'badge-success', MAINTENANCE:'badge-warning' }[g] || '' }

onMounted(load)
</script>