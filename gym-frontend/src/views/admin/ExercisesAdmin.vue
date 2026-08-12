<template>
  <div class="fade-in">
    <div class="page-header">
      <h2>QUẢN LÝ BÀI TẬP</h2>
      <div style="display:flex;gap:10px">
        <el-button @click="openInjuryManager">QUẢN LÝ NHÓM CƠ ẢNH HƯỞNG</el-button>
        <el-button type="primary" @click="openAdd">+ THÊM BÀI TẬP</el-button>
      </div>
    </div>

    <el-card>
      <div class="exercise-filters"><el-input v-model="keyword" placeholder="Tìm tên bài tập..." clearable/><el-select v-model="muscleFilter" placeholder="Nhóm cơ" clearable><el-option v-for="m in muscles" :key="m" :value="m"/></el-select><el-select v-model="activeFilter" placeholder="Trạng thái" clearable><el-option label="Đang hiển thị" :value="true"/><el-option label="Đã ẩn" :value="false"/></el-select></div>
      <el-table :data="pagedExercises" v-loading="loading" stripe>
        <el-table-column label="STT" width="60" align="center"><template #default="{ $index }">{{ (page - 1) * pageSize + $index + 1 }}</template></el-table-column>
        <el-table-column label="Tên"  prop="name" min-width="150"/>
        <el-table-column label="Nhóm cơ" prop="muscleGroup" width="110"/>
        <el-table-column label="Nhóm cơ bị ảnh hưởng" min-width="220">
          <template #default="{row}">{{ affectedLabels(row) }}</template>
        </el-table-column>
        <el-table-column label="Độ khó" width="110" align="center">
          <template #default="{row}">
            <span class="badge" :class="diffBadge(row.difficulty)">{{ diffLabel(row.difficulty) }}</span>
          </template>
        </el-table-column>
        <!-- Score columns -->
        <el-table-column label=" Tăng cơ" width="85" align="center">
          <template #default="{row}">
            <span :style="{color: scoreColor(row.muscleGainScore), fontWeight:700}">{{ row.muscleGainScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label=" Giảm cân" width="90" align="center">
          <template #default="{row}">
            <span :style="{color: scoreColor(row.weightLossScore), fontWeight:700}">{{ row.weightLossScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label=" Sức bền" width="85" align="center">
          <template #default="{row}">
            <span :style="{color: scoreColor(row.enduranceScore), fontWeight:700}">{{ row.enduranceScore }}</span>
          </template>
        </el-table-column>
        <!-- MỚI: cột thể lực tiêu hao -->
        <el-table-column label=" Thể lực" width="85" align="center">
          <template #default="{row}">
            <span style="color:#f59e0b;font-weight:700">{{ row.staminaCost ?? 10 }}</span>
          </template>
        </el-table-column>
        <el-table-column label=" Dùng tạ" width="90" align="center">
  <template #default="{row}">
    <span class="badge" :class="row.usesWeight ? 'badge-success' : 'badge-info'">
      {{ row.usesWeight ? 'Có' : 'Không' }}
    </span>
  </template>
    </el-table-column>
        <el-table-column label="Trạng thái" width="95" align="center">
          <template #default="{row}">
            <span class="badge" :class="row.isActive?'badge-success':'badge-danger'">{{ row.isActive?'Hiển thị':'Ẩn' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Thao tác" width="130" align="center" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">Sửa</el-button>
            <el-button v-if="row.isActive" size="small" type="danger" plain @click="remove(row.id)">Ẩn</el-button>
            <el-button v-else size="small" type="success" plain @click="restore(row.id)">Khôi phục</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-if="filteredExercises.length" v-model:current-page="page" v-model:page-size="pageSize" :total="filteredExercises.length" :page-sizes="[5,10,20]" layout="total, sizes, prev, pager, next" style="margin-top:18px;justify-content:flex-end"/>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="formDialog" :title="editId?'SỬA BÀI TẬP':'THÊM BÀI TẬP'" width="580px" align-center append-to-body>
      <el-form :model="form" label-position="top">
        <!-- Thông tin cơ bản -->
        <el-form-item label="Tên bài tập">
          <el-input v-model="form.name" placeholder="VD: Bench Press"/>
        </el-form-item>
        <el-form-item label="Động tác">
          <el-input v-model="form.description" type="textarea" :rows="2"/>
        </el-form-item>
        <div class="grid-2">
          <el-form-item label="Nhóm cơ">
            <el-select v-model="form.muscleGroup" style="width:100%">
              <el-option v-for="m in muscles" :key="m" :label="m" :value="m"/>
            </el-select>
          </el-form-item>
          <el-form-item label="Độ khó">
            <el-select v-model="form.difficulty" style="width:100%">
              <el-option label=" Dễ" value="EASY"/>
              <el-option label=" Trung bình" value="MEDIUM"/>
              <el-option label=" Khó" value="HARD"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="Nhóm cơ bị ảnh hưởng">
          <el-select v-model="form.affectedAreas" multiple clearable filterable allow-create default-first-option
                     style="width:100%" placeholder="Chọn hoặc nhập nhóm cơ khác rồi nhấn Enter"
                     @change="handleAffectedAreasChange">
            <el-option v-for="name in customAffectedGroups" :key="`CUSTOM:${name}`" :label="name" :value="`AFFECTED_CUSTOM:${name}`"/>
          </el-select>
          <div class="field-note">Danh sách gồm toàn bộ cơ và vùng khớp bị tác động. Có thể nhập thêm giá trị khác rồi nhấn Enter.</div>
        </el-form-item>
        <div class="grid-3">
          <el-form-item label="Số sets">
            <el-input-number v-model="form.defaultSets" :min="1" :max="20" style="width:100%"/>
          </el-form-item>
          <el-form-item label="Số reps">
            <el-input-number v-model="form.defaultReps" :min="0" :max="100" style="width:100%"/>
          </el-form-item>
          <el-form-item label="Kcal/set">
            <el-input-number v-model="form.caloriesBurned" :min="0" :max="200" style="width:100%"/>
          </el-form-item>
        </div>
        <el-form-item label="Link video YouTube">
          <el-input v-model="form.videoUrl" placeholder="https://youtube.com/watch?v=..."/>
        </el-form-item>

        <!-- MỚI: Thể lực (mana) tiêu hao khi tập bài này -->
        <div class="stamina-box">
          <div style="font-weight:700;color:#b45309;margin-bottom:4px"> THỂ LỰC TIÊU HAO (0–200)</div>
          <div style="font-size:0.78rem;color:var(--c-text3);margin-bottom:10px">
            Số thể lực (mana) sẽ bị trừ khi người dùng tập bài này với tỉ lệ hoàn thành 100%.
            Nếu hoàn thành thấp hơn, thể lực trừ sẽ tính theo tỉ lệ tương ứng.
          </div>
          <div style="display:flex;align-items:center;gap:12px">
            <el-slider v-model="form.staminaCost" :min="0" :max="200" :step="5" style="flex:1"/>
            <span style="font-weight:700;width:50px;text-align:right;color:#f59e0b">{{ form.staminaCost }}</span>
          </div>
        </div>
        <!-- MỚI: Bài tập có dùng tạ/thiết bị tạ hay không -->
<el-form-item label="Loại bài tập">
  <div style="display:flex;align-items:center;gap:10px">
    <el-switch v-model="form.usesWeight" active-text="Dùng tạ " inactive-text="Không dùng tạ (bodyweight)"/>
  </div>
</el-form-item>

        <!-- Điểm hiệu quả theo mục tiêu -->
        <div class="score-section">
          <div class="score-section-title"> ĐIỂM HIỆU QUẢ THEO MỤC TIÊU (0–10)</div>
          <div style="font-size:0.78rem;color:var(--c-text3);margin-bottom:14px">
            Điểm càng cao → bài tập càng được ưu tiên khi tạo giáo án với mục tiêu đó
          </div>
          <div class="score-inputs">
            <div class="score-input-item">
              <div class="score-input-label"> Tăng cơ</div>
              <el-slider v-model="form.muscleGainScore" :min="0" :max="10" :step="1" show-stops/>
              <div class="score-input-val" :style="{color:scoreColor(form.muscleGainScore)}">{{ form.muscleGainScore }}/10</div>
            </div>
            <div class="score-input-item">
              <div class="score-input-label"> Giảm cân</div>
              <el-slider v-model="form.weightLossScore" :min="0" :max="10" :step="1" show-stops/>
              <div class="score-input-val" :style="{color:scoreColor(form.weightLossScore)}">{{ form.weightLossScore }}/10</div>
            </div>
            <div class="score-input-item">
              <div class="score-input-label"> Sức bền</div>
              <el-slider v-model="form.flexibilityScore" :min="0" :max="10" :step="1" show-stops/>
              <div class="score-input-val" :style="{color:scoreColor(form.flexibilityScore)}">{{ form.flexibilityScore }}/10</div>
            </div>
            <div class="score-input-item">
              <div class="score-input-label"> Duy trì</div>
              <el-slider v-model="form.maintenanceScore" :min="0" :max="10" :step="1" show-stops/>
              <div class="score-input-val" :style="{color:scoreColor(form.maintenanceScore)}">{{ form.maintenanceScore }}/10</div>
            </div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="formDialog=false">Hủy</el-button>
        <el-button type="primary" @click="submit">{{ editId?'CẬP NHẬT':'THÊM MỚI' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="injuryDialog" title="QUẢN LÝ NHÓM CƠ ẢNH HƯỞNG" width="560px" append-to-body>
      <div class="injury-add-row">
        <el-input v-model="newInjuryLabel" placeholder="Nhập tên nhóm cơ hoặc vùng bị ảnh hưởng" @keyup.enter="addInjury"/>
        <el-button type="primary" :loading="injurySaving" @click="addInjury">Thêm</el-button>
      </div>
      <el-table :data="injuryCatalog" v-loading="injuryLoading" stripe>
        <el-table-column type="index" label="STT" width="65"/>
        <el-table-column label="Tên hiển thị" min-width="260">
          <template #default="{row}">
            <el-input v-if="editingInjuryId===row.id" v-model="editingInjuryLabel" @keyup.enter="saveInjury(row)"/>
            <span v-else>{{ row.label }}</span>
          </template>
        </el-table-column>
        <el-table-column label="Thao tác" width="180" align="right">
          <template #default="{row}">
            <template v-if="editingInjuryId===row.id">
              <el-button size="small" type="primary" @click="saveInjury(row)">Lưu</el-button>
              <el-button size="small" @click="editingInjuryId=null">Hủy</el-button>
            </template>
            <template v-else>
              <el-button size="small" @click="startEditInjury(row)">Sửa</el-button>
              <el-button size="small" type="danger" plain @click="deleteInjury(row)">Xóa</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { exerciseAPI, injuryAreaAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const exercises  = ref([])
const loading    = ref(true)
const formDialog = ref(false)
const injuryDialog = ref(false), injuryLoading = ref(false), injurySaving = ref(false)
const injuryCatalog = ref([]), newInjuryLabel = ref(''), editingInjuryId = ref(null), editingInjuryLabel = ref('')
const editId     = ref(null)
const muscles    = ['CHEST','BACK','SHOULDERS','ARMS','LEGS','CORE','CARDIO','FULL_BODY']
const injuryOptions = [
  { value:'KNEE', label:'Đầu gối' }, { value:'LOWER_BACK', label:'Lưng dưới' },
  { value:'SHOULDER', label:'Vai' }, { value:'WRIST', label:'Cổ tay' },
  { value:'ELBOW', label:'Khuỷu tay' }, { value:'ANKLE', label:'Cổ chân' },
  { value:'NECK', label:'Cổ' }
]
const customAffectedGroups = computed(() => {
  const builtIn = new Set(muscles.map(m => m.toUpperCase()))
  const unique = new Map()
  exercises.value.forEach(ex => {
    csvArray(ex.secondaryMuscleGroups).forEach(name => {
      const key = name.toLocaleLowerCase('vi-VN')
      if (!builtIn.has(name.toUpperCase()) && !unique.has(key)) unique.set(key, name)
    })
  })
  return [...unique.values()].sort((a, b) => a.localeCompare(b, 'vi'))
})
const keyword = ref(''), muscleFilter = ref(''), activeFilter = ref(null), page = ref(1), pageSize = ref(10)
const filteredExercises = computed(() => exercises.value.filter(e => (!keyword.value || e.name?.toLowerCase().includes(keyword.value.toLowerCase())) && (!muscleFilter.value || e.muscleGroup === muscleFilter.value) && (activeFilter.value === null || e.isActive === activeFilter.value)))
const pagedExercises = computed(() => filteredExercises.value.slice((page.value-1)*pageSize.value,page.value*pageSize.value))

const defaultForm = () => ({
  name:'', description:'', muscleGroup:'CHEST', difficulty:'MEDIUM',
  affectedAreas:[],
  defaultSets:3, defaultReps:10, caloriesBurned:8, videoUrl:'', restSeconds:60,
  muscleGainScore:5, weightLossScore:5, enduranceScore:5, flexibilityScore:5, maintenanceScore:5,
  staminaCost: 10,
  usesWeight: false
})
const form = reactive(defaultForm())

async function load() {
  loading.value = true
  try { const r = await exerciseAPI.getAll({ includeInactive:true }); exercises.value = r.data || [] }
  finally { loading.value = false }
}

function openAdd() {
  editId.value = null
  Object.assign(form, defaultForm())
  formDialog.value = true
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(form, {
    name: row.name, description: row.description||'',
    muscleGroup: row.muscleGroup, difficulty: row.difficulty,
    affectedAreas: toAffectedAreas(row),
    defaultSets: row.defaultSets||3, defaultReps: row.defaultReps||0,
    caloriesBurned: row.caloriesBurned||0, videoUrl: row.videoUrl||'',
    restSeconds: row.restSeconds||60,
    muscleGainScore:  row.muscleGainScore  ?? 5,
    weightLossScore:  row.weightLossScore  ?? 5,
    enduranceScore:   row.enduranceScore   ?? 5,
    flexibilityScore: row.flexibilityScore ?? 5,
    maintenanceScore: row.maintenanceScore ?? 5,
    staminaCost: row.staminaCost ?? 10,
    usesWeight: row.usesWeight ?? false,
  })
  formDialog.value = true
}

async function submit() {
  if (!form.name) { ElMessage.warning('Nhập tên bài tập'); return }
  const payload = {
    ...form,
    secondaryMuscleGroups: form.affectedAreas
      .filter(v => !v.startsWith('INJURY:'))
      .map(v => v === 'AFFECTED:SHOULDER' ? 'SHOULDERS'
        : (v.startsWith('AFFECTED_CUSTOM:') ? v.slice(16) : (v.startsWith('MUSCLE:') ? v.slice(7) : v.trim())))
      .filter(Boolean).join(','),
    contraindicatedInjuries: form.affectedAreas
      .filter(v => v.startsWith('INJURY:') || v === 'AFFECTED:SHOULDER' || v.startsWith('AFFECTED_CUSTOM:'))
      .map(v => v === 'AFFECTED:SHOULDER' ? 'SHOULDER'
        : (v.startsWith('AFFECTED_CUSTOM:') ? v.slice(16) : v.slice(7)))
      .concat(form.affectedAreas.filter(v => !v.includes(':')).map(v => v.trim()).filter(Boolean))
      .join(',')
  }
  delete payload.affectedAreas
  if (editId.value) { await exerciseAPI.update(editId.value, payload); ElMessage.success('Đã cập nhật!') }
  else              { await exerciseAPI.create(payload); ElMessage.success('Đã thêm bài tập!') }
  formDialog.value = false; load()
}

async function remove(id) {
  await ElMessageBox.confirm('Ẩn bài tập này?', 'Xác nhận', { type:'warning' })
  await exerciseAPI.delete(id); ElMessage.success('Đã ẩn bài tập'); load()
}
async function restore(id) { await exerciseAPI.restore(id); ElMessage.success('Đã khôi phục bài tập'); load() }

async function loadInjuries() {
  injuryLoading.value = true
  try { injuryCatalog.value = (await injuryAreaAPI.getAll()).data || [] }
  finally { injuryLoading.value = false }
}
function openInjuryManager() { injuryDialog.value = true; editingInjuryId.value = null; loadInjuries() }
async function addInjury() {
  if (!newInjuryLabel.value.trim()) { ElMessage.warning('Nhập tên chấn thương'); return }
  injurySaving.value = true
  try { await injuryAreaAPI.create(newInjuryLabel.value.trim()); newInjuryLabel.value=''; ElMessage.success('Đã thêm'); await loadInjuries() }
  finally { injurySaving.value = false }
}
function startEditInjury(row) { editingInjuryId.value=row.id; editingInjuryLabel.value=row.label }
async function saveInjury(row) {
  if (!editingInjuryLabel.value.trim()) { ElMessage.warning('Tên không được để trống'); return }
  await injuryAreaAPI.update(row.id, editingInjuryLabel.value.trim()); editingInjuryId.value=null; ElMessage.success('Đã cập nhật'); await loadInjuries()
}
async function deleteInjury(row) {
  await ElMessageBox.confirm(`Xóa “${row.label}” khỏi danh sách chấn thương?`, 'Xác nhận', { type:'warning' })
  await injuryAreaAPI.delete(row.id); ElMessage.success('Đã xóa'); await loadInjuries()
}

function affectedAreaName(value) {
  return String(value || '').replace(/^(MUSCLE:|INJURY:|AFFECTED_CUSTOM:)/, '').replace(/^AFFECTED:SHOULDER$/, 'Vai').trim()
}
function handleAffectedAreasChange(values) {
  const historyByName = new Map(customAffectedGroups.value.map(name => [name.toLocaleLowerCase('vi-VN'), name]))
  const seen = new Set()
  const cleaned = []
  let duplicateName = ''

  values.forEach(value => {
    let normalizedValue = value
    const name = affectedAreaName(value)
    const key = name.toLocaleLowerCase('vi-VN')

    if (!String(value).includes(':') && historyByName.has(key)) {
      duplicateName = historyByName.get(key)
      normalizedValue = `AFFECTED_CUSTOM:${duplicateName}`
    }
    if (seen.has(key)) {
      duplicateName = duplicateName || name
      return
    }
    seen.add(key)
    cleaned.push(normalizedValue)
  })

  form.affectedAreas = cleaned
  if (duplicateName) ElMessage.warning(`Nhóm cơ ảnh hưởng “${duplicateName}” đã tồn tại`)
}

function scoreColor(v) {
  if (!v && v !== 0) return 'var(--c-text3)'
  if (v >= 8) return 'var(--c-success)'
  if (v >= 5) return 'var(--c-warning)'
  return 'var(--c-danger)'
}
function diffLabel(d) { return { EASY:'Dễ',MEDIUM:'Trung bình',HARD:'Khó' }[d]||d }
function diffBadge(d) { return { EASY:'badge-success',MEDIUM:'badge-warning',HARD:'badge-danger' }[d]||'' }
const csvArray = value => !value ? [] : String(value).split(',').map(v => v.trim()).filter(Boolean)
const muscleLabel = value => ({ CHEST:'Ngực', BACK:'Lưng', SHOULDERS:'Vai', ARMS:'Tay', LEGS:'Chân', CORE:'Cơ lõi', CARDIO:'Tim mạch', FULL_BODY:'Toàn thân' }[value] || value)
const toAffectedAreas = row => {
  const musclesInvolved = csvArray(row.secondaryMuscleGroups)
  const injuries = csvArray(row.contraindicatedInjuries)
  const shoulder = musclesInvolved.includes('SHOULDERS') && injuries.includes('SHOULDER')
  const standardMuscles = new Set(['CHEST','BACK','SHOULDERS','ARMS','LEGS','CORE','CARDIO','FULL_BODY'])
  const sharedCustom = musclesInvolved.filter(v => !standardMuscles.has(v.toUpperCase()) && injuries.includes(v))
  return [
    ...(shoulder ? ['AFFECTED:SHOULDER'] : []),
    ...sharedCustom.map(v => `AFFECTED_CUSTOM:${v}`),
    ...musclesInvolved.filter(v => (!shoulder || v !== 'SHOULDERS') && !sharedCustom.includes(v)).map(v => `MUSCLE:${v}`),
    ...injuries.filter(v => (!shoulder || v !== 'SHOULDER') && !sharedCustom.includes(v)).map(v => `INJURY:${v}`)
  ]
}
const affectedLabels = row => [
  ...csvArray(row.secondaryMuscleGroups).map(muscleLabel),
  ...csvArray(row.contraindicatedInjuries).map(v => injuryOptions.find(i => i.value === v)?.label || v)
].filter((value, index, all) => all.findIndex(v => v.toLocaleLowerCase('vi-VN') === value.toLocaleLowerCase('vi-VN')) === index).join(', ') || '--'

onMounted(load)
</script>

<style scoped>
.score-section {
  background:var(--c-card2); border:1px solid var(--c-border2);
  border-radius:var(--radius-lg); padding:16px; margin-top:8px;
}
.score-section-title {
  font-family:var(--font-display); font-size:0.9rem; letter-spacing:0.06em;
  color:var(--c-accent); margin-bottom:6px;
}
.score-inputs { display:flex; flex-direction:column; gap:12px; }
.score-input-item { display:flex; align-items:center; gap:12px; }
.score-input-label { width:90px; font-size:0.82rem; font-weight:600; color:var(--c-text2); flex-shrink:0; }
.score-input-val   { width:38px; font-family:var(--font-mono); font-size:0.8rem; font-weight:700; text-align:right; flex-shrink:0; }
.el-slider { flex:1; }

/* MỚI */
.stamina-box {
  background:#fffbeb; border:1px solid #fde68a; border-radius:var(--radius-lg);
  padding:14px; margin-top:14px;
}
.exercise-filters{display:grid;grid-template-columns:2fr 1fr 1fr;gap:12px;margin-bottom:16px}@media(max-width:700px){.exercise-filters{grid-template-columns:1fr}}
.field-note{width:100%;margin-top:6px;color:var(--c-text3);font-size:.75rem}
.injury-add-row{display:flex;gap:10px;margin-bottom:16px}
</style>
