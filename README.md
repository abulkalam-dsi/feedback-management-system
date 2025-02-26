# Feedback Management System - Dockerized 🚀

This project is a Spring Boot-based **Feedback Management System** that allows users to submit feedback, assign approvers, and track history. It is fully Dockerized for easy deployment.

## 🛠️ Prerequisites
Before running the project, ensure you have:
- **Docker** installed ([Get Docker](https://docs.docker.com/get-docker/))
- **Docker Compose** installed ([Guide](https://docs.docker.com/compose/install/))

## 🚀 Running the Application

### **1️⃣ Clone the Repository**
```sh
git clone https://github.com/your-repo/feedback-management.git
cd feedback-management
API Endpoint: http://localhost:8080/api/
Admin	admin@gmail.com	12345678
User akash@gmail.com 12345678

Use the login API to get a token:
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "admin@gmail.com", "password": "12345678"}'
