# Java Concurrency Lab: thread và counter

Lab nhỏ này biến các khái niệm Java Memory Model thành ví dụ có thể chạy và
test. Nó cố ý giữ `UnsafeCounter` và `VolatileCounter` sai để so sánh với ba
cách sửa đúng.

## Chạy lab

Yêu cầu JDK 21 và Maven 3.9+:

```bash
cd concurrency-lab
mvn test
java -cp target/classes dev.studyforge.concurrency.ThreadLifecycleDemo
```

Demo lifecycle in ra `NEW → WAITING → TERMINATED`. Trong thực tế scheduler
quyết định khi nào thread ở `RUNNABLE`; các trạng thái `BLOCKED`, `WAITING` và
`TIMED_WAITING` đều không có nghĩa là thread đang dùng CPU.

## Lost update và atomicity

`value++` là một chuỗi **đọc → cộng → ghi**, không phải một thao tác nguyên tử.
Hai thread có thể cùng đọc `10`, cùng tính `11`, rồi cùng ghi `11`: một lần tăng
đã mất. Constructor thử nghiệm `new UnsafeCounter(workers)` đặt barrier giữa
đọc và ghi để race tái hiện chắc chắn, thay vì viết một test lúc xanh lúc đỏ.

Ba thuộc tính cần phân biệt:

* **Atomicity:** thao tác được quan sát như một khối không thể xen ngang.
* **Visibility:** một thread có nhìn thấy write của thread khác hay không.
* **Ordering:** compiler/CPU được phép đổi thứ tự nếu vẫn giữ hành vi đơn luồng;
  đồng bộ hóa đặt ra các ràng buộc thứ tự giữa nhiều thread.

## Công cụ nào giải quyết việc gì?

| Công cụ | Ý nghĩa | Dùng cho counter? |
| --- | --- | --- |
| `synchronized` | Mutual exclusion bằng intrinsic/monitor lock; unlock publish dữ liệu cho lần lock tiếp theo | Có; phù hợp khi cần bảo vệ một invariant hoặc nhiều field |
| `volatile` | Read luôn thấy write phù hợp gần nhất và tạo ordering; không khóa | **Không** cho `value++`; chỉ thích hợp cho flag/trạng thái độc lập như `StopFlag` |
| `AtomicLong` | Read-modify-write nguyên tử, hỗ trợ CAS và lấy chính xác giá trị hiện tại | Có; lựa chọn tốt cho một counter đơn |
| `LongAdder` | Chia contention lên nhiều cell rồi cộng khi gọi `sum()` | Có; tốt cho metrics nhiều writer, nhưng `sum()` đang concurrent không phải atomic snapshot |

`SynchronizedCounter.value()` cũng phải lấy cùng lock: chỉ khóa writer mà đọc
không khóa vẫn gây data race. `volatile long` làm read/write của field nhìn thấy
nhau, nhưng không biến ba bước của `++` thành một bước.

## Happens-before ở mức sử dụng

Happens-before là cam kết visibility và ordering, **không phải** thời gian trên
đồng hồ. Các quy tắc thường dùng trong lab này:

* Mọi hành động trước `Thread.start()` happens-before hành động của thread mới.
* Hành động trong worker happens-before thread khác trở về thành công từ
  `Thread.join()`.
* Unlock một monitor happens-before lần lock kế tiếp trên cùng monitor.
* Write vào field `volatile` happens-before read tiếp theo thấy write đó.
* Các synchronizer như `CountDownLatch` cung cấp memory-consistency guarantee;
  không thay chúng bằng `sleep()`.

## Concurrent test không bị treo

Test dùng latch để worker xuất phát gần nhau, `CountDownLatch.await(timeout)` để
giới hạn phần chạy đồng thời, và `@Timeout` làm giới hạn ngoài cùng. Nếu hết thời
gian, runner interrupt các worker rồi fail rõ ràng. Không dùng `Thread.sleep()`
để đoán scheduler và không dùng race ngẫu nhiên làm assertion.

### Bài tập tiếp theo

1. Chạy test rồi đổi `UnsafeCounter` sang `VolatileCounter`; giải thích vì sao
   test lost-update hiện tại cần một coordination hook để luôn tái hiện race.
2. Thêm hai field cần cập nhật cùng nhau và chứng minh `AtomicLong` riêng lẻ
   không bảo vệ invariant giữa chúng, còn một intrinsic lock thì có.
3. Benchmark ba counter bằng JMH (không dùng thời gian trong unit test làm
   benchmark), tăng dần số worker và giải thích khi `LongAdder` có lợi.
