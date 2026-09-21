from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

out = Path('output/report/plantuml-phu-luc-a')
out.mkdir(parents=True, exist_ok=True)
style = '''skinparam monochrome true
skinparam shadowing false
skinparam backgroundColor white
skinparam defaultFontName Arial
skinparam defaultFontSize 16
skinparam ArrowColor Black
skinparam activity {
  BackgroundColor White
  BorderColor Black
  DiamondBackgroundColor White
  DiamondBorderColor Black
}
skinparam swimlane {
  BorderColor Black
  TitleBackgroundColor WhiteSmoke
}
'''

groups = [('mua_hang', '7_17', 'Mua hàng trực tuyến'),
          ('quan_tri', '7_18', 'Quản trị và vận hành cửa hàng'),
          ('ca_lam', '7_19', 'Phân công và thực hiện ca làm việc')]
for key, number, title in groups:
    text = Path(f'output/report/schema-43/Use_Case_{key}.puml').read_text(encoding='utf-8-sig')
    text = text.replace('@startuml', f'@startuml\ntitle {number.replace("_", ".")} - Use Case {title}\n{style}', 1)
    text = text.replace('"USER"', '"Người dùng"').replace('"STAFF"', '"Nhân viên"').replace('"ADMIN"', '"Quản trị viên"')
    text = text.replace('"GYMIFY - Cửa hàng và nhân viên"', '"HỆ THỐNG GYMIFY"')
    if key == 'ca_lam':
        text = text.replace('\n}\n', '''
usecase "Kiểm tra nhân viên\\nvà thời gian ca" as CheckSchedule
usecase "Kiểm tra quyền\\nvà trạng thái ca" as CheckShift
usecase "Tổng hợp doanh thu\\nvà tính chênh lệch" as Reconcile
}
''', 1)
        text = text.replace('@enduml', '''UC53 ..> CheckSchedule : <<include>>
UC54 ..> CheckShift : <<include>>
UC55 ..> CheckShift : <<include>>
UC55 ..> Reconcile : <<include>>
@enduml''')
    (out / f'{number}_Use_Case_{key}.puml').write_text(text, encoding='utf-8')

activities = {}
activities['mua_hang'] = r'''|Người dùng|
start
:Xem sản phẩm và chọn phân loại;
:Thêm sản phẩm, cập nhật giỏ hàng;
|Hệ thống|
:Hiển thị giỏ hàng và giá sản phẩm;
|Người dùng|
:Đăng nhập để đặt hàng;
repeat
  :Nhập hoặc chỉnh sửa người nhận,\nsố điện thoại, địa chỉ và\nphương thức thanh toán;
  :Nhập mã giảm giá nếu có,\nXác nhận đặt hàng;
  |Hệ thống|
  :Kiểm tra giỏ, thông tin giao hàng,\ntồn kho và mã giảm giá,\nTính tổng tiền;
  if (Dữ liệu hợp lệ?) then (Có)
    :Chấp nhận yêu cầu đặt hàng;
  else (Không)
    :Hiển thị lỗi cần chỉnh sửa;
  endif
  |Người dùng|
repeat while (Còn lỗi cần chỉnh sửa?) is (Có) not (Không)
|Hệ thống|
:Tạo đơn và lưu chi tiết,\nTrừ tồn, ghi nhật ký,\nGhi nhận sử dụng voucher nếu có,\nXóa giỏ hàng;
if (Thanh toán COD?) then (Có)
  :Đặt trạng thái đơn đã xác nhận;
  |Người dùng|
  :Nhận mã đơn,\nThanh toán khi nhận hàng;
else (Không)
  |Hệ thống|
  :Đặt trạng thái chờ thanh toán,\nHiển thị QR hoặc cổng thanh toán;
  |Người dùng|
  :Thực hiện thanh toán;
  |Hệ thống|
  :Kiểm tra kết quả thanh toán\ntừ nguồn xác nhận hợp lệ;
  if (Thanh toán thành công?) then (Có)
    :Cập nhật đã thanh toán,\nGhi lịch sử đơn hàng;
  else (Không)
    :Hiển thị trạng thái chờ,\nthất bại hoặc hết hạn;
    |Người dùng|
    :Xem trạng thái, có thể thử lại\nkhi đơn còn hiệu lực;
  endif
endif
|Người dùng|
:Xem và theo dõi đơn hàng;
stop
'''

activities['quan_tri'] = r'''|Quản trị viên / Nhân viên|
start
:Đăng nhập;
:Chọn chức năng cửa hàng;
|Hệ thống|
:Kiểm tra quyền theo chức năng;
if (Có quyền truy cập?) then (Không)
  :Thông báo từ chối truy cập;
  stop
else (Có)
endif
if (Quản lý danh mục?) then (Có)
  |Quản trị viên / Nhân viên|
  :ADMIN nhập hoặc sửa sản phẩm,\nthuộc tính, biến thể hoặc voucher;
  |Hệ thống|
  :Kiểm tra dữ liệu;
  if (Dữ liệu hợp lệ?) then (Có)
    :Lưu thay đổi;
  else (Không)
    :Trả lỗi để chỉnh sửa;
  endif
elseif (Xử lý đơn trực tuyến?) then (Có)
  :Hiển thị đơn hàng và thao tác\nđược phép theo trạng thái;
  |Quản trị viên / Nhân viên|
  :STAFF / ADMIN chọn đơn\nvà thao tác xử lý;
  |Hệ thống|
  :Kiểm tra chuyển trạng thái;
  if (Thao tác hợp lệ?) then (Có)
    :Cập nhật đơn và ghi lịch sử;
  else (Không)
    :Trả lỗi trạng thái đơn;
  endif
elseif (Bán hàng tại quầy?) then (Có)
  |Quản trị viên / Nhân viên|
  :STAFF / ADMIN chọn khách,\nsản phẩm, phân loại, số lượng,\nNhập voucher nếu có;
  :Ghi nhận thanh toán tiền mặt\nhoặc chuyển khoản,\nXác nhận tạo đơn POS;
  |Hệ thống|
  :Kiểm tra khách, giá, tồn kho,\nsố lượng và mã giảm giá;
  if (Dữ liệu hợp lệ?) then (Có)
    :Lưu đơn POS đã thanh toán,\nTrừ tồn và ghi nhật ký;
  else (Không)
    :Trả lỗi để chỉnh sửa;
  endif
else (Báo cáo / kho)
  :Tổng hợp báo cáo doanh thu,\ntồn thấp hoặc nhật ký kho\ntheo yêu cầu của ADMIN;
endif
|Hệ thống|
:Hiển thị kết quả hoặc lỗi;
|Quản trị viên / Nhân viên|
:Xem kết quả xử lý;
stop
'''

activities['ca_lam'] = r'''|Quản trị viên / Nhân viên|
start
if (Đã có ca được phân công?) then (Chưa)
  repeat
    :ADMIN chọn nhân viên, ngày\nvà giờ làm, lưu phân công;
    |Hệ thống|
    :Kiểm tra nhân viên và thời gian ca;
    if (Thông tin hợp lệ?) then (Có)
      :Lưu lịch làm việc;
    else (Không)
      :Hiển thị lỗi phân công;
    endif
    |Quản trị viên / Nhân viên|
  repeat while (Cần sửa phân công?) is (Có) not (Không)
else (Có)
endif
:Người được phân công / ADMIN\nchọn ca và nhập tiền đầu ca,\nYêu cầu vào ca;
|Hệ thống|
:Kiểm tra quyền với ca\nvà ca chưa được vào;
if (Đủ điều kiện vào ca?) then (Không)
  :Thông báo từ chối vào ca;
  stop
else (Có)
  :Lưu thời điểm vào, tiền đầu ca,\nChuyển trạng thái đang làm;
endif
|Quản trị viên / Nhân viên|
:Thực hiện công việc trong ca;
:Nhập tiền mặt kiểm đếm\nvà ghi chú bàn giao,\nYêu cầu kết ca;
|Hệ thống|
:Kiểm tra quyền với ca,\nCa đã vào và chưa kết;
if (Đủ điều kiện kết ca?) then (Không)
  :Thông báo từ chối kết ca;
  stop
else (Có)
endif
:Tổng hợp doanh thu POS\ntheo nhân viên và thời gian ca;
:Tiền dự kiến = tiền đầu ca\n+ doanh thu POS tiền mặt;
:Chênh lệch = tiền kiểm đếm\n- tiền dự kiến;
:Lưu giờ kết ca, doanh thu,\nchênh lệch và ghi chú bàn giao,\nChuyển trạng thái hoàn thành;
|Quản trị viên / Nhân viên|
:Xem kết quả đối soát;
stop
'''


for key, number, title in groups:
    text = f'@startuml\ntitle {number.replace("_", ".")} - Activity Diagram {title}\n{style}\n' + activities[key] + '@enduml\n'
    (out/f'{number}_Activity_{key}.puml').write_text(text, encoding='utf-8')

files = sorted(out.glob('*.puml'))
assert len(files) == 6
for file in files:
    text=file.read_text(encoding='utf-8')
    assert text.count('@startuml') == text.count('@enduml') == 1
    assert text.count('\nif (') + text.count('\n  if (') + text.count('\n    if (') == text.count('endif')

with ZipFile(out.parent/'PlantUML_6_so_do_Phu_luc_A.zip','w',ZIP_DEFLATED) as z:
    for file in files:z.write(file,file.name)
print('Exported 6 UTF-8 PlantUML files; checked diagram delimiters and conditional blocks.')
