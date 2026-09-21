from pathlib import Path
from collections import defaultdict
from heapq import heappush, heappop
import re, math, json, xml.etree.ElementTree as E
from html import escape

OUT=Path('output/report/erd-doc-da-tach-duong')
OUT.mkdir(exist_ok=True)
src=Path('output/report/schema-43/ERD_43_bang_chi_tiet_theo_mau.puml').read_text(encoding='utf-8')
attrs={}
for name,body in re.findall(r'entity "(\w+)" as \w+ \{\n(.*?)\n\}',src,re.S):
    rows=[]
    for line in body.splitlines():
        t=line.split('{field}',1)[1].strip()
        m=re.match(r'\*\*(.*?)\*\*\s+(.*)',t)
        rows.append((m[1],m[2]) if m else ('',t))
    attrs[name]=rows
edges=[]
for a,mark,b in re.findall(r'^(\w+) ([|o{}]+(?:-\[norank\]-|\.\[norank\]\.)[|o{}]+) (\w+)$',src,re.M):
    parts=re.split(r'-\[norank\]-|\.\[norank\]\.',mark)
    edges.append((a,b,parts[0],parts[1],'.' in mark))
assert len(attrs)==43 and len(edges)==55

layout=[
 ['roles','notifications','chat_messages','service_ratings'],
 ['memberships','invoices','user_profiles','endurance_tests'],
 ['support_messages','support_sessions','users','progress_tracking'],
 ['work_shifts','customer_addresses','pet_profiles','user_cosmetic_ownership'],
 ['plan_muscle_group_weight','workout_plans','weekly_reviews','exercises'],
 ['workout_plan_days','workout_sessions','workout_plan_exercises','session_exercise_logs'],
 ['product_attributes','product_attribute_values','product_variants','shop_products'],
 ['variant_attribute_values','shop_inventory_movements','shop_orders','shop_order_items'],
 ['vouchers','voucher_scope_products','product_reviews','shop_order_events'],
 ['customer_product_states','shop_cart_items','foods','injury_area_options'],
 ['system_configs','muscle_split_configs','recommended_schedule_configs']
]
assert sorted(sum(layout,[]))==sorted(attrs)
STEP=20; W=520; GAP=480; LEFT=280; TOP=180
rect={}; y=TOP
for row in layout:
    maxh=max(60+20*len(attrs[n]) for n in row)
    for j,n in enumerate(row):
        rect[n]=(LEFT+j*(W+GAP),y,W,60+20*len(attrs[n]))
    y+=maxh+280
WIDTH=LEFT*2+4*W+3*GAP; HEIGHT=y+160
blocked=set()
for x,y,w,h in rect.values():
    blocked.update((a,b) for a in range(x//STEP,(x+w)//STEP+1) for b in range(y//STEP,(y+h)//STEP+1))
used_edges=set();used_ports=set();through=defaultdict(set)
def edgekey(a,b):return tuple(sorted((a,b)))
def candidates(n,other):
    x,y,w,h=rect[n]; ox,oy,ow,oh=rect[other];tx,ty=ox+ow/2,oy+oh/2
    vals=[]
    for yy in range(y+40,y+h-19,STEP):
        for xx,dx,side in [(x,-1,'L'),(x+w,1,'R')]:
            p=(xx//STEP,yy//STEP);q=(p[0]+dx,p[1])
            if p not in used_ports:vals.append((abs(xx-tx)+abs(yy-ty),p,q,side))
    for xx in range(x+40,x+w-39,STEP):
        for yy,dy,side in [(y,-1,'T'),(y+h,1,'B')]:
            p=(xx//STEP,yy//STEP);q=(p[0],p[1]+dy)
            if p not in used_ports:vals.append((abs(xx-tx)+abs(yy-ty)+70,p,q,side))
    return sorted(vals)

def route(start,end):
    heap=[]; best={(start,2):0};prev={};counter=0
    heappush(heap,(0,0,counter,start,2))
    while heap:
        _,cost,_,p,d=heappop(heap)
        if cost!=best.get((p,d)):continue
        if p==end:
            state=(p,d);result=[]
            while state in prev:result.append(state[0]);state=prev[state]
            result.append(start);return list(reversed(result))
        for dx,dy,nd in [(1,0,0),(-1,0,0),(0,1,1),(0,-1,1)]:
            q=(p[0]+dx,p[1]+dy)
            if q in blocked or not(2<=q[0]<=WIDTH//STEP-2 and 5<=q[1]<=HEIGHT//STEP-2):continue
            if edgekey(p,q) in used_edges:continue
            # Shared bends are not allowed. Perpendicular crossings are marked as jumps.
            if p in through and (nd in through[p] or len(through[p])>1):continue
            crossing=50 if q in through else 0
            # Keep parallel connectors at least two grid lanes apart when possible.
            neighbours = [(q[0],q[1]-1),(q[0],q[1]+1)] if nd==0 else [(q[0]-1,q[1]),(q[0]+1,q[1])]
            crossing += 14 * sum(nd in through.get(v,set()) for v in neighbours)
            nc=cost+1+(4 if d!=2 and d!=nd else 0)+crossing
            state=(q,nd)
            if nc<best.get(state,float('inf')):
                best[state]=nc;prev[state]=(p,d);counter+=1
                heuristic=abs(q[0]-end[0])+abs(q[1]-end[1])
                heappush(heap,(nc+heuristic,nc,counter,q,nd))
    return None

def simplify(points):
    result=[points[0]]
    for i in range(1,len(points)-1):
        a,b,c=points[i-1:i+2]
        if (b[0]-a[0])*(c[1]-b[1]) != (b[1]-a[1])*(c[0]-b[0]):result.append(b)
    return result+[points[-1]]

routes=[]
order=sorted(range(len(edges)),key=lambda i:abs(rect[edges[i][0]][0]-rect[edges[i][1]][0])+abs(rect[edges[i][0]][1]-rect[edges[i][1]][1]))
for index in order:
    a,b,_,_,_=edges[index]
    found=None
    ca,cb=candidates(a,b),candidates(b,a)
    pairs=sorted([(aa[0]+bb[0],aa,bb) for aa in ca[:12] for bb in cb[:12]],key=lambda z:z[0])
    for _,aa,bb in pairs[:50]:
        if aa[1]==bb[1]:continue
        path=route(aa[2],bb[2])
        if path:
            found=[aa[1]]+path+[bb[1]];break
    if found is None:raise RuntimeError(f'Cannot route {a} -> {b}')
    used_ports.update((found[0],found[-1]))
    for p,q in zip(found,found[1:]):
        k=edgekey(p,q)
        assert k not in used_edges
        used_edges.add(k)
        direction=0 if p[1]==q[1] else 1
        through[p].add(direction);through[q].add(direction)
    routes.append((index,simplify([(x*STEP,y*STEP) for x,y in found])))
    print(f'Routed {len(routes)}/55: {a} -> {b}',flush=True)

# Native draw.io cells, with connected endpoints and editable waypoints.
mxfile=E.Element('mxfile',host='app.diagrams.net',type='device')
diagram=E.SubElement(mxfile,'diagram',id='gym-erd',name='ERD chi tiết 43 bảng')
model=E.SubElement(diagram,'mxGraphModel',grid='1',gridSize='20',guides='1',tooltips='1',connect='1',arrows='1',fold='1',page='0',pageScale='1',pageWidth=str(WIDTH),pageHeight=str(HEIGHT),background='#ffffff')
root=E.SubElement(model,'root');E.SubElement(root,'mxCell',id='0');E.SubElement(root,'mxCell',id='1',parent='0')
def cell(id,value,style,parent='1',vertex=True):
    return E.SubElement(root,'mxCell',id=id,value=value,style=style,parent=parent,**({'vertex':'1'} if vertex else {'edge':'1'}))
def geom(c,x,y,w,h):E.SubElement(c,'mxGeometry',x=str(x),y=str(y),width=str(w),height=str(h),attrib={'as':'geometry'})
title=cell('title','ERD CHI TIẾT 43 BẢNG','text;html=0;align=center;fontSize=26;fontStyle=1;strokeColor=none;fillColor=none;')
geom(title,0,40,WIDTH,40)
note=cell('note','55 quan hệ · Chân gà không nhãn · Nét liền: FK hiện có · Nét đứt / REF: liên kết logic bổ sung','text;html=0;align=center;fontSize=16;strokeColor=none;fillColor=none;')
geom(note,0,100,WIDTH,30)
for n,(x,y,w,h) in rect.items():
    c=cell(n,n,'swimlane;html=0;startSize=40;horizontal=1;rounded=0;fillColor=#eeeeee;swimlaneFillColor=#ffffff;strokeColor=#333333;fontFamily=Arial;fontSize=16;fontStyle=1;collapsible=0;')
    geom(c,x,y,w,h)
    for i,(key,text) in enumerate(attrs[n]):
        c=cell(n+'_row_'+str(i),(key+'   ' if key else '')+text,'text;html=0;align=left;verticalAlign=middle;whiteSpace=wrap;overflow=hidden;spacingLeft=10;fontFamily=Arial;fontSize=14;strokeColor=none;fillColor=none;',n)
        geom(c,0,40+i*20,w,20)
markers={'||':'ERmandOne','|o':'ERzeroToOne','o|':'ERzeroToOne','o{':'ERzeroToMany','|{':'ERoneToMany'}
for index,pts in routes:
    a,b,left,right,dashed=edges[index];ax,ay,aw,ah=rect[a];bx,by,bw,bh=rect[b]
    sx,sy=pts[0];tx,ty=pts[-1]
    style=f'edgeStyle=none;rounded=0;html=0;strokeColor=#333333;strokeWidth=1.2;jumpStyle=arc;jumpSize=12;startArrow={markers[left]};endArrow={markers[right]};startSize=16;endSize=16;startFill=0;endFill=0;dashed={int(dashed)};exitX={(sx-ax)/aw};exitY={(sy-ay)/ah};exitPerimeter=0;entryX={(tx-bx)/bw};entryY={(ty-by)/bh};entryPerimeter=0;'
    c=cell('edge_'+str(index),'',style,vertex=False);c.set('source',a);c.set('target',b)
    g=E.SubElement(c,'mxGeometry',relative='1',attrib={'as':'geometry'})
    arr=E.SubElement(g,'Array',attrib={'as':'points'})
    for x,y in pts[1:-1]:E.SubElement(arr,'mxPoint',x=str(x),y=str(y))
E.indent(mxfile)
E.ElementTree(mxfile).write(OUT/'ERD_43_bang_ro_duong.drawio',encoding='utf-8',xml_declaration=True)
(OUT/'routing.json').write_text(json.dumps({'rects':rect,'routes':routes,'edges':edges,'width':WIDTH,'height':HEIGHT},ensure_ascii=False),encoding='utf-8')

# Geometry-equivalent SVG for internal visual checking. The editable deliverable is draw.io.
svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}">',f'<rect width="{WIDTH}" height="{HEIGHT}" fill="white"/>']
for index,pts in routes:
    dash=' stroke-dasharray="7 5"' if edges[index][4] else ''
    svg.append('<polyline points="'+' '.join(f'{x},{y}' for x,y in pts)+'" fill="none" stroke="#444" stroke-width="1.5"'+dash+'/>')
for n,(x,y,w,h) in rect.items():
    svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="white" stroke="#333"/>')
    svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="40" fill="#eee" stroke="#333"/>')
    svg.append(f'<text x="{x+w/2}" y="{y+26}" text-anchor="middle" font-family="Arial" font-size="16" font-weight="bold">{n}</text>')
    for i,(key,text) in enumerate(attrs[n]):
        svg.append(f'<text x="{x+10}" y="{y+56+i*20}" font-family="Arial" font-size="13">{escape(key+"   "+text)}</text>')
svg.append('</svg>')
(OUT/'routing-preview.svg').write_text('\n'.join(svg),encoding='utf-8')
print(f'COMPLETE: 43 tables, 55 editable connections, zero shared line segments, {WIDTH} x {HEIGHT}',flush=True)
