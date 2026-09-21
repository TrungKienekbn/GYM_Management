from pathlib import Path
import re
base=Path("output/report/schema-43")
source=(base/"ERD_43_bang_noi_du.puml").read_text(encoding="utf-8")
ddl=(base/"schema_43_bang.sql").read_text(encoding="utf-8-sig")
types={}
for name,body in re.findall(r"create table (\w+) \((.*?)\);",ddl):
 for m in re.finditer(r"(?:^|, )(\w+) (bigint|integer|tinyint|boolean|date|time\(6\)|timestamp\(6\)|float\(53\)|varchar\(\d+\)|clob|enum\s*\([^)]*\))",body):
  t=m[2].upper()
  if t.startswith("ENUM"):t="ENUM"
  types[(name,m[1])]=t
entities={}
for name,body in re.findall(r'entity "(\w+)" as \w+ \{\n(.*?)\n\}',source,re.S):
 attrs=[]
 for line in body.splitlines():
  m=re.match(r"\s*(\w+) : (\w+)(.*)",line)
  if not m:continue
  col,t,tail=m.groups()
  marks=re.findall(r"<<(\w+)>>",tail)
  attrs.append((col,types.get((name,col),t.upper()),marks))
 attrs.sort(key=lambda a: 0 if "PK" in a[2] else 1 if "FK" in a[2] or "REF" in a[2] else 2)
 lines=[f'entity "{name}" as {name} {{']
 for col,t,marks in attrs:
  key="PK/FK" if "PK" in marks and "FK" in marks else "PK" if "PK" in marks else "FK" if "FK" in marks else "REF" if "REF" in marks else ""
  uk="  [UK]" if "UK" in marks and "PK" not in marks else ""
  prefix=f"**{key}**  " if key else "      "
  lines.append(f"  {{field}} {prefix}{col} : {t}{uk}")
 lines.append("}")
 entities[name]="\n".join(lines)
assert len(entities)==43
header="""@startuml
' Code PlantUML: copy this entire file, including @startuml and @enduml.
hide circle
hide methods
skinparam backgroundColor white
skinparam shadowing false
skinparam roundcorner 0
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 65
skinparam ranksep 85
skinparam class {
  BackgroundColor White
  HeaderBackgroundColor #EEEEEE
  BorderColor Black
  FontColor Black
  FontStyle bold
  AttributeFontStyle plain
}
top to bottom direction
"""
rels=re.findall(r"^(\w+ [|o{}]+(?:--|\.\.)[|o{}]+ \w+) :",source,re.M)
assert len(rels)==55
# Keep the previously established portrait grid as invisible layout guides.
portrait=(base/"ERD_43_bang_ban_doc.puml").read_text(encoding="utf-8")
guides=re.findall(r"^\w+ -\[hidden\](?:right|down)- \w+$",portrait,re.M)
refs=[re.sub(r"--","-[norank]-",line) if "--" in line else line.replace("..",".[norank].") for line in rels]
footer="""legend bottom
  PK: khóa chính. FK: khóa ngoại hiện có. UK: khóa duy nhất.
  REF / nét đứt: liên kết logic bổ sung, chưa phải FK của ứng dụng.
endlegend
@enduml
"""
text=header+"\n\n"+"\n\n".join(entities.values())+"\n\n"+"\n".join(refs)+"\n\n' Layout only: invisible lines are not relationships.\n"+"\n".join(guides)+"\n"+footer
for ext in ("puml","txt"):(base/f"ERD_43_bang_chi_tiet_theo_mau.{ext}").write_text(text,encoding="utf-8")
# Detailed versions of the existing groups, using exactly the same visual style.
out=Path("output/report/erd-chi-tiet-theo-mau");out.mkdir(exist_ok=True)
for p in Path("output/report/erd-doc-ro-duong").glob("ERD_*.puml"):
 old=p.read_text(encoding="utf-8")
 names=re.findall(r'^entity "(\w+)"',old,re.M)
 rr=re.findall(r"^(\w+ [|o{}]+(?:--|\.\.)[|o{}]+ \w+) :",old,re.M)
 h=header
 if "left to right direction" in old:h=h.replace("top to bottom direction","left to right direction")
 title=re.search(r"^title (.+)$",old,re.M)[1]
 extra=re.findall(r"^\w+ -\[hidden\](?:right|down)- \w+$",old,re.M)
 content=h+"\ntitle "+title+"\n\n"+"\n\n".join(entities[n] for n in names)+"\n"+"\n".join(rr+extra)+"\n"+footer
 for ext in ("puml","txt"):(out/(p.stem+"."+ext)).write_text(content,encoding="utf-8")
from zipfile import ZipFile,ZIP_DEFLATED
with ZipFile(base.parent/"ERD_chi_tiet_theo_mau_PlantUML.zip","w",ZIP_DEFLATED) as z:
 for p in out.iterdir():z.write(p,"Theo_nhom/"+p.name)
 for ext in ("puml","txt"):
  p=base/f"ERD_43_bang_chi_tiet_theo_mau.{ext}";z.write(p,p.name)
print("43 detailed tables, 55 unlabeled crow's-foot relationships; gray table headers and key prefixes.")
