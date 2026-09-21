from pathlib import Path
import re
base=Path("output/report/schema-43")
src=(base/"ERD_43_bang.mmd").read_text(encoding="utf-8-sig")
tables={}
for m in re.finditer(r"^    (\w+) \{\n(.*?)^    \}",src,re.M|re.S):
 tables[m[1]]=[line.strip().split() for line in m[2].splitlines() if line.strip()]
relations=re.findall(r'^    (\w+) (\S+--\S+) (\w+) : "(\w+)"',src,re.M)
patch=(base/"Bo_sung_16_lien_ket_diagram_SQLServer.sql").read_text(encoding="utf-8-sig")
extra=re.findall(r'ALTER TABLE \[dbo\]\.\[(\w+)\].*?FOREIGN KEY \(\[(\w+)\]\) REFERENCES \[dbo\]\.\[(\w+)\]\s*\(\[(\w+)\]\)',patch,re.S)
assert len(tables)==43 and len(relations)==39 and len(extra)==16, (len(tables),len(relations),len(extra))
refs={(child,col) for child,col,_,_ in extra}
targetcols={(parent,col) for _,_,parent,col in extra}
for child,col,parent,target in extra:
 assert any(a[1]==col for a in tables[child])
 assert any(a[1]==target for a in tables[parent])
linked=set()
for a,_,b,_ in relations:linked.update((a,b))
for a,_,b,_ in extra:linked.update((a,b))
isolated=sorted(set(tables)-linked)
assert isolated==['foods','injury_area_options','muscle_split_configs','recommended_schedule_configs','system_configs']
for compact in (False,True):
 lines=['@startuml','title ERD logic 43 bảng - 39 FK và 16 liên kết logic bổ sung',
 'hide circle','skinparam monochrome true','skinparam shadowing false','skinparam defaultFontName Arial',
 'skinparam defaultFontSize 12','skinparam linetype ortho','skinparam nodesep 40','skinparam ranksep 65',
 'top to bottom direction', '']
 for name,attrs in tables.items():
  lines.append(f'entity "{name}" as {name} {{')
  for attr in attrs:
   typ,col=attr[:2]; tags=attr[2].split(',') if len(attr)>2 else []
   logical=(name,col) in refs
   if compact and not (tags or logical or (name,col) in targetcols):continue
   marks=tags+(['REF'] if logical else [])
   label=' '.join(f'<<{mark}>>' for mark in marks)
   lines.append(f'  {col} : {typ} {label}'.rstrip())
  lines.append('}')
 for parent,card,child,col in relations:
  lines.append(f'{parent} {card} {child} : "{col}"')
 lines.append("' Dotted lines are logical references, not existing database FK constraints.")
 for child,col,parent,target in extra:
  card='||' if child=='user_cosmetic_ownership' else '|o'
  lines.append(f'{parent} {card}..o{{ {child} : "{col} -> {target}"')
 lines+=['legend bottom',
 '  Đường liền: 39 khóa ngoại trong schema gốc.',
 '  Đường nét đứt / REF: 16 liên kết logic bổ sung; chưa phải FK của ứng dụng.',
 '  PK: khóa chính. FK: khóa ngoại hiện có. UK: khóa duy nhất.',
 '  Năm bảng danh mục/cấu hình đứng riêng do chưa có cột tham chiếu phù hợp.',
 '  notifications.ref_id phụ thuộc ref_type, không có một bảng đích cố định.',
 'endlegend','@enduml']
 file=base/('ERD_43_bang_noi_du_gon.puml' if compact else 'ERD_43_bang_noi_du.puml')
 file.write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(f"Verified 43 tables; {len(relations)} existing FKs; {len(extra)} extra logical references.")
print("Independent tables:",", ".join(isolated))
