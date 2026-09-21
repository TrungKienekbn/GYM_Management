from pathlib import Path
import re
base=Path("output/report/schema-43")
s=(base/"ERD_43_bang_noi_du_gon.puml").read_text(encoding="utf-8")
s=s.replace("title ERD logic 43 bảng - 39 FK và 16 liên kết logic bổ sung","title ERD logic 43 bảng - Bố cục dọc")
s=s.replace("skinparam nodesep 40","skinparam nodesep 55").replace("skinparam ranksep 65","skinparam ranksep 65")
rows=[
["roles","users","user_profiles"],
["endurance_tests","progress_tracking","pet_profiles"],
["memberships","invoices","user_cosmetic_ownership"],
["notifications","chat_messages","service_ratings"],
["support_sessions","support_messages","work_shifts"],
["workout_plans","plan_muscle_group_weight","weekly_reviews"],
["workout_plan_days","workout_plan_exercises","exercises"],
["workout_sessions","session_exercise_logs","foods"],
["shop_products","product_variants","product_attributes"],
["shop_cart_items","product_attribute_values","variant_attribute_values"],
["shop_orders","shop_order_items","shop_inventory_movements"],
["shop_order_events","customer_addresses","customer_product_states"],
["vouchers","voucher_scope_products","system_configs"],
["injury_area_options","muscle_split_configs","recommended_schedule_configs"],
["product_reviews"]]
names=re.findall(r'^entity "(\w+)"',s,re.M)
assert sorted(sum(rows,[]))==sorted(names)
s=re.sub(r'^(\w+ [|o{}]+)--([|o{}]+ \w+ :)',r'\1-[norank]-\2',s,flags=re.M)
s=re.sub(r'^(\w+ [|o{}]+)\.\.([|o{}]+ \w+ :)',r'\1.[norank].\2',s,flags=re.M)
layout=["' Invisible layout guides only; not database relationships."]
for row in rows:
 for a,b in zip(row,row[1:]):layout.append(f'{a} -[hidden]right- {b}')
for upper,lower in zip(rows,rows[1:]):
 for a,b in zip(upper,lower):layout.append(f'{a} -[hidden]down- {b}')
s=s.replace("legend bottom","\n".join(layout)+"\nlegend bottom")
s=s.replace("  Đường nét đứt / REF: 16 liên kết logic bổ sung; chưa phải FK của ứng dụng.","  Nét đứt / REF: 16 liên kết logic bổ sung, chưa phải FK của ứng dụng.")
for ext in ("puml","txt"):
 (base/f"ERD_43_bang_ban_doc.{ext}").write_text(s,encoding="utf-8")
print("43 tables and 55 relationships retained; added invisible layout guides.")
