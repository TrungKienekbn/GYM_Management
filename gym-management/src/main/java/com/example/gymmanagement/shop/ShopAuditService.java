package com.example.gymmanagement.shop;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import java.time.LocalDateTime; import java.util.*; import org.springframework.security.core.context.SecurityContextHolder;
@Service @RequiredArgsConstructor public class ShopAuditService {
 private final ShopOrderEventRepository events; private final ShopInventoryMovementRepository movements;
 public String actor(){var a=SecurityContextHolder.getContext().getAuthentication();return a==null||"anonymousUser".equals(a.getName())?"Hệ thống":a.getName();}
 public void event(StoreOrder o,OrderStatus before,String note){var e=new ShopOrderEvent();e.setOrderId(o.getId());e.setFromStatus(before==null?null:before.name());e.setToStatus(o.getStatus().name());e.setActor(actor());e.setNote(note);e.setCreatedAt(LocalDateTime.now());events.save(e);}
 public void stock(Long productId,Long variantId,int before,int after,Long orderId,String reason){if(before==after)return;var m=new ShopInventoryMovement();m.setProductId(productId);m.setVariantId(variantId);m.setBeforeStock(before);m.setAfterStock(after);m.setOrderId(orderId);m.setReason(reason);m.setActor(actor());m.setCreatedAt(LocalDateTime.now());movements.save(m);}
 public List<ShopOrderEvent> history(Long id){return events.findByOrderIdOrderByIdAsc(id);}
}
