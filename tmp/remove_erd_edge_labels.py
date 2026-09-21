from pathlib import Path
import re
from zipfile import ZipFile, ZIP_DEFLATED
root=Path("output/report")
out=root/"erd-chan-ga-khong-nhan"
out.mkdir(exist_ok=True)
sources=list((root/"erd-doc-ro-duong").glob("ERD_*.puml"))
sources += [root/"schema-43/ERD_43_bang_ban_doc.puml"]
total=0
for p in sources:
 s=p.read_text(encoding="utf-8-sig")
 pattern=r'(?m)^(\w+\s+[|o{}]+(?:--|\.\.|-\[norank\]-|\.\[norank\]\.)[|o{}]+\s+\w+)[ \t]*:[ \t]*"[^"]*"[ \t]*$'
 s,n=re.subn(pattern,r'\1',s)
 if p.name=="ERD_43_bang_ban_doc.puml":assert n==55,n
 else:total+=n
 for ext in ("puml","txt"):
  (out/(p.stem+"."+ext)).write_text(s,encoding="utf-8")
assert total==55,total
with ZipFile(root/"ERD_chan_ga_khong_chu.zip","w",ZIP_DEFLATED) as z:
 for p in out.iterdir():z.write(p,p.name)
print("Removed labels only: 55 relationships in grouped diagrams and 55 in full portrait. Crow's feet and fields retained.")
