package com.example.gymmanagement.shop;

import com.example.gymmanagement.entity.*;
import com.example.gymmanagement.enums.Goal;
import com.example.gymmanagement.repository.*;
import com.example.gymmanagement.service.*;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.util.*; import java.util.stream.*;

@Service @RequiredArgsConstructor
public class ShopService {
 private final ProductRepository products; private final CartItemRepository carts; private final StoreOrderRepository orders;
 private final UserRepository users; private final UserProfileRepository profiles; private final MembershipService memberships;
 private final BankQrService bankQr; private final NotificationService notifications;

 public List<Map<String,Object>> products(String email,String category,String keyword){
  User u=user(email); Goal goal=profiles.findByUserId(u.getId()).map(UserProfile::getGoal).orElse(null);
  return products.findByActiveTrueOrderByCreatedAtDesc().stream()
   .filter(p->category==null||category.isBlank()||p.getCategory().name().equals(category))
   .filter(p->keyword==null||keyword.isBlank()||p.getName().toLowerCase().contains(keyword.toLowerCase()))
   .sorted(Comparator.comparing((Product p)->isRecommended(p,goal)).reversed())
   .map(p->productMap(p,goal)).toList();
 }
 public List<Map<String,Object>> allProducts(){return products.findAll().stream().map(p->productMap(p,null)).toList();}
 @Transactional public Product saveProduct(Long id,Product input){
  Product p=id==null?new Product():products.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm"));
  if(input.getName()==null||input.getName().isBlank()||input.getPrice()==null||input.getPrice()<0||input.getStock()==null||input.getStock()<0)throw new RuntimeException("Tên, giá và tồn kho không hợp lệ");
  p.setName(input.getName().trim());p.setCategory(input.getCategory());p.setDescription(input.getDescription());p.setBrand(input.getBrand());p.setImageUrl(input.getImageUrl());p.setPrice(input.getPrice());p.setSalePrice(input.getSalePrice());p.setStock(input.getStock());p.setSuitableGoals(input.getSuitableGoals());p.setRequiredEquipmentCode(input.getRequiredEquipmentCode());p.setActive(input.getActive()==null?true:input.getActive());return products.save(p);
 }
 @Transactional public void hideProduct(Long id){Product p=products.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm"));p.setActive(false);products.save(p);}

 public List<Map<String,Object>> cart(String email){return carts.findByUserId(user(email).getId()).stream().map(this::cartMap).toList();}
 @Transactional public void addCart(String email,Long productId,int qty){
  User u=user(email);Product p=products.findById(productId).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm")); if(!Boolean.TRUE.equals(p.getActive()))throw new RuntimeException("Sản phẩm đã ngừng bán");
  CartItem c=carts.findByUserIdAndProductId(u.getId(),productId).orElse(CartItem.builder().user(u).product(p).quantity(0).build());int next=c.getQuantity()+Math.max(1,qty);if(next>p.getStock())throw new RuntimeException("Sản phẩm chỉ còn "+p.getStock()+" sản phẩm");c.setQuantity(next);carts.save(c);
 }
 @Transactional public void updateCart(String email,Long id,int qty){CartItem c=ownedCart(email,id);if(qty<=0){carts.delete(c);return;}if(qty>c.getProduct().getStock())throw new RuntimeException("Không đủ tồn kho");c.setQuantity(qty);carts.save(c);}
 @Transactional public void removeCart(String email,Long id){carts.delete(ownedCart(email,id));}

 @Transactional public Map<String,Object> checkout(String email,Map<String,Object> req){
  User u=user(email);List<CartItem> list=carts.findByUserId(u.getId());if(list.isEmpty())throw new RuntimeException("Giỏ hàng đang trống");
  String receiver=str(req.get("receiverName")),phone=str(req.get("phone")),address=str(req.get("shippingAddress"));if(receiver.isBlank()||phone.isBlank()||address.isBlank())throw new RuntimeException("Vui lòng nhập đủ người nhận, số điện thoại và địa chỉ");
  Map<Long,Product> locked=new HashMap<>();for(CartItem c:list){Product p=products.findLocked(c.getProduct().getId()).orElseThrow(()->new RuntimeException("Sản phẩm không tồn tại"));locked.put(p.getId(),p);if(c.getQuantity()>p.getStock())throw new RuntimeException(p.getName()+" không đủ tồn kho");}
  double subtotal=list.stream().mapToDouble(c->price(c.getProduct())*c.getQuantity()).sum();double discount=memberships.isVip(u)?Math.round(subtotal*.05):0;double shipping=subtotal>=500000||memberships.isVip(u)?0:30000;
  StoreOrder o=StoreOrder.builder().user(u).status(OrderStatus.PENDING_PAYMENT).subtotal(subtotal).discount(discount).shippingFee(shipping).total(subtotal-discount+shipping).receiverName(receiver).phone(phone).shippingAddress(address).note(str(req.get("note"))).expiresAt(LocalDateTime.now().plusMinutes(15)).build();orders.save(o);
  for(CartItem c:list){Product p=locked.get(c.getProduct().getId());p.setStock(p.getStock()-c.getQuantity());products.save(p);o.getItems().add(OrderItem.builder().order(o).productId(p.getId()).productName(p.getName()).imageUrl(p.getImageUrl()).unitPrice(price(p)).quantity(c.getQuantity()).lineTotal(price(p)*c.getQuantity()).build());}
  BankQrService.BankQrResult qr=bankQr.generate("SHOP"+o.getId(),Math.round(o.getTotal()));o.setTransferCode(qr.getTransferCode());o.setQrRawPayload(qr.getQrRawPayload());o.setQrCodeUrl(qr.getQrImageUrl());orders.save(o);carts.deleteByUserId(u.getId());return orderMap(o);
 }
 public List<Map<String,Object>> myOrders(String email){expire();return orders.findByUserIdOrderByCreatedAtDesc(user(email).getId()).stream().map(this::orderMap).toList();}
 public Map<String,Object> order(String email,Long id){expire();StoreOrder o=ownedOrder(email,id);return orderMap(o);}
 public List<Map<String,Object>> allOrders(){expire();return orders.findAllByOrderByCreatedAtDesc().stream().map(this::orderMap).toList();}
 @Transactional public Map<String,Object> cancel(String email,Long id){StoreOrder o=ownedOrder(email,id);if(o.getStatus()!=OrderStatus.PENDING_PAYMENT)throw new RuntimeException("Chỉ có thể hủy đơn đang chờ thanh toán");restore(o);o.setStatus(OrderStatus.CANCELLED);return orderMap(orders.save(o));}
 @Transactional public Map<String,Object> updateStatus(Long id,OrderStatus next){StoreOrder o=orders.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy đơn hàng"));Map<OrderStatus,List<OrderStatus>> allowed=Map.of(OrderStatus.PAID,List.of(OrderStatus.PREPARING),OrderStatus.PREPARING,List.of(OrderStatus.SHIPPING),OrderStatus.SHIPPING,List.of(OrderStatus.DELIVERED),OrderStatus.DELIVERED,List.of(OrderStatus.COMPLETED));if(!allowed.getOrDefault(o.getStatus(),List.of()).contains(next))throw new RuntimeException("Không thể chuyển từ "+o.getStatus()+" sang "+next);o.setStatus(next);notifications.sendToUser(o.getUser().getId(),"Đơn hàng #"+o.getId()+" đã cập nhật",statusLabel(next),"SYSTEM");return orderMap(orders.save(o));}
 @Transactional public boolean handleWebhook(Map<String,Object> payload){String content=str(payload.get("content")).toUpperCase();Object a=payload.get("transferAmount");if(a==null)return false;for(StoreOrder o:orders.findByStatusIn(List.of(OrderStatus.PENDING_PAYMENT,OrderStatus.EXPIRED))){if(o.getTransferCode()!=null&&content.replaceAll("[^A-Z0-9]"," ").contains(o.getTransferCode())){if(Math.round(o.getTotal())!=Long.parseLong(String.valueOf(a)))throw new RuntimeException("Số tiền đơn hàng không khớp");if(o.getStatus()==OrderStatus.EXPIRED)throw new RuntimeException("Đơn hàng đã hết hạn và tồn kho đã được hoàn lại");o.setStatus(OrderStatus.PAID);o.setPaidAt(LocalDateTime.now());o.setTransactionId(str(payload.getOrDefault("referenceCode",payload.get("id"))));orders.save(o);notifications.sendToUser(o.getUser().getId(),"Thanh toán đơn hàng thành công","Đơn hàng #"+o.getId()+" đang được chuẩn bị.","SYSTEM");return true;}}return false;}
 @Transactional public void expire(){for(StoreOrder o:orders.findByStatusAndExpiresAtBefore(OrderStatus.PENDING_PAYMENT,LocalDateTime.now())){restore(o);o.setStatus(OrderStatus.EXPIRED);orders.save(o);}}
 private void restore(StoreOrder o){for(OrderItem i:o.getItems())products.findById(i.getProductId()).ifPresent(p->{p.setStock(p.getStock()+i.getQuantity());products.save(p);});}
 private User user(String e){return users.findByEmail(e).orElseThrow(()->new RuntimeException("Không tìm thấy người dùng"));} private double price(Product p){return p.getSalePrice()!=null&&p.getSalePrice()>0?p.getSalePrice():p.getPrice();}
 private CartItem ownedCart(String e,Long id){CartItem c=carts.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy sản phẩm trong giỏ"));if(!c.getUser().getEmail().equalsIgnoreCase(e))throw new RuntimeException("Không có quyền");return c;} private StoreOrder ownedOrder(String e,Long id){StoreOrder o=orders.findById(id).orElseThrow(()->new RuntimeException("Không tìm thấy đơn hàng"));if(!o.getUser().getEmail().equalsIgnoreCase(e))throw new RuntimeException("Không có quyền");return o;}
 private String str(Object o){return o==null?"":String.valueOf(o).trim();} private boolean isRecommended(Product p,Goal g){return g!=null&&p.getSuitableGoals()!=null&&p.getSuitableGoals().contains(g.name());}
 private Map<String,Object> productMap(Product p,Goal g){Map<String,Object>m=new LinkedHashMap<>();m.put("id",p.getId());m.put("name",p.getName());m.put("category",p.getCategory());m.put("description",p.getDescription());m.put("brand",p.getBrand());m.put("imageUrl",p.getImageUrl());m.put("price",p.getPrice());m.put("salePrice",p.getSalePrice());m.put("stock",p.getStock());m.put("active",p.getActive());m.put("suitableGoals",p.getSuitableGoals());m.put("requiredEquipmentCode",p.getRequiredEquipmentCode());m.put("recommended",isRecommended(p,g));return m;}
 private Map<String,Object> cartMap(CartItem c){Map<String,Object>m=new LinkedHashMap<>(productMap(c.getProduct(),null));m.put("cartItemId",c.getId());m.put("quantity",c.getQuantity());m.put("unitPrice",price(c.getProduct()));m.put("lineTotal",price(c.getProduct())*c.getQuantity());return m;}
 private Map<String,Object> orderMap(StoreOrder o){Map<String,Object>m=new LinkedHashMap<>();m.put("id",o.getId());m.put("status",o.getStatus());m.put("subtotal",o.getSubtotal());m.put("discount",o.getDiscount());m.put("shippingFee",o.getShippingFee());m.put("total",o.getTotal());m.put("receiverName",o.getReceiverName());m.put("phone",o.getPhone());m.put("shippingAddress",o.getShippingAddress());m.put("note",o.getNote());m.put("transferCode",o.getTransferCode());m.put("qrRawPayload",o.getQrRawPayload());m.put("qrCodeUrl",o.getQrCodeUrl());m.put("createdAt",o.getCreatedAt());m.put("expiresAt",o.getExpiresAt());m.put("paidAt",o.getPaidAt());m.put("userName",o.getUser().getFullName());m.put("userEmail",o.getUser().getEmail());m.put("items",o.getItems().stream().map(i->Map.of("productId",i.getProductId(),"productName",i.getProductName(),"unitPrice",i.getUnitPrice(),"quantity",i.getQuantity(),"lineTotal",i.getLineTotal())).toList());return m;}
 private String statusLabel(OrderStatus s){return switch(s){case PREPARING->"Đơn đang được chuẩn bị";case SHIPPING->"Đơn đang được giao";case DELIVERED->"Đơn đã giao";case COMPLETED->"Đơn đã hoàn thành";default->s.name();};}
}
