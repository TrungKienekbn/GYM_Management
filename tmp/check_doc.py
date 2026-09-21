from docx import Document
import re, zipfile, os
p=r'C:\Users\anh15\Downloads\SD-41_DATNSP26_Hoan_Thanh.docx'; d=Document(p)
for i,p in enumerate(d.paragraphs):
 t=' '.join(p.text.split())
 if any(x in t.lower() for x in ['erd','use case','activity','43 bảng','43 bảng','199 ca','gym pro','gymify','khóa ngoại','foreign','quan hệ']): print(f'{i}: {t[:500]}')
print('inline_shapes',len(d.inline_shapes))
with zipfile.ZipFile(p) as z:
 imgs=[x for x in z.namelist() if x.startswith('word/media/')]
 print('images',len(imgs),imgs[:10])
