# Cars Bidding Application

## Overview

The **Cars Bidding Application** is a comprehensive platform designed for automotive enthusiasts, buyers, and sellers to engage in a transparent, secure, and efficient car marketplace. With powerful features like real-time bidding, VIN verification, comprehensive car listings, and flexible subscription plans, this application streamlines the entire car buying and selling experience.

![Cars Bidding Application](https://img.shields.io/badge/Status-Production-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Latest-brightgreen)

## Key Features

### For Buyers
- **Advanced Car Search & Browsing**: Find vehicles based on brand, model, year, price range, and more
- **Bidding System**: Place competitive bids on vehicles with real-time updates
- **Buy Now Option**: Purchase vehicles instantly at a fixed price
- **VIN Verification**: Check vehicle identification numbers for authenticity and history
- **Saved Searches**: Save favorite searches for quick access
- **Watchlist**: Monitor specific vehicles of interest

### For Sellers
- **Comprehensive Ad Management**: Create, edit, and monitor vehicle listings with detailed specifications
- **Media Support**: Upload high-quality images to showcase vehicles
- **Bid Management**: Track and respond to bids from potential buyers
- **Analytics Dashboard**: View insights about listing performance and buyer interest
- **Flexible Pricing Options**: Set Buy Now prices or enable auction-style bidding
- **Auto-Extend Listings**: Configure ads to automatically extend when receiving bids near closing time

### Account Management
- **Subscription Plans**: Choose from various subscription tiers with different benefits
- **Transaction History**: View complete record of all past transactions
- **Wallet System**: Secure payment processing for bids and purchases
- **Profile Management**: Update personal information and preferences
- **Notification System**: Receive alerts for bid updates, auction ends, and more

## Technology Stack

### Backend
- **Java 21**
- **Spring Boot**: Modern framework for building production-ready applications
- **Spring Security**: Comprehensive security for authentication and authorization
- **Spring Data JPA**: Simplified data access layer with Hibernate
- **Spring MVC**: Web application development with Model-View-Controller pattern

### Frontend
- **Thymeleaf**: Modern server-side Java template engine
- **TailwindCSS**: Utility-first CSS framework for rapid UI development
- **JavaScript**: Enhanced client-side functionality
- **Responsive Design**: Optimized for all device types (desktop, tablet, mobile)

### Database & Storage
- **MySQL**: Reliable relational database for structured data storage
- **Redis**: Caching for improved performance (optional implementation)

### Integration & Services
- **RESTful APIs**: Clean API design for service integration
- **VIN Verification Service**: External integration for vehicle history checks
- **Payment Processing**: Secure transaction handling

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 21+
- Maven 3.8+
- MySQL 8+
- Modern web browser

### Installation

1. **Clone the repository**
```shell script
git clone https://github.com/your-organization/cars-bidding-application.git
   cd cars-bidding-application
```


2. **Configure database**
   - Create a PostgreSQL database named `cars_bidding`
   - Update `application.properties` with your database credentials

3. **Build the application**
```shell script
mvn clean install
```


4. **Run the application**
```shell script
mvn spring-boot:run
```


5. **Access the application**
   - Open your browser and navigate to `http://localhost:8081`

## User Roles and Permissions

| Feature | Guest | Basic User | Premium User | Admin |
|---------|-------|------------|--------------|-------|
| View Listings | ✅ | ✅ | ✅ | ✅ |
| Create Listings | ❌ | ✅ (limited) | ✅ (unlimited) | ✅ |
| Place Bids | ❌ | ✅ | ✅ | ✅ |
| VIN Checks | ❌ | ✅ (limited) | ✅ (unlimited) | ✅ |
| Auto-Extend | ❌ | ❌ | ✅ | ✅ |
| Analytics | ❌ | ❌ | ✅ | ✅ |
| User Management | ❌ | ❌ | ❌ | ✅ |

## Subscription Plans

| Feature | Free | Standard | Premium |
|---------|------|----------|---------|
| Monthly Price | $0 | $9.99 | $19.99 |
| Active Listings | 2 | 10 | Unlimited |
| Featured Listings | 0 | 2 | 5 |
| VIN Checks/Month | 5 | 20 | 50 |
| Auto-Extend | ❌ | ✅ | ✅ |
| Advanced Analytics | ❌ | ❌ | ✅ |

## API Documentation

The application provides a comprehensive RESTful API for developers and integrators:

- **Authentication**: `/api/auth/*` - Login, registration, and token management
- **Listings**: `/api/ads/*` - CRUD operations for vehicle advertisements
- **Bidding**: `/api/bids/*` - Bid placement and management
- **User**: `/api/users/*` - User profile and preference management
- **Subscriptions**: `/api/subscriptions/*` - Subscription handling

For detailed API documentation, access the Swagger UI at `/swagger-ui.html` when running the application.

## Project Structure

```
src/
├── main/
│   ├── java/app/
│   │   ├── ad/           # Advertisement management
│   │   ├── auth/         # Authentication and security
│   │   ├── bid/          # Bidding functionality
│   │   ├── config/       # Application configuration
│   │   ├── subscription/ # Subscription plans
│   │   ├── transaction/  # Payment processing
│   │   ├── user/         # User management
│   │   ├── utils/        # Utility classes
│   │   ├── vin/          # VIN verification
│   │   └── wallet/       # User wallet system
│   └── resources/
│       ├── static/       # Static resources (CSS, JS)
│       ├── templates/    # Thymeleaf templates
│       └── application.properties
└── test/                 # Test cases
```


## Contributing

We welcome contributions to the Cars Bidding Application! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please contact our team at support@carsbidding.example.com or open an issue in the GitHub repository.

---

© 2025 Cars Bidding Application. All rights reserved.