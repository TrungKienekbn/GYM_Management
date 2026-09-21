import { ref, watch } from 'vue'
import { shopAPI } from '@/api'
import { useAuthStore } from '@/stores/auth'
const read = key => { try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] } }
const wishlist = ref(read('guest_wishlist')), recent = ref(read('guest_recent')), guestCart = ref(read('guest_cart'))
export function useShopCustomer() {
 const auth = useAuthStore()
 watch(() => auth.user?.userId, () => { wishlist.value=[];recent.value=[];guestCart.value=read('guest_cart');refresh() })
 async function refresh() {
  if (auth.isLoggedIn) { const owner=auth.user?.userId;const r = await shopAPI.customerState();if(owner!==auth.user?.userId)return; wishlist.value = r.data.wishlist; recent.value = r.data.recent }
  else { wishlist.value = read('guest_wishlist'); recent.value = read('guest_recent') }
 }
 async function toggle(p) {
  const next = !wishlist.value.some(x => x.id === p.id)
  if (auth.isLoggedIn) { await shopAPI.setCustomerState(p.id, { wishlisted: next }); await refresh() }
  else { wishlist.value = next ? [...wishlist.value, p] : wishlist.value.filter(x => x.id !== p.id); localStorage.setItem('guest_wishlist', JSON.stringify(wishlist.value)) }
 }
 async function viewed(p) {
  if (auth.isLoggedIn) { await shopAPI.setCustomerState(p.id, { viewed: true }); await refresh() }
  else { recent.value = [p, ...recent.value.filter(x => x.id !== p.id)].slice(0, 30); localStorage.setItem('guest_recent', JSON.stringify(recent.value)) }
 }
 function persistCart() { guestCart.value.forEach(i => { i.quantity=Math.min(Math.max(1,Math.floor(Number(i.quantity)||1)),Math.max(1,Number(i.stock)||1)) });localStorage.setItem('guest_cart', JSON.stringify(guestCart.value)) }
 return { wishlist, recent, guestCart, refresh, toggle, viewed, persistCart }
}
