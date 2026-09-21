from docx import Document
p=r'C:\Users\anh15\Downloads\SD-41_DATNSP26_Hoan_Thanh (1).docx';d=Document(p)
for ti,t in enumerate(d.tables):
 txt=' | '.join(' '.join(c.text.split()) for row in t.rows for c in row.cells)
 if any(k in txt.lower() for k in ['use case','khách vãng lai','staff','nhân viên','phân công và thực hiện','đăng ký tài khoản']): print('\nTABLE',ti,txt[:2500])
