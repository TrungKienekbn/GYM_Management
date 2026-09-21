<template>
 <el-collapse><el-collapse-item title="Sổ địa chỉ nhận hàng" name="addresses">
 <div v-for="a in addresses" :key="a.id" class="address"><b>{{a.receiverName}} · {{a.phone}}</b><el-tag v-if="a.defaultAddress" size="small">Mặc định</el-tag><p>{{a.address}}</p><el-button size="small" @click="$emit('select',a)">Dùng địa chỉ này</el-button><el-button size="small" @click="form={...a}">Sửa</el-button><el-button size="small" @click="makeDefault(a)" :disabled="a.defaultAddress">Đặt mặc định</el-button><el-button text type="danger" @click="remove(a)">Xóa</el-button></div>
 <el-divider>{{form.id?'Sửa địa chỉ':'Thêm địa chỉ'}}</el-divider>
 <el-input v-model="form.receiverName" placeholder="Người nhận"/><el-input v-model="form.phone" placeholder="Số điện thoại"/><el-input v-model="form.address" type="textarea" placeholder="Địa chỉ đầy đủ"/><el-checkbox v-model="form.defaultAddress">Mặc định</el-checkbox><el-button :loading="saving" @click="save">Lưu địa chỉ</el-button><el-button v-if="form.id" @click="form=blank()">Hủy sửa</el-button>
 </el-collapse-item></el-collapse>
</template>
<script setup>
import {ref,onMounted} from 'vue';import {shopAPI} from '@/api';import {ElMessage,ElMessageBox} from 'element-plus'
const emit=defineEmits(['select']);const addresses=ref([]),saving=ref(false);const blank=()=>({receiverName:'',phone:'',address:'',defaultAddress:false});const form=ref(blank())
async function load(){addresses.value=(await shopAPI.addresses()).data||[]}
async function save(){saving.value=true;try{await shopAPI.saveAddress(form.value);form.value=blank();await load();ElMessage.success('Đã lưu địa chỉ')}finally{saving.value=false}}
async function makeDefault(a){await shopAPI.saveAddress({...a,defaultAddress:true});await load()}
async function remove(a){try{await ElMessageBox.confirm('Xóa địa chỉ này?','Xác nhận')}catch{return}await shopAPI.deleteAddress(a.id);await load()}
onMounted(async()=>{await load();const a=addresses.value.find(x=>x.defaultAddress);if(a)emit('select',a)})
</script><style scoped>.address{padding:12px 0;border-bottom:1px solid var(--el-border-color)}.el-input,.el-textarea{margin:5px 0}</style>
