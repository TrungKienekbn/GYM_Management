from pathlib import Path
import json, re

base = Path('output/report/schema-43')
data = json.loads((base/'schema_43_bang.json').read_text(encoding='utf-8'))
tables = data['tables']
fk = [(a,c,b,'id') for a,c,b in data['foreign_keys']]
logical = [(a,c,b,'id') for a,c,b in data['logical_links']]
logical.append(('shop_orders','voucher_code','vouchers','code'))
lines = ['// ERD 43 bang - DBML for dbdiagram.io',
         '// 39 FK hien co + 16 lien ket logic bo sung de ve so do.',
         '// Cac Ref logic KHONG co nghia la database hien tai da co constraint.',
         '// Khong dung ban nay de tao migration SQL ma chua ra soat cac Ref logic.', '']
enums = {}
for name,t in tables.items():
    for c in t['columns']:
        if c['type'].startswith('enum '):
            values = re.findall(r"'([^']*)'", c['type'])
            enum_name = name+'_'+c['name']+'_enum'
            enums[name,c['name']] = enum_name
            lines += ['Enum '+enum_name+' {'] + ['  "'+v+'"' for v in values] + ['}', '']
for name,t in tables.items():
    lines.append('Table '+name+' {')
    cols = sorted(t['columns'], key=lambda c: (not c['pk'], not any(a==name and f==c['name'] for a,f,b,r in fk+logical)))
    for c in cols:
        opts=[]
        if c['pk'] and len(t['pk'])==1: opts.append('pk')
        if not c['nullable']: opts.append('not null')
        if c['identity']: opts.append('increment')
        if c['unique']: opts.append('unique')
        if any(a==name and f==c['name'] for a,f,b,r in logical):
            opts.append("note: 'REF logic bo sung; chua co FK constraint trong schema nguon'")
        datatype=enums.get((name,c['name']), c['type'])
        lines.append('  '+c['name']+' '+datatype+(' ['+', '.join(opts)+']' if opts else ''))
    indexes=[]
    if len(t['pk'])>1: indexes.append('('+', '.join(t['pk'])+') [pk]')
    for unique in t['unique']:
        if len(unique)>1: indexes.append('('+', '.join(unique)+') [unique]')
    if indexes: lines += ['  indexes {']+['    '+s for s in indexes]+['  }']
    lines += ['}', '']
for label,refs in [('Khoa ngoai hien co',fk),('Lien ket logic bo sung cho so do',logical)]:
    lines += ['// '+label]
    for a,c,b,r in refs:
        assert c in {col['name'] for col in tables[a]['columns']}
        assert r in {col['name'] for col in tables[b]['columns']}
        one = [c] == tables[a]['pk'] or [c] in tables[a]['unique']
        lines.append(f'Ref: {b}.{r} - {a}.{c}' if one else f'Ref: {a}.{c} > {b}.{r}')
    lines.append('')
text='\n'.join(lines)
assert len(tables)==43 and len(fk)==39 and len(logical)==16
out=Path('output/report/dbdiagram-erd');out.mkdir(exist_ok=True)
for ext in ['dbml','txt']:
    (out/f'ERD_43_bang_dbdiagram.{ext}').write_text(text,encoding='utf-8')
print(f'Generated: {len(tables)} tables, {sum(len(t["columns"]) for t in tables.values())} columns, {len(fk)+len(logical)} refs.')
