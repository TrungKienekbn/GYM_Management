<template>
  <div class="fade-in">
    <el-alert v-if="isRetuning" title="Cập nhật số liệu để tạo lại giáo án" type="warning" :closable="false" show-icon style="margin-bottom:18px">
      Hãy kiểm tra cân nặng, chiều cao, mục tiêu, trình độ và số ngày có thể tập. Sau khi lưu, chọn “Tạo giáo án phù hợp hơn”.
    </el-alert>
    <div class="page-header">
      <h2>HỒ SƠ CÁ NHÂN</h2>
      <el-button v-if="profile && !editing" type="primary" plain @click="startEditing">✏️ CHỈNH SỬA HỒ SƠ</el-button>
      <el-button v-else type="primary" @click="save" :loading="saving">LƯU HỒ SƠ</el-button>
    </div>

    <!-- Khu vực kết quả lớn, ưu tiên trình bày khi demo -->
    <section ref="overviewEl" class="profile-overview">
      <div class="goal-banner">
        <span class="goal-emoji">{{ goalMeta(form.goal).emoji }}</span>
        <div>
          <div class="overview-label">MỤC TIÊU TẬP LUYỆN</div>
          <div class="goal-name">{{ goalMeta(form.goal).label }}</div>
          <div class="goal-description">{{ goalMeta(form.goal).description }}</div>
        </div>
      </div>

      <div class="overview-content">
        <div class="metric-grid">
          <div v-for="item in overviewMetrics" :key="item.label" class="metric-card">
            <span class="metric-icon">{{ item.icon }}</span>
            <div>
              <div class="metric-value">{{ item.value }}<small>{{ item.unit }}</small></div>
              <div class="metric-label">{{ item.label }}</div>
            </div>
          </div>
        </div>

        <div class="bmi-card">
          <div class="bmi-circle" :style="{borderColor: bmiColor}">
            <div class="bmi-num">{{ liveBmi || '--' }}</div>
            <div class="bmi-lbl">BMI</div>
          </div>
          <div class="bmi-info">
            <div class="overview-label">CHỈ SỐ KHỐI CƠ THỂ</div>
            <div class="bmi-cat" :style="{color: bmiColor}">{{ bmiVietnameseLabel }}</div>
            <div class="bmi-note">Chỉ số được cập nhật theo chiều cao và cân nặng trong hồ sơ.</div>
          </div>
        </div>
      </div>

      <div class="bmi-scale-wrap">
        <div class="bmi-scale-title"><strong>Vị trí BMI của bạn</strong><span>{{ liveBmi || '--' }}</span></div>
        <div class="bmi-scale">
          <div class="scale-segment under">Thiếu cân</div>
          <div class="scale-segment normal">Bình thường</div>
          <div class="scale-segment over">Thừa cân</div>
          <div class="scale-segment obese">Béo phì</div>
          <span v-if="liveBmi" class="bmi-pointer" :style="{ left: bmiPointerPosition }">▼</span>
        </div>
      </div>

      <div class="overview-lower">
        <div class="completion-card">
          <div class="completion-head">
            <div><span class="completion-value">{{ completionPercent }}%</span><strong> Hồ sơ hoàn thiện</strong></div>
            <span>{{ missingFields.length ? `Còn thiếu ${missingFields.length} mục` : 'Đã sẵn sàng tạo giáo án' }}</span>
          </div>
          <el-progress :percentage="completionPercent" :stroke-width="12" :show-text="false"/>
          <div v-if="missingFields.length" class="missing-text">Cần bổ sung: {{ missingFields.join(', ') }}.</div>
        </div>

        <div class="profile-tags-card">
          <div class="tag-row"><strong>🧰 Thiết bị</strong><div><el-tag v-for="item in selectedEquipmentLabels" :key="item" type="success">{{ item }}</el-tag><span v-if="!selectedEquipmentLabels.length">Chưa chọn</span></div></div>
          <div class="tag-row"><strong>🛡️ Cần chú ý</strong><div><el-tag v-for="item in selectedInjuryLabels" :key="item" type="danger">{{ item }}</el-tag><el-tag v-if="!selectedInjuryLabels.length" type="success">Không khai báo chấn thương</el-tag></div></div>
          <div class="tag-row"><strong>🚫 Không yêu thích</strong><div><el-tag v-for="item in form.dislikedExercises" :key="item" type="warning">{{ item }}</el-tag><span v-if="!form.dislikedExercises.length">Không có</span></div></div>
        </div>
      </div>

      <div class="personalization-box">
        <div class="personalization-title">✨ GIÁO ÁN SẼ ĐƯỢC CÁ NHÂN HÓA</div>
        <div class="personalization-grid">
          <div v-for="item in personalizationItems" :key="item.text" class="personalization-item"><span>{{ item.icon }}</span>{{ item.text }}</div>
        </div>
      </div>
    </section>

    <div v-if="editing" ref="formHeadingEl" class="form-heading">
      <div>
        <h3>THÔNG TIN HỒ SƠ</h3>
        <p>Nhập hoặc cập nhật thông tin bên dưới</p>
      </div>
    </div>

    <!-- Phần nhập thông tin được đưa xuống dưới -->
    <div v-if="editing" class="profile-form-layout">
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
            <div class="goal-choice-grid">
              <button v-for="goal in goalOptions" :key="goal.value" type="button" class="goal-choice" :class="{active:form.goal === goal.value}" @click="form.goal = goal.value">
                <span>{{ goal.emoji }}</span><strong>{{ goal.shortLabel }}</strong>
              </button>
            </div>
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

      <div class="form-actions">
        <el-card>
          <template #header>HOÀN TẤT HỒ SƠ</template>
          <p class="action-note">Lưu thông tin trước khi chuyển sang tạo giáo án cá nhân.</p>
          <el-button type="primary" style="width:100%;margin-bottom:10px" @click="save" :loading="saving">Lưu hồ sơ</el-button>
            <el-button type="primary" style="width:100%" @click="$router.push('/app/plan')">
               {{ isRetuning ? 'Tạo giáo án phù hợp hơn' : 'Chuyển đến tạo giáo án' }}
            </el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { profileAPI, exerciseAPI, injuryAreaAPI } from '@/api'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'

const profile = ref(null)
const saving  = ref(false)
const editing = ref(false)
const overviewEl = ref(null)
const formHeadingEl = ref(null)
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

async function startEditing() {
  editing.value = true
  await nextTick()
  formHeadingEl.value?.scrollIntoView({ behavior:'smooth', block:'start' })
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

const goalOptions = [
  { value:'WEIGHT_LOSS', emoji:'🔥', shortLabel:'Giảm cân', label:'Giảm cân / Đốt mỡ', description:'Ưu tiên vận động đốt năng lượng và kiểm soát cân nặng.' },
  { value:'MUSCLE_GAIN', emoji:'💪', shortLabel:'Tăng cơ', label:'Tăng cơ / Tăng sức mạnh', description:'Ưu tiên các bài kháng lực để phát triển cơ bắp và sức mạnh.' },
  { value:'ENDURANCE', emoji:'🏃', shortLabel:'Sức bền', label:'Tăng sức bền', description:'Nâng cao thể lực tim mạch và khả năng duy trì vận động.' },
  { value:'MAINTENANCE', emoji:'⚖️', shortLabel:'Duy trì', label:'Duy trì thể hình', description:'Giữ vóc dáng cân đối và nền tảng thể lực ổn định.' }
]
const goalMeta = value => goalOptions.find(goal => goal.value === value) || goalOptions[0]

const liveBmi = computed(() => {
  const height = Number(form.height) / 100
  const weight = Number(form.weight)
  return height > 0 && weight > 0 ? (weight / (height * height)).toFixed(1) : null
})
const liveBmiCategory = computed(() => {
  const bmi = Number(liveBmi.value)
  if (!bmi) return null
  if (bmi < 18.5) return 'Underweight'
  if (bmi < 25) return 'Normal'
  if (bmi < 30) return 'Overweight'
  return 'Obese'
})
const bmiVietnameseLabel = computed(() => ({
  Underweight:'Thiếu cân', Normal:'Bình thường', Overweight:'Thừa cân', Obese:'Béo phì'
}[liveBmiCategory.value] || 'Chưa có dữ liệu'))
const bmiPointerPosition = computed(() => {
  const bmi = Math.min(40, Math.max(14, Number(liveBmi.value) || 14))
  return `${((bmi - 14) / 26) * 100}%`
})
const overviewMetrics = computed(() => [
  { icon:'🎂', value:calculatedAge.value ?? '--', unit:calculatedAge.value != null ? ' tuổi' : '', label:'Tuổi' },
  { icon:'📏', value:form.height || '--', unit:form.height ? ' cm' : '', label:'Chiều cao' },
  { icon:'⚖️', value:form.weight || '--', unit:form.weight ? ' kg' : '', label:'Cân nặng' },
  { icon:'📅', value:form.availableDaysPerWeek || '--', unit:form.availableDaysPerWeek ? ' buổi' : '', label:'Mỗi tuần' }
])

const equipmentOptions = [
  { value:'BODYWEIGHT', label:'Không cần dụng cụ' }, { value:'MAT', label:'Thảm' },
  { value:'DUMBBELL', label:'Tạ đơn' }, { value:'RESISTANCE_BAND', label:'Dây kháng lực' },
  { value:'BENCH', label:'Ghế tập' }, { value:'BARBELL', label:'Thanh đòn' },
  { value:'PULL_UP_BAR', label:'Xà đơn' }, { value:'CABLE', label:'Máy cáp' },
  { value:'MACHINE', label:'Máy tập' }, { value:'CARDIO_MACHINE', label:'Máy cardio' }
]
const selectedEquipmentLabels = computed(() => form.availableEquipment.map(value => equipmentOptions.find(item => item.value === value)?.label || value))
const selectedInjuryLabels = computed(() => form.injuryAreas.map(value => injuryOptions.value.find(item => item.value === value)?.label || value))
const requiredProfileFields = computed(() => [
  ['ngày sinh', form.dateOfBirth], ['chiều cao', form.height], ['cân nặng', form.weight],
  ['mục tiêu', form.goal], ['trình độ', form.fitnessLevel], ['số buổi mỗi tuần', form.availableDaysPerWeek],
  ['thời lượng buổi tập', form.preferredSessionDuration], ['thiết bị', form.availableEquipment.length]
])
const missingFields = computed(() => requiredProfileFields.value.filter(([, value]) => value === null || value === undefined || value === '' || value === 0).map(([label]) => label))
const completionPercent = computed(() => Math.round(((requiredProfileFields.value.length - missingFields.value.length) / requiredProfileFields.value.length) * 100))
const personalizationItems = computed(() => [
  { icon:goalMeta(form.goal).emoji, text:`Ưu tiên bài tập phù hợp mục tiêu ${goalMeta(form.goal).shortLabel.toLowerCase()}.` },
  { icon:'📅', text:`Xây dựng lịch ${form.availableDaysPerWeek || '--'} buổi mỗi tuần, khoảng ${form.preferredSessionDuration || '--'} phút/buổi.` },
  { icon:'🧰', text:`Chỉ chọn bài phù hợp với ${selectedEquipmentLabels.value.length || 0} loại thiết bị đã khai báo.` },
  { icon:'🛡️', text:form.injuryAreas.length ? `Loại bài có thể ảnh hưởng đến: ${selectedInjuryLabels.value.join(', ')}.` : 'Không có vùng chấn thương được khai báo.' },
  { icon:'🚫', text:form.dislikedExercises.length ? `Không sử dụng ${form.dislikedExercises.length} bài tập bạn không thích.` : 'Không có bài tập nào bị loại theo sở thích.' }
])
const injuryOptions = ref([])
const exerciseOptions = ref([])
const csvToArray = (value, numeric = false) => !value ? [] : String(value).split(',').filter(Boolean).map(v => numeric ? Number(v) : v)

const bmiColor = computed(() => {
  const cat = liveBmiCategory.value
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
    editing.value = false
  } catch { editing.value = true }
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
    editing.value = false
    ElMessage.success('Hồ sơ đã được cập nhật. Giáo án tiếp theo sẽ sử dụng thông tin mới!')
    await nextTick()
    overviewEl.value?.scrollIntoView({ behavior:'smooth', block:'start' })
  } finally { saving.value = false }
}

function goalLabel(g)  { return { WEIGHT_LOSS:'Giảm cân', MUSCLE_GAIN:'Tăng cơ', ENDURANCE:'Sức bền', FLEXIBILITY:'Linh hoạt', MAINTENANCE:'Duy trì' }[g]||g }
function levelLabel(l) { return { BEGINNER:'Mới bắt đầu', INTERMEDIATE:'Trung bình', ADVANCED:'Nâng cao' }[l]||l }
onMounted(() => Promise.all([load(), loadCustomInjuryOptions()]))
</script>

<style scoped>
.profile-overview { margin-bottom:32px; border:1px solid var(--c-border2); border-radius:18px; overflow:hidden; background:var(--c-card); box-shadow:var(--shadow); }
.goal-banner { display:flex; align-items:center; gap:22px; padding:24px 30px; background:linear-gradient(135deg, var(--c-accent-soft), var(--c-card)); border-bottom:1px solid var(--c-border2); }
.goal-emoji { display:grid; place-items:center; width:82px; height:82px; flex:0 0 82px; border-radius:20px; background:#fff; font-size:3.2rem; box-shadow:0 8px 24px rgba(249,115,22,.14); }
.overview-label { margin-bottom:5px; color:var(--c-text3); font-size:.78rem; font-weight:800; letter-spacing:.1em; }
.goal-name { color:var(--c-text); font-family:var(--font-display); font-size:2rem; font-weight:800; line-height:1.15; }
.goal-description { margin-top:7px; color:var(--c-text2); font-size:1rem; line-height:1.5; }
.overview-content { display:grid; grid-template-columns:minmax(0, 1.6fr) minmax(300px, .8fr); gap:24px; padding:26px 30px; }
.metric-grid { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:14px; }
.metric-card { display:flex; align-items:center; gap:15px; min-height:96px; padding:17px 19px; border:1px solid var(--c-border2); border-radius:14px; background:var(--c-bg); }
.metric-icon { display:grid; place-items:center; width:50px; height:50px; flex:0 0 50px; border-radius:13px; background:var(--c-card); font-size:1.75rem; }
.metric-value { color:var(--c-text); font-family:var(--font-display); font-size:1.75rem; font-weight:800; line-height:1.05; }
.metric-value small { font-family:var(--font-body); font-size:.85rem; font-weight:700; color:var(--c-text2); }
.metric-label { margin-top:5px; color:var(--c-text3); font-size:.82rem; font-weight:700; }
.bmi-card { display:flex; align-items:center; justify-content:center; gap:22px; padding:22px; border:2px solid var(--c-border2); border-radius:16px; }
.bmi-circle {
  width:112px; height:112px; border-radius:50%; border:6px solid var(--c-border);
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  flex-shrink:0; transition:border-color 0.3s;
}
.bmi-num { font-family:var(--font-display); font-size:2.15rem; font-weight:800; line-height:1; color:var(--c-text); }
.bmi-lbl { margin-top:4px; font-size:0.72rem; font-weight:800; text-transform:uppercase; letter-spacing:0.1em; color:var(--c-text3); }
.bmi-info { flex:1; }
.bmi-cat  { font-family:var(--font-display); font-size:1.5rem; font-weight:800; margin-bottom:6px; }
.bmi-note { color:var(--c-text3); font-size:.8rem; line-height:1.45; }
.bmi-scale-wrap { padding:0 30px 28px; }
.bmi-scale-title { display:flex; justify-content:space-between; margin-bottom:9px; color:var(--c-text2); font-size:.86rem; }
.bmi-scale-title span { color:var(--c-text); font-size:1rem; font-weight:800; }
.bmi-scale { position:relative; display:grid; grid-template-columns:18% 32% 25% 25%; height:30px; border-radius:9px; color:#fff; font-size:.72rem; font-weight:800; line-height:30px; text-align:center; }
.scale-segment:first-child { border-radius:9px 0 0 9px; }
.scale-segment:nth-child(4) { border-radius:0 9px 9px 0; }
.scale-segment.under { background:#3b82f6; }
.scale-segment.normal { background:#22c55e; }
.scale-segment.over { background:#f59e0b; }
.scale-segment.obese { background:#ef4444; }
.bmi-pointer { position:absolute; top:-20px; transform:translateX(-50%); color:var(--c-text); font-size:1.15rem; line-height:1; transition:left .25s ease; }
.overview-lower { display:grid; grid-template-columns:.8fr 1.2fr; gap:20px; padding:26px 30px; border-top:1px solid var(--c-border2); background:var(--c-bg); }
.completion-card, .profile-tags-card { padding:20px; border:1px solid var(--c-border2); border-radius:14px; background:var(--c-card); }
.completion-head { display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:14px; color:var(--c-text2); font-size:.8rem; }
.completion-head > div { color:var(--c-text); }
.completion-value { color:var(--c-accent); font-family:var(--font-display); font-size:1.55rem; font-weight:800; }
.missing-text { margin-top:10px; color:var(--c-warning); font-size:.8rem; line-height:1.4; }
.profile-tags-card { display:flex; flex-direction:column; gap:14px; }
.tag-row { display:grid; grid-template-columns:145px 1fr; gap:12px; align-items:start; color:var(--c-text2); font-size:.82rem; }
.tag-row > div { display:flex; flex-wrap:wrap; gap:6px; }
.personalization-box { padding:24px 30px 28px; border-top:1px solid var(--c-border2); }
.personalization-title { margin-bottom:15px; color:var(--c-accent); font-size:.9rem; font-weight:900; letter-spacing:.06em; }
.personalization-grid { display:grid; grid-template-columns:repeat(2, minmax(0, 1fr)); gap:10px 24px; }
.personalization-item { display:flex; align-items:flex-start; gap:10px; color:var(--c-text2); font-size:.9rem; line-height:1.5; }
.personalization-item span { font-size:1.15rem; }
.form-heading { display:flex; align-items:end; justify-content:space-between; margin-bottom:14px; scroll-margin-top:24px; }
.form-heading h3 { margin:0; color:var(--c-text); font-size:1.35rem; }
.form-heading p { margin:4px 0 0; color:var(--c-text3); font-size:.9rem; }
.profile-form-layout { display:grid; grid-template-columns:minmax(0, 1fr) 300px; gap:22px; align-items:start; }
.form-actions { position:sticky; top:20px; }
.action-note { margin:0 0 16px; color:var(--c-text2); font-size:.9rem; line-height:1.55; }
.form-actions :deep(.el-button + .el-button) { margin-left:0; }
.goal-choice-grid { display:grid; grid-template-columns:repeat(4, minmax(0, 1fr)); gap:10px; width:100%; }
.goal-choice { display:flex; flex-direction:column; align-items:center; gap:7px; min-height:88px; padding:12px 8px; border:2px solid var(--c-border2); border-radius:12px; background:var(--c-card); color:var(--c-text2); cursor:pointer; transition:.2s ease; }
.goal-choice:hover { border-color:var(--c-accent); transform:translateY(-2px); }
.goal-choice.active { border-color:var(--c-accent); background:var(--c-accent-soft); color:var(--c-accent2); box-shadow:0 6px 18px rgba(249,115,22,.12); }
.goal-choice span { font-size:1.8rem; }
.goal-choice strong { font-size:.82rem; }
:deep(.el-form-item__label) { font-size:.95rem; font-weight:700; }
:deep(.el-input__inner), :deep(.el-select__placeholder), :deep(.el-select__selected-item) { font-size:.95rem; }

.tips-list { display:flex; flex-direction:column; gap:10px; }
.tip-item  { font-size:0.84rem; color:var(--c-text2); line-height:1.5; }
.section-title { margin:18px 0 12px; padding-top:14px; border-top:1px solid var(--c-border2); font-weight:700; color:var(--c-accent); font-size:.82rem; letter-spacing:.04em; }
.choice-wrap { display:flex; flex-wrap:wrap; gap:8px; }
.choice-wrap :deep(.el-checkbox) { margin-right:0; }
.field-help { width:100%; margin-top:6px; color:var(--c-text3); font-size:.74rem; line-height:1.45; }

@media (max-width: 1050px) {
  .overview-content, .profile-form-layout, .overview-lower { grid-template-columns:1fr; }
  .form-actions { position:static; }
}
@media (max-width: 650px) {
  .goal-banner, .overview-content { padding:20px; }
  .goal-emoji { width:66px; height:66px; flex-basis:66px; font-size:2.5rem; }
  .goal-name { font-size:1.5rem; }
  .metric-grid { grid-template-columns:1fr; }
  .bmi-card { flex-direction:column; text-align:center; }
  .bmi-scale-wrap, .overview-lower, .personalization-box { padding-left:20px; padding-right:20px; }
  .personalization-grid { grid-template-columns:1fr; }
  .goal-choice-grid { grid-template-columns:repeat(2, minmax(0, 1fr)); }
  .tag-row { grid-template-columns:1fr; }
  .bmi-scale { font-size:.6rem; }
}
</style>
