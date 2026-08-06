# Mô hình dữ liệu và object trong Java

Tài liệu này ghi lại mô hình tư duy tôi dùng khi đọc code Java. Đây là **mô
hình để suy luận**, không phải sơ đồ vật lý bắt buộc của JVM: JIT có thể tối ưu
và đặc tả Java không cam kết mọi biến cục bộ thật sự nằm trên stack.

## 1. Primitive type và reference type

Java có tám primitive type: `byte`, `short`, `int`, `long`, `float`, `double`,
`char` và `boolean`. Một biến primitive giữ **chính giá trị** theo kiểu đó. Ví
dụ, sau `int a = 10`, value của `a` là số `10`; gán `int b = a` sao chép `10`,
nên đổi `b` không ảnh hưởng `a`.

Mọi kiểu còn lại (class, interface, array, enum, record...) là reference type.
Biến reference không chứa toàn bộ object mà chứa một **reference value** cho
phép tìm tới object, hoặc giá trị đặc biệt `null`. Gán `Person second = first`
sao chép reference; không nhân bản `Person`. Vì hai reference có thể chỉ cùng
một object, thay đổi object qua `second` cũng quan sát được qua `first`.

Khác biệt thực dụng:

| Câu hỏi | Primitive | Reference |
| --- | --- | --- |
| Biến giữ gì? | Giá trị primitive | Reference tới object hoặc `null` |
| Giá trị mặc định của field | `0`, `false`, `\u0000` tùy kiểu | `null` |
| Có gọi method trực tiếp không? | Không (có thể được autobox) | Có, nếu không `null` |
| Phép gán sao chép gì? | Giá trị | Reference value |

Biến cục bộ không được tự động cấp giá trị mặc định; compiler buộc gán trước
khi đọc, bất kể nó là primitive hay reference.

## 2. Mô hình stack reference và heap object

Khi gọi method, tôi hình dung JVM tạo một **stack frame** chứa tham số, biến
cục bộ và dữ liệu phục vụ lời gọi. Một biến cục bộ như `Account account` giữ
reference value trong frame; object `Account` được hình dung ở **heap**, nơi
nhiều reference có thể cùng trỏ đến:

```text
stack frame                         heap
+--------------------+             +----------------------+
| account: ref A ----+------------>| Account(balance=100) |
| alias:   ref A ----+------------>|                      |
+--------------------+             +----------------------+
```

`account = null` chỉ thay reference trong biến `account`; nó không xóa object
và không thay `alias`. Object chỉ có thể được garbage collector thu hồi khi
không còn reachable từ các GC root. Tôi không dựa vào thời điểm thu hồi vì Java
không bảo đảm nó xảy ra ngay.

## 3. `==`, `equals`, mutable và immutable

Với primitive, `==` so sánh value (sau các phép numeric promotion nếu có). Với
reference, `==` trả lời “hai reference có trỏ tới **cùng một object** không?”.
Nó không tự so sánh nội dung.

`equals` là method dành cho so sánh bình đẳng về mặt logic. Cài đặt mặc định từ
`Object` gần giống identity, nhưng các class như `String`, wrapper và nhiều
value object override nó để so sánh nội dung. Vì vậy:

```java
String x = new String("Java");
String y = new String("Java");

x == y       // false: hai object
x.equals(y)  // true: cùng nội dung
```

Không gọi `x.equals(y)` nếu `x` có thể là `null`; có thể dùng
`Objects.equals(x, y)`. Với array, `equals` vẫn là identity; dùng
`Arrays.equals` (hoặc `deepEquals` cho cấu trúc lồng). Khi override `equals`,
cần override `hashCode` nhất quán để object hoạt động đúng trong `HashMap` và
`HashSet`.

Object **mutable** cho phép trạng thái quan sát được thay đổi sau khi tạo, ví dụ
`ArrayList` hay `StringBuilder`. Object **immutable** không cho thay đổi trạng
thái sau khi tạo, ví dụ `String`, `Integer` và `BigDecimal`; thao tác có vẻ sửa
chúng thực ra trả về object mới. `final` trên một reference chỉ cấm gán reference
khác, không tự làm object được trỏ tới trở thành immutable.

## 4. Java luôn pass-by-value

Mỗi lần gọi method, Java sao chép **value của đối số** vào tham số:

- Với `int`, value được sao chép là con số. Gán lại tham số không đổi biến ở
  caller.
- Với `Account`, value được sao chép là reference. Caller và callee tạm thời có
  hai biến reference cùng tới một object, nên callee có thể mutate object đó.
- Gán tham số sang `new Account(...)` chỉ thay bản sao reference của callee,
  không làm biến của caller trỏ sang object mới.

Do đó câu “object được truyền bằng reference” dễ gây hiểu sai. Chính xác hơn là
**reference được truyền bằng value**. Java không có pass-by-reference theo nghĩa
callee có thể gán lại trực tiếp biến của caller.

## 5. Autoboxing, unboxing và wrapper cache

Autoboxing là compiler chuyển primitive thành wrapper khi ngữ cảnh cần object,
ví dụ `Integer boxed = 42` tương đương ý tưởng với `Integer.valueOf(42)`.
Unboxing chuyển ngược lại, ví dụ `int n = boxed` gọi giá trị `int` bên trong.

Hai bẫy quan trọng:

1. Unbox `null` ném `NullPointerException`.
2. `==` giữa hai wrapper thường so identity, nên kết quả có thể phụ thuộc cache.

Java bảo đảm các kết quả boxing của một số constant value thường dùng có cùng
identity, trong đó `Integer` từ `-128` đến `127`. Không nên biến chi tiết cache
thành logic chương trình: so wrapper bằng `equals`, hoặc unbox có chủ đích sau
khi xử lý `null`.

## 6. Overflow, floating-point và `BigDecimal`

Số nguyên Java có độ rộng cố định. Khi phép toán vượt miền biểu diễn, kết quả
quấn vòng theo phần bit thấp mà không tự ném exception. Ví dụ
`Integer.MAX_VALUE + 1 == Integer.MIN_VALUE`. Nếu overflow là lỗi nghiệp vụ,
dùng `Math.addExact`, `subtractExact`, `multiplyExact`... để nhận
`ArithmeticException`, hoặc chọn `BigInteger` khi cần miền tùy ý.

`float` và `double` dùng biểu diễn nhị phân dấu phẩy động. Nhiều phân số thập
phân, như `0.1`, không có biểu diễn nhị phân hữu hạn, nên `0.1 + 0.2` không nhất
thiết bằng chính xác `0.3`. Chúng vẫn phù hợp cho nhiều bài toán khoa học khi có
sai số cho phép, nhưng không phù hợp để lưu tiền cần quy tắc thập phân chính xác.

`BigDecimal` biểu diễn số thập phân theo unscaled integer và scale. Nên tạo từ
chuỗi (`new BigDecimal("0.1")`) hoặc dùng `BigDecimal.valueOf(double)`, tránh
`new BigDecimal(0.1)` vì constructor đó giữ lại xấp xỉ nhị phân của `double`.
Các phép toán trả object mới; phải nhận kết quả. `equals` xét cả value lẫn scale,
nên `1.0` không `equals` `1.00`; `compareTo` trả `0` nếu chúng bằng nhau về giá
trị số. Phép chia không kết thúc cần chỉ rõ scale và `RoundingMode`.

## 7. Các chương trình kiểm chứng

Mã trong [`examples/object-model`](examples/object-model) cố ý nhỏ và chỉ dùng
JDK. Chạy toàn bộ bằng:

```bash
bash examples/object-model/run-examples.sh
```

Script bật assertions (`-ea`) và chạy lần lượt:

- `PrimitiveAndReferenceDemo`: phép gán primitive so với reference.
- `EqualityAndMutabilityDemo`: identity, equality, mutable và immutable.
- `PassByValueDemo`: mutate object được nhưng không gán lại biến của caller.
- `BoxingDemo`: boxing, unboxing, `null` và cache.
- `NumberPitfallsDemo`: overflow, precision và cách tạo/so sánh `BigDecimal`.

Mỗi assertion là một kết luận có thể làm sai để quan sát chương trình thất bại;
phần output giúp đối chiếu kết quả khi học bằng debugger.
