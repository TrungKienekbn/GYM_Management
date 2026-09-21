exec(open("tmp/erd_grouped_routing.py",encoding="utf-8").read().split('used=[];')[0])
from zipfile import ZipFile, ZIP_DEFLATED
out=Path("output/report/erd-doc-ro-duong");out.mkdir(exist_ok=True)
seen=[]
for i,(title,children) in enumerate(groups,1):
 es=[e for e in edges if e[2] in children];seen+=es
 names=set(children)
 for a,_,c,_ in es:names.update([a,c])
 direction="left to right direction" if i in (1,2,3,7) else "top to bottom direction"
 lines=["@startuml",f"title ERD logic - {i}. {title}","hide circle",
 "skinparam monochrome true","skinparam backgroundColor white","skinparam shadowing false",
 "skinparam defaultFontName Arial","skinparam defaultFontSize 14","skinparam linetype polyline",
 "skinparam nodesep 65","skinparam ranksep 100",direction]
 for n in sorted(names):lines.extend([f'entity "{n}" as {n} {{',entities[n],"}"])
 for a,card,c,col in es:lines.append(f'{a} {card} {c} : "{col}"')
 if not es:
  for a,c in zip(sorted(names),sorted(names)[1:]):lines.append(f'{a} -[hidden]down- {c}')
 lines+=["legend bottom","  Liền: FK hiện có. Nét đứt / REF: liên kết logic bổ sung.",
 "  Bảng cùng tên ở các hình là cùng một bảng trong CSDL.","endlegend","@enduml"]
 for ext in ("puml","txt"):(out/f"ERD_{i:02}.{ext}").write_text("\n".join(lines)+"\n",encoding="utf-8")
assert len(seen)==55
(out/"HUONG_DAN.txt").write_text("Tám sơ đồ là các phần của cùng ERD 43 bảng và 55 quan hệ.\nMở từng file TXT, copy toàn bộ vào PlantUML. Chèn các hình nối tiếp trong Word.\n01 Tài khoản và thể chất\n02 Gói tập và nhân vật\n03 Hỗ trợ và nhân viên\n04 Giáo án và buổi tập\n05 Sản phẩm và thuộc tính\n06 Đơn hàng và kho\n07 Giỏ hàng và trải nghiệm\n08 Danh mục độc lập\nCác bảng dùng chung lặp lại để tránh đường nối xuyên nhóm.\n",encoding="utf-8")
with ZipFile(out.parent/"ERD_doc_ro_duong_PlantUML.zip","w",ZIP_DEFLATED) as z:
 for p in out.iterdir():z.write(p,p.name)
print("8 diagrams; all 55 relationships preserved.")
