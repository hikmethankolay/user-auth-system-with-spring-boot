# User Authentication System with Spring Boot and JWT

A comprehensive authentication and authorization system built with Spring Boot and JWT, providing secure user management, role-based access control, and protection against common security threats.

![Badge Combined](assets/badge_combined.svg) ![Badge Branch Coverage](assets/badge_branchcoverage.svg) ![Badge Line Coverage](assets/badge_linecoverage.svg) ![Badge Method Coverage](assets/badge_methodcoverage.svg)

## Features

- **User Authentication**: Secure login with JWT tokens
- **Registration System**: User sign-up with validation
- **Remember Me Functionality**: Persistent authentication with HTTP-only cookies
- **Role-Based Authorization**: Granular access control with USER, MODERATOR, and ADMIN roles
- **Brute Force Protection**: Account locking after multiple failed attempts
- **Token Management**: Token refresh, validation, and secure storage
- **Comprehensive API**: User management endpoints with pagination
- **Security Best Practices**: CORS protection, validation, and secure cookie handling
- **Extensive Documentation**: Code documentation and API documentation via Swagger

## Technologies Used

- Java 21
- Spring Boot 3.4.2
- Spring Security
- JWT Authentication (Auth0 JWT library)
- PostgreSQL
- JUnit 5 & Mockito
- Swagger/OpenAPI
- JaCoCo for test coverage
- Doxygen for code documentation

## System Requirements

- Java 21 JDK
- PostgreSQL database
- Windows OS (for batch scripts)
- Internet connection (for dependency download)

## Project Structure

```
user-auth-system/
├── src/
│   ├── main/
│   │   ├── java/com/hikmethankolay/user_auth_system/
│   │   │   ├── aspect/           # AOP logging
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # API endpoints
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── enums/            # Enumerations
│   │   │   ├── exception/        # Exception handlers
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── security/         # Security config
│   │   │   ├── service/          # Business logic
│   │   │   ├── util/             # Utility classes
│   │   │   ├── validator/        # Custom validators
│   │   │   └── UserAuthSystemApplication.java
│   │   └── resources/
│   │       ├── application.properties  # App configuration
│   │       └── example.env             # Environment variables template
│   └── test/                      # Test classes
├── target/                        # Compiled output
├── 1-create-git-ignore.bat        # Git ignore setup
├── 2-install-package-manager.bat  # Package manager setup
├── 3-install-required-apps.bat    # Required apps installation
├── 4-build-app.bat                # Application build script
├── 5-run-app.bat                  # Application run script
└── 6-run-documentation-webpage.bat # Documentation server
```

## Installation & Setup

### Prerequisites

1. Ensure Java 21 is installed and JAVA_HOME is properly configured
2. Have PostgreSQL installed and running
3. Clone this repository

### Quick Start (Windows)

Run the setup scripts in sequence:

```bash
# 1. Set up Git ignore
.\1-create-git-ignore.bat

# 2. Install package managers (Chocolatey and Scoop)
.\2-install-package-manager.bat

# 3. Install required applications
.\3-install-required-apps.bat

# 4. Build the application
.\4-build-app.bat
```

### Manual Setup

1. Configure database
   - Create a PostgreSQL database
   - Create a `.env` file in the `user-auth-system` directory based on `example.env`

2. Build the application
   ```bash
   cd user-auth-system
   ./mvnw clean package
   ```

## Configuration

Create a `.env` file in the `user-auth-system` directory with the following variables:

```
DB_NAME=yourdbname
CURRENT_SCHEMA=public
DB_USERNAME=yourusername
DB_PASSWORD=yourpassword
JWT_SECRET=your-secret-key-should-be-very-long-and-secure
JWT_EXPIRATION_TIME=900000
JWT_REMEMBER_ME_EXPIRATION_TIME=2592000000
API_USERNAME=admin
API_PASSWORD=password
API_ROLES=ADMIN
```

## Running the Application

### Using Batch File

```bash
.\5-run-app.bat
```

### Manual Run

```bash
cd user-auth-system
java -jar target/user-auth-system-1.0.jar
```

The application will start on port 8080 by default.

## API Documentation

The API documentation is available through Swagger UI when the application is running:

```
http://localhost:8080/swagger-ui/index.html
```

### Main Endpoints

- **Authentication**
  - POST `/api/auth/register` - Register a new user
  - POST `/api/auth/login` - Login with credentials
  - POST `/api/auth/logout` - Logout current user
  - POST `/api/auth/refresh-token` - Refresh authentication token

- **User Management**
  - GET `/api/users` - List all users (Admin only)
  - GET `/api/users/me` - Get current user info
  - PATCH `/api/users/me` - Update current user
  - GET `/api/users/{id}` - Get specific user (Admin only)
  - PATCH `/api/users/{id}` - Update specific user (Admin only)
  - DELETE `/api/users/{id}` - Delete user (Admin only)

- **Role Management**
  - GET `/api/roles` - List all roles (Admin only)
  - GET `/api/roles/{name}` - Get specific role (Admin only)

## Security Features

### Password Requirements

Passwords must:
- Be 8-32 characters in length
- Include at least one uppercase letter
- Include at least one lowercase letter
- Include at least one digit
- Include at least one special character

### Brute Force Protection

The system includes protection against brute force attacks:
- Accounts are temporarily locked after 10 failed login attempts
- IP addresses are temporarily blocked after 10 failed login attempts
- Login attempt tracking for both username and IP address

### Token Security

- JWT tokens are signed with HMAC256
- Short-lived tokens (15 minutes) by default
- Optional long-lived tokens (30 days) with "Remember Me"
- HTTP-only cookies for token storage
- Automatic token refreshing

### CORS Protection

Configured CORS policy allowing only specific origins and methods:
- Allowed origins: localhost:3000 and yourdomain.com
- Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Allowed headers: Authorization, Content-Type

## Testing

The application includes extensive unit tests for all components:

### Running Tests

```bash
cd user-auth-system
./mvnw test
```

### Test Coverage

The project aims for high test coverage, with JaCoCo reports available after testing:
- View coverage reports: `user-auth-system/target/site/coveragereport/index.html`

## Documentation

### API Documentation

Swagger UI provides interactive API documentation:
```
http://localhost:8080/docs
```

### Code Documentation

Doxygen-generated code documentation:

```bash
# Run the documentation web server
.\6-run-documentation-webpage.bat
```

The documentation website will be available at:
```
http://localhost:9000/
```

## Project Output

After building, the following artifacts are available in the `release` directory:

- `application-binary.tar.gz` - Executable JAR files
- `test-jacoco-report.tar.gz` - JaCoCo test coverage reports
- `test-coverage-report.tar.gz` - ReportGenerator test coverage reports
- `application-documentation.tar.gz` - Doxygen code documentation
- `doc-coverage-report.tar.gz` - Documentation coverage reports
- `application-site.tar.gz` - Maven site

## License

This project is authored by Hikmethan Kolay (© 2025) and is available for educational purposes.

## Author

- **Hikmethan Kolay** - [GitHub](https://github.com/hikmethankolay)
- **Email**: hikmethan_kolay22@erdogan.edu.tr
- **Organization**: Recep Tayyip Erdogan University