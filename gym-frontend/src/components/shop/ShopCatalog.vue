<template>
 <div class="catalog">
  <div class="filters">
   <el-input v-model="filters.keyword" clearable placeholder="Tìm tên sản phẩm" @keyup.enter="search" />
   <el-select v-model="filters.category" clearable placeholder="Danh mục" @change="search"><el-option v-for="c in categories" :key="c[0]" :value="c[0]" :label="c[1]" /></el-select>
   <el-input-number v-model="filters.minPrice" :min="0" :step="50000" placeholder="Giá từ" aria-label="Giá từ" />
   <el-input-number v-model="filters.maxPrice" :min="0" :step="50000" placeholder="Giá đến" aria-label="Giá đến" />
   <el-select v-model="filters.sort" @change="search"><el-option label="Mới nhất" value="newest"/><el-option label="Giá tăng dần" value="priceAsc"/><el-option label="Giá giảm dần" value="priceDesc"/><el-option label="Đánh giá cao" value="rating"/></el-select>
   <el-button type="primary" @click="search">Lọc</el-button><el-button @click="reset">Đặt lại</el-button>
  </div>
  <el-button @click="compareOpen=true">So sánh ({{compare.length}}/3)</el-button><el-dialog v-model="compareOpen" title="So sánh sản phẩm" width="min(760px,95vw)"><el-empty v-if="!compare.length" description="Chọn tối đa 3 sản phẩm"/><el-table v-else :data="compareRows"><el-table-column prop="label" label="Thông tin"/><el-table-column v-for="p in compare" :key="p.id" :prop="String(p.id)" :label="p.name"/></el-table><el-button @click="compare=[];saveCompare()">Bỏ chọn tất cả</el-button></el-dialog><el-tabs v-model="mode"><el-tab-pane :label="'Sản phẩm ('+total+')'" name="all"/><el-tab-pane :label="'Yêu thích ('+wishlist.length+')'" name="wishlist"/><el-tab-pane label="Đã xem gần đây" name="recent"/></el-tabs>
  <el-alert v-if="error" :title="error" type="error" :closable="false"/><el-empty v-if="!loading&&!shown.length" description="Chưa có sản phẩm"/>
  <div v-loading="loading" class="grid">
   <el-card v-for="p in shown" :key="p.id" class="product">
    <router-link :to="'/shop/product/'+p.id"><img :src="p.imageUrl||p.images?.[0]||fallback" :alt="p.name"/><h3>{{p.name}}</h3></router-link>
    <el-tag v-if="p.recommended" type="success" size="small">Phù hợp mục tiêu của bạn</el-tag><small>{{p.brand}} · {{p.stock>0?'Còn '+p.stock:'Hết hàng'}}</small>
    <p>{{p.hasVariants?'Từ ':''}}{{money(p.displayPrice??p.salePrice??p.price)}}</p>
    <small v-if="p.reviewCount">★ {{p.averageRating}} · {{p.reviewCount}} đánh giá</small>
    <div class="actions"><el-button type="primary" :disabled="p.stock<=0" @click="$emit('add',p)">{{p.hasVariants?'Chọn phân loại':'Thêm giỏ'}}</el-button><el-button :type="wishlist.some(x=>x.id===p.id)?'danger':'default'" aria-label="Yêu thích" @click="toggle(p)">♥</el-button><el-checkbox :model-value="compare.some(x=>x.id===p.id)" @change="toggleCompare(p)">So sánh</el-checkbox></div>
   </el-card>
  </div>
  <el-pagination v-if="mode==='all'" v-model:current-page="page" :page-size="12" :total="total" layout="prev, pager, next, total" @current-change="load"/>
 </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { shopAPI } from '@/api'
import { useShopCustomer } from '@/composables/useShopCustomer'
defineEmits(['add'])
let saved=[];try{saved=JSON.parse(localStorage.getItem('guest_compare')||'[]')}catch{}
const compare=ref(saved.slice(0,3)),compareOpen=ref(false),compareRows=computed(()=>[['brand','Thương hiệu'],['displayPrice','Giá từ'],['stock','Tồn kho'],['averageRating','Đánh giá']].map(([key,label])=>({label,...Object.fromEntries(compare.value.map(p=>[p.id,key==='displayPrice'?money(p[key]??p.salePrice??p.price):p[key]??'—']))})))
function saveCompare(){localStorage.setItem('guest_compare',JSON.stringify(compare.value))}
function toggleCompare(p){if(compare.value.some(x=>x.id===p.id))compare.value=compare.value.filter(x=>x.id!==p.id);else if(compare.value.length<3)compare.value.push(p);else{ElMessage.warning('Chọn tối đa 3 sản phẩm');return}saveCompare()}
const { wishlist, recent, refresh, toggle } = useShopCustomer()
const defaults = () => ({keyword:'', category:'', minPrice:undefined, maxPrice:undefined, sort:'newest'})
const filters=ref(defaults()), page=ref(1), total=ref(0), items=ref([]), loading=ref(false), mode=ref('all'), error=ref('')
const categories=[['SUPPLEMENT','Thực phẩm bổ sung'],['FOOD','Đồ ăn'],['EQUIPMENT','Dụng cụ']]
const fallback='https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=500'
const money=n=>Number(n||0).toLocaleString('vi-VN')+' đ'
const shown=computed(()=>mode.value==='wishlist'?wishlist.value:mode.value==='recent'?recent.value:items.value)
let request=0
async function load(){const id=++request;loading.value=true;error.value='';try{const r=await shopAPI.catalog({...filters.value,category:filters.value.category||undefined,page:page.value-1,size:12});if(id===request){items.value=r.data.items;total.value=r.data.total}}catch{if(id===request)error.value='Không tải được sản phẩm. Vui lòng thử lại.'}finally{if(id===request)loading.value=false}}
function search(){if(filters.value.maxPrice!=null&&filters.value.minPrice>filters.value.maxPrice){error.value='Giá đến phải lớn hơn hoặc bằng giá từ';return}page.value=1;mode.value='all';load()}
function reset(){filters.value=defaults();search()}
onMounted(()=>{load();refresh()})
</script>
<style scoped>
.filters{display:flex;gap:10px;flex-wrap:wrap;margin:12px 0}.filters>:deep(*),.filters>*{max-width:210px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:18px;margin:20px 0}.product img{width:100%;height:180px;object-fit:cover;border-radius:8px}.product a{color:inherit;text-decoration:none}.product h3{margin:10px 0}.product p{font-weight:700;color:var(--el-color-primary)}.actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:14px}.actions .el-button+.el-button{margin-left:0}.el-pagination{justify-content:center;flex-wrap:wrap}
</style>
