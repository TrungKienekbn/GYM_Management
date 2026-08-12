<template>
  <div class="fade-in">
    <el-alert v-if="isRetuning" title="Cập nhật số liệu để tạo lại giáo án" type="warning" :closable="false" show-icon style="margin-bottom:18px">
      Hãy kiểm tra cân nặng, chiều cao, mục tiêu, trình độ và số ngày có thể tập. Sau khi lưu, chọn “Tạo giáo án phù hợp hơn”.
    </el-alert>
    <div class="page-header">
      <h2>HỒ SƠ CÁ NHÂN</h2>
      <el-button type="primary" @click="save" :loading="saving"> LƯU HỒ SƠ</el-button>
    </div>

    <div class="grid-2" style="gap:24px;align-items:start">
      <!-- Form card -->
      <el-card>
        <template #header>THÔNG TIN THỂ TRẠNG & MỤC TIÊU</template>
        <el-form :model="form" label-position="top">
          <div class="grid-2">
            <el-form-item label="Chiều cao (cm)">
              <el-input-number v-model="form.height" :min="100" :max="250" style="width:100%"/>
            </el-form-item>
            <el-form-item label="Cân nặng (kg)">
              <el-input-number v-model="form.weight" :min="30" :max="250" :precision="1" style="width:100%"/>
            </el-form-item>
          </div>
          <div class="grid-2">
            <el-form-item label="Ngày sinh">
              <el-date-picker
                v-model="form.dateOfBirth"
                type="date"
                placeholder="Chọn ngày sinh"
                format="DD/MM/YYYY"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledBirthDate"
                style="width:100%"
              />
              <div v-if="calculatedAge != null" style="font-size:.75rem;color:var(--c-text3);margin-top:5px">Tuổi hiện tại: {{ calculatedAge }}</div>
            </el-form-item>
            <el-form-item label="Giới tính">
              <el-select v-model="form.gender" style="width:100%">
                <el-option label="Nam" value="male"/>
                <el-option label="Nữ" value="female"/>
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="Mục tiêu tập luyện">
            <el-select v-model="form.goal" style="width:100%">
              <el-option label=" Giảm cân / Đốt mỡ" value="WEIGHT_LOSS"/>
              <el-option label=" Tăng cơ / Tăng sức mạnh" value="MUSCLE_GAIN"/>
              <el-option label=" Tăng sức bền" value="ENDURANCE"/>
              <el-option label=" Duy trì thể hình" value="MAINTENANCE"/>
            </el-select>
          </el-form-item>
          <el-form-item label="Trình độ giáo án">
            <el-select v-model="form.fitnessLevel" style="width:100%">
              <el-option label=" Mới bắt đầu" value="BEGINNER"/>
              <el-option label=" Trung bình" value="INTERMEDIATE"/>
              <el-option label=" Nâng cao" value="ADVANCED"/>
            </el-select>
          </el-form-item>
          <div class="section-title">KINH NGHIỆM VÀ LỊCH TẬP</div>
          <div class="grid-2">
            <el-form-item label="Lần tập luyện gần đây nhất">
              <el-select v-model="form.trainingExperienceMonths" style="width:100%">
                <el-option label="Trong vòng 3 tháng" :value="2"/>
                <el-option label="Từ 3 đến 6 tháng" :value="6"/>
                <el-option label="Từ 6 đến 12 tháng" :value="12"/>
                <el-option label="Trên 1 năm" :value="13"/>
                <el-option label="Chưa từng tập" :value="720"/>
              </el-select>
              <div class="field-help">Nghỉ tập trên 1 năm sẽ hạ một bậc trình độ giáo án để đảm bảo an toàn.</div>
            </el-form-item>
            <el-form-item label="Số ngày rảnh / tuần">
              <el-input-number v-model="form.availableDaysPerWeek" :min="1" :max="7" style="width:100%"/>
            </el-form-item>
          </div>
          <div class="section-title">THIẾT BỊ TẬP LUYỆN</div>
          <el-form-item label="Thiết bị có thể sử dụng">
            <el-checkbox-group v-model="form.availableEquipment" class="choice-wrap">
              <el-checkbox v-for="e in equipmentOptions" :key="e.value" :value="e.value" border>{{ e.label }}</el-checkbox>
            </el-checkbox-group>
            <div class="field-help">Hệ thống sẽ không chọn bài cần thiết bị bạn không có.</div>
          </el-form-item>

          <div class="section-title">AN TOÀN VÀ SỞ THÍCH</div>
          <el-form-item label="Vùng đang đau hoặc từng chấn thương">
            <el-checkbox-group v-model="form.injuryAreas" class="choice-wrap">
              <el-checkbox v-for="i in injuryOptions" :key="i.value" :value="i.value" border>{{ i.label }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="Bài tập không muốn xuất hiện">
            <el-select v-model="form.dislikedExercises" multiple filterable clearable collapse-tags
                       collapse-tags-tooltip placeholder="Tìm và chọn bài tập" style="width:100%">
              <el-option v-for="exercise in exerciseOptions" :key="exercise.id" :label="exercise.name" :value="exercise.name"/>
            </el-select>
            <div class="field-help">Các bài đã chọn sẽ không được đưa vào giáo án của bạn.</div>
          </el-form-item>

        </el-form>
      </el-card>

      <!-- Right column -->
      <div style="display:flex;flex-direction:column;gap:16px">

        <!-- BMI card -->
        <el-card>
          <template #header>CHỈ SỐ CƠ THỂ</template>
          <div class="bmi-display">
            <div class="bmi-circle" :style="{borderColor: bmiColor}">
              <div class="bmi-num">{{ profile?.bmi || '--' }}</div>
              <div class="bmi-lbl">BMI</div>
            </div>
            <div class="bmi-info">
              <div class="bmi-cat" :style="{color: bmiColor}">{{ profile?.bmiCategory || 'Chưa tính' }}</div>
              <div class="bmi-ranges">
                <div :class="{active: profile?.bmiCategory==='Underweight'}">Thiếu cân: &lt; 18.5</div>
                <div :class="{active: profile?.bmiCategory==='Normal'}">Bình thường: 18.5–24.9</div>
                <div :class="{active: profile?.bmiCategory==='Overweight'}">Thừa cân: 25–29.9</div>
                <div :class="{active: profile?.bmiCategory==='Obese'}">Béo phì: ≥ 30</div>
              </div>
            </div>
          </div>

          <el-descriptions :column="1" size="small" border style="margin-top:14px" v-if="profile">
            <el-descriptions-item label="Chiều cao">{{ profile.height || '--' }} cm</el-descriptions-item>
            <el-descriptions-item label="Cân nặng">{{ profile.weight || '--' }} kg</el-descriptions-item>
            <el-descriptions-item label="Mục tiêu">{{ goalLabel(profile.goal) }}</el-descriptions-item>
            <el-descriptions-item label="Trình độ giáo án">{{ levelLabel(profile.fitnessLevel) }}</el-descriptions-item>
            <el-descriptions-item label="Lịch tập">{{ profile.availableDaysPerWeek }} ngày/tuần </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- Tips card -->
        <el-card>
            <el-button type="primary" style="width:100%" @click="$router.push('/app/plan')">
               {{ isRetuning ? 'Tạo giáo án phù hợp hơn' : 'Chuyển đến tạo giáo án' }}
            </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { profileAPI, exerciseAPI, injuryAreaAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const profile = ref(null)
const saving  = ref(false)
const route = useRoute()
const isRetuning = computed(() => route.query.retune === '1')
const calculatedAge = computed(() => {
  if (!form.dateOfBirth) return null
  const birth = new Date(form.dateOfBirth)
  if (Number.isNaN(birth.getTime())) return null
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  if (now < new Date(now.getFullYear(), birth.getMonth(), birth.getDate())) age--
  return age
})

function disabledBirthDate(date) {
  const now = new Date()
  const newest = new Date(now.getFullYear() - 16, now.getMonth(), now.getDate())
  const oldest = new Date(now.getFullYear() - 80, now.getMonth(), now.getDate())
  return date > newest || date < oldest
}

const form    = reactive({
  height: 170,
  weight: 65,
  dateOfBirth: '',
  gender: 'male',
  goal: 'WEIGHT_LOSS',
  fitnessLevel: 'BEGINNER',
  availableDaysPerWeek: 3,
  preferredSessionDuration: 45,
  trainingExperienceMonths: 2,
  trainingLocation: 'GYM',
  availableEquipment: ['DUMBBELL', 'BENCH', 'BARBELL', 'CABLE', 'MACHINE'],
  injuryAreas: [],
  dislikedExercises: []
})

const equipmentOptions = [
  { value:'BODYWEIGHT', label:'Không cần dụng cụ' }, { value:'MAT', label:'Thảm' },
  { value:'DUMBBELL', label:'Tạ đơn' }, { value:'RESISTANCE_BAND', label:'Dây kháng lực' },
  { value:'BENCH', label:'Ghế tập' }, { value:'BARBELL', label:'Thanh đòn' },
  { value:'PULL_UP_BAR', label:'Xà đơn' }, { value:'CABLE', label:'Máy cáp' },
  { value:'MACHINE', label:'Máy tập' }, { value:'CARDIO_MACHINE', label:'Máy cardio' }
]
const injuryOptions = ref([])
const exerciseOptions = ref([])
const csvToArray = (value, numeric = false) => !value ? [] : String(value).split(',').filter(Boolean).map(v => numeric ? Number(v) : v)

const bmiColor = computed(() => {
  const cat = profile.value?.bmiCategory
  if (!cat) return 'var(--c-border)'
  if (cat === 'Normal') return 'var(--c-success)'
  if (cat === 'Underweight') return 'var(--c-info)'
  if (cat === 'Overweight') return 'var(--c-warning)'
  return 'var(--c-danger)'
})



async function load() {
  try {
    const r = await profileAPI.get()
    profile.value = r.data
    // Cập nhật mapping dữ liệu
    Object.assign(form, {
      height: r.data.height,
      weight: r.data.weight,
      dateOfBirth: r.data.dateOfBirth, // Lấy dateOfBirth từ response
      gender: r.data.gender,
      goal: r.data.goal,
      fitnessLevel: r.data.fitnessLevel,
      availableDaysPerWeek: r.data.availableDaysPerWeek,
      preferredSessionDuration: r.data.preferredSessionDuration || 45,
      trainingExperienceMonths: r.data.trainingExperienceMonths ?? 2,
      trainingLocation: r.data.trainingLocation || 'GYM',
      availableEquipment: csvToArray(r.data.availableEquipment),
      injuryAreas: csvToArray(r.data.injuryAreas),
      dislikedExercises: csvToArray(r.data.dislikedExercises)
    })
  } catch {}
}

async function loadCustomInjuryOptions() {
  try {
    const [catalogRes, exerciseRes] = await Promise.all([injuryAreaAPI.getAll(), exerciseAPI.getAll()])
    exerciseOptions.value = (exerciseRes.data || []).filter(ex => ex.isActive !== false)
      .sort((a, b) => a.name.localeCompare(b.name, 'vi'))
    const catalog = (catalogRes.data || []).map(item => ({ value:item.code, label:item.label }))
    const knownMuscles = new Set(['CHEST','BACK','SHOULDERS','ARMS','LEGS','CORE','CARDIO','FULL_BODY'])
    const unique = new Map(catalog.map(item => [item.label.toLocaleLowerCase('vi-VN'), item]))
    ;(exerciseRes.data || []).forEach(ex => {
      ;[...csvToArray(ex.secondaryMuscleGroups), ...csvToArray(ex.contraindicatedInjuries)].forEach(name => {
        const normalized = name.trim()
        const key = normalized.toLocaleLowerCase('vi-VN')
        if (normalized && !knownMuscles.has(normalized.toUpperCase()) && !unique.has(key)) {
          unique.set(key, { value:normalized, label:normalized })
        }
      })
    })
    injuryOptions.value = [...unique.values()].sort((a, b) => a.label.localeCompare(b.label, 'vi'))
  } catch { injuryOptions.value = []; exerciseOptions.value = [] }
}


async function save() {
  if (calculatedAge.value == null || calculatedAge.value < 16 || calculatedAge.value > 80) {
    ElMessage.warning('Độ tuổi sử dụng hệ thống phải từ 16 đến 80')
    return
  }
  if (!Number.isFinite(Number(form.weight)) || form.weight < 30 || form.weight > 250) {
    ElMessage.warning('Cân nặng phải từ 30 đến 250 kg')
    return
  }
  if (!form.availableEquipment.length) {
    ElMessage.warning('Hãy chọn ít nhất một loại thiết bị hoặc “Không cần dụng cụ”')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      availableEquipment: form.availableEquipment.join(','),
      injuryAreas: form.injuryAreas.join(','),
      dislikedExercises: form.dislikedExercises.join(','),
      medicalConditions: null,
      dailyActivityLevel: null
    }
    const r = await profileAPI.save(payload)
    profile.value = r.data
    ElMessage.success('Hồ sơ đã được lưu thành công! ')
  } finally { saving.value = false }
}

function goalLabel(g)  { return { WEIGHT_LOSS:'Giảm cân', MUSCLE_GAIN:'Tăng cơ', ENDURANCE:'Sức bền', FLEXIBILITY:'Linh hoạt', MAINTENANCE:'Duy trì' }[g]||g }
function levelLabel(l) { return { BEGINNER:'Mới bắt đầu', INTERMEDIATE:'Trung bình', ADVANCED:'Nâng cao' }[l]||l }
onMounted(() => Promise.all([load(), loadCustomInjuryOptions()]))
</script>

<style scoped>
.bmi-display { display:flex; gap:20px; align-items:center; }
.bmi-circle {
  width:90px; height:90px; border-radius:50%; border:4px solid var(--c-border);
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  flex-shrink:0; transition:border-color 0.3s;
}
.bmi-num { font-family:var(--font-display); font-size:1.8rem; line-height:1; color:var(--c-text); }
.bmi-lbl { font-size:0.65rem; text-transform:uppercase; letter-spacing:0.1em; color:var(--c-text3); }
.bmi-info { flex:1; }
.bmi-cat  { font-family:var(--font-display); font-size:1.2rem; margin-bottom:6px; }
.bmi-ranges div { font-size:0.75rem; color:var(--c-text3); padding:2px 0; }
.bmi-ranges div.active { color:var(--c-text); font-weight:700; }

.tips-list { display:flex; flex-direction:column; gap:10px; }
.tip-item  { font-size:0.84rem; color:var(--c-text2); line-height:1.5; }
.section-title { margin:18px 0 12px; padding-top:14px; border-top:1px solid var(--c-border2); font-weight:700; color:var(--c-accent); font-size:.82rem; letter-spacing:.04em; }
.choice-wrap { display:flex; flex-wrap:wrap; gap:8px; }
.choice-wrap :deep(.el-checkbox) { margin-right:0; }
.field-help { width:100%; margin-top:6px; color:var(--c-text3); font-size:.74rem; line-height:1.45; }
</style>
