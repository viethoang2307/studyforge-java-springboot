# Lý thuyết — OOP Payments

## 1. Bài toán

Một payment service thường hỗ trợ nhiều phương thức: thẻ, chuyển khoản và ví
điện tử. Mỗi phương thức có cách gọi provider khác nhau, nhưng use case của
ứng dụng chỉ cần biết “thanh toán bằng loại X và nhận receipt”. Mục tiêu của
lab là dùng abstraction, polymorphism và composition để business service không
phụ thuộc vào chi tiết provider.

Luồng xử lý là:

```text
PaymentService
    -> chọn PaymentMethod theo PaymentType
    -> PaymentMethod.pay(request)
    -> PaymentGateway.charge(request)
    -> PaymentReceipt
```

## 2. Encapsulation và invariant

`PaymentRequest` là record immutable mô tả một yêu cầu hợp lệ. Compact constructor
giữ các invariant tại boundary:

- `sourceReference` không được null hoặc blank và được trim;
- `amount` không null và phải lớn hơn zero;
- `currency` không null;
- amount được canonicalize bằng `stripTrailingZeros`.

Đặt validation ở nơi tạo request giúp phần còn lại của hệ thống có thể giả định
request hợp lệ. Đây là nguyên tắc “validate once at the boundary”, nhưng các
điều kiện phụ thuộc provider vẫn nên để adapter/gateway kiểm tra.

`PaymentReceipt` cũng tự bảo vệ invariant: transaction id không blank và payment
type không null. Receipt là kết quả bất biến, nên caller không thể sửa kết quả
đã phát ra.

## 3. Interface và polymorphism

`PaymentMethod` là abstraction ổn định:

```java
public interface PaymentMethod {
    PaymentType type();
    PaymentReceipt pay(PaymentRequest request);
}
```

`CardPayment`, `BankTransferPayment` và `WalletPayment` cùng tuân theo interface
nhưng giữ gateway riêng. `PaymentService` chỉ gọi `method.pay(request)`; nó không
cần biết method nào đang dùng HTTP, SDK hay mock.

Đây là runtime polymorphism: cùng một lời gọi method, implementation thực tế
được chọn theo object được inject. Khi thêm `CryptoPayment`, ta tạo implementation
mới và đăng ký nó, thay vì sửa một chuỗi `if/else` lớn trong service.

## 4. Composition và dependency injection thủ công

Các payment class nhận `PaymentGateway` qua constructor. Đây là dependency
injection đơn giản:

```java
PaymentGateway fake = request -> "tx-123";
PaymentMethod card = new CardPayment(fake);
```

Business code không tự `new` client bên trong method thanh toán. Nhờ vậy test có
thể cung cấp fake gateway, kiểm tra request truyền xuống và mô phỏng lỗi provider.
Constructor cũng dùng `Objects.requireNonNull` để fail fast nếu wiring sai.

Đây là composition over inheritance: ta ghép service với gateway và ghép
`PaymentService` với các strategy `PaymentMethod`, không tạo cây class kế thừa
phức tạp.

## 5. Registry thay cho conditional dispatch

`PaymentService` nhận `List<PaymentMethod>` rồi index thành `EnumMap<PaymentType,
PaymentMethod>`. Cách này có ba lợi ích:

1. tra cứu theo enum rõ ràng và nhanh;
2. phát hiện duplicate type ngay lúc khởi tạo;
3. lỗi loại payment chưa hỗ trợ được biểu diễn bằng
   `UnsupportedPaymentTypeException`.

`Map.copyOf` tạo snapshot không thay đổi được của registry. Service vì vậy giữ
được cấu hình ổn định sau constructor.

## 6. Khi nào dùng inheritance, interface hay composition?

- Dùng **interface** khi cần một capability/contract mà nhiều implementation có
  thể cung cấp.
- Dùng **composition** khi một object cần sử dụng một dependency có thể thay thế,
  như gateway trong lab.
- Dùng **inheritance** chỉ khi quan hệ “is-a” thật sự ổn định và subclass có thể
  thay thế superclass mà không phá invariant.

Trong bài toán này, interface + composition phù hợp hơn base class chung vì
provider có thể khác nhau về SDK, retry, credential và giao thức.

## 7. Cách đọc code và test

- `PaymentRequestTest`: kiểm tra validation và normalization ở boundary.
- Các test của từng payment method: kiểm tra adapter gọi đúng gateway và tạo
  receipt đúng loại.
- `PaymentServiceTest`: kiểm tra dispatch, duplicate registration và unsupported
  type.

Khi viết test, hãy test business contract qua `PaymentMethod`/`PaymentService`,
đồng thời test adapter riêng cho mapping provider. Không cần test implementation
private hoặc chi tiết của `EnumMap` nếu hành vi public đã được đảm bảo.

## 8. Bài tập gợi ý

1. Thêm `PaymentMethod` mới dùng gateway riêng mà không sửa logic dispatch.
2. Viết fake gateway ghi lại `PaymentRequest` nhận được.
3. Thêm test cho gateway trả transaction id blank và quyết định lỗi nên được
   phát hiện ở gateway hay ở receipt.
4. So sánh implementation registry với một `switch` và nêu trade-off khi số loại
   payment tăng.

## Checklist cần nhớ

- Validation giúp object không tồn tại ở trạng thái vô nghĩa.
- Interface tách contract khỏi implementation.
- Constructor injection làm dependency rõ ràng và testable.
- Registry/strategy giảm conditional logic và hỗ trợ mở rộng.
- Exception nghiệp vụ nên mô tả rõ failure ở boundary nào.
