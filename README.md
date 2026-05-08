# HanCosStore - Huong Dan Cai Dat Website Ban Quan Ao

Ung dung website ban quan ao HancosShop, xay dung bang Spring Boot + Thymeleaf.

## 1. Yeu Cau Moi Truong

- Java 21
- SQL Server (khuyen nghi SQL Server 2019+)
- Maven (khong bat buoc neu dung Maven Wrapper)
- Git

Kiem tra nhanh:

```bash
java -version
```

## 2. Clone Du An

```bash
git clone <repo-url>
cd HanCosStore
```

## 3. Tao Va Cau Hinh Co So Du Lieu

Du an dang su dung:

- Database: `CLOTHES_STORE`
- SQL Server host mac dinh: `localhost:1433`
- `spring.jpa.hibernate.ddl-auto=none` (khong tu tao schema)

Ban can:

1. Tao database `CLOTHES_STORE` trong SQL Server.
2. Import schema co ban cua du an (neu team cua ban dang quan ly schema rieng).
3. Chay cac script migration trong thu muc `docs/` neu schema hien tai chua co:
   - `docs/forgot-password.sql`
   - `docs/return-request-migration.sql`
   - `docs/order-shipping-error-migration.sql`
   - `docs/order-status-migration.sql`
   - `docs/rbac-migration.sql`

Luu y: thu tu migration co the thay doi theo trang thai DB hien tai. Nen backup DB truoc khi chay migration tren moi truong that.

## 4. Cau Hinh Ung Dung

Cau hinh chinh nam o file `src/main/resources/application.properties`.

Gia tri mac dinh hien tai:

- Datasource URL: `jdbc:sqlserver://localhost:1433;databaseName=CLOTHES_STORE;encrypt=true;trustServerCertificate=true;`
- Username: `sa`
- Password: `123456`
- Port: `8080`

### Bien moi truong khuyen nghi

Dat cac bien moi truong sau truoc khi chay app:

- `GEMINI_API_KEY`: API key cho chatbot Gemini
- `GHTK_TOKEN`: token Giao Hang Tiet Kiem
- `GHTK_PARTNER_CODE`: partner code GHTK


Vi du PowerShell:

```powershell
$env:GEMINI_API_KEY="AIzaSyCg6TB8oBVukrKW4Mb-Y9t6sV_YqNVGl58echo AIzaSyCg6TB8oBVukrKW4Mb-Y9t6sV_YqNVGl58"
$env:GHTK_TOKEN="3QEjuuFP1N9Ph17zUBiIZFgytdvSabdl1oFEjqg"
$env:GHTK_PARTNER_CODE="HanCosStore"

```

## 5. Chay Ung Dung

### Windows

```powershell
./mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
./mvnw spring-boot:run
```

Sau khi chay thanh cong, truy cap:

- http://localhost:8080

## 6. Tai Khoan Demo Duoc Seed Tu Dong

Khi app khoi dong, he thong se seed role va tai khoan demo:

- Admin: `admin` / `123456`
- Staff: `nhanvien` / `123456`
- Customer: `user` / `123456`

## 7. Chay Test

### Windows

```powershell
./mvnw.cmd test
```

### macOS/Linux

```bash
./mvnw test
```

## 8. Luu Y Bao Mat

- Khong dung thong tin SMTP/DB mac dinh cho production.
- Nen tach cau hinh nhay cam sang bien moi truong hoac secret manager.
- Kiem tra lai `app.reset-password.base-url` truoc khi deploy.

## 9. SePay (Webhook + Kiem Tra Trang Thai)

### Cau hinh
- `sepay.api.key` trong `application.properties`
- Webhook endpoint: `POST /api/sepay/webhook`
- Header xac thuc: `Authorization: Apikey <API_KEY>`

### Luu y khi test local qua ngrok
- SePay phai goi vao **public URL dang hoat dong** (vi du: `https://<ngrok-domain>/api/sepay/webhook`).
- Neu ngrok hien `ERR_NGROK_3200` thi tunnel da tat/het han/doi domain, khong phai loi code Spring Boot.
- Cach fix:
  1. Chay app local tren port 8080.
  2. Mo tunnel moi: `ngrok http 8080`
  3. Cap nhat lai webhook URL tren SePay dashboard thanh domain ngrok moi.
  4. Gui lai request webhook de kiem tra.

### Payload mau (toi thieu)
```json
{
  "content": "DH-ABC12345",
  "amount": 199000,
  "transactionId": "SEPAY-123"
}
```

### Kiem tra trang thai don
```
GET /api/order/status/{code}
```

Response:
```json
{
  "code": "DH-ABC12345",
  "amount": 199000,
  "status": "PENDING"
}
```

### Luu y
- Don hang thanh toan SePay khoi tao `TrangThai = PENDING`.
- Khi webhook hop le: update `TrangThai = PAID` + luu giao dich vao `GIAO_DICH_THANH_TOAN`.

---

Neu ban muon, minh co the viet them ban README tieng Viet co dau + bo sung phan huong dan deploy production.
