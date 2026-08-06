# Interface, abstract class và composition

Ví dụ nhỏ này minh họa cách đưa hành vi gửi thông báo vào một service mà không
buộc service kế thừa một lớp gửi cụ thể. Chạy bằng Java 17 và Maven:

```bash
mvn test
```

## 1. Interface và abstract class

| | Interface | Abstract class |
| --- | --- | --- |
| Mục đích chính | Mô tả **khả năng/contract** | Chia sẻ contract, state và phần implementation cho một họ class có quan hệ chặt |
| Kế thừa | Một class implement được nhiều interface | Một class chỉ extend được một class |
| State | Không có instance state; field mặc định là `public static final` | Có instance field, constructor và state dùng chung |
| Method | Abstract, `default`, `static` và `private` method | Abstract và concrete method với nhiều access modifier |
| Khi dùng | Các class khác nhau cần cùng một capability | Các subtype thực sự cùng một bản chất và cần invariant/template chung |

`Notification` là interface vì Email, SMS và Console chỉ cùng contract
`send`; chúng không cần chung state hay vòng đời. Nếu nhiều notification thực
sự cần cùng state bắt buộc (ví dụ retry counter được quản lý theo cùng một
template), abstract class *có thể* hợp lý. Đừng tạo superclass chỉ để tái sử
dụng vài dòng format/validation; hãy tách helper hoặc cộng tác viên riêng.

## 2. Default method

`Notification.sendIfPresent` có implementation ngay trong interface. Default
method hữu ích để thêm hành vi tương thích ngược vào contract: implementation
cũ không bắt buộc sửa ngay, nhưng vẫn có thể `override`. Nó phù hợp với hành vi
nhỏ dựa trên contract hiện có, không phải nơi cất mutable state hoặc nghiệp vụ
phức tạp. Trong ví dụ, method bỏ qua message rỗng rồi gọi method đa hình
`send`.

Nếu một class nhận hai interface có default method cùng chữ ký, class đó phải
override để tự giải quyết xung đột. Method của class cha cụ thể luôn được ưu
tiên hơn default method của interface.

## 3. “is-a” và “has-a”

- **is-a (inheritance):** `EmailNotification is-a Notification`, nên có thể
  thay nó vào mọi nơi cần `Notification` mà vẫn giữ đúng contract.
- **has-a (composition):** `OrderService has-a Notification`. Gửi thông báo là
  một dependency/hành vi được ủy quyền, không phải bản chất của order service.

Một phép thử nhanh: câu “A là một B” có đúng về nghiệp vụ và subtype có thay thế
được base type không (Liskov Substitution Principle)? Nếu chỉ muốn gọi lại code
của B, đó không phải lý do đủ để inheritance.

## 4. Abstraction và ba implementation

- `Notification`: contract và default method dùng chung.
- `EmailNotification`, `SmsNotification`, `ConsoleNotification`: ba strategy
  triển khai cách gửi khác nhau.
- `OrderService`: nhận `Notification` qua constructor và chỉ phụ thuộc vào
  abstraction. Có thể chọn strategy ở composition root/configuration; test dùng
  lambda fake mà không gửi thật.

Ví dụ wiring:

```java
Notification notification = new EmailNotification(System.out);
OrderService service = new OrderService(notification);
service.confirm("A-42", "customer@example.com");
```

Đổi `EmailNotification` thành `SmsNotification` không cần sửa `OrderService`.
Trong Spring, ba implementation có thể là bean và được inject qua constructor;
khi có nhiều bean, chọn bằng `@Qualifier` hoặc cấu hình tạo bean. Domain service
vẫn nên là Java thuần, không cần biết framework.

## 5. Ví dụ inheritance sai và bản refactor

`legacy.BadEmailOrderService extends EmailSenderBase` chỉ để dùng lại
`sendEmail`. Thiết kế này phát biểu sai rằng order service **là một** email
sender, làm public model khó hiểu, gắn cứng Email và dùng mất “suất” superclass
duy nhất của Java.

Bản refactor là `OrderService` **có một** `Notification` và delegate việc gửi.
Code dùng chung nằm sau contract đúng, thay vì bị lấy làm lý do tạo quan hệ cha
con. Constructor injection cũng làm dependency bắt buộc, rõ ràng và dễ fake.

## 6. Trade-off

### Inheritance

**Ưu điểm**

- Ngắn gọn khi subtype thực sự có quan hệ is-a ổn định.
- Abstract class quản lý tốt invariant, protected state hoặc template method
  chung cho một họ class chặt chẽ.
- Có polymorphism trực tiếp qua base type.

**Nhược điểm**

- Coupling mạnh với superclass; thay đổi protected behavior dễ ảnh hưởng mọi
  subtype (fragile base class).
- Chọn quan hệ ở compile time, không đổi behavior linh hoạt lúc runtime.
- Java chỉ cho single class inheritance; hierarchy sâu khó đọc và test.
- Dễ vi phạm LSP nếu kế thừa chỉ nhằm reuse code.

### Composition

**Ưu điểm**

- Coupling qua interface nhỏ; behavior có thể thay hoặc bọc (decorator) độc lập.
- Dễ unit test bằng fake và giữ class tập trung vào một trách nhiệm.
- Mô hình đúng quan hệ has-a và tránh hierarchy sâu.

**Nhược điểm**

- Nhiều object, constructor parameter và code delegate/wiring hơn.
- Cần quyết định lifetime/configuration của dependency.
- Nếu abstraction được tạo quá sớm chỉ có một implementation, nó có thể tăng
  độ phức tạp mà chưa tạo giá trị.

Quy tắc thực dụng: ưu tiên composition cho behavior thay đổi được; dùng
inheritance khi mô hình domain thực sự là is-a và base class bảo vệ một invariant
có ý nghĩa, không chỉ để tiết kiệm vài dòng.
