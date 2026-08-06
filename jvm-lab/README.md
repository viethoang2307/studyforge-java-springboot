# JVM Lab: từ source code đến runtime và bộ nhớ

Lab này dùng **JDK 17+**, không cần Maven. Các ví dụ gây lỗi có cờ `--run` để
tránh chạy nhầm. Chúng chỉ làm hỏng tiến trình Java hiện tại, nhưng vẫn nên chạy
trên máy cá nhân/container và giữ đúng giới hạn bộ nhớ được hướng dẫn.

## 1. Hành trình của một chương trình Java

1. **Source code** (`.java`) là mã lập trình viên viết. `javac` kiểm tra cú pháp,
   kiểu và sinh file `.class`.
2. **Bytecode** là tập lệnh trung gian, độc lập tương đối với CPU. Xem bằng
   `javap -c -p`; thêm `-v` để xem constant pool, descriptor và metadata.
3. **Class loading** gồm loading (đọc/tạo `Class`), linking và initialization.
   Linking lại gồm verification, preparation và resolution. Bootstrap loader
   nạp lớp nền tảng; platform/application loader nạp các lớp còn lại theo cơ
   chế delegation.
4. **Verification** từ chối bytecode không hợp lệ: sai kiểu operand, nhảy tới vị
   trí không hợp lệ hay vi phạm cấu trúc class. Đây là rào chắn an toàn, không
   thay thế validation dữ liệu nghiệp vụ.
5. **Interpretation** cho phép JVM chạy bytecode ngay, từng lệnh một, không chờ
   biên dịch native toàn bộ chương trình.
6. **JIT compilation** phát hiện method/loop "nóng", biên dịch chúng thành mã
   máy và có thể tối ưu như inlining. Giả định tối ưu sai có thể dẫn tới
   deoptimization; vì vậy hiệu năng thường có giai đoạn warm-up.

Thực hành:

```bash
cd jvm-lab
./run-lab.sh

# Tập trung vào method square và main
javap -classpath out -c -p JvmLifecycleDemo

# Unified logging cho class loading; PrintCompilation cho hoạt động JIT
java -Xlog:class+load=info -XX:+PrintCompilation -cp out JvmLifecycleDemo
```

Trong bytecode, tìm `invokestatic` gọi `square`, `imul` thực hiện phép nhân và
`invokeinterface` khi gọi qua interface `List`. Không nên suy luận hiệu năng chỉ
từ bytecode: JIT có thể biến đổi đáng kể mã thực thi cuối cùng.

## 2. Các vùng nhớ JVM cần biết

| Vùng | Phạm vi | Chứa gì | Lỗi thường gặp |
| --- | --- | --- | --- |
| **Heap** | dùng chung | object và array; GC quản lý | `OutOfMemoryError: Java heap space` |
| **Java thread stack** | riêng mỗi thread | stack frame, biến cục bộ, operand stack, thông tin lời gọi | `StackOverflowError`, hoặc không tạo được thread mới |
| **Metaspace** | native memory, dùng chung | metadata của class và loader | `OutOfMemoryError: Metaspace` |

Biến local kiểu tham chiếu có thể nằm trong frame, nhưng object mà nó trỏ tới
thường nằm trên heap (JIT có quyền tối ưu nếu escape analysis cho phép).
Metaspace **không chứa mọi thứ từng gọi là “static”**: metadata của class ở
metaspace, còn object được field static tham chiếu vẫn ở heap.

## 3. Allocation và garbage collection ở mức thực dụng

- Object mới thường được cấp phát rất nhanh trong vùng riêng của thread (TLAB)
  rồi bắt đầu ở young generation; đây là mô hình thực dụng, không phải cam kết
  rằng mọi object luôn đi đúng đường này.
- GC bắt đầu từ **GC roots** (stack đang sống, static reference, JNI...) và thu
  hồi object không còn reachable. GC không dựa đơn thuần vào scope hay đặt biến
  thành `null`.
- Object sống qua nhiều chu kỳ có thể được đưa sang old generation. Allocation
  rate cao, tập object sống lớn và reference chain dài đều có thể gây áp lực GC.
- Đừng gọi `System.gc()` như cách sửa leak. Hãy giới hạn cache/queue, đóng
  resource bằng try-with-resources, bỏ listener khi hết dùng và đo bằng profiler.

`RetentionLeakDemo` mô phỏng cache không có eviction. Nó kết thúc sau một số
batch để an toàn, nhưng static list vẫn giữ các array reachable trong suốt vòng
đời tiến trình:

```bash
java -cp out RetentionLeakDemo 50
```

## 4. Quan sát heap, tiến trình và GC bằng JDK

Terminal A chạy demo (có khoảng dừng ba giây ở cuối); terminal B dùng PID được
in ra. Có thể tăng thời gian `sleep` trong source khi thực hành thủ công.

```bash
# Terminal A
java -Xms64m -Xmx64m -Xlog:gc*:file=logs/gc.log:time,level,tags \
  -cp out JvmLifecycleDemo 10000000

# Terminal B: tìm JVM, xem heap/class histogram và native memory
jcmd -l
jcmd <PID> GC.heap_info
jcmd <PID> GC.class_histogram
jcmd <PID> VM.native_memory summary

# Native Memory Tracking phải bật từ lúc khởi động nếu muốn báo cáo đầy đủ:
java -XX:NativeMemoryTracking=summary -cp out JvmLifecycleDemo
```

Đọc `logs/gc.log`: chú ý collector, loại pause, heap trước/sau GC, thời lượng và
tần suất. Một lần pause dài chưa đủ kết luận; đối chiếu allocation rate,
latency mục tiêu và workload. Có thể tạo heap dump để phân tích bằng VisualVM,
JDK Mission Control hoặc Eclipse MAT:

```bash
jcmd <PID> GC.heap_dump /tmp/jvm-lab.hprof
```

Heap dump có thể rất lớn và chứa dữ liệu nhạy cảm; không commit hoặc lấy dump
production tùy tiện.

## 5. Tái hiện lỗi có kiểm soát

### `StackOverflowError`

Mỗi lần đệ quy tạo thêm frame cho tới khi stack của **thread đó** hết chỗ:

```bash
java -Xss256k -cp out StackOverflowDemo --run
```

Kỳ vọng: exit code khác 0 và stack trace có `StackOverflowError`. Cách sửa thật
sự thường là thêm điều kiện dừng, đổi recursion sâu thành vòng lặp/cấu trúc dữ
liệu tường minh; tăng `-Xss` chỉ trì hoãn lỗi và làm mỗi thread tốn thêm memory.

### Heap `OutOfMemoryError`

Chạy trong JVM chỉ có heap 32 MiB. Chương trình giữ reference đến từng mảng nên
GC không thể thu hồi:

```bash
java -Xms32m -Xmx32m \
  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/jvm-lab.hprof \
  -Xlog:gc*:file=logs/oom-gc.log:time,level,tags \
  -cp out HeapOomDemo --run
```

Kỳ vọng: exit code khác 0, `OutOfMemoryError: Java heap space`, GC log cho thấy
GC cố thu hồi nhưng live set vẫn cao. Xóa file dump sau khi phân tích.

## 6. “Memory leak” trong Java khác gì C/C++?

- Trong Java, leak thường là **object không còn hữu ích nhưng vẫn reachable** do
  static collection, cache không eviction, listener không gỡ hoặc queue không
  giới hạn. GC hoạt động đúng nhưng không thể biết object không còn giá trị về
  mặt nghiệp vụ.
- Trong ngôn ngữ quản lý thủ công, leak kinh điển là vùng nhớ đã cấp phát nhưng
  không được `free/delete` (thường cũng đã mất reference). Ngoài leak còn có
  use-after-free, double-free và dangling pointer; Java tránh phần lớn lỗi này
  cho object được GC quản lý.
- Java vẫn có thể rò **native/off-heap resource** như file descriptor, socket,
  direct buffer hay JNI allocation. GC không bảo đảm đóng chúng đúng thời điểm;
  dùng ownership rõ ràng và try-with-resources.

Quy trình điều tra thực dụng: xác nhận heap tăng qua nhiều chu kỳ GC → lấy hai
histogram/dump cách nhau → tìm class tăng bất thường → xem dominator/retained
size và đường tới GC root → sửa owner/lifecycle → chạy lại cùng workload để
kiểm chứng. “Heap đang dùng nhiều” tự nó chưa chứng minh có leak.

## Checklist hoàn thành

- [ ] Giải thích được source → bytecode → loading/linking/init → interpreter/JIT.
- [ ] Chỉ ra `square` trong output của `javap`.
- [ ] Phân biệt heap, stack từng thread và metaspace.
- [ ] Đọc được heap trước/sau và pause time trong GC log.
- [ ] Tái hiện hai lỗi bằng giới hạn đã cho và giải thích nguyên nhân.
- [ ] Dùng reachability/GC root để giải thích Java memory leak.
