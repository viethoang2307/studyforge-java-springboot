# Lý thuyết — Generics

## 1. Vì sao cần generics?

Generics cho phép viết code dùng lại với nhiều kiểu nhưng vẫn kiểm tra kiểu ở
compile time. Không có generics, một container thường phải trả `Object`, caller
phải cast và lỗi chỉ xuất hiện lúc chạy:

```java
Object value = "Java";
Integer number = (Integer) value; // lỗi runtime
```

Với `Box<String>`, compiler biết `get()` trả `String` và cấm `set(123)`. Generics
vì vậy vừa tăng khả năng tái sử dụng vừa loại bỏ nhiều cast không an toàn.

## 2. Type parameter và generic class

`Box<T>` dùng `T` như một placeholder được thay bằng kiểu cụ thể tại nơi sử dụng.
`T` không phải một kiểu runtime mới; nó là thông tin để compiler kiểm tra quan hệ
giữa input và output.

```java
Box<String> text = new Box<>("Java");
String value = text.get();
```

Diamond operator `<>` cho phép compiler suy luận type argument. Constructor của
`Box` reject `null`, nên class này có invariant rằng giá trị luôn tồn tại.

## 3. Bounded type parameter

Method `max` cần so sánh phần tử. Thay vì nhận mọi `T`, nó đặt bound:

```java
<T extends Comparable<? super T>> T max(List<? extends T> values)
```

Có hai ý riêng:

- `T extends Comparable...`: `T` phải cung cấp khả năng so sánh.
- `? super T`: `T` có thể compare thông qua chính nó hoặc một supertype của nó.

Đây là dạng recursive bound thường gặp trong Java Collections. Nó linh hoạt hơn
`T extends Comparable<T>` nhưng vẫn giữ được compile-time safety.

Method cũng reject list rỗng bằng exception rõ ràng. Đây là precondition của bài
toán tìm maximum, không nên để lỗi `IndexOutOfBoundsException` tình cờ xuất hiện.

## 4. Wildcard và nguyên tắc PECS

Wildcard `?` biểu diễn một type chưa biết. Khi collection là nguồn dữ liệu, dùng
`extends`; khi collection là nơi nhận dữ liệu, dùng `super`.

**PECS — Producer Extends, Consumer Super**:

```java
public static <T> void copy(
        List<? extends T> source,
        List<? super T> destination) {
    destination.addAll(source);
}
```

`List<Integer>` có thể produce `Integer` cho `T = Number`, còn
`List<Number>` có thể consume những `Integer` đó. Nhưng `List<Integer>` không
phải subtype của `List<Number>`: nếu được phép, code có thể thêm `Double` vào
list integer. Đây là lý do Java dùng invariant generics.

Wildcard không phải lúc nào cũng cần. Dùng type parameter khi cần diễn đạt quan
hệ giữa nhiều vị trí; dùng wildcard khi chỉ cần giới hạn một vị trí sử dụng.

## 5. Generic interface với nhiều ràng buộc

`Repository<ID, E extends Identifiable<ID>>` mô tả repository cho entity có ID
đúng kiểu `ID`. Ràng buộc này ngăn cấu hình vô nghĩa và cho phép repository gọi
`entity.id()` mà không cast.

```java
Repository<UUID, Student> repository = new InMemoryRepository<>();
```

`InMemoryRepository` dùng `Map<ID, E>` để lưu trữ, `Optional<E>` cho kết quả có
thể không tồn tại và `List.copyOf` để không lộ collection nội bộ. Việc save cùng
ID cập nhật entity cũ, đúng semantics của map.

## 6. Type erasure và giới hạn

Java generics chủ yếu được kiểm tra lúc compile rồi bị type erasure khi chạy. Vì
vậy không thể làm các việc như `new T()`, `T.class` hoặc `instanceof List<String>`
trực tiếp. Khi cần thông tin runtime, thường phải truyền `Class<T>`, factory
hoặc strategy vào API.

Type erasure giúp tương thích với Java cũ nhưng cũng có nghĩa generic type không
phải một phần đầy đủ của runtime type identity. Đừng dùng reflection để thay thế
cho một API generic rõ ràng nếu không thật sự cần.

## 7. Cách đọc code và test

- `Box<T>`: generic class đơn giản, type-safe getter/setter.
- `GenericAlgorithms.max`: bounded type parameter và wildcard producer.
- `GenericAlgorithms.copy`: PECS trong một API thực tế.
- `Identifiable`, `Repository` và `InMemoryRepository`: ràng buộc giữa ID và entity.

Test repository dùng record `Student` cục bộ để chứng minh cùng một
implementation có thể dùng cho entity bất kỳ miễn là entity implement đúng
contract. Việc test không cần cast là một acceptance criterion quan trọng.

## 8. Bài tập gợi ý

1. Thêm `findFirst` nhận `List<? extends T>` và predicate tương ứng.
2. Viết `swap(List<T>, int, int)` để luyện invariant type parameter.
3. Tạo entity `Course` có `Long` ID và dùng lại `InMemoryRepository`.
4. Thử thay `List<? super T>` trong `copy` bằng `List<T>` và ghi lại API nào bị
   mất khả năng sử dụng.

## Checklist cần nhớ

- Generics chuyển nhiều lỗi cast từ runtime về compile time.
- `extends` đọc/produce; `super` ghi/consume.
- Bounded type diễn đạt capability cần thiết của thuật toán.
- Không nên phá type safety bằng raw type hoặc cast tùy tiện.
- Generic repository nên ràng buộc entity bằng interface nhỏ, rõ nghĩa.
