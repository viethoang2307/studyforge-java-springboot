# Lý thuyết — Object Contracts

## 1. Bức tranh tổng quan

Một object tốt không chỉ cần chứa dữ liệu và có các method chạy đúng. Nó còn phải
giữ được các **contract** mà Java Collections, test framework và code gọi nó cùng
dựa vào. Lab này tập trung vào bốn câu hỏi:

- Khi nào hai object được xem là cùng một giá trị?
- `equals` và `hashCode` phải phối hợp với nhau như thế nào?
- Vì sao value object nên immutable?
- Điều gì xảy ra khi một field dùng để tính hash bị thay đổi sau khi object đã
  được đưa vào `HashSet` hoặc `HashMap`?

## 2. `equals` là equality logic, không phải identity

`==` kiểm tra hai reference có trỏ tới cùng một object hay không. `equals` trả lời
câu hỏi ở tầng nghiệp vụ: hai object có đại diện cho cùng một giá trị hay không.

Ví dụ, hai `Money` được tạo từ `10.0 USD` và `10.00 USD` là hai instance khác
nhau nhưng cùng một giá trị tiền. Vì vậy chúng cần thỏa mãn:

```java
Money first = Money.of("10.0", "USD");
Money second = Money.of("10.00", "USD");

first.equals(second);       // true
first == second;             // false
first.hashCode() == second.hashCode(); // true
```

Một implementation của `equals` phải giữ các tính chất sau:

1. **Reflexive**: `x.equals(x)` là `true`.
2. **Symmetric**: `x.equals(y)` giống `y.equals(x)`.
3. **Transitive**: nếu `x = y` và `y = z` thì `x = z`.
4. **Consistent**: kết quả không tự thay đổi khi trạng thái liên quan không đổi.
5. Với `null`, kết quả luôn là `false`.

## 3. Contract giữa `equals` và `hashCode`

Nếu `x.equals(y)` là `true`, bắt buộc `x.hashCode() == y.hashCode()`. Chiều
ngược lại không bắt buộc: hai object có thể cùng hash nhưng vẫn không bằng nhau.

`HashSet` và `HashMap` dùng hash để chọn bucket trước, sau đó mới dùng `equals`
để xác nhận. Vì vậy nếu một field tham gia vào cả hai method thay đổi sau khi
insert, lookup có thể không tìm thấy chính reference đang nằm trong collection.

`MutableHashKeyDemo` cố ý minh họa lỗi này:

```java
Set<MutableKey> keys = new HashSet<>();
keys.add(key);       // bucket được chọn từ hash("before")
key.setValue("after");
keys.contains(key);  // false: lookup tìm bucket của hash("after")
```

Reference không biến mất khỏi bộ nhớ; nó chỉ trở thành entry “mồ côi” trong bucket
cũ. Cách an toàn là dùng key immutable, hoặc không thay đổi field dùng cho
`equals`/`hashCode` trong suốt thời gian key đang được dùng bởi hash collection.

## 4. Thiết kế value object immutable

Value object được nhận diện bởi giá trị, không phải bởi một identity kỹ thuật như
địa chỉ object hay số lần tạo instance. Một value object nên có các đặc điểm:

- field là `private final`;
- constructor không để lộ trạng thái chưa hợp lệ;
- factory method chuẩn hóa và validate input;
- không có setter làm thay đổi giá trị sau khi tạo;
- không trả ra collection mutable nội bộ;
- method biến đổi trả về object mới.

`Money` áp dụng các nguyên tắc này. `BigDecimal` được canonicalize bằng
`stripTrailingZeros`, còn phép `add` kiểm tra currency rồi trả về `Money` mới.
Do đó:

```java
Money original = Money.of("10", "USD");
Money result = original.add(Money.of("2.50", "USD"));
// original vẫn là 10 USD; result là 12.5 USD
```

`BigDecimal` là một pitfall quan trọng: `equals` phân biệt scale
(`10.0` khác `10.00`), còn `compareTo` xem chúng có cùng giá trị số. Nếu nghiệp
vụ coi chúng giống nhau, phải chuẩn hóa scale hoặc dùng `compareTo` trong
`equals` và tạo `hashCode` tương thích. Lab chọn cách chuẩn hóa trước khi so sánh.

## 5. Record, validation và defensive copy

`AccountId` là record: Java tự sinh accessor, `equals`, `hashCode` và
`toString` dựa trên component. Compact constructor được dùng để chặn `null`.
Record giảm boilerplate nhưng không tự động validate nghiệp vụ; validation vẫn
phải viết trong constructor.

`Email` normalize bằng `strip()` và `toLowerCase(Locale.ROOT)` trước khi lưu.
Nhờ vậy hai cách viết khác nhau về khoảng trắng hoặc chữ hoa vẫn có equality ổn
định. Regex ở đây chỉ là validation đơn giản cấp ứng dụng, không phải đặc tả đầy
đủ mọi email hợp lệ trên Internet.

`AccountSnapshot` dùng `List.copyOf` ở constructor. Đây là defensive copy: thay
đổi list nguồn sau đó không ảnh hưởng snapshot. List trả về cũng không cho phép
caller sửa trực tiếp. Defensive copy chỉ bảo vệ collection; các phần tử bên trong
cũng cần immutable nếu muốn snapshot thực sự an toàn.

## 6. Cách đọc code và test của lab

- `Money`: equality theo `amount + currency`, canonicalize số tiền, phép cộng
  cùng currency.
- `AccountId`: identity kiểu mạnh quanh `UUID` thay vì truyền UUID/string rời rạc.
- `Email`: normalize trước khi so sánh.
- `AccountSnapshot`: bảo vệ collection bằng copy.
- `MutableHashKeyDemo`: phản ví dụ về key mutable.

Các test kiểm tra cả positive case và invariant: reflexivity, symmetry,
transitivity, hash consistency, bất biến sau method `add`, và khả năng truy cập
collection sau khi key bị đổi. Khi thêm class value object mới, hãy viết test
contract trước khi tối ưu implementation.

## 7. Bài tập gợi ý

1. Tạo `PhoneNumber` immutable, normalize input và validate format tối thiểu.
2. Viết test cho `equals`/`hashCode` của class mới với ít nhất ba giá trị tương
   đương.
3. Thử bỏ `stripTrailingZeros` khỏi `Money` và quan sát test nào thất bại.
4. Sửa `MutableHashKeyDemo` bằng key immutable và giải thích vì sao lookup hoạt
   động lại.

## Checklist cần nhớ

- `equals` mô tả equality nghiệp vụ; `==` mô tả cùng reference.
- Object bằng nhau phải có cùng hash code.
- Không mutate field dùng trong `equals`/`hashCode` khi object đang là hash key.
- Immutable object dễ chia sẻ, cache và test hơn.
- Defensive copy bảo vệ boundary của aggregate.
