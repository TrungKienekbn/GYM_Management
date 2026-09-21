from pathlib import Path

src = Path('tmp/build_editable_erd.py').read_text(encoding='utf-8')
src = src.replace("OUT=Path('output/report/drawio-erd')", "OUT=Path('output/report/erd-doc-da-tach-duong')\nOUT.mkdir(exist_ok=True)")
src = src.replace("['roles','user_profiles','users','endurance_tests']", "['roles','notifications','chat_messages','service_ratings']")
src = src.replace("['notifications','chat_messages','progress_tracking','service_ratings']", "['memberships','invoices','user_profiles','endurance_tests']")
src = src.replace("['memberships','invoices','pet_profiles','user_cosmetic_ownership']", "['support_messages','support_sessions','users','progress_tracking']")
src = src.replace("['support_messages','support_sessions','work_shifts','customer_addresses']", "['work_shifts','customer_addresses','pet_profiles','user_cosmetic_ownership']")
src = src.replace('GAP=340', 'GAP=480').replace('y+=maxh+220', 'y+=maxh+280')
src = src.replace('crossing=18 if q in through else 0', '''crossing=50 if q in through else 0
            # Keep parallel connectors at least two grid lanes apart when possible.
            neighbours = [(q[0],q[1]-1),(q[0],q[1]+1)] if nd==0 else [(q[0]-1,q[1]),(q[0]+1,q[1])]
            crossing += 14 * sum(nd in through.get(v,set()) for v in neighbours)''')
src = src.replace('jumpSize=8;', 'jumpSize=12;')
src = src.replace('fontSize=13;', 'fontSize=14;')
Path('tmp/build_erd_portrait_v2.py').write_text(src, encoding='utf-8')
align=Path('tmp/align_erd_types.py').read_text(encoding='utf-8')
align=align.replace("Path('output/report/drawio-erd')", "Path('output/report/erd-doc-da-tach-duong')")
align=align.replace('ERD_43_bang_kieu_du_lieu_ben_phai.drawio','ERD_43_bang_doc_da_tach_duong.drawio').replace('fontSize=13;', 'fontSize=14;')
Path('tmp/align_erd_portrait_v2.py').write_text(align, encoding='utf-8')
