# Hình bổ sung Phụ lục A

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
