# AI-Powered Document & Multimedia Q&A App

A full-stack Spring Boot + React application that lets users upload PDF, audio, and video files and ask AI-powered questions about them.

---

## Tech Stack

| Layer      | Technology                     |
|------------|-------------------------------|
| Backend    | Java 17, Spring Boot 3.2      |
| Auth       | JWT (jjwt)                    |
| Database   | MySQL 8                       |
| AI         | OpenAI GPT-3.5-turbo API      |
| PDF        | Apache PDFBox                 |
| Frontend   | React (create-react-app)      |
| Container  | Docker + Docker Compose       |

---

## Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8 running locally
- Node.js 18+ (for frontend)
- An OpenAI API key — get one at https://platform.openai.com/api-keys
- Eclipse IDE (Spring Tools 4 plugin installed)

---

## Setup (Step by Step)

### 1. Create the MySQL database

Open MySQL Workbench or terminal and run:
```sql
CREATE DATABASE docqa_db;
```

### 2. Set your credentials in application.properties

Open `src/main/resources/application.properties` and update:
```
spring.datasource.password=YOUR_MYSQL_PASSWORD
openai.api.key=sk-YOUR_OPENAI_KEY
```

### 3. Run in Eclipse

1. Import project: File → Import → Existing Maven Projects → select this folder
2. Right-click `DocumentQaAppApplication.java` → Run As → Spring Boot App
3. App starts at http://localhost:8080

### 4. Run via Docker Compose

```bash
# Create a .env file with your OpenAI key
echo "OPENAI_API_KEY=sk-your-key-here" > .env

# Start everything (MySQL + backend + frontend)
docker-compose up --build
```

---

## API Endpoints

### Auth (no token required)

| Method | URL                  | Body                                  | Description    |
|--------|----------------------|---------------------------------------|----------------|
| POST   | /api/auth/register   | `{"email":"x","password":"y"}`        | Register user  |
| POST   | /api/auth/login      | `{"email":"x","password":"y"}`        | Login user     |

Both return: `{ "token": "...", "userId": 1, "email": "..." }`

**All other endpoints require:** `Authorization: Bearer <token>` header

### Files

| Method | URL                        | Description               |
|--------|----------------------------|---------------------------|
| POST   | /api/files/upload          | Upload PDF/audio/video    |
| GET    | /api/files/user/{userId}   | List user's files         |
| GET    | /api/files/{fileId}        | Get single file metadata  |
| DELETE | /api/files/{fileId}        | Delete a file             |

Upload uses `multipart/form-data` with fields: `file` (the file) and `userId` (number).

### Chat / AI

| Method | URL                           | Body                                              | Description         |
|--------|-------------------------------|---------------------------------------------------|---------------------|
| POST   | /api/chat/ask                 | `{"userId":1,"fileId":1,"question":"..."}`        | Ask a question      |
| POST   | /api/chat/summarize/{fileId}  | —                                                 | Summarize a file    |
| GET    | /api/chat/history?userId=1&fileId=1 | —                                           | Get chat history    |

---

## Testing with Postman

1. Register: POST http://localhost:8080/api/auth/register
2. Copy the `token` from the response
3. Add header: `Authorization: Bearer <token>` to all other requests
4. Upload a PDF: POST http://localhost:8080/api/files/upload (form-data)
5. Ask a question: POST http://localhost:8080/api/chat/ask

---

## Running Tests

```bash
mvn test
```

---

## Project Structure

```
document-qa-app/
├── src/main/java/com/docqa/
│   ├── DocumentQaAppApplication.java   ← main entry point
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── FileController.java
│   │   └── ChatController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── FileService.java
│   │   ├── ChatService.java
│   │   └── OpenAIService.java
│   ├── model/
│   │   ├── User.java
│   │   ├── UploadedFile.java
│   │   └── ChatMessage.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── FileRepository.java
│   │   └── ChatRepository.java
│   ├── security/
│   │   ├── JwtUtil.java
│   │   └── JwtFilter.java
│   └── config/
│       └── SecurityConfig.java
├── src/test/java/com/docqa/
│   ├── AuthServiceTest.java
│   └── ChatServiceTest.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```
