# HTTP, Networking, hệ điều hành và hành trình của một request

Tài liệu này cung cấp nền tảng tối thiểu để hiểu một ứng dụng web hoạt động như
thế nào, đồng thời đưa ra các lệnh `curl` có thể dùng ngay khi debug API.

## 1. HTTP

HTTP là giao thức request–response: client gửi **request**, server xử lý và trả
về **response**.

### Cấu trúc request và response

Một HTTP request thường gồm:

- **Method**: hành động client muốn thực hiện, ví dụ `GET` hoặc `POST`.
- **URL**: địa chỉ tài nguyên, gồm scheme, host, port (nếu không dùng port mặc
  định), path và query string; ví dụ
  `https://api.example.com:8443/users?page=1`.
- **Header**: metadata như `Accept`, `Content-Type`, `Authorization` và
  `Cookie`.
- **Body**: dữ liệu gửi lên server, thường là JSON; `GET` thường không có body.

Ví dụ request:

```http
POST /users HTTP/1.1
Host: api.example.com
Content-Type: application/json
Authorization: Bearer <access-token>

{"name":"An"}
```

Response gồm status line, response header và body. **Status code** cho biết kết
quả ở mức giao thức; body cung cấp dữ liệu hoặc chi tiết lỗi.

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /users/42

{"id":42,"name":"An"}
```

### Phân biệt các method

| Method | Mục đích thường dùng | Tính chất quan trọng |
| --- | --- | --- |
| `GET` | Đọc tài nguyên | Safe, idempotent; không nên làm thay đổi trạng thái server |
| `POST` | Tạo tài nguyên hoặc kích hoạt một hành động | Thường không idempotent; gửi lặp có thể tạo nhiều kết quả |
| `PUT` | Thay thế toàn bộ tài nguyên tại URI đã biết | Idempotent; gửi cùng nội dung nhiều lần cho cùng kết quả |
| `PATCH` | Cập nhật một phần tài nguyên | Không mặc định idempotent; phụ thuộc cách thiết kế patch |
| `DELETE` | Xóa tài nguyên | Idempotent về trạng thái cuối, dù response các lần có thể khác |

**Safe** nghĩa là method chỉ dùng để đọc. **Idempotent** nghĩa là thực hiện một
lần hay nhiều lần cùng một request đều tạo ra cùng trạng thái cuối trên server.
Idempotent không có nghĩa là status code hoặc log ở mọi lần phải giống nhau.

### Các nhóm status code

- **`2xx` — thành công:** `200 OK` cho request thành công, `201 Created` khi tạo
  tài nguyên, `204 No Content` khi thành công nhưng không có response body.
- **`4xx` — lỗi phía client:** `400 Bad Request` khi dữ liệu sai, `401
  Unauthorized` khi chưa xác thực hợp lệ, `403 Forbidden` khi đã xác định danh
  tính nhưng không đủ quyền, `404 Not Found`, và `409 Conflict` khi xung đột
  trạng thái.
- **`5xx` — lỗi phía server:** `500 Internal Server Error` cho lỗi không mong
  đợi, `502 Bad Gateway` khi gateway nhận response không hợp lệ từ upstream,
  và `503 Service Unavailable` khi dịch vụ tạm thời không sẵn sàng.

Client không nên tự động retry mọi lỗi. Retry thường phù hợp với một số lỗi tạm
thời (`502`, `503`, timeout), cần giới hạn số lần, dùng exponential backoff và
đặc biệt thận trọng với operation không idempotent.

### Cookie, session và token

- **Cookie** là cặp dữ liệu nhỏ trình duyệt lưu theo chỉ dẫn `Set-Cookie` của
  server rồi tự gửi lại trong header `Cookie` khi domain/path phù hợp. Thuộc
  tính `Secure`, `HttpOnly` và `SameSite` lần lượt hạn chế việc gửi qua kết nối
  không an toàn, truy cập từ JavaScript và một số request cross-site.
- **Session** lưu trạng thái đăng nhập ở server. Cookie thường chỉ giữ một
  session ID ngẫu nhiên; server dùng ID đó để tìm dữ liệu session trong memory,
  database hoặc Redis. Logout hay session hết hạn sẽ làm ID mất hiệu lực.
- **Token** mang thông tin hoặc tham chiếu đến quyền truy cập và thường được gửi
  qua `Authorization: Bearer <token>`. Token có thể là opaque token hoặc JWT.
  JWT được ký để phát hiện sửa đổi, nhưng dữ liệu payload mặc định **không được
  mã hóa**. Token phải có thời hạn, phạm vi quyền phù hợp và không được ghi vào
  log hay commit vào Git.

Cookie là cơ chế lưu/gửi dữ liệu của trình duyệt; session là cách server quản lý
trạng thái; token là credential. Chúng không phải ba lựa chọn hoàn toàn loại trừ
nhau: token hoặc session ID đều có thể được chứa trong cookie.

## 2. Networking

### DNS làm nhiệm vụ gì?

DNS chuyển tên dễ nhớ như `api.example.com` thành địa chỉ IP mà máy có thể định
tuyến tới. Resolver lần lượt có thể kiểm tra cache của trình duyệt/hệ điều hành,
DNS resolver cấu hình trên máy, rồi truy vấn hệ thống DNS phân cấp. Kết quả có
TTL để cache. DNS không tìm URL path như `/users`; path chỉ được gửi cho HTTP
server sau khi kết nối được thiết lập.

### TCP connection được thiết lập như thế nào?

Với HTTP/1.1 hoặc HTTP/2 chạy trên TCP, client mở kết nối bằng **three-way
handshake**:

1. Client gửi `SYN` đến IP và port của server.
2. Server trả `SYN-ACK`.
3. Client trả `ACK`; kết nối sẵn sàng truyền byte.

TCP cung cấp một byte stream tin cậy và có thứ tự: đánh số segment, xác nhận dữ
liệu, truyền lại dữ liệu mất và kiểm soát luồng/tắc nghẽn. Một connection có thể
được tái sử dụng cho nhiều request. HTTP/3 là ngoại lệ phổ biến: nó dùng QUIC
trên UDP thay vì TCP, nhưng vẫn cung cấp các bảo đảm cần thiết ở tầng QUIC.

### TLS bảo vệ điều gì?

Sau TCP handshake, HTTPS thực hiện TLS handshake. Client và server thương lượng
phiên bản/bộ mã, server gửi certificate, client kiểm tra hostname, thời hạn và
chuỗi tin cậy, rồi hai phía tạo khóa phiên. TLS bảo vệ:

- **Confidentiality:** mã hóa nội dung truyền trên mạng.
- **Integrity:** phát hiện dữ liệu bị thay đổi.
- **Authentication:** certificate giúp client xác thực server; mutual TLS có
  thể xác thực cả client.

TLS bảo vệ dữ liệu **trên đường truyền**, không tự bảo vệ dữ liệu trước mã độc ở
endpoint, sau khi server giải mã, hoặc dữ liệu bị ghi lộ trong log/database.

### HTTP và HTTPS khác nhau ở đâu?

HTTPS là HTTP chạy qua TLS. HTTP thuần thường dùng port `80`, dữ liệu có thể bị
đọc hoặc sửa trên đường truyền và không xác thực server. HTTPS thường dùng port
`443`, mã hóa request/response (gồm header, body và path sau khi TLS được thiết
lập), kiểm tra toàn vẹn và xác thực server. IP đích và một số metadata kết nối
vẫn có thể quan sát được; DNS cũng không tự được mã hóa nếu không dùng DoH/DoT.

## 3. Khái niệm hệ điều hành

### Process và thread

- **Process** là một chương trình đang chạy, có không gian địa chỉ và tài nguyên
  riêng như file descriptor. Sự cô lập giúp một process khó trực tiếp làm hỏng
  memory của process khác.
- **Thread** là đơn vị thực thi trong process. Các thread của cùng process chia
  sẻ heap, code và tài nguyên, nhưng mỗi thread có stack và register riêng.

Thread thường nhẹ hơn process nhưng việc chia sẻ memory khiến đồng bộ hóa trở
nên cần thiết. Trong Java, nhiều request có thể được xử lý đồng thời bởi các
thread trong cùng JVM process.

### Stack và heap

- **Stack** thuộc về từng thread, chứa stack frame của lời gọi hàm, biến cục bộ
  và thông tin trả về. Stack nhanh, có vòng đời theo lời gọi hàm và hữu hạn;
  recursion quá sâu có thể gây `StackOverflowError`.
- **Heap** được các thread trong process chia sẻ, chứa phần lớn object tạo bằng
  `new`. Trong Java, garbage collector thu hồi object không còn reachable. Heap
  đầy có thể dẫn đến `OutOfMemoryError`.

Biến local kiểu reference nằm trong stack frame nhưng object mà nó trỏ tới
thường nằm trên heap; JVM có thể tối ưu cách bố trí thực tế.

### Context switching

Khi CPU chuyển từ thread/process đang chạy sang thread/process khác, hệ điều
hành lưu trạng thái thực thi hiện tại và nạp trạng thái tiếp theo. Việc này cho
phép đa nhiệm nhưng có chi phí: scheduler, register và cache CPU phải được cập
nhật. Tạo quá nhiều thread cho công việc nhỏ có thể khiến thời gian context
switch lớn hơn lợi ích chạy đồng thời.

### Race condition

Race condition xảy ra khi kết quả phụ thuộc vào thứ tự thực thi không được kiểm
soát giữa nhiều thread/process. Ví dụ hai request cùng đọc số dư `100`, cùng trừ
`80`, rồi cùng ghi `20`, khiến một lần trừ bị mất. Có thể phòng tránh bằng
transaction/locking phù hợp, atomic operation, immutable data hoặc giảm chia sẻ
mutable state. Chỉ thêm `volatile` không biến chuỗi read–modify–write thành một
operation nguyên tử.

### Deadlock

Deadlock xảy ra khi các luồng chờ tài nguyên của nhau vô thời hạn. Ví dụ thread
A giữ khóa tài khoản 1 và chờ tài khoản 2, trong khi thread B giữ khóa tài khoản
2 và chờ tài khoản 1. Biện pháp phổ biến là luôn lấy khóa theo cùng một thứ tự,
giữ khóa trong thời gian ngắn, dùng timeout/try-lock và để database phát hiện rồi
rollback một transaction. Ứng dụng vẫn phải xử lý hoặc retry có giới hạn.

## 4. Kiểm tra bằng `curl`

Các ví dụ dùng endpoint công khai giả định `https://api.example.com`. Thay URL
và token bằng giá trị của môi trường đang kiểm tra; không dán token thật vào
shell history hoặc tài liệu được commit.

### Request header và response header

Gửi request header bằng `-H`; `-v` hiển thị request header (`>`) và response
header (`<`) cùng thông tin kết nối trên stderr:

```bash
curl -v \
  -H 'Accept: application/json' \
  -H 'Authorization: Bearer <access-token>' \
  https://api.example.com/users/42
```

Chỉ lấy response header với `-I` (gửi `HEAD`), hoặc giữ nguyên method và bỏ body
bằng `-D - -o /dev/null`:

```bash
curl -I https://api.example.com/health
curl -D - -o /dev/null https://api.example.com/users/42
```

### Redirect

Không có `-L`, `curl` hiển thị response redirect nhưng không tự đi theo. Thêm
`-L` để follow header `Location`; `--max-redirs` ngăn vòng lặp vô hạn:

```bash
curl -i https://example.com/old-path
curl -L --max-redirs 5 -i https://example.com/old-path
```

### Status code

`-sS` ẩn progress nhưng vẫn báo lỗi, `-o /dev/null` bỏ body và `-w` in metadata:

```bash
curl -sS -o /dev/null -w '%{http_code}\n' https://api.example.com/health
```

Lưu ý: HTTP `404` hoặc `500` không mặc định làm `curl` trả exit code khác `0`.
Dùng `--fail-with-body` trong script nếu muốn coi status từ `400` là lỗi mà vẫn
giữ body để debug:

```bash
curl --fail-with-body -sS https://api.example.com/users/unknown
```

### Thời gian phản hồi

Đo các mốc DNS, TCP, TLS, byte đầu tiên và tổng thời gian:

```bash
curl -sS -o /dev/null \
  -w 'dns=%{time_namelookup}s tcp=%{time_connect}s tls=%{time_appconnect}s ttfb=%{time_starttransfer}s total=%{time_total}s\n' \
  https://api.example.com/health
```

Các giá trị là mốc tích lũy từ lúc bắt đầu lệnh. Có thể suy ra gần đúng thời
gian từng pha bằng phép trừ, nhưng connection reuse, proxy và protocol có thể
làm kết quả khác mô hình đơn giản.

## 5. Hành trình đầy đủ từ URL đến HTTP response

Giả sử người dùng nhập `https://shop.example.com/products/42` vào trình duyệt.
Trình duyệt phân tích URL: scheme `https` quyết định dùng HTTP qua TLS, hostname
là `shop.example.com`, port mặc định là `443`, còn `/products/42` là path. Nó có
thể kiểm tra cache, service worker, HSTS và chính sách proxy trước khi truy cập
mạng.

Nếu chưa có địa chỉ IP hợp lệ trong cache, máy thực hiện **DNS resolution**.
Resolver tìm bản ghi phù hợp (thường là `A` cho IPv4 hoặc `AAAA` cho IPv6), có
thể đi qua cache và hệ thống DNS phân cấp, rồi trả về một hay nhiều IP. Trình
duyệt chọn địa chỉ để kết nối; IP đó có thể thuộc CDN hoặc load balancer chứ
không phải máy chạy backend cuối cùng.

Với HTTP/1.1 hoặc HTTP/2, client thiết lập **TCP connection** đến IP trên port
443 bằng SYN → SYN-ACK → ACK. Tiếp theo là **TLS handshake**: hai phía thương
lượng tham số mật mã, client xác minh certificate của server và thiết lập khóa
phiên. ALPN có thể chọn HTTP/2 hoặc HTTP/1.1. Nếu dùng HTTP/3, QUIC trên UDP kết
hợp thiết lập transport và bảo mật TLS theo một luồng khác, không có TCP
handshake.

Khi kênh bảo mật sẵn sàng, trình duyệt gửi **HTTP request**, chẳng hạn `GET
/products/42`, kèm hostname, định dạng chấp nhận, cookie hợp lệ và các header
khác. TLS mã hóa request trên đường truyền. Request có thể đi qua CDN, reverse
proxy, WAF hoặc load balancer. Những lớp này có thể trả cache ngay, chặn request
không hợp lệ, kết thúc TLS hoặc chuyển tiếp request đến một backend khỏe mạnh.

Tại **backend**, web server nhận request và framework định tuyến path/method đến
controller. Middleware/filter có thể gắn request ID, ghi log an toàn, xác thực
session/token, kiểm tra quyền, rate limit và validate input. Controller nên gọi
service chứa nghiệp vụ; service phối hợp repository và transaction thay vì đặt
toàn bộ logic trong controller.

Repository gửi query qua connection pool đến **database**. Database parse và
lập kế hoạch query, dùng index nếu phù hợp, lấy lock/đảm bảo isolation khi cần,
đọc hoặc cập nhật dữ liệu rồi commit hay rollback transaction. Nó trả rows hoặc
kết quả cập nhật về backend. Database không trực tiếp tạo HTTP response; backend
mapping dữ liệu thành DTO và xử lý trường hợp không tìm thấy hay lỗi nghiệp vụ.

Cuối cùng server serialize DTO thành JSON và tạo **HTTP response** với status
code, header (`Content-Type`, cache policy, có thể có `Set-Cookie`) và body. Các
proxy có thể nén hoặc cache response trước khi các byte được gửi về trên kết nối
đã mã hóa. Trình duyệt giải mã TLS, đọc status/header/body, cập nhật cookie và
cache theo chính sách, rồi render nội dung hoặc cung cấp dữ liệu cho JavaScript.
Kết nối có thể được giữ lại để tái sử dụng, giúp các request sau tránh lặp lại
toàn bộ chi phí thiết lập.

Luồng tổng quát:

```text
URL → DNS → TCP/QUIC → TLS → HTTP request
    → CDN/proxy/load balancer → backend → database
    → backend → HTTP response → trình duyệt
```

Khi debug chậm hoặc lỗi, hãy xác định pha cụ thể thay vì chỉ nói “server chậm”:
DNS có chậm không, TCP/TLS có kết nối được không, proxy trả status nào, time to
first byte bao lâu, backend mất thời gian ở nghiệp vụ hay database, và response
có bị redirect hoặc retry ngoài dự kiến không.
