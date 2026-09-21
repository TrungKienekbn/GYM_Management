from zipfile import ZipFile
from lxml import etree as E
from pathlib import Path
p=Path(r"C:/Users/anh15/Downloads/SD-41_DATNSP26_chinhsua (11-41_18092006).docx")
out=Path("tmp/usecase-reference");out.mkdir(parents=True,exist_ok=True)
ns={"w":"http://schemas.openxmlformats.org/wordprocessingml/2006/main","a":"http://schemas.openxmlformats.org/drawingml/2006/main","r":"http://schemas.openxmlformats.org/officeDocument/2006/relationships"}
with ZipFile(p) as z:
 root=E.fromstring(z.read("word/document.xml"))
 rels={e.get("Id"):e.get("Target") for e in E.fromstring(z.read("word/_rels/document.xml.rels"))}
 active=False
 lines=[]
 for i,e in enumerate(root.find("w:body",ns)):
  t=" ".join(e.xpath(".//w:t/text()",namespaces=ns))
  if "PHỤ LỤC A" in t.upper() and i>100: active=True
  if active:
   imgs=[]
   for rid in e.xpath(".//a:blip/@r:embed",namespaces=ns):
    target=rels[rid];name=Path(target).name
    (out/name).write_bytes(z.read("word/"+target))
    imgs.append(name)
   lines.append(f"{i}: {t[:600]} IMAGES={imgs}")
 (out/"extract.txt").write_text("\n".join(lines),encoding="utf8")
 print("\n".join(lines)[:21000])
