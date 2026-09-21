from pathlib import Path
import re
b=Path("output/report/schema-43")
s=(b/"ERD_43_bang_noi_du_gon.puml").read_text(encoding="utf-8")
entities=dict(re.findall(r'entity "(\w+)" as \w+ \{\n(.*?)\n\}',s,re.S))
edges=re.findall(r'^(\w+) ([|o{}]+(?:--|\.\.)[|o{}]+) (\w+) : "([^"]+)"',s,re.M)
groups=[
("Tài khoản và theo dõi thể chất",["users","user_profiles","endurance_tests","progress_tracking","notifications","chat_messages","service_ratings"]),
("Gói tập và nhân vật đồng hành",["memberships","invoices","pet_profiles","user_cosmetic_ownership"]),
("Hỗ trợ và nhân viên",["support_sessions","support_messages","work_shifts","customer_addresses"]),
("Giáo án và buổi tập",["workout_plans","plan_muscle_group_weight","weekly_reviews","workout_plan_days","workout_plan_exercises","workout_sessions","session_exercise_logs"]),
("Sản phẩm và thuộc tính",["product_variants","product_attribute_values","variant_attribute_values","voucher_scope_products"]),
("Đơn hàng và biến động kho",["shop_orders","shop_order_items","shop_order_events","shop_inventory_movements"]),
("Giỏ hàng và trải nghiệm khách hàng",["shop_cart_items","customer_product_states","product_reviews"]),
("Danh mục và cấu hình độc lập",["foods","system_configs","injury_area_options","muscle_split_configs","recommended_schedule_configs"])
]
used=[];tables=set(); group_names=[]
lines=["@startuml","title ERD logic 43 bảng - Nhóm chức năng xếp dọc","hide circle",
"skinparam monochrome true","skinparam backgroundColor white","skinparam shadowing false",
"skinparam defaultFontName Arial","skinparam defaultFontSize 13","skinparam linetype polyline",
"skinparam nodesep 55","skinparam ranksep 65","skinparam packageStyle rectangle","top to bottom direction"]
for i,(label,children) in enumerate(groups,1):
 es=[e for e in edges if e[2] in children];used+=es
 names=set(children)
 for a,_,c,_ in es:names.update([a,c])
 tables.update(names)
 group_names.append(sorted(names))
 lines.append(f'package "{i}. {label}" as G{i} {{')
 for name in sorted(names):
  lines += [f'entity "{name}" as g{i}_{name} {{',entities[name],"}"]
 for a,card,c,col in es:
  lines.append(f'g{i}_{a} {card} g{i}_{c} : "{col}"')
 if not es:
  for a,c in zip(sorted(names),sorted(names)[1:]):lines.append(f'g{i}_{a} -[hidden]down- g{i}_{c}')
 lines.append("}")
for i in range(1,len(groups)):
 for a in group_names[i-1]:
  for c in group_names[i]:lines.append(f'g{i}_{a} -[hidden]down- g{i+1}_{c}')
lines+=["legend bottom",
"  Bảng cùng tên xuất hiện ở nhiều nhóm vẫn là MỘT bảng trong CSDL.",
"  Đường liền: 39 FK hiện có. Nét đứt / REF: 16 liên kết logic bổ sung.",
"  PK: khóa chính. FK: khóa ngoại. UK: khóa duy nhất.",
"  Nhóm 8 đứng độc lập vì chưa có cột tham chiếu phù hợp.",
"endlegend","@enduml"]
assert len(used)==len(edges)==55
assert len(tables)==43
for ext in ("puml","txt"):
 (b/f"ERD_43_bang_doc_theo_nhom.{ext}").write_text("\n".join(lines)+"\n",encoding="utf-8")
print("Verified 43 unique tables and all 55 relationships appear exactly once.")
