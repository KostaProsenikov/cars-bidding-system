# **Cars Bidding Application**
Welcome to the **Cars Bidding Application**, a modern platform for car enthusiasts, buyers, and sellers to engage in transparent car buying and selling through dynamic bidding. This application provides users with features like managing car advertisements, placing bids, monitoring subscriptions, and reviewing transaction histories.
## **Features**
### For General Users:
- **Browse Cars**: View a wide range of listed cars with detailed descriptions, images, and specifications.
- **Car Search**: Search for cars based on brand, model, manufacture year, and price range.
- **Bidding**: Participate in active car auctions and place competitive bids for your dream car.

### For Advertisers:
- **List Adverts**: Create, update, and manage car listings with photos and detailed information.
- **Auto-Extend Options**: Automatically extend your advert duration for maximum visibility.
- **Buy Now Options**: Allow buyers to purchase your car instantly with a predefined price.

### Additional Functionalities:
- **Dynamic Pricing**: Adjust car advert durations and manage flexible pricing plans (default, plus, professional).
- **Subscriptions**: Upgrade your subscription plan for increased advert limits and enhanced features.
- **Transaction Management**: View all past transactions, including successful and failed payments.
- **VIN Checks**: Access free VIN (Vehicle Identification Number) checks, depending on your subscription level.

## **Tech Stack**
The Cars Bidding Application is built using modern technologies to ensure performance, security, and rapid scalability.
### Backend:
- **Java 22**: Main programming language.
- **Spring Boot**: For building a robust and modular backend application.
- **Spring Security**: To support user authentication and role-based authorization.
- **Spring Data JPA**: Database ORM for interacting with PostgreSQL or another relational database.

### Frontend:
- **Thymeleaf**: Server-side rendering for dynamic HTML templates.
- **TailwindCSS**: For consistent and responsive UI styling.
- **JavaScript**: Enhancing user interactivity with dynamic behaviors.

### Database:
- **PostgreSQL**: Stores user data, advertisements, transactions, bidding information, and subscriptions.

### Others:
- **Maven**: Dependency management and project building.
- **HTML5 & CSS3**: For responsive, visually appealing frontend design.
- **JUnit**: For maintaining high test coverage and ensuring reliable code performance.

## **Installation and Setup**
To get started with the Cars Bidding Application, follow these steps:

### Prerequisites
Ensure the following tools are installed on your system:
- Java Development Kit (JDK) 22+
- Maven 3.8+
- Docker (optional, for containerized deployment)

1. Access the application:
    - Open your browser and navigate to `http://localhost:8081`.

## **Usage**

### User Roles

- **Guest**: Can view car advertisements, but cannot participate in bidding or create adverts.
- **Registered User**: Can bid on cars, view transaction history, and create limited car advertisements.
- **Premium User**: Access additional features like "Auto-Extend Ads", premium bidding, VIN check for Automobiles.

### Key Pages

- **Home Page**: Displays all active auctions with filters to narrow the search.
- **My Ads**: Manage personal adverts and their statuses.
- **Subscriptions**: Upgrade or downgrade your subscription plans.
- **Transactions**: Review past transaction details.
- **Bidding Page**: Participate in live car bidding or buy cars directly with the "Buy Now" feature.

## **API Endpoints**

Here are some key API endpoints for developers:

### Authentication
- `POST /auth/login`: Authenticate a user.
- `POST /auth/register`: Register a new user.

### Ads
- `GET /ads`: Fetch all advertisements.
- `POST /ads`: Create a new car advert.
- `PUT /ads/{id}`: Update an existing advert.
- `DELETE /ads/{id}`: Remove a car advert.

### Bidding
- `GET /bids/{advertId}`: Get bids for a specific car advertisement.
- `POST /bids`: Place a new bid on an advert.

### Transactions
- `GET /transactions`: Fetch all user transactions.

### Subscriptions
- `POST /subscriptions/change/{type}`: Change subscription type.

## **License**

This project is licensed under the **MIT License**. For more details, see the `LICENSE` file.

## **Contact**

- **Author**: Kostadin Prosenikov
- **Profile**: https://github.com/KostaProsenikov

Let us know if you have questions or need support!
