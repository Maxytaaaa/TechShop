# TechShop

🔗 Repository: https://github.com/Maxytaaaa/TechShop

TechShop is a web-based e-commerce application for selling electronics, built with Java and Spring Boot.
It allows users to browse products, manage a shopping cart, place orders, and pay online using Stripe.

The application also includes authentication (including Google login) and an admin panel for managing products, categories, and orders.

---

## Features

* User registration and login
* Google OAuth2 authentication
* Product catalog with categories
* Product search
* Shopping cart (add, remove, update quantity)
* Order creation and history
* Online payment with Stripe
* Product reviews and ratings
* Admin panel for managing products, categories, and orders

---

## Tech Stack

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* OAuth2 (Google)
* Thymeleaf
* PostgreSQL
* Stripe API
* Lombok
* Maven

---

## Getting Started

### Prerequisites

* Java 17+
* Maven
* PostgreSQL

---

### Clone the repository

```
git clone https://github.com/Maxytaaaa/TechShop.git
cd TechShop
```

---

### Configuration

Create environment variables:

```
DATABASE_URL=your_database_url
DB_USER=your_db_user
DB_PASSWORD=your_db_password

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key

APP_BASE_URL=http://localhost:8080

APP_ADMIN_EMAIL=admin@example.com
APP_ADMIN_PASSWORD=secret
```

---

### Run locally

1. Set environment variables
2. Run the application:

```
./mvnw spring-boot:run
```

3. Open in browser:

```
http://localhost:8080
```

---

## Authentication

The application supports:

* standard login with username and password
* Google OAuth2 login

Passwords are encrypted using BCrypt.

---

## Roles

* USER – regular user
* ADMIN – has access to admin panel

Admin endpoints:

```
/admin
```

---

## Payment

Payments are handled using Stripe Checkout.

Flow:

1. User adds products to basket
2. Proceeds to checkout
3. Redirected to Stripe
4. After successful payment, order is created

---

## Screenshots

*Add screenshots here (catalog, basket, checkout)*

---

## Author

* GitHub: https://github.com/Maxytaaaa
