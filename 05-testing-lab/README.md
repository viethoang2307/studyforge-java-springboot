# Testing Lab — JUnit 5 và Mockito

Module nhỏ này dùng nghiệp vụ chuyển tiền để luyện cách chọn test theo **test
pyramid**, thay vì cố đưa mọi kiểm tra qua Spring context:

```text
          / E2E \          Ít, chậm: chỉ các luồng quan trọng xuyên hệ thống
         / Integration \   Vừa đủ: database, HTTP, message broker thật
        /   Unit tests   \  Nhiều, nhanh: nghiệp vụ và value object
```

Các test hiện tại thuộc tầng unit. `TransferServiceTest` cô lập dependency bên
ngoài (`AccountRepository`, `TransferNotifier`) bằng Mockito, nhưng dùng
`Account`, `Money` và `TransferReceipt` thật. `TransferConcurrencyTest` dùng
implementation in-memory nhỏ để chạy nhiều thread, tránh biến test thành bài
kiểm tra nội bộ của Mockito.

## Bản đồ nội dung thực hành

| Chủ đề | Ví dụ |
| --- | --- |
| Parameterized test | boundary của `Money`, cặp account không hợp lệ, null input |
| Nested test | nhóm successful, invalid và failed transfer |
| Lifecycle | `@BeforeEach` tạo fixture mới; `PER_CLASS` cho method source của nested class |
| Assertions | `assertEquals`, `assertSame`, `assertThrows`, `assertAll`, `assertTrue` |
| State | số dư hai account sau thành công/thất bại |
| Behavior | notifier được gọi đúng một lần hoặc không được gọi |
| Exception | account không tồn tại, thiếu tiền và overflow |
| Invariant | tiền không âm, tổng số dư được bảo toàn, lỗi không làm đổi state |
| Concurrent behavior | transfer hai chiều đồng thời không deadlock hay làm mất tiền |

## Chạy test và đo coverage

Từ thư mục repository:

```bash
mvn -f 05-testing-lab/pom.xml clean verify
```

Mở báo cáo tại `05-testing-lab/target/site/jacoco/index.html`. Coverage là tín
hiệu để tìm vùng chưa được quan sát, **không phải mục tiêu chất lượng duy nhất**:
test vẫn cần tên rõ ràng, kiểm tra outcome có ý nghĩa và bắt được regression.
Module không đặt coverage gate cố định để tránh khuyến khích test vô nghĩa chỉ
nhằm đạt một con số.

## Bài tập refactor giữ test xanh

1. Chạy `mvn -f 05-testing-lab/pom.xml test` để có baseline xanh.
2. Refactor cách khóa account (ví dụ tách một `AccountPairLock`) mà không đổi
   hành vi public.
3. Chạy test sau từng thay đổi nhỏ. Nếu test đỏ, hoàn tác bước gần nhất hoặc sửa
   refactor — không sửa assertion chỉ để hợp thức hóa lỗi.
4. Chạy `clean verify`, đọc báo cáo JaCoCo rồi review lại chất lượng assertion.

Gợi ý mở rộng pyramid: thêm integration test cho repository với PostgreSQL
Testcontainers, rồi một số ít test HTTP xuyên Controller → Service → database.
