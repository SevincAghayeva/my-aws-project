# my_aws_project

A robust, enterprise-grade Spring Boot microservice designed 
for high-performance document uploading and secure metadata registration. 
This project utilizes an individual cloud infrastructure 
setup powered by **AWS SDK v2**, **Docker**, **Dockerfile**, and **Terraform** 
to emulate local AWS components (**S3, DynamoDB, SQS**) inside **LocalStack**.

---

## Architecture & Component Flow

The ecosystem handles resources dynamically in a completely automated cycle:
1. **Terraform** initializes and connects to LocalStack (`http://localhost:4566`) to spin up the independent cloud units.
2. **Spring Boot App** runs inside an isolated container built from a lightweight Alpine JRE environment via the local `Dockerfile`.
3. **Resilient Handling:** At startup, `DocumentService` executes a dynamic retry loop to wait until Terraform provisions the buckets, avoiding context boot race-condition crashes.

---

## Project Ecosystem

*   **Application Core:** Java 17 (Eclipse Temurin JRE Alpine) & Spring Boot 3.3.4
*   **AWS Clients:** AWS Java SDK v2 (`S3Client`, `DynamoDbEnhancedClient`, `SqsClient`)
*   **Infrastructure as Code (IaC):** Terraform v1.x+
*   **Local Engine:** LocalStack (S3 Storage, SQS Messengers, DynamoDB Tables)

---

## Infrastructure & Service Deployment

Run these exact terminal operations sequentially to clear cached containers, 
compile artifacts, and deploy the stack:

### 1. Build Application Executable
Compile source files and pack them into a production JAR:
```bash
./gradlew clean bootJar