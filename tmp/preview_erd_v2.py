from pathlib import Path
import json, xml.etree.ElementTree as E
from html import escape

p=Path('output/report/erd-doc-da-tach-duong')
d=json.loads((p/'routing.json').read_text(encoding='utf-8'))
root=E.parse(p/'ERD_43_bang_doc_da_tach_duong.drawio').find('.//root')
cells=root.findall('mxCell'); rect=d['rects']
svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{d["width"]}" height="{d["height"]}" viewBox="0 0 {d["width"]} {d["height"]}">', '<rect width="100%" height="100%" fill="white"/>', '<defs>']
shapes={'one':'<path d="M -5 -7 V 7 M -11 -7 V 7"/>',
        'zeroone':'<path d="M -5 -7 V 7"/><circle cx="-17" cy="0" r="5" fill="white"/>',
        'zeromany':'<path d="M 0 -7 L -12 0 L 0 7 M 0 0 H -12"/><circle cx="-20" cy="0" r="5" fill="white"/>',
        'onemany':'<path d="M 0 -7 L -12 0 L 0 7 M 0 0 H -12 M -19 -7 V 7"/>'}
for name,shape in shapes.items():
    svg.append(f'<marker id="{name}" markerUnits="userSpaceOnUse" markerWidth="30" markerHeight="20" viewBox="-27 -10 30 20" refX="0" refY="0" orient="auto-start-reverse"><g stroke="#333" stroke-width="1.4" fill="none">{shape}</g></marker>')
svg.append('</defs>')
marks={'||':'one','|o':'zeroone','o|':'zeroone','o{':'zeromany','|{':'onemany'}
for index,pts in d['routes']:
    a,b,l,r,dashed=d['edges'][index]
    points=' '.join(f'{x},{y}' for x,y in pts)
    svg.append(f'<polyline points="{points}" fill="none" stroke="white" stroke-width="7"/>')
    dash='stroke-dasharray="7 5"' if dashed else ''
    svg.append(f'<polyline points="{points}" fill="none" stroke="#333" stroke-width="1.4" {dash} marker-start="url(#{marks[l]})" marker-end="url(#{marks[r]})"/>')
for name,(x,y,w,h) in rect.items():
    svg += [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="white" stroke="#333"/>',f'<rect x="{x}" y="{y}" width="{w}" height="40" fill="#eee" stroke="#333"/>',f'<text x="{x+w/2}" y="{y+26}" text-anchor="middle" font-family="Arial" font-size="16" font-weight="bold">{name}</text>']
    for c in cells:
        if c.get('parent')!=name:continue
        g=c.find('mxGeometry'); right='align=right;' in c.get('style','')
        xx=x+float(g.get('x'))+(float(g.get('width'))-10 if right else 10)
        yy=y+float(g.get('y'))+15
        svg.append(f'<text x="{xx}" y="{yy}" text-anchor="{"end" if right else "start"}" font-family="Arial" font-size="14">{escape(c.get("value"))}</text>')
svg.append('</svg>')
(p/'ERD_43_bang_doc_da_tach_duong.svg').write_text('\n'.join(svg),encoding='utf-8')
print('SVG exported with aligned types and crowfeet.')
