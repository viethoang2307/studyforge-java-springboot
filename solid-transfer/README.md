# Bài thực hành SOLID: Transfer Service

Module Java 17 nhỏ này minh họa quá trình **characterization test → refactor**, thay vì tách class chỉ để có nhiều file. Chạy bằng `mvn test` trong thư mục này.

## 1. SRP: “một lý do để thay đổi” là gì?

SRP không có nghĩa “mỗi class chỉ có một method”. Một *lý do để thay đổi* là một nhóm yêu cầu đến từ cùng một actor/chính sách. `BadTransferService` có ít nhất bốn lý do độc lập:

1. **Input/validation** đổi khi hợp đồng đầu vào đổi.
2. **Business rule** đổi khi giới hạn của một payment method đổi.
3. **Persistence** đổi khi Map được thay bằng SQL/JPA hoặc yêu cầu atomicity đổi.
4. **Notification** đổi khi chuyển từ ghi trực tiếp sang email/Kafka.

Bản refactor dùng đúng bốn collaborator có ý nghĩa: `TransferValidator`, `PaymentMethod`, `AccountRepository`, và `NotificationGateway`. `TransferService` chỉ điều phối use case. Không tạo mapper/factory/base class nếu chưa có lý do thay đổi thực tế.

## 2. OCP: mở để mở rộng, đóng với sửa đổi

Bản xấu dùng chuỗi `if/else` theo `paymentMethod`; thêm phương thức mới buộc sửa use case đang ổn định. Bản refactor nhận một collection `PaymentMethod`, chọn implementation theo `id()` rồi gọi `verify()` bằng polymorphism. Thêm `INSTANT` nghĩa là thêm implementation và cấu hình dependency injection; **không sửa `TransferService`**. Test `addingPaymentMethodDoesNotModifyTransferService` chứng minh extension point này.

OCP không có nghĩa không bao giờ sửa code. Composition root vẫn phải biết implementation mới; mục tiêu là tránh sửa thuật toán transfer và tránh type checking lan rộng.

## 3. LSP: subtype phải giữ hợp đồng quan sát được

### `Square extends Rectangle`

Với `Rectangle` mutable, client có quyền kỳ vọng postcondition: sau `setWidth(5)`, width là 5 nhưng height không đổi; sau `setHeight(4)`, height là 4 nhưng width không đổi. `Square` override cả hai setter để bảo vệ invariant `width == height`, nên làm hỏng postcondition của `Rectangle`. Nếu không override, nó lại phá invariant của chính Square. Đây là dấu hiệu quan hệ kế thừa sai, không phải lỗi có thể chữa bằng một nhánh `instanceof`.

Thiết kế thay thế là `Shape` chỉ cam kết `area()`, với `ImmutableRectangle` và `ImmutableSquare` là hai implementation ngang hàng. Mỗi type kiểm tra precondition kích thước dương lúc khởi tạo, giữ invariant riêng, và đáp ứng postcondition của `area()`.

### Override rồi ném `UnsupportedOperationException`

Giả sử `Report.export()` hứa rằng mọi report export được, nhưng `ReadOnlyReport.export()` override rồi ném `UnsupportedOperationException`. Subclass đã **tăng precondition ẩn** (“chỉ gọi nếu subtype hỗ trợ”) và làm yếu postcondition (“trả bytes” thành “có thể ném”), nên không thay thế được base type.

Ví dụ tốt dùng capability interface `ExportableReport`: chỉ `PdfReport` có khả năng export mới implement nó. Một report chỉ để xem không cần kế thừa contract export. Exception chỉ hợp lệ khi chính contract khai báo failure đó cho mọi implementation, không phải để che một subtype không hỗ trợ operation.

### Checklist review LSP

- **Precondition:** subtype không được đòi đầu vào chặt hơn base type.
- **Postcondition:** subtype phải bảo đảm ít nhất những gì base type đã hứa.
- **Invariant:** subtype giữ invariant của base type; invariant mới không được khiến operation hợp lệ của base trở nên sai.
- **Exception/side effect:** không thêm failure hoặc side effect bất ngờ ngoài contract.

## 4. Trình tự refactor

1. Viết `BadTransferServiceCharacterizationTest` để khóa happy path, failure path, thông báo, và limit hiện tại. Đây là safety net mô tả hành vi cũ, không phải chứng nhận thiết kế tốt.
2. Tách `TransferValidator` — cải thiện **SRP**, vì hợp đồng input thay đổi độc lập.
3. Đưa việc đọc/ghi balance vào `AccountRepository` — cải thiện **SRP/DIP** và đặt atomic transfer thành contract persistence.
4. Đưa delivery vào `NotificationGateway` — cải thiện **SRP/DIP**; use case không biết transport.
5. Thay type checking bằng `PaymentMethod` — cải thiện **OCP**; rule theo phương thức dùng polymorphism.
6. Chạy cùng các kỳ vọng characterization trên thiết kế mới, rồi thêm test extension `INSTANT`.

> Giới hạn có chủ ý: ví dụ in-memory kiểm tra rồi cập nhật trong một lời gọi, nhưng production implementation phải dùng database transaction/locking để bảo đảm atomicity và concurrency. Notification đáng tin cậy sau commit cần outbox; không giả vờ rằng abstraction tự giải quyết vấn đề đó.
