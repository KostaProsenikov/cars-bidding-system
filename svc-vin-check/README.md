# svc-vin-check
## Overview
The **svc-vin-check** is a specialized microservice designed to provide Vehicle Identification Number (VIN) verification and history tracking services for the Cars Bidding Application. This service enables users to validate vehicle authenticity, retrieve detailed vehicle information, and maintain a history of previously checked VINs to avoid redundant verification costs.
## Key Features
- **VIN Validation**: Verify the authenticity and structure of vehicle identification numbers
- **Vehicle Information Retrieval**: Access manufacturer details, model year, and assembly information
- **Historical Tracking**: Record and track VIN checks to prevent duplicate charges
- **User Association**: Link VIN checks to specific user accounts for analytics and quotas
- **API Integration**: Connect with external automotive databases for comprehensive vehicle data
- **Historical Lookup**: Query previous check results for returning users

## Technology Stack
- **Java 21**: Modern language features for robust microservice development
- **Spring Boot**: Framework for building production-ready microservices
- **Spring Data JPA**: Simplified data access layer
- **REST API**: Clean, documented endpoints for service integration
- **MySQL**: Persistent storage for VIN check history and results
- **Maven**: Dependency management and build automation

## API Endpoints
### VIN Verification
- `GET /api/vin/{vinNumber}` - Retrieve information about a specific VIN
- `POST /api/vin/check` - Perform a new VIN check and store results
- `GET /api/vin/user/{userId}` - Retrieve all VIN checks for a specific user
- `GET /api/vin/validate/{vinNumber}` - Validate a VIN without saving the check

### VIN History
- `GET /api/vin/history/{vinNumber}` - Retrieve check history for a specific VIN
- `GET /api/vin/history/user/{userId}` - Get all VIN checks performed by a user
- `POST /api/vin/history` - Create a new VIN check history record
- `GET /api/vin/history/exists?vin={vinNumber}&userId={userId}` - Check if this user has previously checked this VIN

## Getting Started
### Prerequisites
- Java Development Kit (JDK) 21+
- Maven 3.8+
- MySQL 16+

### Installation
1. **Clone the repository**
``` bash
   git clone https://github.com/your-organization/svc-vin-check.git
   cd svc-vin-check
```
1. **Configure database**
    - Create a PostgreSQL database named `vin_check_service`
    - Update with your database credentials: `application.properties`
``` properties
     spring.datasource.url=jdbc:postgresql://localhost:5432/vin_check_service
     spring.datasource.username=postgres
     spring.datasource.password=your_password
```
1. **Build the application**
``` bash
   mvn clean install
```
1. **Run the application**
``` bash
   mvn spring-boot:run
```

## Configuration
The service can be configured through the file: `application.properties`
``` properties
# Server configuration
server.port=8082
spring.application.name=vin-check-service

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vin_check_service
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# VIN check configuration
vin.check.external.api.url=https://vehicle-data-provider.example.com/api
vin.check.api.key=${VIN_API_KEY:default_key_for_development}
vin.check.cache.expiration=24h

# Logging configuration
logging.level.app.vin=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
```
## Project Structure
``` 
src/
├── main/
│   ├── java/app/vin/
│   │   ├── controller/   # REST API endpoints
│   │   ├── model/        # Entity classes
│   │   ├── repository/   # Data access layer
│   │   ├── service/      # Business logic
│   │   ├── client/       # External API integrations
│   │   ├── config/       # Service configuration
│   │   ├── exception/    # Exception handling
│   │   └── util/         # Utility classes
│   └── resources/
│       ├── application.properties
│       ├── data.sql      # Initial data setup
│       └── logback.xml   # Logging configuration
└── test/                 # Test cases
```
## Integration with Cars Bidding Application
The main Cars Bidding Application communicates with this service through REST API calls. Typical integration points include:
1. **VIN Verification Process**:
    - When a user selects "Check VIN" on a vehicle listing
    - When a seller adds a new vehicle listing

2. **VIN History Verification**:
    - Before processing a VIN check, the main application queries if the user has already checked this VIN
    - The service responds with check history, preventing duplicate checks if configured to do so

3. **VIN Information Display**:
    - After successful verification, vehicle details are displayed in the main application
    - Information includes manufacturer, model year, assembly plant, and verification status

## Data Models
### VIN Check Record
``` json
{
  "id": "uuid-string",
  "userId": "user-uuid-string",
  "vinNumber": "17CHAR_VIN_NUMBER",
  "checkDate": "2023-05-15T14:30:45Z",
  "manufacturer": "TOYOTA",
  "modelYear": "2020",
  "assemblyPlant": "USA-KENTUCKY",
  "status": "VALID",
  "additionalInfo": {
    "model": "CAMRY",
    "engineType": "2.5L 4-CYL",
    "transmissionType": "AUTOMATIC"
  }
}
```
## Monitoring and Management
The service provides the following endpoints for monitoring:
- `/actuator/health` - Check service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Metrics for service performance monitoring

## Error Handling
The service uses the following HTTP status codes:
- `200 OK` - Request successful
- `400 Bad Request` - Invalid VIN format or request parameters
- `404 Not Found` - VIN information or history not found
- `429 Too Many Requests` - Rate limit exceeded for external VIN API
- `500 Internal Server Error` - Unexpected service errors

Each error response includes:
``` json
{
  "timestamp": "2023-05-15T14:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid VIN format: should be 17 characters",
  "path": "/api/vin/ABC123"
}
```
## Security Considerations
- The service requires API key authentication for external applications
- User identification is required for all VIN check operations
- Rate limiting prevents abuse of external VIN data providers
- All API endpoints are secured using Spring Security

## License
This project is licensed under the MIT License - see the LICENSE file for details.
## Support
For support, please contact our development team at dev-support@carsbidding.example.com or open an issue in the GitHub repository.

© 2025 Cars Bidding Application. All rights reserved.
