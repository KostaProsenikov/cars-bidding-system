# Cars Bidding System

## Project Overview
This repository contains the Cars Bidding System, a comprehensive platform for online vehicle auctions. The system consists of a main bidding application and supporting microservices designed to facilitate the entire vehicle bidding process, from listing to purchase.

## Components

### Main Application: Cars Bidding
The core bidding system that handles user registration, vehicle listings, bid management, auction scheduling, payment processing, and user notifications.

### Microservice: svc-vin-check
A specialized microservice designed to provide Vehicle Identification Number (VIN) verification and history tracking services for the Cars Bidding Application. This service enables users to validate vehicle authenticity, retrieve detailed vehicle information, and maintain a history of previously checked VINs.

## Key Features

### Cars Bidding System
- User management with different roles (buyers, sellers, administrators)
- Vehicle listing creation and management
- Real-time bidding functionality
- Auction scheduling and management
- Secure payment processing
- Notification system for bid updates, auction start/end events
- User dashboard for tracking bids, listings, and purchases
- Admin panel for system oversight and management

### svc-vin-check Microservice
- **VIN Validation**: Verify the authenticity and structure of vehicle identification numbers
- **Vehicle Information Retrieval**: Access manufacturer details, model year, and assembly information
- **Historical Tracking**: Record and track VIN checks to prevent duplicate charges
- **User Association**: Link VIN checks to specific user accounts for analytics and quotas
- **API Integration**: Connect with external automotive databases for comprehensive vehicle data
- **Historical Lookup**: Query previous check results for returning users

## Technology Stack

### Common Technologies
- **Java 21+**: Modern language features for robust application development
- **Spring Boot**: Framework for building production-ready applications and services
- **Spring Data JPA**: Simplified data access layer
- **REST API**: Clean, documented endpoints for service integration
- **Maven**: Dependency management and build automation

### Cars Bidding Application
- **Spring MVC**: Web application framework
- **Spring Security**: Authentication and authorization
- **MySQL**: Primary database
- **WebSocket**: Real-time bidding updates
- **Thymeleaf**: Frontend with Thymeleaf

### svc-vin-check Microservice
- **Spring Boot**: Microservice framework
- **MySQL**: Persistent storage for VIN check history and results
- **External API Integration**: Connections to vehicle data providers

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 21+
- Maven 3.8+
- PostgreSQL 16+ (for main application)
- MySQL 16+ (for svc-vin-check)

### Installation and Setup

#### 1. Clone the repository
```bash
git clone https://github.com/your-organization/cars-bidding-system.git
cd cars-bidding-system
```

#### 2. Configure the databases
- Create a PostgreSQL database for the main application
- Create a MySQL database for the VIN check service
- Update the connection settings in respective application.properties files

#### 3. Build the applications
```bash
# Build all services
mvn clean install

# Build specific service
cd svc-vin-check
mvn clean install
```

#### 4. Run the applications
```bash
# Start the main application
cd cars-bidding-app
mvn spring-boot:run

# Start the VIN check service
cd svc-vin-check
mvn spring-boot:run
```

## Configuration
Each component has its own application.