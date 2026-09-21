from pathlib import Path
p=Path("tmp/export_plantuml_appendix.py")
s=p.read_text(encoding="utf-8")
for key in ("mua_hang","quan_tri","ca_lam"):
 s=s.replace("activities['"+key+"'] = '''","activities['"+key+"'] = r'''")
start=s.index("activities['mua_hang']")
end=s.index("for key, number, title in groups:",start)
body=s[start:end]
lines=[]
for line in body.splitlines():
 if line.lstrip().startswith(":") and line.endswith(";"):
  line=line[:-1].replace(";", ",")+";"
 lines.append(line)
s=s[:start]+"\n".join(lines)+"\n\n"+s[end:]
p.write_text(s,encoding="utf-8")
