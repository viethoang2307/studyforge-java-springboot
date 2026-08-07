# Lý thuyết — Collections

## 1. Chọn collection theo semantics

Java Collections Framework tách **interface** khỏi implementation. Trước khi
chọn class cụ thể, hãy xác định nhu cầu:

- `List`: thứ tự, phần tử trùng lặp, truy cập theo index.
- `Set`: không trùng theo `equals`/`hashCode`.
- `Map`: ánh xạ key sang value, mỗi key có tối đa một value.
- `Queue`/`Deque`: xử lý theo thứ tự vào-ra hoặc hai đầu.

`TransactionHistory` cần tra cứu theo ID và giữ thứ tự thêm vào, nên dùng
`LinkedHashMap<UUID, Transaction>`. `EnumMap` phù hợp cho key là enum vì biểu
diễn gọn và có thứ tự enum tự nhiên. `HashMap` phù hợp lookup nhanh khi không cần
thứ tự.

## 2. Độ phức tạp và benchmark

Thông thường:

| Collection | `get(index)` | Tìm theo key | Điểm mạnh |
|---|---:|---:|---|
| `ArrayList` | O(1) | O(n) | truy cập index và iteration tốt |
| `LinkedList` | O(n) | O(n) | thao tác đầu/cuối qua deque |
| `HashMap` | trung bình O(1) | O(1) theo key | lookup không cần thứ tự |
| `LinkedHashMap` | trung bình O(1) | O(1) theo key | lookup + insertion order |

Đây là mô hình asymptotic, không phải cam kết thời gian tuyệt đối. Cache CPU,
allocation, kích thước dữ liệu và pattern truy cập đều ảnh hưởng kết quả.
`CollectionBenchmark` dùng JMH với warmup, measurement và fork để giảm sai lệch
do JVM chưa warm hoặc dead-code elimination. Không nên kết luận từ một vòng
`System.nanoTime` đơn giản.

`LinkedList` thường không phải lựa chọn mặc định cho list: random access phải đi
qua từng node, còn `ArrayList` có locality tốt. Chọn linked structure khi thao
tác node/deque thực sự phù hợp, không chỉ vì tên “linked”.

## 3. Equality, hash và ordering

`Transaction` định nghĩa identity nghiệp vụ bằng `id`. Hai snapshot cùng ID được
xem là cùng transaction dù amount hoặc thời điểm khác nhau. Vì identity đó dùng
cho `equals`, `hashCode` và `compareTo`, `HashSet` không nhân đôi object và
`TreeSet` không vô tình loại hai transaction có ID khác nhau.

Một collection có thứ tự dùng comparator để quyết định phần tử có “bằng” nhau
hay không. Nếu `compare(a, b) == 0` trong khi `a.equals(b)` là `false`, `TreeSet`
hoặc `TreeMap` có thể loại dữ liệu hợp lệ. Vì vậy natural order của lab dùng ID,
nhất quán với business identity.

Các comparator `BY_AMOUNT_DESCENDING` và `BY_TIME` là **presentation/query
order**, không thay thế equality. Comparator có tie-breaker bằng thời gian và ID
để thứ tự deterministic.

`BigDecimal.equals` phân biệt scale, còn so sánh amount nghiệp vụ thường cần
`compareTo`. Method `hasAmount` minh họa cách so sánh numeric equality.

## 4. Iterator và fail-fast

Iterator là cursor duyệt collection. Với `ArrayList`, thay đổi cấu trúc trực tiếp
trong lúc iterator đang chạy thường làm iterator ném
`ConcurrentModificationException`. Đây là cơ chế **best effort** để phát hiện
lỗi sử dụng, không phải synchronization và không nên dùng như cơ chế bảo vệ
thread.

Nếu cần xóa phần tử đang duyệt, dùng `iterator.remove()` sau một lần `next()`.
Các thao tác khác nên gom lại hoặc dùng API như `removeIf`. Trong môi trường nhiều
thread, cần thiết kế synchronization/concurrent collection riêng; fail-fast
không biến collection thành thread-safe.

## 5. Immutability ở boundary

`TransactionHistory` trả `List.copyOf` và các map được bọc không sửa được. Điều
này ngăn caller phá invariant nội bộ như duplicate ID hoặc làm sai insertion
order. `groupByType` tạo list mới cho từng nhóm rồi copy thành immutable list.

`find` lọc và sort trên stream, trả snapshot mới; thứ tự lưu trữ không bị thay
đổi. Đây là phân biệt quan trọng giữa query và mutation.

## 6. Aggregation và map idioms

- `putIfAbsent` dùng để reject duplicate identity.
- `computeIfAbsent` tạo bucket khi group lần đầu xuất hiện.
- `merge` cộng dồn total theo account.
- `EnumMap` biểu diễn grouping theo `TransactionType`.
- `LinkedHashMap` giữ first-seen order cho tổng theo account.

Các idiom này làm rõ ý định hơn so với tự kiểm tra `containsKey` rồi lặp lại logic
put. Tuy nhiên vẫn phải đặt precondition ở boundary và quyết định rõ null có
được phép hay không.

## 7. Cách đọc code và test

- `Transaction`: identity, validation, natural order và comparator.
- `TransactionHistory`: insertion order, query, grouping và aggregation.
- `IteratorBehaviorTest`: mutation đúng/sai trong lúc iterate.
- `CollectionBenchmark`: cách thiết lập benchmark có warmup và fork.

Các test quan trọng không chỉ kiểm tra giá trị trả về mà còn kiểm tra collection
không bị mutate sau query, duplicate identity bị reject và thứ tự deterministic.

## 8. Bài tập gợi ý

1. Thêm query lấy transaction mới nhất theo account mà không thay đổi history.
2. Viết comparator theo loại transaction rồi đến amount và ID.
3. Thử dùng `TreeSet` với comparator chỉ theo amount; tạo test chứng minh dữ liệu
   có thể bị mất.
4. Chạy benchmark với size lớn hơn và giải thích vì sao kết quả không nên đọc như
   một con số cố định.

## Checklist cần nhớ

- Chọn collection theo contract cần có, không theo thói quen.
- `HashMap` cần equality/hash ổn định; `TreeSet` cần comparator phù hợp.
- Fail-fast iterator không phải thread safety.
- Query nên trả snapshot/immutable view khi không muốn lộ state nội bộ.
- Benchmark JVM phải warmup và tránh kết luận từ một lần đo.
