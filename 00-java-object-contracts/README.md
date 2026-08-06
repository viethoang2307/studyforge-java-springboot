# Object identity, equality và immutable value object

Module nhỏ này biến các contract của `Object` thành ví dụ chạy được và test tự
động. Chạy toàn bộ bài học bằng:

```bash
cd 00-java-object-contracts
mvn test
mvn package
java -cp target/classes dev.studyforge.demo.MutableHashKeyDemo
```

## 1. Object identity và logical equality

- **Identity** hỏi hai biến có trỏ tới đúng cùng một object hay không, dùng
  `a == b` (hoặc `assertSame`).
- **Logical equality** hỏi hai object có biểu diễn cùng một giá trị nghiệp vụ
  hay không, dùng `a.equals(b)`.

Hai `Money` được tạo riêng biệt có identity khác nhau nhưng vẫn equal nếu amount
đã chuẩn hóa và currency giống nhau. Không override `equals`, implementation từ
`Object` chỉ so identity.

## 2. Contract của `equals`

Với các object không null `x`, `y`, `z`, `equals` phải:

1. **Reflexive:** `x.equals(x)` là `true`.
2. **Symmetric:** `x.equals(y)` và `y.equals(x)` có cùng kết quả.
3. **Transitive:** nếu `x` equal `y`, `y` equal `z`, thì `x` equal `z`.
4. **Consistent:** gọi lại cho cùng state phải trả cùng kết quả.
5. `x.equals(null)` luôn là `false`.

`ValueObjectTest` kiểm chứng từng tính chất thay vì chỉ test một cặp object.

## 3. Contract giữa `equals` và `hashCode`

- Nếu `a.equals(b)` thì bắt buộc `a.hashCode() == b.hashCode()`.
- Hai object không equal **vẫn có thể** trùng hash (collision).
- Khi field tham gia `equals` thay đổi thì kết quả `hashCode` thường thay đổi;
  bởi vậy không thay đổi key trong lúc nó nằm trong hash collection.
- Override `equals` thì phải override `hashCode` bằng cùng tập field.

Hash chỉ chọn bucket; collection vẫn dùng `equals` để tìm đúng entry trong
bucket đó. Hash bằng nhau không chứng minh hai object equal.

## 4. Thí nghiệm mutable hash key và lỗi quan sát được

`MutableHashKeyDemo` thêm cùng một mutable key vào `HashSet` và `HashMap`, rồi
đổi `value`—field được cả `equals` và `hashCode` sử dụng. Reference không biến
mất: iteration vẫn thấy nó. Tuy nhiên `contains`, `get`, và `remove` tính hash
mới rồi tìm bucket mới, trong khi entry vẫn nằm ở bucket được chọn bởi hash cũ.
Kết quả là entry trở nên “mất dấu”; map có thể giữ dữ liệu không truy xuất/xóa
được bằng key thông thường. Đây không phải lỗi của collection mà là key đã phá
giả định rằng hash ổn định trong thời gian lưu trữ.

Giải pháp ưu tiên là dùng key/value object immutable. Nếu thật sự phải đổi key,
hãy remove bằng state cũ trước, thay đổi, rồi insert lại; cách này dễ sai hơn.

## 5. Thiết kế các immutable value object

- `Money`: `final`, field `final`, dùng `BigDecimal` và `Currency`; chuẩn hóa
  trailing zero để `10.0 USD` equal `10.00 USD`; phép `add` tạo object mới.
- `AccountId`: record bọc `UUID`, tránh truyền nhầm một `String` bất kỳ làm ID.
- `Email`: validate và normalize đúng một lần ở factory; equality dùng giá trị
  canonical. Việc lowercase toàn bộ địa chỉ là **quy tắc của ví dụ này**; ứng
  dụng thực cần quyết định normalization theo yêu cầu nghiệp vụ.

Các thành phần được giữ bên trong ba object trên cũng immutable. Vì vậy accessor
có thể trả chúng trực tiếp mà không làm lộ state có thể sửa.

## 6. Defensive copy

`AccountSnapshot` nhận một `List<Money>` và dùng `List.copyOf` tại boundary.
Việc này vừa tách snapshot khỏi list của caller, vừa không cho caller sửa list
qua accessor. Một unmodifiable view như `Collections.unmodifiableList(source)`
chưa đủ: caller vẫn có thể sửa `source`.

Với phần tử mutable, chỉ copy list là **shallow copy** và chưa đủ; cần copy từng
phần tử hoặc chuyển chúng thành immutable value. Test bao phủ cả hai hướng tấn
công: sửa list đầu vào sau constructor và sửa list trả về từ accessor.
