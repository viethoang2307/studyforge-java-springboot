# Lý thuyết — Streams và Optional

## 1. Stream là pipeline xử lý dữ liệu

Java Stream không phải collection mới và cũng không lưu dữ liệu riêng theo cách
collection lưu. Stream mô tả một pipeline:

```text
source -> intermediate operations -> terminal operation
```

Ví dụ:

```java
transactions.stream()
        .filter(transaction -> transaction.amount().signum() > 0)
        .map(Transaction::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
```

`filter`, `map`, `sorted`, `flatMap` là intermediate operation và trả về stream
mới. `reduce`, `collect`, `toList`, `findFirst` là terminal operation; terminal
operation mới kích hoạt việc duyệt dữ liệu.

## 2. Laziness và side effect

Intermediate operation lazy. Test `intermediateOperationsAreLazyUntilTerminalOperationRuns`
đếm số lần `map` chạy: trước `toList()` là zero, sau đó mới là ba.

Laziness cho phép pipeline không làm công việc nếu không cần, đồng thời hỗ trợ
các terminal operation short-circuit như `findFirst`, `anyMatch` và `limit`.

Pipeline nên tránh mutation biến bên ngoài. Lambda có side effect khiến thứ tự,
khả năng parallel và việc debug trở nên khó đoán. Hãy để kết quả đi qua
terminal operation/collector thay vì tự `add` vào một list bên ngoài.

## 3. Các phép biến đổi thường gặp

- `filter`: giữ phần tử thỏa predicate.
- `map`: biến một phần tử thành một giá trị khác, ví dụ transaction thành amount.
- `flatMap`: trải nhiều stream con thành một stream phẳng.
- `distinct`: loại bản ghi trùng theo `equals`.
- `sorted`: sắp xếp theo comparator.
- `reduce`: gộp nhiều phần tử thành một kết quả, như tổng tiền.

`categoriesForAccounts` dùng `flatMap` vì input là `List<List<Transaction>>`.
Nếu chỉ dùng `map`, kết quả vẫn là stream của list và chưa thể lấy category trực
tiếp.

## 4. Collector cho báo cáo

`TransactionService.createReport` trả một `TransactionReport` gồm nhiều câu hỏi
nghiệp vụ:

- `reduce` tính tổng toàn bộ amount;
- `groupingBy` + `reducing` tính tổng theo `TransactionType`;
- `groupingBy` gom transaction theo category;
- `partitioningBy` chia thành nhóm đạt/ngưỡng lớn;
- grouping theo account rồi tạo `AccountSummary`.

`groupingBy` phù hợp khi có nhiều nhóm theo key. `partitioningBy` phù hợp đúng
hai nhóm boolean. Dùng collector chuyên biệt làm rõ ý định hơn việc tự quản lý
nhiều map/list trong một loop, nhưng collector phức tạp không phải lúc nào cũng
dễ đọc hơn loop.

`BigDecimal::add` được dùng làm accumulator để không mất độ chính xác tiền tệ.
Ngưỡng dùng `compareTo`, vì `60.0` và `60.00` có cùng ý nghĩa số học.

## 5. Optional biểu diễn kết quả có thể vắng mặt

`findById` trả `Optional<Transaction>` thay vì trả `null`:

```java
service.findById(transactions, "tx-1").ifPresent(this::display);
```

Optional buộc caller đối diện với trường hợp không tìm thấy. Có thể dùng
`isPresent`, `orElse`, `orElseThrow`, `map` hoặc `flatMap` tùy business rule.

Không nên dùng `Optional.get()` vô điều kiện, dùng Optional làm field/entity
persisted một cách máy móc, hoặc trả `null` thay cho `Optional.empty()`. Optional
phù hợp nhất ở return type của operation có thể không có kết quả.

## 6. Stream và loop: chọn công cụ phù hợp

Stream hữu ích khi pipeline là chuỗi biến đổi/tổng hợp độc lập và muốn diễn đạt
business question ở dạng declarative. Loop thường dễ đọc hơn khi có stateful
logic phức tạp, nhiều nhánh, break/continue đặc biệt hoặc cần tối ưu rõ ràng.

Lab giữ cả `createReport` và `createReportWithLoops`, sau đó test chúng cho cùng
kết quả. Đây là cách so sánh readability mà không biến “dùng stream” thành mục
tiêu tự thân. Correctness và tính dễ bảo trì quan trọng hơn số lượng stream.

## 7. Snapshot, null và stream lifecycle

Service tạo `List.copyOf(transactions)` trước khi xử lý để chụp input ổn định và
fail fast nếu list/input chứa null. Một stream chỉ được consume một lần; sau
terminal operation không được tái sử dụng stream đó. Nếu cần xử lý lại, tạo
stream mới từ source.

Khi dùng `parallelStream`, collector và lambda phải đáp ứng yêu cầu về thread
safety, associativity và non-interference. Không nên chuyển sang parallel chỉ vì
pipeline dài; hãy đo bằng workload thực tế.

## 8. Cách đọc code và test

- `createReport`: một pipeline cho mỗi câu hỏi báo cáo.
- `createReportWithLoops`: baseline imperative để đối chiếu.
- `findById`: kết quả optional.
- `categoriesForAccounts`: flatten nested collections.
- `largestFirst`: filter rồi sort giảm dần.

Test kiểm tra tổng, grouping, partition, equivalence giữa stream và loop, laziness
và flattening. Khi thêm pipeline mới, hãy test empty input, boundary threshold,
không tìm thấy ID và nhiều phần tử cùng group.

## 9. Bài tập gợi ý

1. Thêm method trả `Optional<Transaction>` lớn nhất của một account.
2. Viết báo cáo chỉ lấy category distinct nhưng vẫn sort theo alphabet.
3. Tạo test cho threshold đúng bằng amount và threshold lớn hơn mọi amount.
4. Viết lại một collector phức tạp bằng loop rồi so sánh khả năng đọc.

## Checklist cần nhớ

- Intermediate operation lazy; terminal operation mới chạy pipeline.
- `map` biến đổi một phần tử, `flatMap` làm phẳng nhiều container.
- Collector chọn theo hình dạng kết quả: group, partition hay reduce.
- Optional làm rõ “có thể không có kết quả” ở API.
- Tránh side effect và chỉ dùng parallel khi invariant/thread-safety đã rõ.
