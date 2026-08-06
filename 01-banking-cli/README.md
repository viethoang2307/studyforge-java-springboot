# Bài 1 — Class và đóng gói với `BankAccount`

Module nhỏ này minh họa class, field, method, constructor và constructor
chaining. Chạy bài tập bằng:

```bash
mvn test
```

## Những điểm cần quan sát

- `BankAccount` là **class**; `accountNumber` và `balance` là instance field,
  nên mỗi object có trạng thái riêng. `BANK_NAME` là static member dùng chung.
- Constructor một tham số gọi constructor hai tham số bằng `this(...)`.
- `accountNumber` là `final`: tham chiếu chỉ được gán một lần. `BANK_NAME` là
  hằng `static final`. `BankAccount` là final class nên không thể bị kế thừa.
- `MemberVisibilityExample` đặt cạnh nhau `private`, package-private (không có
  keyword), `protected` và `public`; method `describePrivateMember` là final nên
  subclass không thể override.
- Hai field của tài khoản là `private`. Không có `setBalance`; chỉ `deposit` và
  `withdraw` được đổi balance sau khi validation.
- Tiền dùng `BigDecimal`, không dùng `double`.

## Invariant được bảo vệ

1. Account number không `null`, rỗng hoặc chỉ có khoảng trắng.
2. Opening balance và balance sau giao dịch không âm.
3. Amount nạp/rút không `null` và phải lớn hơn 0.
4. Rút quá số dư bị từ chối trước khi field thay đổi.

Test gồm cả happy path và failure path, đồng thời kiểm tra thao tác thất bại
không làm thay đổi trạng thái và API public không lộ setter cho balance.
