from pathlib import Path
import html,re,json,sys
root=Path(__file__).resolve().parents[3]
out=root/'output/report/schema-43'
source=Path(sys.argv[1]) if len(sys.argv)>1 else out/'Thong_so_43_bang.md'
target=Path(sys.argv[2]) if len(sys.argv)>2 else out/'Thong_so_cap_nhat_43_bang.html'
lines=source.read_text(encoding='utf-8').splitlines()
buf=[];i=0
while i<len(lines):
    line=lines[i]
    if line.startswith('|'):
        rows=[]
        while i<len(lines) and lines[i].startswith('|'):
            if not lines[i].startswith('|---'):rows.append([html.escape(x.strip()) for x in lines[i].strip('|').split('|')])
            i+=1
        buf.append('<table><thead><tr>'+''.join('<th>'+v+'</th>' for v in rows[0])+'</tr></thead><tbody>')
        for row in rows[1:]:buf.append('<tr>'+''.join('<td>'+v+'</td>' for v in row)+'</tr>')
        buf.append('</tbody></table>');continue
    m=re.match(r'^(#{1,3}) (.*)',line)
    if m:
        level=len(m[1]);anchor='sec-'+str(i)
        buf.append(f'<h{level} id="{anchor}">'+html.escape(m[2])+f'</h{level}>')
    elif line:buf.append('<p>'+html.escape(line)+'</p>')
    i+=1
content='''<!doctype html><html lang="vi"><head><meta charset="utf-8"><title>Đặc tả đầy đủ 43 bảng</title>
<style>body{font-family:Arial,sans-serif;color:#151515;max-width:1120px;margin:40px auto;padding:0 28px;line-height:1.5}h1{font-size:30px;line-height:1.2}h2{font-size:23px;margin-top:40px}h3{font-size:18px;margin-top:28px}table{border-collapse:collapse;width:100%;margin:16px 0 24px;table-layout:fixed}th,td{border:1px solid #ccc;padding:9px;text-align:left;vertical-align:top;overflow-wrap:anywhere;font-size:14px}th{background:#eee}thead{display:table-header-group}tr{break-inside:avoid}h2,h3{break-after:avoid}@media print{body{max-width:none;margin:0;font-size:10pt;padding:0}th,td{font-size:9pt;padding:5px}@page{size:A4;margin:16mm}}</style></head><body>'''+ '\n'.join(buf)+'</body></html>'
content=content.replace('<title>Đặc tả đầy đủ 43 bảng</title>', '<title>'+html.escape(lines[0].lstrip('# '))+'</title>')
target.write_text(content,encoding='utf-8')
schema=json.loads((out/'schema_43_bang.json').read_text(encoding='utf-8'))
if len(sys.argv)==1:
    assert sum(1 for x in lines if re.match(r'### 4\.\d+\.',x))==43
    assert all(n in content for n in schema['tables'])
print('HTML generated: '+str(target))
