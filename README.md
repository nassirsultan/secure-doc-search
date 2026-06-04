<img width="1578" height="623" alt="Screenshot 2026-06-04 113213" src="https://github.com/user-attachments/assets/227d96b6-374b-4f6f-a992-8d653d0f9d78" />

# Secure Document Search System

A secure, full-featured document management platform built with **Spring Boot** and **Spring Security**. Enables authenticated users to upload, encrypt, and perform keyword-based searches within confidential documents — ensuring end-to-end data privacy and role-based access control.

## Tech Stack

- **Java** (Spring Boot)
- **Spring Security** (Authentication & Authorization)
- **AES Encryption** (document-level encryption at rest)
- **Apache Tika** (in-memory text extraction for full-text search)
- **Spring Data JPA / Hibernate**
- **Thymeleaf** (frontend templating)
- **MySQL**
- **Maven**
- **Docker** (deployment ready)

## Features

- **AES Encryption** — every uploaded document is encrypted before storage; content remains unreadable without authorization
- **JWT-based Authentication** — only registered users can access the system
- **Role-Based Access Control (RBAC)**
  - `ADMIN` — full privileges (upload, search, download any document)
  - `USER` — can upload and search only their own documents
- **Full-Text Search** — uses Apache Tika to extract and index text from encrypted files after in-memory decryption, enabling keyword-based search within document content (not just filenames)
- **Document Upload & Download** with secure access validation
- **Clean Thymeleaf UI** with intuitive navigation across upload, search, and dashboard modules
- **Cloud & Docker deployable**

## Security Principles

| Principle | Implementation |
|-----------|---------------|
| Confidentiality | AES encryption at rest |
| Integrity | Role-based access prevents unauthorized modification |
| Availability | Authorized users can access documents securely anytime |

## Getting Started

### Prerequisites
- Java 11+
- MySQL
- Maven
- Docker (optional)

### Run the Application

```bash
git clone https://github.com/nassirsultan/secure-doc-search
cd secure-doc-search
# Configure database in src/main/resources/application.properties
mvn spring-boot:run
```

### Run with Docker

```bash
docker build -t secure-doc-search .
docker run -p 8080:8080 secure-doc-search
```

### Access the Application
```
http://localhost:8080
```

## Project Structure

```
src/
├── controller/      # Web and REST controllers
├── service/         # Business logic (encryption, search, upload)
├── repository/      # JPA repositories
├── model/           # Entity classes
├── security/        # Spring Security config, JWT, RBAC
├── util/            # AES encryption / decryption utilities
└── templates/       # Thymeleaf UI templates
```

## Use Cases

- Secure storage and retrieval of confidential enterprise documents
- Role-managed document access in academic or organizational settings
- Keyword search within encrypted document content
