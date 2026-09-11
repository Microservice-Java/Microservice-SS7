# FinBank Digital Bank - API Gateway Routing Architecture (SS7 - Exercise 1)

Dự án xây dựng **API Gateway** làm điểm vào duy nhất (Single Entry Point) cho toàn bộ hệ thống Microservice Ngân hàng Số FinBank.

---

## 1. Kiến Trúc Điểm Vào Duy Nhất (Single Entry Point Port 8222)

```text
                             [Client Apps / Postman]
                                        │
                                        ▼ (Port 8222 duy nhất)
                      ┌───────────────────────────────────┐
                      │    FinBank API Gateway (8222)     │
                      └─────────────────┬─────────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
    ┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
    │customer-service │        │ account-service │        │transaction-serv │
    │   (Port 8081)   │        │   (Port 8082)   │        │   (Port 8083)   │
    └─────────────────┘        └─────────────────┘        └─────────────────┘
```

### Ưu điểm của API Gateway:
- **Tập trung cổng vào**: Client (Mobile app / Web app) chỉ cần nhớ duy nhất 1 địa chỉ `http://localhost:8222` thay vì phải lưu từng IP/Port của từng service (`8081`, `8082`, `8083`).
- **An toàn & Bảo mật**: Ẩn hoàn toàn cấu trúc mạng nội bộ và port thực tế của các microservice phía sau.
- **Dễ dàng bảo trì**: Khi một microservice thay đổi port hoặc scale thành nhiều instances, ứng dụng client hoàn toàn không bị ảnh hưởng.

---

## 2. Danh Sách Định Tuyến (Gateway Routes Configuration)

Cấu hình các Route trong `application.yml` của `api-gateway`:

| Service ID | Port Nội Bộ | Predicate Path | Gateway Endpoint URL (Port 8222) |
| :--- | :--- | :--- | :--- |
| `customer-service` | `8081` | `/api/customers/**` | [http://localhost:8222/api/customers](http://localhost:8222/api/customers) |
| `account-service` | `8082` | `/api/accounts/**` | [http://localhost:8222/api/accounts](http://localhost:8222/api/accounts) |
| `transaction-service` | `8083` | `/api/transactions/**` | [http://localhost:8222/api/transactions](http://localhost:8222/api/transactions) |
| `loan-service` | `8084` | `/api/loans/**` | [http://localhost:8222/api/loans](http://localhost:8222/api/loans) |
| `notification-service` | `8085` | `/api/notifications/**` | [http://localhost:8222/api/notifications](http://localhost:8222/api/notifications) |

---

## 3. Thứ Tự Khởi Chạy Hệ Thống

> [!IMPORTANT]
> Vui lòng khởi chạy theo đúng thứ tự 4 bước dưới đây để các service tự động đăng ký và kết nối thành công.

### Bước 1: Khởi Chạy Config Server (Port 8888)
```bash
cd config-server && ./gradlew bootRun
```

### Bước 2: Khởi Chạy Eureka Server (Port 8761)
```bash
cd discovery-server && ./gradlew bootRun
```

### Bước 3: Khởi Chạy Các Microservices Nghiệp Vụ (Port 8081 - 8085)
```bash
cd customer-service && ./gradlew bootRun
cd account-service && ./gradlew bootRun
cd transaction-service && ./gradlew bootRun
```

### Bước 4: Khởi Chạy FinBank API Gateway (Port 8222)
```bash
cd api-gateway && ./gradlew bootRun
```

---

## 4. Hướng Dẫn Kiểm Thử Bằng Postman Collection

File Postman Collection được lưu trữ tại `postman/FinBank_Gateway_Collection.json`.

1. Mở ứng dụng **Postman** -> **Import** file `postman/FinBank_Gateway_Collection.json`.
2. Kiểm thử các yêu cầu gọi **Duy nhất qua Port 8222**:
   - `GET http://localhost:8222/api/customers` -> Trả về thông tin từ `customer-service`
   - `POST http://localhost:8222/api/accounts` -> Định tuyến thành công tới `account-service`
   - `GET http://localhost:8222/api/transactions` -> Định tuyến thành công tới `transaction-service`
