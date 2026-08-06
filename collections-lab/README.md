# Java Collections Lab — Transaction History

Module thực hành Collections Framework bằng một lịch sử giao dịch có lọc, sắp
xếp và gom nhóm. Chạy test trước, đọc implementation, sau đó thay đổi cấu trúc
dữ liệu và giải thích điều gì xảy ra.

## 1. Contract quan trọng

| Interface | Contract cốt lõi | Chọn khi |
| --- | --- | --- |
| `List<E>` | Có thứ tự theo vị trí, cho phép phần tử trùng; truy cập bằng index | Cần sequence và vị trí |
| `Set<E>` | Không chứa hai phần tử bằng nhau theo contract equality | Cần uniqueness/membership |
| `Queue<E>` | Thường FIFO; `offer/poll/peek` trả trạng thái, `add/remove/element` có thể ném exception | Xử lý công việc theo hàng đợi |
| `Deque<E>` | Thêm/xóa/đọc ở cả hai đầu; dùng tốt cho queue và stack | Cần FIFO hoặc LIFO; thường chọn `ArrayDeque` thay `Stack` |
| `Map<K,V>` | Mỗi key ánh xạ tối đa một value; key uniqueness dựa trên equality hoặc ordering | Tra cứu value bằng key |

Không suy ra implementation từ interface: `List` không hứa truy cập index
nhanh, `Set`/`Map` không mặc định hứa iteration order, và `Queue` không phải lúc
nào cũng FIFO (ví dụ priority queue).

## 2. So sánh implementation

### `ArrayList` và `LinkedList`

- `ArrayList` dùng mảng liên tục: `get` nhanh, iteration locality tốt, append
  thường nhanh; chèn/xóa giữa danh sách phải dịch phần tử và đôi khi resize.
- `LinkedList` là doubly-linked list: node tốn thêm bộ nhớ, cache locality kém,
  `get(index)` phải đi qua node. Chèn/xóa O(1) **chỉ khi đã có iterator/node ở
  vị trí đó**; tìm vị trí vẫn O(n). Nó cũng implement `Deque`, nhưng
  `ArrayDeque` thường là lựa chọn queue/stack tốt hơn.

### Các `Set`

| Loại | Thứ tự iteration | Chi phí kỳ vọng |
| --- | --- | --- |
| `HashSet` | Không bảo đảm | `add/contains/remove` trung bình O(1) |
| `LinkedHashSet` | Thứ tự chèn | Trung bình O(1), thêm liên kết và bộ nhớ |
| `TreeSet` | Natural order hoặc `Comparator` | O(log n), có range/navigation |

### Các `Map`

| Loại | Thứ tự iteration | Chi phí kỳ vọng |
| --- | --- | --- |
| `HashMap` | Không bảo đảm | `get/put` trung bình O(1) |
| `LinkedHashMap` | Thứ tự chèn, hoặc access-order nếu cấu hình | Trung bình O(1), phù hợp nền tảng LRU |
| `TreeMap` | Key được sort | O(log n), có range/navigation |

`TransactionHistory` dùng `LinkedHashMap<UUID, Transaction>` để uniqueness theo
ID, lookup trung bình nhanh và giữ thứ tự nhận giao dịch. `EnumMap` phù hợp để
group theo enum và duyệt theo thứ tự khai báo enum.

## 3. Hashing ở mức cơ chế

1. `hashCode()` của key được trộn bit và đổi thành bucket index trong backing
   table. Nhiều key có thể vào cùng bucket: đó là **collision**.
2. Khi collision, implementation vẫn gọi `equals` để tìm đúng key. Vì vậy object
   bằng nhau **phải** có cùng hash code; key mutable có thể làm entry “thất lạc”.
3. **Capacity** là số bucket hiện có, không phải số entry. **Load factor** là
   ngưỡng mật độ; `HashMap` mặc định thường resize khi `size > capacity × 0.75`.
4. Capacity quá nhỏ gây resize/collision; quá lớn tốn bộ nhớ và có thể làm
   iteration chậm. Pre-size chỉ khi có ước lượng đáng tin cậy.

Worst case không tự động là O(1). Chất lượng hash, phân bố key, collision,
resize, kích thước và JVM/hardware đều ảnh hưởng chi phí thực tế.

## 4. `Comparable` và `Comparator`

- `Comparable<T>` định nghĩa một natural order ổn định. `Transaction` dùng ID
  làm natural order để nhất quán với equality theo business identity; comparator
  `BY_TIME` phục vụ cách nhìn theo thời gian rồi ID làm tie-breaker.
- `Comparator<T>` định nghĩa order theo use case. `BY_AMOUNT_DESCENDING` dùng
  amount giảm dần, sau đó time và ID làm tie-breaker.
- Với sorted set/map, `compare(a,b) == 0` quyết định uniqueness. Tốt nhất natural
  order nhất quán với `equals`; nếu comparator cố ý không nhất quán, phải ghi rõ.
- `BigDecimal.equals` xét cả scale (`10.0` khác `10.00`), còn `compareTo` xét giá
  trị số. Domain này dùng `compareTo` cho filter/amount matching.

## 5. Iterator và fail-fast

Iterator của phần lớn general-purpose collections ghi nhận structural
modification count. Sửa collection trực tiếp trong khi đang duyệt có thể làm
lần thao tác iterator kế tiếp ném `ConcurrentModificationException`. Đây là cơ
chế phát hiện lỗi **best effort**, không phải bảo đảm đồng bộ thread. Dùng
`Iterator.remove`, `removeIf`, thu thập thay đổi để áp dụng sau, hoặc concurrent
collection đúng với use case.

## 6. Benchmark đúng câu hỏi

JMH benchmark random access và full iteration của hai list, cùng lookup map ở
hai kích thước. JMH cung cấp warmup, nhiều measurement iteration, fork JVM và
`Blackhole` để tránh dead-code elimination:

```bash
mvn clean package
java -jar target/benchmarks.jar CollectionBenchmark
```

Không dùng benchmark để “chứng minh ArrayList luôn nhanh hơn”. Cần mô tả workload,
phân bố kích thước, JVM/GC/hardware, kiểm tra độ biến thiên và đo thao tác giống
production. Big-O mô tả tăng trưởng; benchmark đo hằng số, cache, allocation và
runtime trong một môi trường cụ thể. Không đưa benchmark vào unit test vì timing
test dễ flaky.

## 7. Bài tập mở rộng

1. Thêm filter nhiều account và khoảng amount, giữ API bất biến.
2. Thêm group theo account rồi theo type bằng nested map; xác định rõ order.
3. So sánh pre-sized và default `HashMap`, thêm benchmark insert/remove.
4. Viết comparator sort type → amount → time và test tie-breaker.
5. Thử dùng mutable key trong map, quan sát lỗi rồi giải thích contract bị phá.

## Chạy kiểm tra

```bash
cd collections-lab
mvn test
```
