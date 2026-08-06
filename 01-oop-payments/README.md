# OOP qua bài toán thanh toán

Module nhỏ này minh họa bốn ý tưởng OOP bằng một use case có thể chạy test, thay
vì chỉ định nghĩa thuật ngữ.

## Chạy test

Yêu cầu JDK 21 và Maven 3.9+:

```bash
cd 01-oop-payments
mvn test
```

## 1. Encapsulation bảo vệ invariant

`PaymentRequest` là immutable record và kiểm tra dữ liệu ngay tại constructor:
nguồn thanh toán không rỗng, amount khác `null` và lớn hơn 0, currency khác
`null`. Vì object không thể được tạo ở trạng thái sai, mọi `PaymentMethod` đều
được phép tin vào các invariant này. Việc chỉ đặt field là `private` chưa đủ;
encapsulation có giá trị khi mọi đường thay đổi trạng thái đều bảo vệ luật nghiệp
vụ.

## 2. Abstraction chỉ biểu diễn điều quan trọng

Business service chỉ cần biết `PaymentMethod.type()` và `PaymentMethod.pay()`.
Nó không cần biết API ngân hàng, token thẻ hay cách ví ghi nợ. `PaymentGateway`
tiếp tục che giấu chi tiết giao tiếp với nhà cung cấp, đồng thời cho phép thay
gateway giả trong unit test.

## 3. Inheritance và giới hạn của “is-a”

Ba payment method **là** các `PaymentMethod`, nên cùng implement interface là
quan hệ “is-a” đúng. Tuy nhiên chúng không kế thừa một `BasePayment` chỉ để dùng
chung field gateway: card không phải là bank transfer và wallet cũng không phải
card. Một superclass dễ tạo coupling, ép subclass nhận trạng thái/hành vi không
phù hợp và gây lỗi Liskov khi subclass không thể giữ đúng contract của lớp cha.

## 4. Polymorphism và dynamic dispatch

`PaymentService` index các implementation theo `PaymentType`. Sau một lần lookup,
lời gọi `method.pay(request)` được Java dynamic dispatch tới đúng implementation.
Service không có chuỗi `if/else` hoặc `switch` kiểm tra từng loại. Muốn thêm loại
mới, tạo implementation mới và đăng ký nó; luồng business không đổi.

Ví dụ wiring (gateway lambda chỉ dùng để minh họa):

```java
PaymentMethod card = new CardPayment(request -> "provider-transaction-id");
PaymentService service = new PaymentService(List.of(card));
PaymentReceipt receipt = service.pay(PaymentType.CARD, request);
```

## 5. Khi nào composition tốt hơn inheritance?

Ưu tiên composition khi mục tiêu là **tái sử dụng khả năng** (“has-a”) chứ không
phải mô hình hóa một subtype thật sự (“is-a”), khi dependency cần được thay lúc
wiring/test, hoặc khi các hành vi có thể thay đổi độc lập. Ở đây mỗi payment
method **có một** `PaymentGateway`; constructor injection cho phép ghép gateway
phù hợp mà không tạo cây kế thừa cứng.

Chỉ cân nhắc inheritance khi subtype thực sự thay thế được base type, contract
và invariant của base type ổn định, và quan hệ đó có ý nghĩa trong domain — không
chỉ vì muốn dùng lại vài dòng code. Interface + composition thường giữ class nhỏ,
dễ test và tránh “fragile base class”.

## Bài tập mở rộng

1. Thêm `CryptoPayment` mà không sửa `PaymentService` (ngoài việc thêm enum nếu
   vẫn chọn enum làm key).
2. Thay `PaymentType` bằng key cấu hình để plugin mới hoàn toàn không cần sửa
   source hiện có.
3. Tạo adapter gateway thật và integration test, nhưng giữ unit test hiện tại.
