# Bài học: Exception trong Banking CLI

Module nhỏ này minh họa cách thiết kế và kiểm thử exception thay vì chỉ ghi nhớ
cú pháp.

## Checked và unchecked exception

- **Checked exception** kế thừa `Exception` (nhưng không kế thừa
  `RuntimeException`). Compiler bắt buộc caller phải `catch` hoặc khai báo
  `throws`. `BankingException` và ba exception nghiệp vụ trong module thuộc nhóm
  này vì caller có thể thông báo lỗi và yêu cầu người dùng sửa thao tác.
- **Unchecked exception** kế thừa `RuntimeException`. Chúng thường biểu thị lỗi
  lập trình hoặc contract bị vi phạm. Ví dụ, truyền `null`, tạo account với ID
  rỗng hoặc thêm trùng ID dẫn đến `NullPointerException`/
  `IllegalArgumentException`; caller không nên giả vờ phục hồi bằng cách bắt
  chung mọi exception.

Checked/unchecked là cơ chế của Java, còn business/programming error là cách
phân loại theo ý nghĩa. Không phải mọi ứng dụng đều phải chọn checked exception
cho business error, nhưng cần chọn nhất quán.

## Propagation và chiến lược bắt lỗi

`Bank.transfer` khai báo `throws BankingException`. Nếu nó không xử lý được lỗi,
lỗi truyền ngược qua call stack cho lớp giao diện. Lớp CLI nên chỉ bắt các lỗi
mà nó biết cách xử lý:

```java
try {
    bank.transfer(source, destination, amount);
} catch (InvalidAmountException | InsufficientBalanceException |
         AccountNotFoundException error) {
    System.err.println(error.getMessage()); // cho phép người dùng nhập lại
}
```

Không bắt `Exception` chỉ để log rồi tiếp tục, không để `catch` rỗng, và không
đổi mọi lỗi thành một thông báo mơ hồ. Chỉ bắt ở nơi có chiến lược như retry,
fallback, đổi sang error response, hoặc dọn dẹp/bổ sung ngữ cảnh rồi throw lại.

## Exception không phải luồng điều khiển thông thường

Không dùng `AccountNotFoundException` để kiểm tra account có tồn tại trong một
vòng lặp bình thường, hoặc dùng exception để thoát vòng lặp. Với nhánh được dự
đoán là xảy ra thường xuyên, hãy cung cấp API như `contains`, `Optional`, điều
kiện `if`, hoặc kết quả có kiểu rõ ràng. Exception dành cho việc thao tác không
thể hoàn tất đúng contract.

`Bank.transfer` kiểm tra toàn bộ failure path trước khi thay đổi số dư. Vì vậy,
thiếu tiền, amount sai và account không tồn tại đều giữ nguyên cả hai object.

## try-with-resources và `AutoCloseable`

`TransactionLog` triển khai `AutoCloseable`, nên `writeOne` có thể dùng:

```java
try (TransactionLog log = new TransactionLog(path)) {
    log.append(entry);
}
```

Java luôn gọi `close`, kể cả khi `append` ném lỗi. Nếu cả body và `close` cùng
ném lỗi, lỗi từ body là lỗi chính và lỗi khi đóng nằm trong
`getSuppressed()`. `IOException` tiếp tục propagate vì `writeOne` không có cách
phục hồi hợp lý. Không cần `finally` thủ công để đóng writer.

## Chạy bài kiểm tra

```bash
mvn test
```

Các test dùng `assertThrowsExactly` để kiểm tra **đúng loại** exception và tiếp tục
assert số dư sau lỗi. Đây là phần quan trọng: chỉ kiểm tra rằng "có lỗi" chưa đủ
để chứng minh transfer có tính all-or-nothing.
