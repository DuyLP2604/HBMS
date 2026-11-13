# HBMS – Vô hiệu hóa và kích hoạt lại tài khoản nhân viên

1. Với database đang dùng, chọn **QLKS** trong SSMS và chạy một lần file `db/HBMS_Staff_Account_Status(V).sql` trước khi cập nhật ứng dụng. Script tự thêm các cột còn thiếu và giữ giá trị của các cột đã có. Có thể chạy lại script.
2. Với database mới, file `db/DB_v6_merged_transaction(I).sql` đã có cả hai cột. File khởi tạo sẽ xóa và tạo lại database QLKS nếu database đã tồn tại.
3. Clean and Build dự án, triển khai lại trên Tomcat, rồi đăng nhập Admin. Mở **Employee List**, chọn **Deactivate** hoặc **Reactivate** và xác nhận. Trạng thái trên danh sách đổi thành **Deactivated** hoặc **Active**.
4. Sau khi kích hoạt lại, nhân viên đăng nhập lại bằng tài khoản và mật khẩu hiện có. Nếu tài khoản vẫn đang chờ đổi mật khẩu lần đầu, nhân viên tiếp tục quy trình đổi mật khẩu tạm. Hồ sơ và các bản ghi nghiệp vụ vẫn được lưu. Các phiên đăng nhập và yêu cầu đặt lại mật khẩu được tạo trước khi trạng thái tài khoản thay đổi sẽ hết hiệu lực.

# HBMS – Patch loại bỏ bảng INVOICE_TRANSACTION

## 1. Mục tiêu và kết quả

Patch được thực hiện trên mã nguồn trong `HBMS.zip` đã cung cấp ngày 12/11/2026.
Project không còn cần bảng nối `INVOICE_TRANSACTION` khi khởi tạo database, ghi nhận
thanh toán, checkout hoặc sao lưu/phục hồi Excel.

Đối với database đang chạy, cần thực thi `db/HBMS_Drop_Invoice_Transaction.sql`
để cập nhật các thủ tục, view và xóa bảng thật trong SQL Server. Việc sửa file SQL
trong project không tự thay đổi database trên máy của bạn. Chưa có kết nối tới
SQL Server của bạn trong lần chỉnh sửa này.

## 2. Quan hệ dữ liệu sau thay đổi

Mỗi hóa đơn thuộc một booking. Các khoản thanh toán của hóa đơn được lấy từ
`PAYMENT.BookingID = INVOICE.BookingID`, sau đó lấy giao dịch qua
`PAYMENT.TransactionID = TRANSACTION.TransactionID`.

| Dữ liệu cần lấy | Quan hệ sử dụng |
| --- | --- |
| Booking của hóa đơn | `INVOICE.BookingID` → `BOOKING.BookingID` |
| Các lần thanh toán | `INVOICE.BookingID` → `PAYMENT.BookingID` |
| Giao dịch của mỗi lần thanh toán | `PAYMENT.TransactionID` → `TRANSACTION.TransactionID` |
| Phương thức thanh toán | `PAYMENT.MethodID` → `PAYMENTMETHOD.MethodID` |

View `V_INVOICE_TRANSACTION_DETAILS` được giữ tên và giữ đủ 17 cột để các truy vấn
đang dùng view tiếp tục hoạt động. Tên view còn chữ `TRANSACTION` vì nó hiển thị
giao dịch; view này không còn đọc bảng `INVOICE_TRANSACTION`.

View chỉ lấy `PAYMENT.Status IN ('PAID', 'REFUNDED')`. Các khoản `PENDING` và `FAILED`
không xuất hiện. `REFUNDED` được giữ để truy vết khoản thu trước khi hoàn tiền;
không dùng tổng view này làm số tiền thu ròng sau hoàn tiền. Các dòng giao dịch
hoàn tiền tiếp tục được quản lý qua `BOOKING_REFUND` và `TRANSACTION` như trước.

## 3. Chi tiết các file đã thay đổi

| File | Thay đổi |
| --- | --- |
| `db/DB_v6_merged_transaction.sql` | Bỏ tạo bảng nối, PK/FK/index riêng, trigger kiểm tra, dữ liệu mẫu và lệnh DENY của bảng nối. Bỏ đoạn INSERT vào bảng nối ở cả hai định nghĩa `SP_RECORD_PAYMENT` và ở `SP_CHECKOUT_WITH_INVOICE`. Viết lại view và kiểm tra dữ liệu mẫu theo quan hệ trực tiếp. |
| `db/HBMS_Drop_Invoice_Transaction.sql` | Thêm migration dành cho database hiện có; cập nhật hai thủ tục và view, bỏ trigger rồi xóa bảng trong một transaction. |
| `db/tests/HBMS_Procedure_Header_Regression.sql` | Thêm 12 ca hồi quy SQL Server cho header có chú thích dài, chú thích lồng nhau, `PROC`, `CREATE OR ALTER` và dữ liệu không hợp lệ. Fixture chỉ nằm trong `tempdb` và được rollback. |
| `db/tests/HBMS_Drop_Invoice_Transaction_Verify.sql` | Thêm script kiểm tra sau migration: bảng/trigger đã mất, module không còn phụ thuộc, view không thiếu/thừa/lặp khoản thanh toán. |
| `src/main/java/dao/DAOFramework.java` | Bỏ tên bảng cũ khỏi `PROCEDURE_TABLES`; giữ nguyên cơ chế bắt buộc các bảng nghiệp vụ còn lại ghi qua thủ tục. |
| `src/main/java/service/DatabaseBackupService.java` | Bỏ bảng cũ khỏi danh sách sao lưu và phục hồi Excel. Ghi chú rõ sheet thừa trong backup cũ được bỏ qua. |
| `src/main/java/entity/Transaction.java` | Cập nhật mô tả quan hệ: hóa đơn tra giao dịch thông qua booking và các khoản thanh toán. Không đổi mapping hay hành vi entity. |
| `src/test/python/test_invoice_payments.py` | Thêm 7 ca kiểm tra quan hệ và bộ lọc của SELECT trong view bằng dữ liệu giả lập. |
| `project_structure.txt` | Bỏ hai dòng mô tả class cũ `InvoiceTransaction.class` và `InvoiceTransactionPK.class`. |
| `docs/PROJECT_STRUCTURE_GUIDE.md` | Chuyển nội dung `README.md` cũ vào đây để giữ hướng dẫn cấu trúc project. |
| `README.md` trong project; `readme.md` tải riêng | Tài liệu tiếng Việt giải thích patch và cách áp dụng. Giữ tên `README.md` gốc khi áp dụng Git để tương thích Windows. |

Trong mã nguồn được cung cấp không có entity `InvoiceTransaction.java` hoặc
`InvoiceTransactionPK.java`, cũng không có khai báo hai entity này trong
`persistence.xml`. Vì vậy không cần xóa thêm entity hay sửa cấu hình JPA.

`InvoiceDAO`, các service thanh toán/check-in/checkout và servlet hiện có gọi
stored procedure hoặc đọc bảng/view khác, không trực tiếp đọc bảng nối. Các file
này không cần sửa chỉ vì bỏ bảng. Tiền thu, giao dịch gốc, số dư ví, kiểm tra quyền,
kiểm tra thanh toán đủ và chống ghi nhận trùng tiếp tục dùng logic hiện có.

## 4. Cách áp dụng cho database đang sử dụng

1. Dùng bản project đã sửa, hoặc áp dụng file patch vào đúng bản gốc `HBMS.zip`.
   Nếu dùng Git, chạy `git apply --check HBMS_remove_invoice_transaction.patch`,
   sau đó `git apply HBMS_remove_invoice_transaction.patch` tại thư mục có `pom.xml`.
2. Tạm dừng ứng dụng và tạo bản sao lưu database trước khi thay đổi cấu trúc.
3. Mở SQL Server Management Studio, chọn database **QLKS**, chạy toàn bộ
   `db/HBMS_Drop_Invoice_Transaction.sql` bằng tài khoản có quyền sửa thủ tục,
   view, trigger và bảng, đồng thời có quyền đọc định nghĩa các module.
4. Chạy `db/tests/HBMS_Drop_Invoice_Transaction_Verify.sql` trên cùng database.
5. Clean and Build project bằng JDK 11 trở lên và Maven: `mvn clean package`.
   Deploy WAR mới lên Tomcat rồi khởi động lại ứng dụng.

Migration chỉ xóa đoạn INSERT vào bảng nối trong định nghĩa hai thủ tục đang có
trong database. Các phần xử lý khác được giữ lại, kể cả nếu đã có chỉnh sửa riêng
ngoài đoạn đó. Migration chuẩn hóa CRLF/LF khi so khớp. Nếu đoạn tham chiếu bảng
cũ đã được viết lại khác mẫu trong `HBMS.zip`, script dừng và rollback để tránh
ghi đè nhầm logic. Khi đó cần đối chiếu thủ tục thực tế rồi điều chỉnh migration.

### Bản sửa lỗi `readme.md: already exists in working directory` trên Windows

Bản patch đầu xóa `README.md` rồi thêm `readme.md`. Trên Windows, hai tên này
thường trỏ tới cùng một file nên `git apply --check` báo file đã tồn tại.

Bản hiện tại giữ tên `README.md` gốc và cập nhật nội dung trực tiếp. Hướng dẫn
cũ vẫn được giữ đầy đủ trong `docs/PROJECT_STRUCTURE_GUIDE.md`. File tài liệu
tải riêng vẫn có tên `readme.md`.

Nếu đã gặp lỗi này ở bước `git apply --check`, tải lại file
`HBMS_remove_invoice_transaction.patch`, chép đè bản patch cũ ở thư mục có
`pom.xml`, rồi chạy lại `git apply --check` và `git apply` như hướng dẫn trên.
Không cần xóa hay đổi tên file README đang có. Lệnh `--check` không áp dụng
thay đổi vào mã nguồn.

### Bản sửa lỗi 51506

Bản migration đầu kiểm tra vị trí từ `PROCEDURE` trong 40 ký tự đầu của
`OBJECT_DEFINITION`. SQL Server có thể lưu cả chú thích đứng trước khai báo,
khiến kiểm tra này từ chối một thủ tục hợp lệ.

Bản hiện tại bỏ giới hạn đó, đọc các từ khóa ở phần khai báo sau khi bỏ qua
khoảng trắng, chú thích dòng và chú thích khối lồng nhau. Hỗ trợ `CREATE`,
`ALTER`, `CREATE OR ALTER` cùng cả `PROC` và `PROCEDURE`. Chỉ đổi từ `CREATE`
đầu khai báo thành `ALTER` khi cần; giữ nguyên phần chú thích và thân thủ tục.
`SET NOCOUNT ON` cũng được thêm để bỏ thông báo số dòng cấu hình tạm.

Nếu đã gặp lỗi 51506 với bản cũ, chạy lại **toàn bộ** bản mới của
`db/HBMS_Drop_Invoice_Transaction.sql` trên QLKS. Với script cũ đã gửi, lỗi này
được bắt trong CATCH và rollback transaction; lệnh xóa bảng nằm sau bước bị lỗi.
Thông báo `(2 rows affected)` đến từ hai dòng trong biến bảng cấu hình `@Modules`,
không phải số giao dịch bị xóa. Không cần chạy lại file khởi tạo database.

Có thể chạy riêng `db/tests/HBMS_Procedure_Header_Regression.sql` trên SQL Server
thử nghiệm để kiểm tra 12 trường hợp. File này chủ động chuyển sang `tempdb`,
không chỉnh dữ liệu HBMS; fixture được rollback. Sau đó chọn lại QLKS để chạy
migration và script verify. Các ca hồi quy SQL mới chưa được thực thi trong
môi trường chỉnh sửa do chưa có SQL Server.

Script dùng khóa ghi chung `SP_HBMS_ACQUIRE_WRITE_LOCK` của project và một
transaction cho toàn bộ thay đổi. Nếu phát hiện liên kết cũ không hợp lệ, phụ
thuộc chưa xử lý hoặc lỗi SQL, toàn bộ thay đổi được rollback. Có thể chạy lại
script sau khi migration đã thành công; không tạo lại bảng cũ.

Migration không xóa dữ liệu trong `INVOICE`, `PAYMENT`, `TRANSACTION` hay các bảng
hoàn tiền/ví. Dữ liệu bị xóa là các dòng của chính bảng nối được yêu cầu bỏ.
Các khóa duy nhất của bảng gốc được giữ để hạn chế thay đổi ngoài phạm vi patch.

**Không chạy `DB_v6_merged_transaction.sql` trên database có dữ liệu cần giữ:**
file này có lệnh xóa và tạo lại toàn bộ database, vốn đã có trong bản gốc.

## 5. Khởi tạo database mới

Chỉ khi tạo môi trường mới hoặc chủ động làm lại dữ liệu mẫu, chạy bản đã sửa của
`db/DB_v6_merged_transaction.sql`. Database mới sẽ không tạo bảng nối ngay từ đầu
và không cần chạy thêm migration xóa bảng. Các script PayOS và xử lý dịch vụ
đi kèm không có tham chiếu bảng nối nên không phải sửa vì yêu cầu này.

## 6. Sao lưu và phục hồi Excel

- Backup mới không tạo sheet `INVOICE_TRANSACTION`.
- Khi import backup cũ, sheet `INVOICE_TRANSACTION` được bỏ qua vì không nằm trong
  danh sách bảng hiện hành. Các sheet gốc như `INVOICE`, `PAYMENT`, `TRANSACTION`
  vẫn được import và kiểm tra tính nhất quán như trước.
- Không cần tái tạo các dòng bảng nối sau import: view suy ra các khoản thanh
  toán trực tiếp từ `BookingID` và `TransactionID`.
- Với công cụ phục hồi ngoài project, cần loại phần khôi phục bảng nối khỏi kịch
  bản cũ, hoặc chạy lại migration sau khi khôi phục một bản database cũ đầy đủ.

## 7. Kiểm tra và giới hạn xác minh

Các kiểm tra đã thực hiện:

- **7/7 ca Python đạt**: nhiều lần thu tiền trên một hóa đơn, tách đúng booking,
  loại `PENDING`/`FAILED`, giữ lịch sử `REFUNDED`, booking chưa có hóa đơn,
  không ghép sai giao dịch và giữ các trường tiền ví/tiền ngoài ví.
- **2/2 file kiểm tra JavaScript hiện có đạt**, gồm kiểm tra trạng thái thanh toán
  tại quầy/check-in và 20 kiểm tra giao diện thanh toán.
- Rà soát mã nguồn Java, cấu hình JPA, các câu SQL, backup/restore và cấu trúc
  project để loại phụ thuộc thực thi vào bảng cũ.
- Phân tích cú pháp **126 file Java** bằng bộ phân tích JDK, đặt mức cú pháp
  Java 11: không có lỗi cú pháp. Bước này chưa kiểm tra liên kết thư viện hoặc
  thay thế việc build WAR bằng Maven.

Chạy lại kiểm tra cục bộ tại thư mục có `pom.xml`:

```bash
python -m unittest discover -s src/test/python -v
node --test src/test/js/*.test.js
```

Kiểm tra Python dùng SQLite cho phần SELECT chuẩn SQL của view, không xác minh
khả năng chạy stored procedure hoặc transaction T-SQL. Môi trường chỉnh sửa
chưa có Maven và SQL Server nên **chưa build WAR và chưa thực thi migration trên
SQL Server**. Script kiểm tra SQL đi kèm cần được chạy tại môi trường của bạn.

Sau khi triển khai, kiểm tra trên database thử: đặt cọc rồi trả phần còn lại,
thu tiền mặt/ví/PayOS, checkout và in lại cùng hóa đơn, xác nhận không tạo thêm
hóa đơn khi gửi lại yêu cầu checkout, sau đó thử backup/restore Excel. Nếu còn
thiếu tiền, checkout phải tiếp tục bị chặn theo quy tắc hiện có.

Nếu cần khôi phục trạng thái trước patch, dùng bản sao lưu database và bản code
cũ tương ứng. Không chạy script khởi tạo để khôi phục dữ liệu đang sử dụng.
