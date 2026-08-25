# 🔗 Scalable URL Shortener with Analytics

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Async_Analytics-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)
![Google OAuth2](https://img.shields.io/badge/Google_OAuth2-Security-4285F4?style=for-the-badge&logo=google&logoColor=white)

A high-performance, fully-featured URL Shortener built with Spring Boot. This project goes beyond basic shortening by implementing enterprise-grade concepts like distributed caching, asynchronous event-driven analytics, and OAuth2 security.

---

## ✨ Key Features

- **Blazing Fast Redirects**: Utilizes **Redis** to cache resolved URLs, drastically reducing database load and ensuring lightning-fast redirects.
- **Asynchronous Click Analytics**: Click events (IP, User-Agent, Timestamp) are instantly published to **Apache Kafka** before redirecting, allowing background workers to process heavy analytics without slowing down the user experience.
- **Google OAuth2 Integration**: Secure user authentication via Google.
- **Custom Aliases**: Authenticated users can request custom alphanumeric aliases (e.g., `mygoogle`), which are algorithmically decoded into the primary key to prevent auto-increment collisions.
- **Personalized Analytics API**: Registered users can view aggregated click statistics for all the URLs they have created.
- **Base62 Encoding**: Anonymous users get short, mathematically secure, Base62 encoded URLs.

---

## 🛠️ Technology Stack

| Component | Technology | Purpose |
| --- | --- | --- |
| **Backend Framework** | Spring Boot (Java 21) | Core application logic and REST APIs |
| **Primary Database** | PostgreSQL | Persistent storage for users, URLs, and analytics |
| **Caching Layer** | Redis | In-memory cache for ultra-fast URL resolution |
| **Message Broker** | Apache Kafka (Kraft) | Async event streaming for click tracking |
| **Security** | Spring Security + OAuth2 | Google login and route protection |

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
- **Java 21**
- **Maven**
- **Docker** (Used to easily spin up Postgres, Redis, and Kafka)

### 1. Environment Setup

Start your dependent services using Docker:

```bash
# Start Redis
docker run -d --name redis -p 6379:6379 redis:latest

# Start Apache Kafka (Kraft Mode)
docker run -d --name kafka -p 9092:9092 apache/kafka:3.7.0
```
*(Ensure you have a PostgreSQL instance running on port `5432` with a database named `urlshortener`)*

### 2. Configure Application
Open `src/main/resources/application.properties` and add your Google OAuth2 credentials:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```
The application will start on `http://localhost:8080`.

---

## 📖 API Documentation

### Public Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/urls/shorten` | Create a standard Base62 short URL. Body: `{"longUrl": "..."}` |
| `GET` | `/{shortUrl}` | Redirects to the original URL (triggers Kafka analytics event). |
| `GET` | `/login` | Triggers the Google OAuth2 login flow. |

### Protected Endpoints (Requires Login / `JSESSIONID` cookie)

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/urls/custom` | Create a custom alias (max 10 alphanumeric chars). Body: `{"longUrl": "...", "customAlias": "..."}` |
| `GET` | `/api/urls/analytics` | View total click statistics for all URLs owned by the logged-in user. |

---

## 🧠 System Architecture Highlights

1. **The Cache-Aside Pattern**: When a user visits a short URL, the system first checks Redis. If it's a `CACHE MISS`, it fetches it from PostgreSQL, caches it in Redis, and then redirects. Subsequent visits are `CACHE HITS`, skipping the database entirely.
2. **Event-Driven Analytics**: To prevent analytics processing from delaying the user's redirect, the `RedirectController` simply fires a JSON payload to a Kafka topic (`url-clicks`) and immediately returns a `302 Redirect`. A background `@KafkaListener` picks up the message moments later and persists it to PostgreSQL.
3. **Smart Custom Alias IDs**: To prevent Spring Data JPA from attempting to `UPDATE` manually generated IDs for custom aliases, the application uses a Native SQL `@Modifying` query to forcefully `INSERT` the backwards-decoded Base62 ID into the IDENTITY column.

---

> Built with ❤️ by Moksh.
