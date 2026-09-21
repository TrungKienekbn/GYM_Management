from pathlib import Path
p=Path("tmp/erd_grouped_routing.py")
s=p.read_text(encoding="utf-8").replace('used=[];tables=set()','used=[];tables=set(); group_names=[]')
s=s.replace('tables.update(names)','tables.update(names)\n group_names.append(sorted(names))')
s=s.replace("for i in range(1,len(groups)):lines.append(f'G{i} -[hidden]down- G{i+1}')","for i in range(1,len(groups)):\n for a in group_names[i-1]:\n  for c in group_names[i]:lines.append(f'g{i}_{a} -[hidden]down- g{i+1}_{c}')")
p.write_text(s,encoding="utf-8")
