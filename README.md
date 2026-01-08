# CloudShare Backend

CloudShare Backend is a scalable and secure REST API built with **Spring Boot** that powers the CloudShare platform. It handles authentication, file storage, payments, credits, and transaction tracking using modern cloud-native services.

---

## 🚀 Features

* **Authentication & User Management**

  * Powered by **Clerk**
  * JWT verification via JWKS
  * Secure webhook handling for user lifecycle events

* **File Upload & Management**

  * Secure file uploads with configurable size limits
  * Cloud storage using **Supabase Buckets**
  * File preview, organization, and deletion
  * Device-agnostic access

* **Secure Storage**

  * Encrypted cloud storage via Supabase
  * Access controlled by authenticated users

* **Payments & Credits**

  * Integrated with **Razorpay**
  * Credit-based billing system
  * Pay-as-you-use model

* **Transaction History**

  * Persistent transaction records in MongoDB
  * Full audit trail for purchases and usage

* **Simple Sharing**

  * Share files via secure, controlled links

* **Observability**

  * Spring Boot Actuator health endpoints
  * Production-ready configuration profiles

---

## 🏗️ Tech Stack

| Layer             | Technology               |
| ----------------- | ------------------------ |
| Backend Framework | Spring Boot              |
| Authentication    | Clerk                    |
| Payments          | Razorpay                 |
| File Storage      | Supabase                 |
| Database          | MongoDB                  |
| Build Tool        | Maven                    |
| Deployment        | Container / Cloud-native |

---

## 📁 Project Structure

```
cloudshare-backend/
├── src/main/java/com/cloudshare
│   ├── auth/              # Clerk authentication & JWT validation
│   ├── config/            # Security, Supabase, Razorpay configs
│   ├── controller/        # REST controllers
│   ├── service/           # Business logic
│   ├── repository/        # MongoDB repositories
│   ├── model/             # Domain models
│   └── webhook/           # Clerk webhook handlers
├── src/main/resources/
│   ├── application.yml
│   └── application-prod.yml
└── pom.xml
```

---

## ⚙️ Environment Configuration

All sensitive values are injected using environment variables.

### Required Environment Variables

```env
# Server
PORT=8080

# MongoDB
MONGODB_URI=mongodb+srv://<user>:<password>@cluster.mongodb.net/cloudshare

# Supabase
SUPABASE_URL=https://xyzcompany.supabase.co
SUPABASE_SERVICE_KEY=your-service-role-key
SUPABASE_BUCKET=cloudshare-files

# Clerk
CLERK_ISSUER=https://clerk.yourdomain.com
CLERK_JWKS_URL=https://clerk.yourdomain.com/.well-known/jwks.json
CLERK_WEBHOOK_SECRET=your-webhook-secret

# Razorpay
RAZORPAY_KEY_ID=rzp_live_xxxxx
RAZORPAY_KEY_SECRET=your-secret-key

# File Upload Limits
MAX_FILE_SIZE=50MB
MAX_REQUEST_SIZE=50MB
```

---

## 🧩 Spring Boot Configuration (Production)

```properties
spring.config.activate.on-profile=prod
server.port=${PORT:8080}

spring.data.mongodb.uri=${MONGODB_URI}

spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
server.servlet.context-path=/api/v1.0

supabase.url=${SUPABASE_URL}
supabase.service.key=${SUPABASE_SERVICE_KEY}
supabase.bucket=${SUPABASE_BUCKET}

clerk.issuer=${CLERK_ISSUER}
clerk.jwks-url=${CLERK_JWKS_URL}
clerk.webhook.secret=${CLERK_WEBHOOK_SECRET}

razorpay.key.id=${RAZORPAY_KEY_ID}
razorpay.key.secret=${RAZORPAY_KEY_SECRET}

spring.servlet.multipart.max-file-size=${MAX_FILE_SIZE}
spring.servlet.multipart.max-request-size=${MAX_REQUEST_SIZE}

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
management.endpoints.web.base-path=/actuator
```

---

## 🔐 Security Overview

* Stateless JWT authentication using Clerk
* No server-side session storage
* Secure webhook verification
* Encrypted cloud file storage
* Environment-based secret management

---

## 🔗 API Base URL

```
/api/v1.0
```

Example:

```
POST /api/v1.0/files/upload
GET  /api/v1.0/files
POST /api/v1.0/payments/create-order
```

---

## 🧪 Running Locally

```bash
# Build
mvn clean install

# Run (dev)
mvn spring-boot:run

# Run (prod)
SPRING_PROFILES_ACTIVE=prod java -jar target/cloudshare-backend.jar
```

---

## 📦 Deployment Notes

* Fully compatible with Docker, Railway, Render, Fly.io, AWS, or GCP
* Ensure environment variables are configured in the deployment platform
* MongoDB Atlas recommended for production

---

## 📊 Health Check

```
GET /api/v1.0/actuator/health
```

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 🤝 Contribution

Pull requests are welcome. For major changes, please open an issue first to discuss your proposal.

---

## 📬 Contact contact

For backend-related questions or integration support, please reach out to the project maintainer.

---

**CloudShare Backend – Secure. Scalable. Cloud-Native.**
