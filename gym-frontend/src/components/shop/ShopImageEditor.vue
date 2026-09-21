<template><div><div class="images"><div v-for="(url,index) in modelValue" :key="index"><img :src="url" alt="Ảnh sản phẩm"/><el-button text type="danger" @click="remove(index)">Xóa</el-button></div></div><input type="file" accept="image/jpeg,image/png,image/webp" :disabled="uploading||modelValue.length>=8" @change="upload"/><small> Tối đa 8 ảnh, 5 MB/ảnh.</small></div></template>
<script setup>
import { ref } from 'vue';import { shopAPI } from '@/api';import { ElMessage } from 'element-plus'
const props=defineProps({modelValue:{type:Array,default:()=>[]}}),emit=defineEmits(['update:modelValue']),uploading=ref(false)
function remove(i){emit('update:modelValue',props.modelValue.filter((_,j)=>i!==j))}
async function upload(e){const file=e.target.files[0];if(!file)return;if(file.size>5*1024*1024){ElMessage.warning('Ảnh tối đa 5 MB');e.target.value='';return}uploading.value=true;try{const r=await shopAPI.uploadImage(file);emit('update:modelValue',[...props.modelValue,r.data])}finally{uploading.value=false;e.target.value=''}}
</script><style scoped>.images{display:flex;flex-wrap:wrap;gap:8px}.images img{width:80px;height:80px;object-fit:cover}.images>div{display:flex;flex-direction:column}small{display:block;color:#888}</style>
