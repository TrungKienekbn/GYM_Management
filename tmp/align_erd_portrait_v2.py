from pathlib import Path
import xml.etree.ElementTree as ET
import re

folder = Path('output/report/erd-doc-da-tach-duong')
source = folder / 'ERD_43_bang_ro_duong.drawio'
target = folder / 'ERD_43_bang_doc_da_tach_duong.drawio'
tree = ET.parse(source)
root = tree.find('.//root')
before_edges = [ET.tostring(c) for c in root.findall('mxCell') if c.get('edge') == '1']
count = 0
for cell in list(root.findall('mxCell')):
    if '_row_' not in cell.get('id', ''):
        continue
    name, datatype = cell.get('value').rsplit(' : ', 1)
    if datatype.endswith(' [UK]'):
        name += ' [UK]'
        datatype = datatype[:-5]
    cell.set('value', name)
    geometry = cell.find('mxGeometry')
    width = float(geometry.get('width'))
    left_width = width - 160
    geometry.set('width', str(left_width))
    cell.set('style', cell.get('style').replace('whiteSpace=wrap;', 'whiteSpace=nowrap;'))
    type_cell = ET.SubElement(root, 'mxCell', {
        'id': cell.get('id') + '_type', 'value': datatype,
        'style': 'text;html=0;align=right;verticalAlign=middle;whiteSpace=nowrap;overflow=hidden;spacingRight=10;fontFamily=Arial;fontSize=14;strokeColor=none;fillColor=none;',
        'parent': cell.get('parent'), 'vertex': '1',
    })
    ET.SubElement(type_cell, 'mxGeometry', {
        'x': str(left_width), 'y': geometry.get('y'),
        'width': '160', 'height': geometry.get('height'), 'as': 'geometry',
    })
    count += 1
assert count > 400
assert before_edges == [ET.tostring(c) for c in root.findall('mxCell') if c.get('edge') == '1']
assert len([c for c in root.findall('mxCell') if c.get('style', '').startswith('swimlane;')]) == 43
ET.indent(tree)
tree.write(target, encoding='utf-8', xml_declaration=True)
print(f'{target}: aligned {count} datatype cells to right; 43 tables and {len(before_edges)} connectors preserved.')
