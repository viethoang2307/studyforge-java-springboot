# Bài thực hành: Functional Interface, Stream và Optional

Module nhỏ này tạo báo cáo giao dịch bằng Java 21. Chạy bài tập bằng:

```bash
mvn test
```

## 1. Functional interface và method reference

Functional interface chỉ có **một abstract method**, vì vậy lambda có thể cung
cấp implementation cho nó. Các kiểu thường gặp trong `java.util.function`:

| Kiểu | Nhận | Trả về | Ví dụ trong Stream |
| --- | --- | --- | --- |
| `Predicate<T>` | `T` | `boolean` | điều kiện của `filter` |
| `Function<T, R>` | `T` | `R` | phép đổi của `map` |
| `Consumer<T>` | `T` | `void` | hành động của `forEach` |
| `Supplier<T>` | không có | `T` | tạo collection/result |
| `BinaryOperator<T>` | `T, T` | `T` | gộp giá trị trong `reduce` |

Method reference là cách viết gọn lambda chỉ gọi lại một method:

```java
transaction -> transaction.amount() // lambda
Transaction::amount                 // method reference
(left, right) -> left.add(right)     // lambda
BigDecimal::add                      // method reference
```

Nên dùng method reference khi tên method giúp ý định rõ hơn; lambda vẫn tốt hơn
khi có thêm điều kiện hoặc nhiều bước xử lý.

## 2. Intermediate, terminal và lazy evaluation

- **Intermediate operation** như `filter`, `map`, `flatMap`, `distinct`,
  `sorted` trả về một Stream mới. Chúng chỉ mô tả pipeline và **chưa xử lý dữ
  liệu ngay**.
- **Terminal operation** như `toList`, `collect`, `reduce`, `findFirst`, `count`
  kích hoạt pipeline và tạo kết quả (hoặc side effect với `forEach`). Một Stream
  không thể dùng lại sau terminal operation.
- Vì intermediate operation được đánh giá lười (lazy), terminal operation có
  thể short-circuit. Ví dụ, `filter(...).findFirst()` dừng khi tìm thấy phần tử
  đầu tiên, thay vì duyệt hết nguồn.

Test `intermediateOperationsAreLazyUntilTerminalOperationRuns` chứng minh
`map` chưa chạy trước khi gọi `toList()`.

## 3. Các operation trong bài

- `map`: lấy `amount` từ mỗi giao dịch.
- `filter`: chọn giao dịch theo id hoặc số tiền tối thiểu.
- `flatMap`: làm phẳng `List<List<Transaction>>` thành một Stream giao dịch.
- `reduce`: cộng các `BigDecimal`, bắt đầu bằng `BigDecimal.ZERO`.
- `collect`: tạo kết quả tổng hợp mutable/phức tạp.
- `groupingBy`: nhóm giao dịch theo loại, category và account.
- `partitioningBy`: chia đúng hai nhóm `true`/`false` theo ngưỡng giao dịch lớn.

`reduce` hợp với một giá trị kết quả; `collect` hợp với container như `Map` hoặc
`List`. Với tiền, bài tập luôn dùng `BigDecimal`, không dùng `double`.

## 4. Báo cáo và so sánh loop với Stream

`TransactionService#createReport` tạo tổng tiền, tổng theo loại, nhóm category,
phân vùng theo ngưỡng và thống kê account. `createReportWithLoops` tạo đúng kết
quả đó bằng loop để so sánh.

- Stream diễn đạt từng câu hỏi nghiệp vụ ngắn gọn và tách biệt, phù hợp với các
  phép biến đổi/tổng hợp.
- Loop cho phép gom nhiều phép tính trong một lượt duyệt và dễ debug từng bước,
  nhưng cần tự quản lý nhiều `Map`, danh sách và biến tạm.
- Không chọn Stream chỉ để code ngắn. Nếu pipeline có quá nhiều nhánh hoặc khó
  đặt tên, hãy tách method hay dùng loop rõ ràng.

## 5. Optional đúng chỗ

`findById` trả `Optional<Transaction>` vì việc không tìm thấy là kết quả bình
thường. Caller phải xử lý rõ ràng bằng `map`, `orElse`, `orElseGet`,
`orElseThrow` hoặc `ifPresent`:

```java
Transaction transaction = service.findById(transactions, id)
        .orElseThrow(() -> new IllegalArgumentException("Unknown transaction: " + id));
```

Quy ước thực hành:

- Dùng `Optional` chủ yếu làm **return type** cho một kết quả có thể vắng mặt.
- Không dùng `Optional` làm entity field: nó gây khó khăn cho JPA/serialization
  và entity đã có thể biểu diễn nullability bằng validation/mapping.
- Không dùng `Optional` làm method parameter: overload, parameter nullable rõ
  nghĩa, hoặc một request/options object thường dễ dùng hơn.
- Không trả về `null` từ method khai báo `Optional`; dùng `Optional.empty()`.
- Với collection, trả collection rỗng thay vì `Optional<List<T>>`.

## 6. Tránh side effect khó đoán

Không sửa biến, collection bên ngoài, database hoặc gọi API bên trong
`map`/`filter`. Lazy evaluation, short-circuiting và parallel Stream có thể làm
thứ tự/số lần side effect khác dự đoán. Hãy để pipeline tạo ra giá trị mới bằng
`toList`, `reduce` hoặc `collect`; thực hiện I/O ở ranh giới rõ ràng sau đó.

`AtomicInteger` trong test lazy evaluation chỉ là công cụ quan sát dành cho
test, **không** phải mẫu code nghiệp vụ nên làm theo.

## Bài tập mở rộng

1. Thêm khoảng ngày vào báo cáo bằng `filter`.
2. Tìm giao dịch lớn nhất bằng `max` và trả `Optional<Transaction>`.
3. Thêm `summarizing` cho số lượng và tổng theo category.
4. Viết test chứng minh `findFirst` short-circuit.
5. Benchmark loop và Stream bằng JMH; không kết luận hiệu năng từ một lần chạy.
