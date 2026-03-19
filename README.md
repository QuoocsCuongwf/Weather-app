# Weather App

README này hướng dẫn cách vẽ sơ đồ bằng **Mermaid** (bạn gõ “Maidmer” có thể là “Mermaid”).

## Mermaid là gì?
Mermaid là cú pháp text để tạo sơ đồ tự động (flowchart, sequence, class, state, ERD...).  
Bạn chỉ cần viết code dạng markdown, GitHub sẽ render thành sơ đồ.

## Cách dùng nhanh
Trong file `.md`, tạo block:

```markdown
```mermaid
flowchart TD
    A[Người dùng mở app] --> B[Tìm thành phố]
    B --> C[Gọi API thời tiết]
    C --> D[Hiển thị nhiệt độ]
```
```

## Ví dụ sơ đồ luồng cho app thời tiết

```mermaid
flowchart TD
    A[Open App] --> B[Nhập tên thành phố]
    B --> C[Validate input]
    C -->|Hợp lệ| D[Fetch current weather]
    C -->|Không hợp lệ| E[Hiển thị lỗi]
    D --> F[Fetch hourly & daily forecast]
    F --> G[Render UI]
```

## Ví dụ sequence diagram

```mermaid
sequenceDiagram
    participant U as User
    participant A as Android App
    participant W as Weather API
    U->>A: Tìm "Da Nang"
    A->>W: GET /weather?q=Da%20Nang
    W-->>A: JSON dữ liệu thời tiết
    A-->>U: Hiển thị kết quả
```

## Sơ đồ kiến trúc (UI -> ViewModel -> Repository -> API)

```mermaid
flowchart LR
    UI[Activity / Fragment UI]
    VM[ViewModel]
    REPO[Repository]
    API[Weather API Service]
    DB[(Local Cache / Room - optional)]

    UI -->|User actions| VM
    VM -->|Request data| REPO
    REPO -->|HTTP call| API
    API -->|Response DTO| REPO
    REPO -->|Map to domain model| VM
    VM -->|UI state| UI
    REPO <--> |Cache read/write| DB
```

Luồng chính: UI nhận thao tác người dùng, ViewModel điều phối trạng thái, Repository xử lý nguồn dữ liệu (remote/local), và API trả dữ liệu thời tiết về để hiển thị.

## Mẹo
- Dùng [Mermaid Live Editor](https://mermaid.live) để preview nhanh.
- Giữ tên node ngắn, rõ nghĩa.
- Tách sơ đồ lớn thành nhiều sơ đồ nhỏ để dễ đọc.
