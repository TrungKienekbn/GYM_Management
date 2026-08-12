<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>CÔNG THỨC HỆ THỐNG</h2>
    </div>

    <p style="color:var(--c-text2); margin-bottom:20px">
      Các hệ số/ngưỡng dưới đây được dùng trực tiếp trong công thức tính điểm thể lực, năng lượng và giáo án tự động.
      Phần giải thích được hiển thị ngay dưới từng công thức để dễ kiểm tra trước khi chỉnh sửa.
    </p>

    <div v-for="cat in categories" :key="cat" style="margin-bottom:28px">
      <h3 class="display" style="font-size:1.1rem; margin-bottom:12px; color:var(--c-accent)">{{ cat }}</h3>
      <el-card>
        <el-table :data="grouped[cat]" v-loading="loading">
          <el-table-column label="Tên công thức" min-width="260">
            <template #default="{row}">
              <div class="config-name">
                {{ vnLabel(row.configKey) }}
                <el-tooltip :content="row.description || 'Chưa có giải thích cho công thức này.'" placement="top-start">
                  <el-icon class="help-icon"><QuestionFilled /></el-icon>
                </el-tooltip>
              </div>
              <div style="font-size:0.72rem; color:var(--c-text3); margin-top:2px">{{ row.configKey }}</div>
              <div class="config-description">{{ row.description || 'Chưa có giải thích cho công thức này.' }}</div>
            </template>
          </el-table-column>

          <el-table-column label="Giá trị hiện tại" width="220" align="center">
            <template #default="{row}">
              <el-select v-if="row.configKey === 'LOW_COMPLETION_ACTION'" v-model="row.configValue" style="width:200px">
                <el-option label="Đổi sang bài dễ hơn" :value="1" />
                <el-option label="Giảm sets và reps" :value="2" />
              </el-select>
              <el-input-number v-else
                v-model="row.configValue"
                :precision="configPrecision(row.configKey)" :step="configStep(row.configKey)"
                :min="configMin(row.configKey)" :max="configMax(row.configKey)"
                controls-position="right" style="width:160px"/>
            </template>
          </el-table-column>

          <el-table-column label="" width="120" align="center">
            <template #default="{row}">
              <el-button type="primary" size="small" @click="save(row)">Lưu</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { systemConfigAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'

const configs = ref([])
const loading  = ref(true)
// Tên tiếng Việt sát nghĩa cho từng configKey — chỉ đổi HIỂN THỊ, không đổi key gốc (key gốc
// vẫn được gửi lên backend khi lưu, đảm bảo không phá vỡ logic tính toán).
const VN_LABELS = {
  // Fitness Score
  FS_WEIGHT_AGE:               'Trọng số Tuổi (Fitness Score)',
  FS_WEIGHT_WEIGHT:            'Trọng số Cân nặng (Fitness Score)',
  // Mana (thể lực)
  MANA_MAX_MULTIPLIER:         'Hệ số tính Mana tối đa',
  MANA_REGEN_RATE_1_DAY:       'Tỉ lệ hồi Mana khi nghỉ 1 ngày',
  MANA_ENOUGH_THRESHOLD:       'Ngưỡng Mana được coi là "đủ sức"',
  STAMINA_COST_DEFAULT:        'Chi phí thể lực mặc định / bài tập',
  // Giáo án
  FREE_PLAN_LIMIT_PER_MONTH:   'Số giáo án miễn phí / tháng',
  PROGRESS_TOLERANCE_PERCENT:  'Dung sai % tiến độ giáo án',
  MIN_DURATION_WEEKS:          'Số tuần tối thiểu / giáo án',
  MAX_DURATION_WEEKS:          'Số tuần tối đa / giáo án',
  ACHIEVEMENT_THRESHOLD:       'Ngưỡng % coi là đã đạt mục tiêu',
  REST_MULTIPLIER_MUSCLE_GAIN: 'Hệ số thời gian nghỉ (Tăng cơ)',
  REST_MULTIPLIER_WEIGHT_LOSS: 'Hệ số thời gian nghỉ (Giảm cân)',
  EXERCISE_DURATION_BEGINNER:  'Hệ số thời lượng bài tập (Mới bắt đầu)',
  EXERCISE_DURATION_ADVANCED:  'Hệ số thời lượng bài tập (Nâng cao)',
  LOW_COMPLETION_THRESHOLD:    'Ngưỡng hoàn thành thấp (%)',
  LOW_COMPLETION_ACTION:       'Cách xử lý khi hoàn thành thấp',
  LOW_COMPLETION_SETS_REDUCTION: 'Số sets cần giảm',
  LOW_COMPLETION_REPS_REDUCTION: 'Số reps cần giảm',
}
function vnLabel(key) {
  return VN_LABELS[key] || key
}
function configPrecision(key) {
  return key.startsWith('LOW_COMPLETION_') ? 0 : 4
}
function configStep(key) {
  return key === 'LOW_COMPLETION_THRESHOLD' ? 5 : key.startsWith('LOW_COMPLETION_') ? 1 : 0.05
}
function configMin(key) {
  if (key === 'LOW_COMPLETION_THRESHOLD') return 1
  if (key.startsWith('LOW_COMPLETION_')) return 0
  return undefined
}
function configMax(key) {
  if (key === 'LOW_COMPLETION_THRESHOLD') return 100
  return undefined
}

const categories = computed(() => [...new Set(configs.value.map(c => c.category))])
const grouped = computed(() => {
  const map = {}
  for (const c of configs.value) {
    if (!map[c.category]) map[c.category] = []
    map[c.category].push(c)
  }
  return map
})

async function load() {
  loading.value = true
  try {
    const r = await systemConfigAPI.getAll()
    configs.value = r.data || []
  } finally { loading.value = false }
}

async function save(row) {
  if (row.configValue === null || row.configValue === undefined) {
    ElMessage.warning('Giá trị không được để trống'); return
  }
  await systemConfigAPI.update(row.configKey, { value: row.configValue })
  ElMessage.success(`Đã cập nhật "${row.configKey}" — áp dụng ngay cho các giáo án tạo mới sau đó.`)
}

onMounted(load)
</script>

<style scoped>
.config-description { max-width:560px; margin-top:7px; color:var(--c-text2); font-size:.78rem; line-height:1.5; }
.config-name{display:flex;align-items:center;gap:7px;font-weight:600}.help-icon{color:var(--c-accent);cursor:help;font-size:16px}
</style>
