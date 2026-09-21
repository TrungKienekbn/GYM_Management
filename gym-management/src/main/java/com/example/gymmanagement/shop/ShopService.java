package com.example.gymmanagement.shop;

import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.Goal;
import com.example.gymmanagement.repository.*;
import com.example.gymmanagement.service.*;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*; import java.util.stream.*;

@Service @RequiredArgsConstructor @Transactional
public class ShopService {
 private final ProductRepository products; private final CartItemRepository carts; private final StoreOrderRepository orders;
 private final UserRepository users; private final UserProfileRepository profiles; private final MembershipService memberships;
 private final BankQrService bankQr; private final NotificationService notifications; private final VoucherService voucherService;
 private final ShopAuditService audit; private final ShopPaymentService payments; private final ProductReviewService reviewService; private final ProductVariantRepository variants;

 public List<Map<String,Object>> products(String email,String category,String keyword){
  Goal goal=null; if(email!=null){User u=user(email); goal=profiles.findByUserId(u.getId()).map(UserProfile::getGoal).orElse(null);}
  Goal finalGoal=goal;
  return products.findByActiveTrueOrderByCreatedAtDesc().stream()
          .filter(p->category==null||category.isBlank()||p.getCategory().name().equals(category))
          .filter(p->keyword==null||keyword.isBlank()||p.getName().toLowerCase().contains(keyword.toLowerCase()))
          .sorted(Comparator.comparing((Product p)->isRecommended(p,finalGoal)).reversed())
          .map(p->productMap(p,finalGoal)).toList();
 }
 public List<Map<String,Object>> allProducts(){return products.findAll().stream().map(p->productMap(p,null)).toList();}
 @Transactional public Product saveProduct(Long id,Product input){
  Product p=id==null?new Product():products.findLocked(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm"));
  if(input.getName()==null||input.getName().isBlank()||input.getPrice()==null||input.getPrice()<0||input.getStock()==null||input.getStock()<0)throw new RuntimeException("Tên, giá và tồn kho không hợp lệ");
  int before=p.getStock()==null?0:p.getStock();p.setImages(ShopImageListConverter.validate(input.getImages()));p.setName(input.getName().trim());p.setCategory(input.getCategory());p.setDescription(input.getDescription());p.setBrand(input.getBrand());p.setImageUrl(input.getImageUrl());p.setPrice(input.getPrice());p.setSalePrice(input.getSalePrice());p.setStock(input.getStock());p.setSuitableGoals(input.getSuitableGoals());p.setRequiredEquipmentCode(input.getRequiredEquipmentCode());p.setActive(input.getActive()==null?true:input.getActive());products.save(p);audit.stock(p.getId(),null,before,p.getStock(),null,"Cập nhật sản phẩm");return p;
 }
 @Transactional public void hideProduct(Long id){Product p=products.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm"));p.setActive(false);products.save(p);}

 public List<Map<String,Object>> cart(String email){return carts.findByUserId(user(email).getId()).stream().map(this::cartMap).toList();}
 @Transactional public void addCart(String email,Long productId,Long variantId,int qty){
  User u=user(email);Product p=products.findById(productId).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm")); if(!Boolean.TRUE.equals(p.getActive()))throw new RuntimeException("Sản phẩm đã ngừng bán");
  List<ProductVariant> pv=variants.findByProductIdOrderByIdAsc(productId);
  String variantLabel=null; int availableStock;
  if(!pv.isEmpty()){
   if(variantId==null)throw new RuntimeException("Vui lòng chọn phân loại sản phẩm");
   Long selectedVariantId=variantId;
   ProductVariant v=pv.stream().filter(x->x.getId().equals(selectedVariantId)).findFirst().orElseThrow(()->new RuntimeException("Phân loại không hợp lệ"));
   if(!Boolean.TRUE.equals(v.getActive()))throw new RuntimeException("Phân loại này đã ngừng bán");
   availableStock=v.getStock();
   variantLabel=variantLabel(v);
  } else { variantId=null; availableStock=p.getStock(); }
  Long vId=variantId;
  CartItem c=carts.findByUserIdAndProductIdAndVariantId(u.getId(),productId,vId).orElse(CartItem.builder().user(u).product(p).variantId(vId).variantLabel(variantLabel).quantity(0).build());
  int next=c.getQuantity()+Math.max(1,qty);if(next>availableStock)throw new RuntimeException("Sản phẩm chỉ còn "+availableStock+" sản phẩm");c.setQuantity(next);carts.save(c);
 }
 @Transactional public void updateCart(String email,Long id,int qty){CartItem c=ownedCart(email,id);if(qty<=0){carts.delete(c);return;}int stock=c.getVariantId()!=null?variants.findById(c.getVariantId()).map(ProductVariant::getStock).orElse(0):c.getProduct().getStock();if(qty>stock)throw new RuntimeException("Không đủ tồn kho");c.setQuantity(qty);carts.save(c);}
 @Transactional public void removeCart(String email,Long id){carts.delete(ownedCart(email,id));}

 @Transactional public Map<String,Object> checkout(String email,Map<String,Object> req){
  String method=str(req.getOrDefault("paymentMethod","BANK_TRANSFER"));payments.requireAvailable(method);User u=users.findLockedByEmail(email).orElseThrow();List<CartItem> list=carts.findByUserId(u.getId());if(list.isEmpty())throw new RuntimeException("Giỏ hàng đang trống");
  String receiver=str(req.get("receiverName")),phone=str(req.get("phone")),address=str(req.get("shippingAddress"));if(receiver.isBlank()||phone.isBlank()||address.isBlank())throw new RuntimeException("Vui lòng nhập đủ người nhận, số điện thoại và địa chỉ");
  list.sort(Comparator.comparing((CartItem c)->c.getProduct().getId()).thenComparing(c->c.getVariantId()==null?0L:c.getVariantId()));
  Map<Long,Product> lockedProducts=new HashMap<>();Map<Long,ProductVariant> lockedVariants=new HashMap<>();
  for(CartItem c:list){
   if(!Boolean.TRUE.equals(c.getProduct().getActive()))throw new RuntimeException("Sản phẩm đã ngừng bán");
   if(c.getVariantId()!=null){ProductVariant v=variants.findLocked(c.getVariantId()).orElseThrow(()->new RuntimeException("Phân loại sản phẩm không tồn tại"));if(!Boolean.TRUE.equals(v.getActive()))throw new RuntimeException("Phân loại đã ngừng bán");lockedVariants.put(c.getVariantId(),v);if(c.getQuantity()>v.getStock())throw new RuntimeException(c.getProduct().getName()+" ("+c.getVariantLabel()+") không đủ tồn kho");}
   else{Product p=products.findLocked(c.getProduct().getId()).orElseThrow(()->new RuntimeException("Sản phẩm không tồn tại"));lockedProducts.put(p.getId(),p);if(c.getQuantity()>p.getStock())throw new RuntimeException(p.getName()+" không đủ tồn kho");}
  }
  double subtotal=list.stream().mapToDouble(c->unitPriceOf(c)*c.getQuantity()).sum();double discount=memberships.isVip(u)?Math.round(subtotal*.05):0;double shipping=subtotal>=500000||memberships.isVip(u)?0:30000;

  String voucherCode=str(req.get("voucherCode")); Voucher voucher=null; double voucherDiscount=0;
  if(!voucherCode.isBlank()){List<VoucherService.CartLine> lines=list.stream().map(c->new VoucherService.CartLine(c.getProduct().getId(),unitPriceOf(c)*c.getQuantity())).toList();var r=voucherService.validate(voucherCode,lines);voucher=r.getKey();voucherDiscount=r.getValue();}

  StoreOrder o=StoreOrder.builder().user(u).paymentMethod(method).status("COD".equals(method)?OrderStatus.CONFIRMED:OrderStatus.PENDING_PAYMENT).subtotal(subtotal).discount(discount)
          .voucherCode(voucher!=null?voucher.getCode():null).voucherDiscount(voucherDiscount)
          .shippingFee(shipping).total(Math.max(0,subtotal-discount-voucherDiscount)+shipping).receiverName(receiver).phone(phone).shippingAddress(address).note(str(req.get("note"))).expiresAt("COD".equals(method)?null:LocalDateTime.now().plusMinutes(15)).build();orders.save(o);
  for(CartItem c:list){
   double unit=unitPriceOf(c);
   if(c.getVariantId()!=null){ProductVariant v=lockedVariants.get(c.getVariantId());audit.stock(v.getProduct().getId(),v.getId(),v.getStock(),v.getStock()-c.getQuantity(),o.getId(),"Đặt hàng online");v.setStock(v.getStock()-c.getQuantity());variants.save(v);}
   else{Product p=lockedProducts.get(c.getProduct().getId());audit.stock(p.getId(),null,p.getStock(),p.getStock()-c.getQuantity(),o.getId(),"Đặt hàng online");p.setStock(p.getStock()-c.getQuantity());products.save(p);}
   o.getItems().add(OrderItem.builder().order(o).productId(c.getProduct().getId()).productName(c.getProduct().getName()).imageUrl(c.getProduct().getImageUrl()).variantId(c.getVariantId()).variantLabel(c.getVariantLabel()).unitPrice(unit).quantity(c.getQuantity()).lineTotal(unit*c.getQuantity()).build());
  }
  if(voucher!=null)voucherService.markUsed(voucher);
  audit.event(o,null,"Tạo đơn online: "+method);if("BANK_TRANSFER".equals(method)){BankQrService.BankQrResult qr=bankQr.generate("SHOP"+o.getId(),Math.round(o.getTotal()));o.setTransferCode(qr.getTransferCode());o.setQrRawPayload(qr.getQrRawPayload());o.setQrCodeUrl(qr.getQrImageUrl());}orders.save(o);carts.deleteByUserId(u.getId());return orderMap(o);
 }
 public List<Map<String,Object>> myOrders(String email){expire();return orders.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream().map(this::orderMap).toList();}
 public Map<String,Object> order(String email,Long id){expire();StoreOrder o=ownedOrder(email,id);return orderMap(o);}
 public Map<String,Object> lookupOrder(Long id,String phone){StoreOrder o=orders.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy đơn hàng"));if(o.getPhone()==null||phone==null||!o.getPhone().trim().equals(phone.trim()))throw new RuntimeException("Mã đơn hàng hoặc số điện thoại không đúng");return orderMap(o);}
 StoreOrder guestOrder(Long id,String email){StoreOrder o=orders.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy đơn hàng"));if(o.getUser()!=null||o.getReceiverEmail()==null||!o.getReceiverEmail().equalsIgnoreCase(email))throw new RuntimeException("Email không khớp đơn khách vãng lai");return o;}
 @Transactional public Map<String,Object> cancelGuest(Long id,String email){StoreOrder o=guestOrder(id,email);if(o.getStatus()!=OrderStatus.PENDING_PAYMENT&&!("COD".equals(o.getPaymentMethod())&&o.getStatus()==OrderStatus.CONFIRMED))throw new RuntimeException("Đơn đã xử lý, không thể hủy");OrderStatus before=o.getStatus();restore(o);o.setStatus(OrderStatus.CANCELLED);audit.event(o,before,"Khách vãng lai hủy đơn bằng OTP email");return orderMap(orders.save(o));}
 public List<Map<String,Object>> allOrders(){expire();return orders.findAllByOrderByCreatedAtDesc().stream().map(this::orderMap).toList();}
 @Transactional public Map<String,Object> cancel(String email,Long id){StoreOrder o=orders.findLocked(id).orElseThrow();if(o.getUser()==null||!o.getUser().getEmail().equalsIgnoreCase(email))throw new RuntimeException("Không có quyền");if(o.getStatus()!=OrderStatus.PENDING_PAYMENT&&!("COD".equals(o.getPaymentMethod())&&o.getStatus()==OrderStatus.CONFIRMED))throw new RuntimeException("Đơn đã xử lý, không thể hủy");OrderStatus before=o.getStatus();restore(o);o.setStatus(OrderStatus.CANCELLED);audit.event(o,before,"Khách hủy đơn");return orderMap(orders.save(o));}
 @Transactional public Map<String,Object> updateStatus(Long id,OrderStatus next){StoreOrder o=orders.findLocked(id).orElseThrow();Map<OrderStatus,List<OrderStatus>> allowed=Map.of(OrderStatus.CONFIRMED,List.of(OrderStatus.PREPARING),OrderStatus.PAID,List.of(OrderStatus.PREPARING),OrderStatus.PREPARING,List.of(OrderStatus.SHIPPING),OrderStatus.SHIPPING,List.of(OrderStatus.DELIVERED),OrderStatus.DELIVERED,List.of(OrderStatus.COMPLETED));if(!allowed.getOrDefault(o.getStatus(),List.of()).contains(next))throw new RuntimeException("Không thể chuyển trạng thái");OrderStatus before=o.getStatus();o.setStatus(next);if("COD".equals(o.getPaymentMethod())&&next==OrderStatus.DELIVERED)o.setPaidAt(LocalDateTime.now());audit.event(o,before,"Cập nhật trạng thái"+(next==OrderStatus.DELIVERED&&"COD".equals(o.getPaymentMethod())?" / xác nhận đã thu tiền COD":""));if(o.getUser()!=null)notifications.sendToUser(o.getUser().getId(),"Đơn hàng #"+o.getId()+" đã cập nhật",statusLabel(next),"SYSTEM");return orderMap(orders.save(o));}
 @Transactional public boolean handleWebhook(Map<String,Object> payload){String content=str(payload.get("content")).toUpperCase();Object a=payload.get("transferAmount");if(a==null)return false;for(StoreOrder candidate:orders.findByStatusIn(List.of(OrderStatus.PENDING_PAYMENT,OrderStatus.EXPIRED))){StoreOrder o=orders.findLocked(candidate.getId()).orElseThrow();if(o.getStatus()!=OrderStatus.PENDING_PAYMENT&&o.getStatus()!=OrderStatus.EXPIRED)continue;if(o.getTransferCode()!=null&&content.replaceAll("[^A-Z0-9]"," ").contains(o.getTransferCode())){if(Math.round(o.getTotal())!=Long.parseLong(String.valueOf(a)))throw new RuntimeException("Số tiền đơn hàng không khớp");if(o.getStatus()==OrderStatus.EXPIRED)throw new RuntimeException("Đơn hàng đã hết hạn và tồn kho đã được hoàn lại");o.setStatus(OrderStatus.PAID);audit.event(o,OrderStatus.PENDING_PAYMENT,"SePay xác nhận thanh toán");o.setPaidAt(LocalDateTime.now());o.setTransactionId(str(payload.getOrDefault("referenceCode",payload.get("id"))));orders.save(o);notifications.sendToUser(o.getUser().getId(),"Thanh toán đơn hàng thành công","Đơn hàng #"+o.getId()+" đang được chuẩn bị.","SYSTEM");return true;}}return false;}
 @Transactional public void expire(){for(StoreOrder candidate:orders.findByStatusAndExpiresAtBefore(OrderStatus.PENDING_PAYMENT,LocalDateTime.now())){StoreOrder o=orders.findLocked(candidate.getId()).orElseThrow();if(o.getStatus()!=OrderStatus.PENDING_PAYMENT)continue;restore(o);o.setStatus(OrderStatus.EXPIRED);audit.event(o,OrderStatus.PENDING_PAYMENT,"Hết hạn thanh toán");orders.save(o);}}
 private void restore(StoreOrder o){for(OrderItem i:o.getItems()){if(i.getVariantId()!=null)variants.findLocked(i.getVariantId()).ifPresent(v->{audit.stock(i.getProductId(),v.getId(),v.getStock(),v.getStock()+i.getQuantity(),o.getId(),"Hoàn tồn đơn hủy/hết hạn");v.setStock(v.getStock()+i.getQuantity());variants.save(v);});else products.findLocked(i.getProductId()).ifPresent(p->{audit.stock(p.getId(),null,p.getStock(),p.getStock()+i.getQuantity(),o.getId(),"Hoàn tồn đơn hủy/hết hạn");p.setStock(p.getStock()+i.getQuantity());products.save(p);});}}
 private User user(String e){return users.findByEmail(e).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng"));}
 double price(Product p){return p.getSalePrice()!=null&&p.getSalePrice()>0?p.getSalePrice():p.getPrice();}
 private double unitPriceOf(CartItem c){if(c.getVariantId()!=null)return variants.findById(c.getVariantId()).map(v->v.getPriceOverride()!=null&&v.getPriceOverride()>0?v.getPriceOverride():price(c.getProduct())).orElse(price(c.getProduct()));return price(c.getProduct());}
 private String variantLabel(ProductVariant v){return v.getAttributeValues().stream().sorted(Comparator.comparing(av->av.getAttribute().getName())).map(av->av.getAttribute().getName()+": "+av.getValue()).reduce((a,b)->a+", "+b).orElse("");}
 private CartItem ownedCart(String e,Long id){CartItem c=carts.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));if(!c.getUser().getEmail().equalsIgnoreCase(e))throw new RuntimeException("Không có quyền");return c;} private StoreOrder ownedOrder(String e,Long id){StoreOrder o=orders.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy đơn hàng"));if(!o.getUser().getEmail().equalsIgnoreCase(e))throw new RuntimeException("Không có quyền");return o;}
 private String str(Object o){return o==null?"":String.valueOf(o).trim();} private boolean isRecommended(Product p,Goal g){return g!=null&&p.getSuitableGoals()!=null&&p.getSuitableGoals().contains(g.name());}
 Map<String,Object> productMap(Product p,Goal g){Map<String,Object>m=new LinkedHashMap<>();m.put("id",p.getId());m.put("name",p.getName());m.put("category",p.getCategory());m.put("description",p.getDescription());m.put("brand",p.getBrand());m.put("imageUrl",p.getImageUrl());m.put("images",p.getImages()==null?List.of():p.getImages());m.put("price",p.getPrice());m.put("salePrice",p.getSalePrice());m.put("stock",p.getStock());m.put("active",p.getActive());m.put("suitableGoals",p.getSuitableGoals());m.put("requiredEquipmentCode",p.getRequiredEquipmentCode());m.put("recommended",isRecommended(p,g));double[]rs=reviewService.summary(p.getId());m.put("averageRating",rs[1]>0?Math.round(rs[0]/rs[1]*10)/10.0:null);m.put("reviewCount",(int)rs[1]);var pv=variants.findByProductIdOrderByIdAsc(p.getId());m.put("hasVariants",!pv.isEmpty());if(!pv.isEmpty()){m.put("stock",pv.stream().filter(v->Boolean.TRUE.equals(v.getActive())).mapToInt(ProductVariant::getStock).sum());m.put("displayPrice",pv.stream().filter(v->Boolean.TRUE.equals(v.getActive())).mapToDouble(v->v.getPriceOverride()!=null&&v.getPriceOverride()>0?v.getPriceOverride():price(p)).min().orElse(price(p)));}else m.put("displayPrice",price(p));return m;}
 private Map<String,Object> cartMap(CartItem c){Map<String,Object>m=new LinkedHashMap<>(productMap(c.getProduct(),null));m.put("stock",c.getVariantId()!=null?variants.findById(c.getVariantId()).filter(v->Boolean.TRUE.equals(v.getActive())).map(ProductVariant::getStock).orElse(0):c.getProduct().getStock());m.put("cartItemId",c.getId());m.put("variantId",c.getVariantId());m.put("variantLabel",c.getVariantLabel());m.put("quantity",c.getQuantity());double unit=unitPriceOf(c);m.put("unitPrice",unit);m.put("lineTotal",unit*c.getQuantity());return m;}
 Map<String,Object> orderMap(StoreOrder o){Map<String,Object>m=new LinkedHashMap<>();m.put("id",o.getId());m.put("status",o.getStatus());m.put("channel",o.getChannel());m.put("paymentMethod",o.getPaymentMethod());m.put("subtotal",o.getSubtotal());m.put("discount",o.getDiscount());m.put("voucherCode",o.getVoucherCode());m.put("voucherDiscount",o.getVoucherDiscount());m.put("shippingFee",o.getShippingFee());m.put("total",o.getTotal());m.put("receiverName",o.getReceiverName());m.put("phone",o.getPhone());m.put("shippingAddress",o.getShippingAddress());m.put("note",o.getNote());m.put("transferCode",o.getTransferCode());m.put("qrRawPayload",o.getQrRawPayload());m.put("qrCodeUrl",o.getQrCodeUrl());m.put("createdAt",o.getCreatedAt());m.put("expiresAt",o.getExpiresAt());m.put("paidAt",o.getPaidAt());m.put("userName",o.getUser()!=null?o.getUser().getFullName():o.getReceiverName());m.put("userEmail",o.getUser()!=null?o.getUser().getEmail():null);m.put("createdByStaffId",o.getCreatedByStaffId());m.put("history",audit.history(o.getId()));m.put("items",o.getItems().stream().map(i->{Map<String,Object>im=new LinkedHashMap<>();im.put("productId",i.getProductId());im.put("productName",i.getProductName());im.put("variantId",i.getVariantId());im.put("variantLabel",i.getVariantLabel());im.put("unitPrice",i.getUnitPrice());im.put("quantity",i.getQuantity());im.put("lineTotal",i.getLineTotal());return im;}).toList());return m;}
 private String statusLabel(OrderStatus s){return switch(s){case PREPARING->"Đơn đang được chuẩn bị";case SHIPPING->"Đơn đang được giao";case DELIVERED->"Đơn đã giao";case COMPLETED->"Đơn đã hoàn thành";default->s.name();};}
}
