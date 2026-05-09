# 🛡️ Backend (Spring Boot)

The backend service for **File-Share**, a secure file-sharing platform. It handles authentication, file operations, payment processing, and database management.

---

## 🚀 Features

### 🔐 Authentication & Security

* Clerk JWT authentication
* Role-based access control (RBAC)
* Secure REST APIs using Spring Security

### 📁 File Management

* Supabase Storage integration
* Upload, download, and file metadata handling
* Supports public and private file access

### 💳 Payments

* Razorpay order creation
* Payment verification system
* Credit-based usage model

### 🗄️ Database

* MongoDB for scalable metadata storage
* Optimized queries for performance

---

## 🛠️ Tech Stack

* Spring Boot 3.x
* Spring Security
* MongoDB
* Supabase Storage
* Razorpay
* Maven

---

## ⚙️ Setup & Run

### 1️⃣ Configure Environment Variables

```env id="b1env"
MONGODB_URI=your_mongodb_uri
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
RAZORPAY_KEY=your_key
RAZORPAY_SECRET=your_secret
CLERK_SECRET_KEY=your_clerk_secret
```

---

### 2️⃣ Run Application

```bash id="b1run"
./mvnw clean install
./mvnw spring-boot:run
```

---

## 🔌 API Endpoints

| Method | Endpoint                          | Description          |
| ------ | --------------------------------- | -------------------- |
| POST   | `/api/v1.0/files/upload`          | Upload files         |
| GET    | `/api/v1.0/files/download/{id}`   | Download file        |
| GET    | `/api/v1.0/files/my`              | Get user files       |
| POST   | `/api/v1.0/payments/create-order` | Create payment order |

---

## 📌 Notes

* Ensure environment variables are set before running
* Use secure secrets in production
* Configure Supabase storage policies (RLS)
