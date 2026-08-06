# Thực hành Java Generics

Module nhỏ này minh họa generics bằng code có test. Chạy bằng:

```bash
mvn test
```

## 1. Generic class và generic method

`Box<T>` là generic class: `T` thuộc về cả class, nên `Box<String>` chỉ nhận và
trả về `String`. `GenericAlgorithms.max` và `copy` là generic method: type
parameter được khai báo ngay trước kiểu trả về và được suy luận tại mỗi lần gọi.

Generics giúp lỗi sai kiểu xuất hiện lúc compile thay vì lúc runtime, đồng thời
không cần ép kiểu khi lấy dữ liệu ra.

## 2. Bounded type parameter

```java
<T extends Comparable<? super T>> T max(List<? extends T> values)
```

`T extends ...` là **upper bound**: `max` chỉ chấp nhận kiểu có thể so sánh với
chính nó hoặc một supertype của nó. Một type parameter có thể có nhiều bound,
ví dụ `<T extends Number & Comparable<T>>`; class (nếu có) phải đứng trước các
interface. Java không hỗ trợ lower bound cho type parameter; lower bound chỉ có
ở wildcard (`? super T`).

## 3. Bốn kiểu `List` dễ nhầm

Giả sử `Integer extends Number`:

| Kiểu | Có thể gán từ | Đọc an toàn thành | Có thể thêm |
| --- | --- | --- | --- |
| `List<Object>` | Chỉ `List<Object>` | `Object` | Mọi `Object` |
| `List<?>` | `List` của bất kỳ kiểu nào | `Object` | Chỉ `null` (không nên dùng để ghi) |
| `List<? extends Number>` | `List<Number>`, `List<Integer>`, ... | `Number` | Chỉ `null` |
| `List<? super Integer>` | `List<Integer>`, `List<Number>`, `List<Object>` | `Object` | `Integer` và subtype của nó |

`List<Integer>` **không phải** subtype của `List<Number>` (generics là
invariant). Nếu điều đó được phép, ta có thể thêm `Double` qua biến
`List<Number>` rồi làm hỏng cam kết rằng list chỉ chứa `Integer`.

Wildcard biểu diễn một kiểu cụ thể nhưng chưa biết, không có nghĩa là
`Object`. Với `List<?>`, ta vẫn có thể đọc thành `Object`, lấy `size`, xóa hoặc
`clear`; nhưng không thể thêm một object tùy ý vì compiler không biết element
type thực sự.

## 4. PECS

**Producer Extends, Consumer Super**:

```java
static <T> void copy(List<? extends T> source, List<? super T> destination)
```

- `source` sản xuất giá trị `T`, nên dùng `extends`.
- `destination` tiêu thụ giá trị `T`, nên dùng `super`.
- Nếu một tham số vừa đọc vừa ghi cùng một kiểu, thường không dùng wildcard
  (`List<T>`).

Ví dụ `copy(List<Integer>, List<Number>)` hợp lệ mà không cần raw type hay cast.

## 5. Type erasure và giới hạn

Compiler kiểm tra type rồi xóa phần lớn thông tin generic; bytecode của
`Box<String>` và `Box<Integer>` dùng cùng một class `Box`. Compiler chèn cast
cần thiết tại nơi đọc và có thể tạo bridge method để giữ polymorphism.

Vì type argument thường không tồn tại đầy đủ ở runtime:

- Không thể viết `new T()`, `T.class` hoặc `new T[10]`.
- Không thể dùng `instanceof List<String>`; chỉ dùng được `instanceof List<?>`.
- Không thể overload chỉ bằng type argument, như `run(List<String>)` và
  `run(List<Integer>)`, vì cả hai cùng bị xóa thành `run(List)`.
- Type parameter không nhận primitive: dùng `Integer`, không dùng `int`.
- Generic class không có static field mang type `T`, và không thể trực tiếp
  `catch`/`throw` một generic exception type theo cách thông thường.
- Varargs của kiểu generic có thể gây heap pollution; chỉ dùng `@SafeVarargs`
  khi implementation thực sự an toàn.

Khi thật sự cần tạo object theo kiểu ở runtime, truyền `Class<T>`, factory
`Supplier<T>` hoặc type token rõ ràng thay vì unchecked cast.

## 6. Generic repository in-memory

`Repository<ID, E extends Identifiable<ID>>` ràng buộc entity và kiểu ID ngay
lúc compile. `InMemoryRepository` dùng `Map<ID, E>`, trả `Optional<E>` khi tìm
theo ID và trả immutable snapshot từ `findAll`. Implementation không dùng raw
type hay unchecked cast.

## Bài tập tự kiểm tra

1. Thêm entity `Course(Long id, String title)` và repository tương ứng.
2. Viết `sum(List<? extends Number>)`; giải thích vì sao không thể thêm `Integer`.
3. Viết `addDefaults(List<? super Integer>)` rồi thử với `List<Object>`.
4. Thay `copy` bằng signature không có wildcard và quan sát lời gọi nào không
   compile.
5. Chạy `javap -c -p target/classes/com/studyforge/generics/Box.class` sau
   `mvn compile` để quan sát type erasure.
