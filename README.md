# StudyForge — Lộ trình Java Backend trong 12 tháng

Lộ trình này dành cho người bắt đầu từ con số 0, học khoảng **10–12 giờ mỗi
tuần**. Mục tiêu cuối năm là có nền tảng Java Backend vững, một project
Digital Wallet đủ chiều sâu để trình bày khi ứng tuyển Junior, và khả năng giải
thích các quyết định kỹ thuật thay vì chỉ chạy được tutorial.

> **Nguyên tắc xuyên suốt:** dành khoảng 30% thời gian cho lý thuyết và 70% cho
> tự code. Sau mỗi chủ đề, hãy làm lại không nhìn tutorial. Với dữ liệu tiền tệ,
> dùng `BigDecimal` hoặc số nguyên theo đơn vị nhỏ nhất; **không dùng `double`**.

## Mục lục

- [Tháng 1–2: Java Core](#tháng-12-từ-zero-đến-java-core)
- [Tháng 3: Computer Science](#tháng-3-nền-tảng-computer-science)
- [Chuyên đề: Java Concurrency](#chuyên-đề-java-concurrency)
- [Tháng 4: SQL và PostgreSQL](#tháng-4-sql-và-postgresql)
- [Tháng 5–6: Spring Boot và REST API](#tháng-56-spring-boot-và-rest-api)
- [Tháng 7: Security và testing](#tháng-7-security-và-testing)
- [Tháng 8–10: Backend production](#tháng-810-backend-production)
- [Tháng 11–12: Capstone](#tháng-1112-capstone-digital-wallet-system)
- [Lịch học và cách tự đánh giá](#lịch-học-mỗi-tuần)
- [Nguồn học chính thức](#nguồn-học-chính-thức)

## Tháng 1–2: Từ zero đến Java Core

### Kiến thức

- Biến, kiểu dữ liệu, điều kiện, vòng lặp và hàm.
- `Array`, `String`, `List`, `Set`, `Map`.
- Class, object, interface và abstract class.
- Encapsulation, inheritance và polymorphism.
- Exception, file I/O và generics.
- Lambda và Stream API.
- Debug bằng IntelliJ IDEA.
- Maven và JUnit cơ bản.

### Project: Banking CLI

Xây dựng ứng dụng dòng lệnh có thể:

- Tạo tài khoản và tra cứu số dư.
- Nạp, rút và chuyển tiền.
- Lưu rồi đọc lại lịch sử giao dịch từ file.
- Validation dữ liệu đầu vào và exception có ý nghĩa.
- Unit test cho các nghiệp vụ quan trọng.

**Definition of Done**

- [ ] Số dư không thể âm sau một giao dịch hợp lệ.
- [ ] Chuyển tiền cập nhật hai tài khoản hoặc không cập nhật tài khoản nào.
- [ ] Số tiền bằng 0, âm, sai định dạng và tài khoản không tồn tại đều bị từ chối.
- [ ] Khởi động lại chương trình vẫn đọc được lịch sử đã lưu.
- [ ] `mvn test` chạy xanh, bao gồm cả happy path và failure path.

## Tháng 3: Nền tảng Computer Science

### DSA đủ dùng cho phỏng vấn Junior

- Big-O; array và linked list.
- Stack, queue và hash map.
- Binary search và các thuật toán sorting phổ biến.
- Tree và recursion cơ bản.
- Mỗi tuần giải 3–5 bài Easy; khi ổn định thì thêm bài Medium.

Không cần giải hàng trăm bài LeetCode. Backend tốt còn cần nền tảng database,
hệ thống, testing và debugging, không chỉ thuật toán.

### Kiến thức nền song song

- **Git:** commit, branch, merge và pull request.
- **Linux:** file, permission, process, port và log.
- **Web:** HTTP method, header, body, status code, cookie; giải thích được DNS,
  TCP và TLS.
- **Hệ điều hành:** process, thread, memory và race condition.

**Mốc kiểm tra:** tự mô tả hành trình của một HTTP request từ trình duyệt tới
server; phân tích độ phức tạp của lời giải; xử lý được một merge conflict nhỏ.

## Chuyên đề: Java Concurrency

Học chuyên đề này sau Java Core và trước khi xử lý tác vụ nền trong Spring.
Mục tiêu không phải là tạo thật nhiều thread, mà là biết giới hạn tài nguyên,
truyền lỗi đúng cách và dừng ứng dụng mà không làm mất công việc đang chạy.

### 1. Từ thread thủ công đến `ExecutorService`

- Không tạo `new Thread(...)` cho từng task. Cách này không giới hạn số thread,
  tốn chi phí tạo/hủy thread và khó quản lý lifecycle.
- Submit `Runnable` hoặc `Callable` vào một `ExecutorService`; executor chịu
  trách nhiệm tái sử dụng worker thread và điều phối task.
- Bắt đầu với `ThreadPoolExecutor` để nhìn rõ các tham số thay vì chỉ ghi nhớ
  các factory method của `Executors`.

```java
ExecutorService executor = new ThreadPoolExecutor(
        4,                         // corePoolSize
        8,                         // maximumPoolSize
        30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(100),
        new ThreadPoolExecutor.CallerRunsPolicy());
```

Khi có task mới, pool ưu tiên tạo worker đến `corePoolSize`, sau đó đưa task
vào queue. Chỉ khi queue đầy mới tăng worker đến `maximumPoolSize`; nếu cả pool
và queue đều đầy thì áp dụng rejection policy. Vì vậy queue không giới hạn có
thể khiến `maximumPoolSize` gần như vô nghĩa và che giấu tình trạng quá tải.

So sánh bốn policy chuẩn:

| Policy | Hành vi | Khi cân nhắc |
| --- | --- | --- |
| `AbortPolicy` | Ném `RejectedExecutionException` | Muốn fail fast và báo lỗi rõ ràng |
| `CallerRunsPolicy` | Thread submit tự chạy task | Tạo backpressure đơn giản |
| `DiscardPolicy` | Bỏ task mới | Chỉ khi mất task được chấp nhận và có metric |
| `DiscardOldestPolicy` | Bỏ task cũ nhất trong queue | Hiếm dùng; phải chứng minh task cũ không còn giá trị |

Luôn chọn queue capacity, pool size và policy một cách có chủ đích; ghi metric
cho queue depth, active thread, thời gian chờ và số task bị reject.

### 2. `Callable`, `Future` và quản lý kết quả

`Runnable` không trả kết quả và không khai báo checked exception. Dùng
`Callable<T>` khi task cần trả về `T` hoặc báo lỗi; `submit` trả về `Future<T>`.

```java
Future<Receipt> future = executor.submit(() -> process(transaction));

try {
    Receipt receipt = future.get(2, TimeUnit.SECONDS);
} catch (TimeoutException timeout) {
    future.cancel(true);
} catch (ExecutionException failed) {
    Throwable cause = failed.getCause();
    // log/translate cause; không nuốt lỗi
} catch (InterruptedException interrupted) {
    Thread.currentThread().interrupt();
}
```

`cancel(true)` chỉ gửi tín hiệu interrupt, không bảo đảm task sẽ dừng. Code xử
lý task phải tôn trọng interruption, dùng API có thể interrupt và không được
nuốt `InterruptedException`. Timeout cũng không đồng nghĩa nghiệp vụ đã rollback;
thiết kế task idempotent và xác định ranh giới transaction rõ ràng.

### 3. Composition với `CompletableFuture`

Dùng `CompletableFuture` để mô tả pipeline thay vì gọi `Future.get()` tuần tự:

```java
CompletableFuture<Receipt> result = CompletableFuture
        .supplyAsync(() -> validate(transaction), executor)
        .thenCompose(valid -> debitAndCreditAsync(valid, executor))
        .thenCombine(loadRiskScoreAsync(transaction, executor),
                this::attachRiskScore)
        .orTimeout(3, TimeUnit.SECONDS)
        .whenComplete((receipt, error) -> recordMetrics(error));
```

- `thenApply`: biến đổi kết quả đồng bộ; `thenCompose`: nối một async stage và
  tránh `CompletableFuture<CompletableFuture<T>>`.
- `thenCombine`: gộp hai công việc độc lập; `allOf`: chờ một batch hoàn tất.
- `exceptionally` cung cấp fallback, `handle` biến đổi cả success lẫn failure,
  còn `whenComplete` phù hợp cho logging/metric mà không đổi kết quả.
- Truyền executor tường minh cho các stage async quan trọng thay vì vô tình
  dùng common pool. Đặt timeout ở ranh giới nghiệp vụ và kiểm tra root cause
  (`CompletionException`) trong test.

### 4. Bài thực hành: Batch Transaction Processor

Xây một chương trình nhận danh sách giao dịch và xử lý song song qua bounded
thread pool. Mỗi giao dịch có `idempotencyKey`; kết quả batch phải giữ được ID,
trạng thái thành công/thất bại và nguyên nhân lỗi của từng phần tử.

Yêu cầu triển khai theo từng bước:

1. Viết phiên bản tuần tự làm baseline và đo throughput/latency.
2. Dùng `Callable<TransactionResult>` và `Future` với timeout cho từng task.
3. Viết lại orchestration bằng `CompletableFuture`, không block bên trong các
   stage; dùng `allOf` để tạo báo cáo batch.
4. Giới hạn queue; chọn rejection policy và test khi pool bị bão hòa.
5. Mô phỏng task chậm, exception, timeout và cancellation; chứng minh task có
   phản ứng với interrupt và lỗi của một item không làm mất kết quả item khác.
6. Giữ database transaction ngắn và độc lập theo từng item. Không chia sẻ một
   transaction hoặc mutable entity/session giữa các worker thread.

**Definition of Done**

- [ ] Không có `new Thread(...)` trong business code; executor được inject hoặc
      được sở hữu bởi một component có lifecycle rõ ràng.
- [ ] Queue có giới hạn, pool size và rejection policy có giải thích bằng số đo.
- [ ] Có test cho success, partial failure, queue saturation, timeout,
      cancellation, interrupt và duplicate idempotency key.
- [ ] Không nuốt exception; báo cáo batch liên kết được lỗi với transaction ID.
- [ ] Sau khi dừng, không còn worker thread và task đã nhận không bị mất âm thầm.

### 5. Shutdown executor đúng cách

Không chỉ gọi `shutdownNow()` ngay lập tức. Ngừng nhận task mới, chờ task đang
chạy hoàn tất trong một khoảng hữu hạn, rồi mới interrupt phần còn lại:

```java
static void shutdown(ExecutorService executor) {
    executor.shutdown();
    try {
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate");
            }
        }
    } catch (InterruptedException interrupted) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

Trong Spring, đặt executor trong bean và khai báo destroy method hoặc đóng nó
ở `@PreDestroy`. Owner tạo executor là owner chịu trách nhiệm shutdown; không
shutdown executor dùng chung từ code xử lý một request.

### 6. CPU-bound và I/O-bound

| Workload | Dấu hiệu | Điểm bắt đầu để sizing |
| --- | --- | --- |
| CPU-bound | Tính toán, ít chờ I/O, CPU gần bão hòa | Xấp xỉ số CPU core (có thể `cores + 1`) |
| I/O-bound | Chờ DB/HTTP/file phần lớn thời gian | Có thể lớn hơn số core, nhưng phải giới hạn theo downstream |

Với I/O-bound có thể ước lượng ban đầu `threads = cores × (1 + wait/compute)`,
sau đó load test và điều chỉnh. Đây không phải công thức bảo đảm: connection
pool, rate limit của downstream, memory, latency target và queue wait mới là
các giới hạn thực tế. Tách pool cho workload CPU-bound và blocking I/O để task
chậm không gây starvation cho toàn hệ thống; không tăng thread chỉ để che một
dependency đang quá tải.

## Tháng 4: SQL và PostgreSQL

### Kiến thức

- Table, relationship, primary key và foreign key.
- `SELECT`, `JOIN`, aggregate, subquery và CTE.
- Chuẩn hóa dữ liệu.
- Index và composite index; hiểu đánh đổi giữa tốc độ đọc và chi phí ghi.
- Transaction, ACID và isolation level.
- Lock và deadlock.
- Đọc kết quả `EXPLAIN ANALYZE`.
- Migration bằng Flyway hoặc Liquibase.

### Project: Expense Tracker

- Thiết kế ERD cho user, account, transaction và category.
- Viết báo cáo theo ngày/tháng.
- Thêm pagination và index phù hợp.
- Viết ít nhất 20 câu SQL bằng tay trước khi dùng ORM.

**Definition of Done**

- [ ] Schema được tạo hoàn toàn bằng migration và có thể dựng lại từ database rỗng.
- [ ] Có file ERD và giải thích cardinality của từng quan hệ.
- [ ] Mỗi index đều gắn với một query thực tế và được kiểm tra bằng
      `EXPLAIN ANALYZE`.
- [ ] Có ví dụ transaction rollback và ghi chú về isolation level đã chọn.

## Tháng 5–6: Spring Boot và REST API

### Kiến thức

- Maven project structure; dependency injection và IoC.
- Luồng Controller → Service → Repository.
- REST API, JSON, DTO, entity và mapping.
- Bean Validation và global exception handler.
- Spring Data JPA/Hibernate; fetch type và vấn đề N+1 query.
- Pagination, filtering và sorting.
- Logging, OpenAPI/Swagger và cấu hình theo environment.
- Clean Architecture ở mức vừa đủ, không tạo abstraction khi chưa có nhu cầu.

### Project: Banking REST API

- Đăng ký user; quản lý account và balance.
- Chuyển tiền và xem lịch sử giao dịch.
- Validation cùng error response thống nhất.
- Dùng PostgreSQL thật, không chỉ H2.
- Cung cấp Swagger UI, README và Postman collection.

**Definition of Done**

- [ ] API dùng status code đúng và không trả entity JPA trực tiếp.
- [ ] Transfer chạy trong một database transaction.
- [ ] Error response có cấu trúc ổn định gồm code, message, timestamp và field errors.
- [ ] Query danh sách hỗ trợ pagination và giới hạn kích thước trang.
- [ ] README hướng dẫn dựng PostgreSQL và chạy ứng dụng từ máy mới.

## Tháng 7: Security và testing

### Security

- Phân biệt authentication và authorization.
- Password hashing; Spring Security.
- JWT access token và refresh token.
- Role, permission, CORS và CSRF.
- SQL injection, XSS và brute force.
- Quản lý secret/config và audit log.

### Testing

- JUnit 5 và Mockito.
- Unit test cho service.
- Repository/integration test.
- Controller test bằng MockMvc.
- Testcontainers với PostgreSQL.
- Test cả happy path và failure path.

**Mốc kiểm tra:** nghiệp vụ chuyển tiền phải có test cho giao dịch thành công,
thiếu tiền, tài khoản không tồn tại, số tiền không hợp lệ và rollback. Không lưu
password thô, secret hay token vào Git/log.

## Tháng 8–10: Backend production

Học theo thứ tự, chỉ thêm công nghệ khi đã mô tả được vấn đề nó giải quyết.

### 1. Redis

- Cache-aside, TTL và cache invalidation.
- Distributed lock ở mức khái niệm và rate limiting.
- Không dùng cache làm nguồn dữ liệu tài chính chính.

### 2. Kafka

- Producer, consumer, topic, partition và consumer group.
- Retry và dead-letter topic.
- Delivery semantics và idempotent consumer.
- Transactional outbox.

### 3. Vận hành production

- Dockerfile và Docker Compose.
- GitHub Actions để build và test tự động.
- Metrics, log, tracing và health check.
- Timeout, retry và circuit breaker.
- Load testing bằng k6 hoặc JMeter.

Chưa cần học sâu Kubernetes ở giai đoạn Junior. Hiểu pod, deployment và service
là đủ; ưu tiên Docker, SQL, testing và debugging.

**Mốc kiểm tra:** có thể dựng toàn bộ môi trường bằng một lệnh; CI tự động chạy
test; dashboard/endpoint quan sát được health và metrics; retry có giới hạn và
không tạo giao dịch trùng.

## Tháng 11–12: Capstone — Digital Wallet System

Đây là project chủ lực để ứng tuyển. Bắt đầu bằng **modular monolith**; chỉ tách
notification service để luyện Kafka. Không tạo nhiều microservice chỉ để project
trông phức tạp.

### Tính năng bắt buộc

- User, wallet và account.
- Deposit và withdrawal giả lập; chuyển tiền.
- Double-entry ledger.
- Idempotency key chống giao dịch trùng.
- Database transaction và row locking.
- Audit trail.
- Authentication và RBAC.
- Notification qua Kafka.
- Redis cache/rate limit.
- PostgreSQL migration.
- Unit test và integration test.
- Docker Compose và CI chạy test khi push.
- Metrics và structured logging.
- README có ERD và giải thích quyết định kỹ thuật.

### Tiêu chí nghiệm thu capstone

- [ ] Tổng debit và credit của mỗi ledger transaction luôn cân bằng.
- [ ] Gửi lại cùng idempotency key không tạo giao dịch tài chính thứ hai.
- [ ] Hai transfer đồng thời không làm sai số dư; có test chứng minh.
- [ ] Event chỉ được publish sau khi dữ liệu nghiệp vụ đã commit (transactional outbox).
- [ ] Consumer xử lý lặp an toàn và có retry/dead-letter policy.
- [ ] API nhạy cảm có authentication, authorization, rate limit và audit trail.
- [ ] Không có secret trong repository; log không lộ password/token/dữ liệu nhạy cảm.
- [ ] `docker compose up` dựng được app, PostgreSQL, Redis và Kafka.
- [ ] CI build/test từ trạng thái sạch; integration test dùng PostgreSQL thật qua Testcontainers.
- [ ] README có kiến trúc, ERD, API examples, trade-off và hướng dẫn chạy.

## Lịch học mỗi tuần

| Hoạt động | Thời lượng | Kết quả mong đợi |
| --- | ---: | --- |
| Đọc lý thuyết/documentation | 3 giờ | Ghi chú ngắn và câu hỏi cần kiểm chứng |
| Code bài tập | 5 giờ | Tự triển khai lại không nhìn tutorial |
| Phát triển project | 3 giờ | Một lát cắt nhỏ có thể demo và test |
| Tổng kết/README/phỏng vấn | 1 giờ | Nhật ký học, quyết định kỹ thuật, flashcards |

Cuối mỗi tuần, trả lời bốn câu hỏi:

1. Tôi đã tự viết được gì mà không chép tutorial?
2. Tôi đã test failure path nào?
3. Tôi có thể giải thích trade-off kỹ thuật nào bằng lời của mình?
4. Tuần tới tôi sẽ hoàn thành một đầu ra nhỏ, đo được nào?

## Nguồn học chính thức

Ưu tiên documentation và guide chính thức; dùng video/blog như nguồn bổ trợ:

- [Dev.java — Learn Java](https://dev.java/learn/)
- [Java API — `ExecutorService`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [Java API — `CompletableFuture`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Apache Maven — Getting Started](https://maven.apache.org/guides/getting-started/)
- [PostgreSQL Tutorial](https://www.postgresql.org/docs/current/tutorial.html)
- [PostgreSQL — Indexes](https://www.postgresql.org/docs/current/indexes.html)
- [Spring — Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Spring — Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring — Testing the Web Layer](https://spring.io/guides/gs/testing-web/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Docker — Get Started](https://docs.docker.com/get-started/)
- [GitHub Actions — Building and testing Java with Maven](https://docs.github.com/actions/automating-builds-and-tests/building-and-testing-java-with-maven)

## Cách dùng repository này

Mỗi project nên nằm trong một thư mục riêng và có README riêng, ví dụ:

```text
studyforge-java-springboot/
├── 01-banking-cli/
├── 02-expense-tracker/
├── 03-banking-api/
└── 04-digital-wallet/
```

Commit theo lát cắt nhỏ có thể kiểm chứng (ví dụ: `feat: validate transfer
amount`) và mở pull request để tự review thiết kế, test và tài liệu trước khi
merge.
