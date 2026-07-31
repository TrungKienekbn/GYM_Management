<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>CÔNG THỨC HỆ THỐNG</h2>
    </div>

    <p style="color:var(--c-text2); margin-bottom:20px">
      Các hệ số/ngưỡng dưới đây được dùng trực tiếp trong công thức tính Fitness Score, Mana và giáo án AI.
      Di chuột vào dấu <b>?</b> để xem giải thích chi tiết trước khi chỉnh sửa.
    </p>

    <div v-for="cat in categories" :key="cat" style="margin-bottom:28px">
      <h3 class="display" style="font-size:1.1rem; margin-bottom:12px; color:var(--c-accent)">{{ cat }}</h3>
      <el-card>
        <el-table :data="grouped[cat]" v-loading="loading">
          <el-table-column label="Tên công thức" min-width="260">
            <template #default="{row}">
              <div style="font-weight:600">
                {{ vnLabel(row.configKey) }}
                <el-tooltip :content="row.description" placement="top-start" raw-content
                            popper-class="config-tooltip">
                  <el-icon style="margin-left:6px; cursor:help; color:var(--c-text3); vertical-align:middle">
                    <QuestionFilled/>
                  </el-icon>
                </el-tooltip>
              </div>
              <div style="font-size:0.72rem; color:var(--c-text3); margin-top:2px">{{ row.configKey }}</div>
            </template>
          </el-table-column>

          <el-table-column label="Giá trị hiện tại" width="220" align="center">
            <template #default="{row}">
              <el-input-number
                v-model="row.configValue"
                :precision="4" :step="0.05"
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
}
function vnLabel(key) {
  return VN_LABELS[key] || key
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

<style>
.config-tooltip { max-width: 340px; line-height: 1.5; }
</style>