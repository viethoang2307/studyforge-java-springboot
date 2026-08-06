# Lab Git, Linux và điều tra backend

Lab dùng HTTP server có sẵn trong Java 21 và port cố định `8085`. Thực hiện từng nhóm lệnh
trong terminal tại thư mục repository; không chạy mù một PID lấy từ ví dụ.

## 1. Git workflow

### Feature branch và commit nhỏ

```bash
git switch -c feature/http-server-lab
git add src/ scripts/build.sh .gitignore
git commit -m "feat: add minimal Java HTTP server"
git add docs/backend-operations-lab.md
git commit -m "docs: add backend operations runbook"
```

Xem lịch sử và thay đổi ở từng mức:

```bash
git log --oneline --graph --decorate --all
git diff                         # thay đổi chưa stage
git diff --staged                # thay đổi đã stage
git diff work...feature/http-server-lab
```

Thử phục hồi an toàn trên file tạm (không làm mất công việc thật):

```bash
printf 'temporary change\n' >> practice.tmp
git add practice.tmp
echo 'changed again' >> practice.tmp
git restore practice.tmp         # bỏ phần chưa stage
git restore --staged practice.tmp
git clean -f practice.tmp
```

### Chủ động tạo và xử lý conflict

Tạo cùng một file trên hai nhánh với nội dung khác nhau, rồi merge:

```bash
git switch work
printf 'main version\n' > conflict-demo.txt
git add conflict-demo.txt && git commit -m "chore: add main conflict example"
git switch feature/http-server-lab
printf 'feature version\n' > conflict-demo.txt
git add conflict-demo.txt && git commit -m "chore: add feature conflict example"
git switch work
git merge feature/http-server-lab            # CONFLICT (add/add)
cat conflict-demo.txt                         # xem <<<<<<<, =======, >>>>>>>
printf 'main + feature: resolved\n' > conflict-demo.txt
git add conflict-demo.txt
git commit                                    # hoàn tất merge
```

### Rebase và revert

Dùng một nhánh thực hành riêng để rebase, sau đó xóa nó. `rebase` viết lại lịch
sử nên không rebase nhánh dùng chung:

```bash
git switch -c practice/rebase HEAD~1
git rebase work
git log --oneline --graph --decorate --all
git switch work
git branch -D practice/rebase
```

`revert` tạo commit đảo ngược thay vì xóa lịch sử. Có thể revert commit thử
nghiệm, kiểm tra diff, rồi revert commit revert để khôi phục:

```bash
git revert <commit-thu-nghiem>
git show --stat HEAD
git revert HEAD
```

## 2. Linux và chạy HTTP server

### File, thư mục và quyền

```bash
pwd
cd /workspace/studyforge-java-springboot
find src -type f -name '*.java'
rg 'createContext|PORT' src
cat scripts/build.sh
less docs/backend-operations-lab.md           # nhấn q để thoát
./scripts/build.sh > target/build.log 2>&1
tail -n 30 target/build.log
printf '#!/usr/bin/env bash\necho ready\n' > /tmp/studyforge-check.sh
chmod u+x /tmp/studyforge-check.sh
/tmp/studyforge-check.sh
```

### Process, port, request và log

Build và chạy Java ở background, đồng thời redirect cả stdout và stderr:

```bash
./scripts/build.sh
java -jar target/backend-operations-lab.jar \
  > target/server.log 2>&1 &
echo $! > target/server.pid
```

Tìm process, PID, port listen và file mà process đang mở:

```bash
ps -ef | rg '[b]ackend-operations-lab.jar'
PID=$(cat target/server.pid)
ps -p "$PID" -o pid,ppid,stat,etime,cmd
ss -ltnp | rg ':8085'
lsof -nP -iTCP:8085 -sTCP:LISTEN
lsof -p "$PID" | rg 'server.log|backend-operations-lab.jar'
```

Gọi endpoint, kiểm tra log rồi dừng đúng process:

```bash
curl --fail-with-body -i http://localhost:8085/api/hello
tail -n 50 target/server.log
kill "$PID"
while kill -0 "$PID" 2>/dev/null; do sleep 0.2; done
ss -ltnp | rg ':8085' || echo 'port 8085 is free'
```

`kill` mặc định gửi `SIGTERM` để JVM chạy shutdown hook có kiểm soát. Chỉ cân nhắc
`kill -KILL "$PID"` sau khi chờ mà process không dừng.

## 3. Runbook: backend “không truy cập được”

Đi theo thứ tự để khoanh vùng, không vội restart vì restart có thể xóa dấu vết.

### Bước 1 — Process có chạy không?

```bash
ps -ef | rg '[b]ackend-operations-lab.jar'
PID=$(cat target/server.pid 2>/dev/null) && ps -p "$PID" -o pid,stat,etime,cmd
```

Không có process: đọc log khởi động. Có process nhưng trạng thái bất thường hoặc
liên tục restart: kiểm tra memory, signal và supervisor/container tương ứng.

### Bước 2 — Port có listen không?

```bash
ss -ltnp | rg ':8085'
lsof -nP -iTCP:8085 -sTCP:LISTEN
```

Không listen thường là app chưa khởi động xong, bind sai interface/port, hoặc đã
crash. PID listen không phải Java mong đợi nghĩa là port conflict. `127.0.0.1`
chỉ nhận local traffic; `0.0.0.0` nhận trên mọi IPv4 interface (firewall vẫn có
thể chặn).

### Bước 3 — Request có tới server không?

```bash
curl -v --connect-timeout 2 http://127.0.0.1:8085/api/hello
tail -f target/server.log
```

- `Connection refused`: không có listener hoặc sai host/port.
- Timeout: kiểm tra route, firewall, proxy/load balancer và server bị treo.
- Có HTTP status: request đã tới một HTTP server; phân biệt `404`, `401/403` và
  `5xx`. Dòng `Received request` xác nhận handler đã nhận endpoint này.

### Bước 4 — Log báo lỗi gì?

```bash
rg -n -i 'error|exception|caused by|failed' target/server.log
tail -n 100 target/server.log
```

Đối chiếu timestamp/request, đọc chuỗi `Caused by` từ nguyên nhân gốc, đồng thời
kiểm tra config, dependency (database/DNS), permission và tài nguyên. Sau khi
sửa, chạy lại `curl`, xác nhận status/body và log rồi mới kết luận đã phục hồi.
