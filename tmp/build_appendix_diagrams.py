from pathlib import Path
from html import escape
import math, json, shutil, zipfile

OUT=Path('output/report/use-case-activity-7-17-7-19')
OUT.mkdir(parents=True,exist_ok=True)

class SVG:
    def __init__(self,w,h,title):
        self.w,self.h=w,h
        self.parts=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">', '<rect width="100%" height="100%" fill="white"/>']
        self.text(w/2,30,title,23,True)
    def text(self,x,y,t,size=18,bold=False):
        lines=t.split('\n')
        for i,line in enumerate(lines):
            yy=y+(i-(len(lines)-1)/2)*size*1.25
            self.parts.append(f'<text x="{x}" y="{yy}" text-anchor="middle" dominant-baseline="middle" font-family="Arial, sans-serif" font-size="{size}" font-weight="{700 if bold else 400}" fill="#111">{escape(line)}</text>')
    def rect(self,x,y,w,h,fill='white',r=0):
        self.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="#222" stroke-width="1.3"/>')
    def ellipse(self,x,y,w,h,t):
        self.parts.append(f'<ellipse cx="{x}" cy="{y}" rx="{w/2}" ry="{h/2}" fill="white" stroke="#222" stroke-width="1.2"/>')
        self.text(x,y,t,19)
    def path(self,pts,arrow=True,dash=False):
        p=' '.join(f'{x},{y}' for x,y in pts)
        self.parts.append(f'<polyline points="{p}" fill="none" stroke="#222" stroke-width="1.4"'+(' stroke-dasharray="8 6"' if dash else '')+'/>')
        if arrow:
            x,y=pts[-1];a,b=pts[-2];angle=math.atan2(y-b,x-a)
            l=10
            q=[(x-l*math.cos(angle-.42),y-l*math.sin(angle-.42)),(x,y),(x-l*math.cos(angle+.42),y-l*math.sin(angle+.42))]
            self.path(q,False)
    def actor(self,x,y,label):
        self.parts.append(f'<circle cx="{x}" cy="{y}" r="14" fill="#f4f4f4" stroke="#222"/>')
        for p in [[(x,y+14),(x,y+62)],[(x-25,y+30),(x+25,y+30)],[(x,y+62),(x-25,y+93)],[(x,y+62),(x+25,y+93)]]:
            self.path(p,False)
        self.text(x,y+117,label,18)
    def circle(self,x,y,final=False):
        if final:self.parts.append(f'<circle cx="{x}" cy="{y}" r="11" fill="white" stroke="#222"/>')
        self.parts.append(f'<circle cx="{x}" cy="{y}" r="7" fill="#222"/>')
    def node(self,x,y,t,w=300,h=54):
        self.rect(x-w/2,y-h/2,w,h,r=12);self.text(x,y,t,17)
    def decision(self,x,y,t,w=260,h=62):
        self.parts.append(f'<polygon points="{x},{y-h/2} {x+w/2},{y} {x},{y+h/2} {x-w/2},{y}" fill="white" stroke="#222" stroke-width="1.2"/>')
        self.text(x,y,t,16)
    def lanes(self,labels):
        width=(self.w-60)/len(labels)
        self.parts.append(f'<rect x="30" y="66" width="{self.w-60}" height="36" fill="#f0f0f0"/>')
        for i,label in enumerate(labels):
            x=30+i*width;self.path([(x,66),(x,self.h-25)],False)
            self.text(x+width/2,84,label,21,True)
        self.path([(self.w-30,66),(self.w-30,self.h-25)],False)
    def save(self,name):
        (OUT/(name+'.svg')).write_text('\n'.join(self.parts+['</svg>']),encoding='utf8')

# Group diagrams retain all UC identifiers from the supplementary catalogue.
s=SVG(1400,1100,'7.17 - Use Case Mua hàng trực tuyến')
s.rect(205,150,990,905,r=3);s.text(700,180,'HỆ THỐNG GYMIFY',25,True)
shared=[(33,'Xem và tìm kiếm\nsản phẩm'),(34,'Xem chi tiết và\nchọn phân loại'),(35,'Quản lý giỏ hàng'),(36,'Quản lý sản phẩm\nyêu thích'),(37,'Xem sản phẩm\nđã xem gần đây'),(38,'So sánh sản phẩm'),(40,'Áp dụng mã giảm giá'),(43,'Theo dõi và\ntra cứu đơn hàng')]
personal=[(39,'Quản lý địa chỉ\nnhận hàng'),(41,'Đặt hàng trực tuyến'),(42,'Thanh toán\nđơn trực tuyến'),(44,'Hủy đơn hàng'),(45,'Đánh giá sản phẩm\nsau mua')]
s.actor(80,240,'Khách vãng lai');s.actor(80,715,'Người dùng')
s.actor(1310,420,'Người dùng');s.actor(975,60,'')
s.text(1120,120,'Cổng thanh toán',18)
for i,(code,label) in enumerate(shared):
    y=255+i*101
    s.path([(105,270),(245,y),(275,y)],False)
    s.path([(105,745),(235,y+12),(278,y+12)],False)
    s.ellipse(450,y,350,86,f'UC-{code}\n{label}')
for i,(code,label) in enumerate(personal):
    y=265+i*160
    s.path([(1285,450),(1180,y),(1130,y)],False)
    s.ellipse(950,y,360,82,f'UC-{code}\n{label}')
s.path([(1000,90),(1170,90),(1170,585),(1130,585)],False)
# Voucher is an optional extension of placing an order.
s.path([(625,861),(695,861),(695,425),(770,425)],True,True)
s.parts.append('<rect x="645" y="632" width="100" height="65" fill="white"/>')
s.text(695,650,'«extend»',17);s.text(695,683,'Khi nhập mã',15)
for x,y,label in [(80,357,'Khách vãng lai'),(80,832,'Người dùng'),(1310,537,'Người dùng')]:
    s.parts.append(f'<rect x="{x-78}" y="{y-14}" width="156" height="28" fill="white"/>')
    s.text(x,y,label,18)
s.text(700,1080,'Người dùng được vẽ ở hai vị trí để giảm giao cắt đường nối.',16)
s.save('7_17_Use_Case_Mua_hang')

s=SVG(1400,1000,'7.18 - Use Case Quản trị và vận hành cửa hàng')
s.rect(220,170,950,780,r=3);s.text(695,200,'HỆ THỐNG GYMIFY',25,True)
s.actor(100,380,'Quản trị viên');s.actor(1300,510,'Nhân viên')
admin=[(46,'Quản lý sản phẩm\ncửa hàng'),(47,'Quản lý thuộc tính\nvà biến thể'),(48,'Quản lý mã giảm giá'),(51,'Xem báo cáo\ncửa hàng'),(52,'Theo dõi tồn thấp\nvà nhật ký kho')]
for i,(code,label) in enumerate(admin):
    y=285+130*i
    s.path([(125,410),(245,y),(280,y)],False)
    s.ellipse(450,y,340,82,f'UC-{code}\n{label}')
for code,label,y in [(49,'Xử lý đơn hàng\ntrực tuyến',340),(50,'Bán hàng tại quầy',575),(40,'Áp dụng mã giảm giá',810)]:
    s.path([(1275,540),(1150,y),(1110,y)],False)
    s.path([(125,410),(190,225),(700,225),(700,y),(770,y)],False)
    s.ellipse(940,y,340,82,f'UC-{code}\n{label}')
s.path([(940,769),(940,616)],True,True)
s.text(1010,690,'«extend»',18);s.text(1010,719,'Khi nhập mã',16)
s.save('7_18_Use_Case_Quan_tri')

s=SVG(1400,740,'7.19 - Use Case Phân công và thực hiện ca làm việc')
s.rect(35,240,1330,465,r=3);s.text(700,270,'HỆ THỐNG GYMIFY',25,True)
s.actor(220,70,'Quản trị viên');s.actor(1160,70,'Nhân viên')
for x,label,code in [(260,'Phân công và xem\nlịch làm việc',53),(710,'Vào ca làm việc',54),(1160,'Kết ca và\nđối soát tiền',55)]:
    s.path([(220,200),(220,315),(x,315),(x,350)],False)
    if code!=53:s.path([(1160,200),(1160,330),(x,330),(x,350)],False)
    s.ellipse(x,395,360,90,f'UC-{code}\n{label}')
for x,label in [(260,'Kiểm tra nhân viên\nvà thời gian ca'),(710,'Kiểm tra quyền\nvà trạng thái ca'),(1160,'Tổng hợp doanh thu\nvà tính chênh lệch')]:
    s.path([(x,440),(x,560)],True,True);s.text(x+68,500,'«include»',18)
    s.ellipse(x,600,350,80,label)
s.path([(1040,433),(840,573)],True,True);s.text(995,525,'«include»',17)
s.save('7_19_Use_Case_Ca_lam')

# Activity 7.17: the primary checkout flow. Side features remain separate goals.
s=SVG(1100,1700,'7.17 - Activity Diagram Mua hàng trực tuyến')
s.lanes(['Người dùng','Hệ thống'])
L,R=290,810
s.circle(L,130);s.path([(L,137),(L,158)])
s.node(L,185,'Xem sản phẩm, chọn phân loại\nvà cập nhật giỏ hàng',350)
s.path([(L,212),(L,232),(R,232),(R,253)])
s.node(R,280,'Hiển thị giỏ hàng và giá\ncủa sản phẩm / biến thể',360)
s.path([(R,307),(R,327),(L,327),(L,348)])
s.node(L,375,'Đăng nhập; nhập người nhận,\nđịa chỉ, phương thức thanh toán',380)
s.path([(L,402),(L,404)])
s.decision(L,414,'',20,20)
s.path([(L,424),(L,425)]);s.node(L,452,'Nhập mã giảm giá nếu có\nvà xác nhận đặt hàng',350)
s.path([(L,479),(L,500),(R,500),(R,525)])
s.node(R,555,'Kiểm tra giỏ, địa chỉ, tồn kho\nvà mã giảm giá; tính tổng tiền',380,60)
s.path([(R,585),(R,609)]);s.decision(R,640,'Dữ liệu hợp lệ?',250)
s.path([(685,640),(L,640),(L,673)]);s.text(525,622,'Không',16)
s.node(L,700,'Xem lỗi và chỉnh sửa\nthông tin đặt hàng',350)
s.path([(115,700),(65,700),(65,414),(280,414)])
s.path([(R,671),(R,703)]);s.text(R+30,688,'Có',16)
s.node(R,735,'Tạo đơn, lưu chi tiết, trừ tồn\nvà ghi nhật ký; xóa giỏ hàng',390,64)
s.path([(R,767),(R,794)]);s.decision(R,825,'Thanh toán COD?',270)
s.path([(675,825),(L,825),(L,863)]);s.text(530,808,'Có',16)
s.node(L,890,'Nhận mã đơn đã xác nhận\nThanh toán khi nhận hàng',350)
s.path([(R,856),(R,884)]);s.text(R+38,870,'Không',16)
s.node(R,913,'Hiển thị QR / cổng thanh toán\nĐơn chờ thanh toán',370,58)
s.path([(R,942),(R,962),(L,962),(L,983)])
s.node(L,1010,'Thực hiện thanh toán',340)
s.path([(L,1037),(L,1060),(R,1060),(R,1081)])
s.node(R,1110,'Kiểm tra kết quả thanh toán\ntừ nguồn xác nhận hợp lệ',380,58)
s.path([(R,1139),(R,1164)]);s.decision(R,1195,'Thanh toán thành công?',310)
s.path([(655,1195),(L,1195),(L,1233)]);s.text(520,1177,'Không',16)
s.node(L,1265,'Xem trạng thái chờ / thất bại;\nthử lại khi đơn còn hiệu lực',380,64)
s.path([(R,1226),(R,1253)]);s.text(R+30,1240,'Có',16)
s.node(R,1285,'Cập nhật đã thanh toán\nvà ghi lịch sử đơn',360,64)
s.path([(R,1317),(R,1430),(L+14,1430)])
s.path([(L,1297),(L,1416)])
s.path([(115,890),(65,890),(65,1430),(L-14,1430)])
s.decision(L,1430,'',28,28)
s.path([(L,1444),(L,1463)])
s.node(L,1490,'Xem và theo dõi đơn hàng',350)
s.path([(L,1517),(L,1570)]);s.circle(L,1581,True)
s.save('7_17_Activity_Mua_hang')

# Activity 7.18 is a dispatch of independent tasks, not a mandatory sequence.
s=SVG(1400,1740,'7.18 - Activity Diagram Quản trị và vận hành cửa hàng')
s.lanes(['Quản trị viên / Nhân viên','Hệ thống'])
L,R=365,1035
s.circle(L,130);s.path([(L,137),(L,159)])
s.node(L,190,'Đăng nhập và chọn chức năng\ncửa hàng cần thực hiện',390,62)
s.path([(L,221),(L,245),(R,245),(R,269)])
s.node(R,300,'Kiểm tra quyền theo chức năng',380,62)
s.path([(R,331),(R,354)]);s.decision(R,385,'Có quyền truy cập?',290)
s.path([(890,385),(L,385),(L,423)]);s.text(680,367,'Không',16)
s.node(L,450,'Nhận thông báo từ chối',340)
s.path([(L,477),(L,510)]);s.circle(L,522,True)
s.path([(R,416),(R,450)]);s.text(R+32,433,'Có',16)
s.decision(R,485,'Chức năng được chọn?',320,70)
s.path([(875,485),(760,485),(760,550),(L,550),(L,576)])
s.text(790,552,'Danh mục',16)
s.node(L,610,'ADMIN: nhập / sửa sản phẩm,\nbiến thể hoặc mã giảm giá',450,68)
s.path([(590,610),(820,610)])
s.node(R,610,'Kiểm tra dữ liệu; lưu thay đổi\nhoặc trả lỗi để chỉnh sửa',430,68)
s.path([(1195,485),(1325,485),(1325,780),(1250,780)])
s.text(1285,703,'Đơn online',16)
s.node(R,780,'Hiển thị đơn và các thao tác\nđược phép theo trạng thái',430,68)
s.path([(820,780),(590,780)])
s.node(L,780,'STAFF / ADMIN: chọn đơn\nvà thao tác xử lý',450,68)
s.path([(L,814),(L,844),(R,844),(R,866)])
s.node(R,900,'Kiểm tra chuyển trạng thái;\nlưu đơn và lịch sử hoặc trả lỗi',430,68)
s.path([(R,520),(R,545),(710,545),(710,1050),(590,1050)])
s.text(750,980,'Bán tại quầy',16)
s.node(L,1050,'STAFF / ADMIN: chọn khách,\nhàng, số lượng và cách thanh toán',460,68)
s.path([(L,1084),(L,1110),(R,1110),(R,1133)])
s.node(R,1170,'Kiểm tra giá, tồn và voucher;\nhợp lệ: lưu đơn POS đã thanh toán,\ntrừ tồn và ghi nhật ký; sai: trả lỗi',480,90)
s.path([(1195,485),(1350,485),(1350,1340),(1260,1340)])
s.text(1275,1280,'Báo cáo / kho',16)
s.node(R,1340,'ADMIN: tổng hợp doanh thu,\ntồn thấp và lịch sử kho',450,68)
# Each independent operation joins the result display through a right-side rail.
for y,edge in [(610,644),(900,934),(1170,1215),(1340,1374)]:
    s.path([(R,edge),(R,edge+18),(1300,edge+18),(1300,1420),(R+14,1420)])
s.decision(R,1420,'',28,28)
s.path([(R,1434),(R,1446)])
s.node(R,1480,'Hiển thị kết quả hoặc lỗi\ncủa thao tác vừa chọn',450,68)
s.path([(R,1514),(R,1540),(L,1540),(L,1570)])
s.node(L,1600,'Xem kết quả xử lý',350,60)
s.path([(L,1630),(L,1660)]);s.circle(L,1672,True)
s.save('7_18_Activity_Quan_tri')

s=SVG(1200,1770,'7.19 - Activity Diagram Phân công và thực hiện ca làm việc')
s.lanes(['Quản trị viên / Nhân viên','Hệ thống'])
L,R=315,885
s.circle(L,120);s.path([(L,127),(L,135)])
s.decision(L,145,'',20,20);s.path([(L,155),(L,160)])
s.node(L,190,'ADMIN: chọn nhân viên, ngày\nvà giờ làm; lưu phân công',420,60)
s.path([(L,220),(L,240),(R,240),(R,263)])
s.node(R,295,'Kiểm tra nhân viên và giờ làm;\nlưu lịch nếu hợp lệ, sai thì báo lỗi',440,64)
s.path([(R,327),(R,353)]);s.decision(R,385,'Đã tạo ca hợp lệ?',280,64)
s.path([(745,385),(L,385),(L,424)]);s.text(600,368,'Không',16)
s.node(L,455,'ADMIN: chỉnh sửa phân công',400,62)
s.path([(115,455),(70,455),(70,145),(L-10,145)])
s.path([(R,417),(R,500),(L,500),(L,528)]);s.text(R+30,448,'Có',16)
s.node(L,560,'Người được phân công / ADMIN:\nchọn ca, nhập tiền đầu ca, vào ca',440,64)
s.path([(L,592),(L,618),(R,618),(R,641)])
s.node(R,675,'Kiểm tra quyền với ca\nvà ca chưa được vào',400,68)
s.path([(R,709),(R,733)]);s.decision(R,765,'Đủ điều kiện vào ca?',300,64)
s.path([(735,765),(L,765),(L,803)]);s.text(600,748,'Không',16)
s.node(L,830,'Nhận thông báo từ chối',360)
s.path([(L,857),(L,886)]);s.circle(L,898,True)
s.path([(R,797),(R,823)]);s.text(R+30,810,'Có',16)
s.node(R,855,'Ghi giờ vào, tiền đầu ca\nvà trạng thái đang làm',420,64)
s.path([(R,887),(R,925),(L,925),(L,948)])
s.node(L,980,'Thực hiện công việc trong ca',420,64)
s.path([(L,1012),(L,1043)])
s.node(L,1075,'Nhập tiền mặt kiểm đếm,\nghi chú bàn giao; yêu cầu kết ca',440,64)
s.path([(L,1107),(L,1130),(R,1130),(R,1153)])
s.node(R,1185,'Kiểm tra quyền: đã vào ca\nvà chưa kết ca',420,64)
s.path([(R,1217),(R,1243)]);s.decision(R,1275,'Đủ điều kiện kết ca?',300,64)
s.path([(735,1275),(L,1275),(L,1310)]);s.text(600,1258,'Không',16)
s.node(L,1340,'Nhận thông báo từ chối',360,60)
s.path([(L,1370),(L,1410)]);s.circle(L,1422,True)
s.path([(R,1307),(R,1333)]);s.text(R+30,1320,'Có',16)
s.node(R,1370,'Tổng hợp doanh thu POS theo\nnhân viên và thời gian ca',430,74)
s.path([(R,1407),(R,1433)])
s.node(R,1470,'Tiền dự kiến = đầu ca + thu tiền mặt\nChênh lệch = kiểm đếm − dự kiến',470,74)
s.path([(R,1507),(R,1533)])
s.node(R,1570,'Lưu kết ca, chênh lệch và bàn giao\nChuyển trạng thái hoàn thành',450,74)
s.path([(R,1607),(R,1630),(L,1630),(L,1653)])
s.node(L,1680,'Xem kết quả đối soát',400)
s.path([(L,1707),(L,1720)]);s.circle(L,1732,True)
s.save('7_19_Activity_Ca_lam')

guide='''# Hình bổ sung Phụ lục A

Thêm ba mục 7.17, 7.18, 7.19 sau mục 7.16 và trước Phần 8.
Mỗi mục: tiêu đề → Activity Diagram → bảng đặc tả nhóm chức năng → Use Case Diagram,
theo thứ tự của tài liệu tham chiếu. Tổng cộng sáu hình mới, không phải ba hình.

| Mục | Nhóm chức năng | Use case chi tiết |
|---|---|---|
| 7.17 | Mua hàng trực tuyến | UC-33 đến UC-45 |
| 7.18 | Quản trị và vận hành cửa hàng | UC-46 đến UC-52; dùng lại UC-40 |
| 7.19 | Phân công và thực hiện ca làm việc | UC-53 đến UC-55 |

7.17–7.19 là số mục trong báo cáo, không phải đổi mã use case thành UC17–UC19.
Ba mục là nhóm chức năng tổng hợp; 23 đặc tả chi tiết trong Bo_sung_Use_Case vẫn là
nguồn mô tả từng chức năng. Không thay 23 đặc tả bằng ba sơ đồ tổng hợp.

## Cách dùng

- Chèn sáu file PNG vào Word; giữ tỷ lệ ảnh. SVG là bản vector có thể sửa và phóng lớn.
- Use Case: dùng trang ngang nếu cần để chữ dễ đọc. Activity: dùng chiều rộng trang dọc.
- Các ảnh đen trắng, font Arial, khung hệ thống và cột tác nhân/hệ thống theo mẫu.
- SVG là bản nguồn của đúng sáu hình PNG trong gói này.

## Phạm vi các activity

- 7.17 thể hiện luồng mua hàng chính từ chọn hàng đến theo dõi đơn, có nhánh COD/online
  và dữ liệu/thanh toán không hợp lệ. Yêu thích, so sánh, quản lý địa chỉ, hủy đơn và
  đánh giá là chức năng độc lập/điều kiện riêng, thể hiện trong Use Case và đặc tả chi tiết.
- 7.18 thể hiện chọn một trong các tác vụ danh mục, xử lý đơn, bán tại quầy, báo cáo/kho.
  Đây là các nhánh lựa chọn, không phải bắt buộc thực hiện tuần tự mọi tác vụ.
- 7.19 thể hiện vòng đời phân công → vào ca → kết ca; mỗi thao tác thực tế là một yêu cầu riêng.
  Một ca đã được phân công có thể bắt đầu ở bước vào ca. Sai điều kiện bị từ chối;
  chênh lệch tiền được lưu để đối soát, không tự sửa đơn hàng.

## Quan hệ UML

Đường liền nối tác nhân với chức năng. Nét đứt «include» chỉ tác vụ luôn được thực hiện;
«extend» đi từ áp dụng mã giảm giá đến đặt hàng/bán tại quầy khi có nhập mã.
Không gắn «include» vào tất cả các chức năng chỉ để giống hình cũ: yêu thích, hủy đơn,
đánh giá và thanh toán online không phải bước bắt buộc của mọi lần mua hàng.

Tài liệu nguồn: SD-41_DATNSP26_chinhsua (11-41_18092006).docx, Phần 7.
Đối chiếu luồng: ShopService, PosService, WorkShiftService và Bo_sung_Use_Case.md.
'''
(OUT/'Huong_dan_ghep_Phu_luc_A.md').write_text(guide,encoding='utf8')
print('Created 6 SVG diagrams and insertion guide in',OUT)

