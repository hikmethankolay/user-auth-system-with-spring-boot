# User Auth System With Spring Boot

This API provides authentication, role management, and user management functionalities.

| Coverage Type | Windows OS                                                             |
| ------------- | ---------------------------------------------------------------------- |
| Line Based    | ![Line Coverage](assets/badge_linecoverage.svg)     |
| Branch Based  | ![Branch Coverage](assets/badge_branchcoverage.svg) |
| Method Based  | ![Method Coverage](assets/badge_methodcoverage.svg) |


## Base URL

```
http://your-domain.com/api
```

## Authentication Endpoints

### Register a New User

**Endpoint:**

```
POST /api/auth/register
```

**Request Body:**

```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com"
  },
  "message": "User registered successfully"
}
```

### User Login

**Endpoint:**

```
POST /api/auth/login
```

**Request Body:**

```json
{
  "identifier": "user123",
  "password": "securepassword"
}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "token": "your-jwt-token",
    "tokenType": "Bearer"
  },
  "message": "User authenticated successfully"
}
```

## Role Management Endpoints

### Get All Roles

**Endpoint:**

```
GET /api/roles
```

**Response:**

```json
{
    "status": "SUCCESS",
    "data": [
        {
            "name": "ROLE_ADMIN",
            "users": [
                {
                    "id": 1,
                    "username": "user123",
                    "email": "user@example.com",
                }
            ]
        },
        {
            "name": "ROLE_MODERATOR",
            "users": []
        },
        {
            "name": "ROLE_USER",
            "users": [
                {
                    "id": 1,
                    "username": "user123",
                    "email": "user@example.com",
                }
            ]
        }
    ],
    "message": "Roles found successfully"
}
```

### Get Role by Name

**Endpoint:**

```
GET /api/roles/{role_name}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data":{
      "name": "ROLE_ADMIN",
      "users": [
          {
              "id": 1,
              "username": "user123",
              "email": "user@example.com",
          }
      ]
  },
  "message": "Role found successfully"
}
```

## User Management Endpoints

### Get All Users (Paginated)

**Endpoint:**

```
GET /api/users?page={page}&size={size}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "content": [
      {
        "id": 1,
        "username": "user123",
        "email": "user@example.com"
      }
    ],
    "totalPages": 5,
    "totalElements": 50,
    "currentPage": 1
  },
  "message": "Users found successfully"
}
```

### Get User by ID

**Endpoint:**

```
GET /api/users/{id}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com"
  },
  "message": "User found successfully"
}
```

### Get Logged-in User

**Endpoint:**

```
GET /api/users/me
```

### Get User by Username

**Endpoint:**

```
GET /api/users?username={username}
```

### Get User by Email

**Endpoint:**

```
GET /api/users?email={email}
```

### Update User

**Endpoint:**

```
PATCH /api/users/{id}
```

**Request Body:**

```json
{
  "username": "newUser123",
  "email": "newuser@example.com"
}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "data": {
    "id": 1,
    "username": "newUser123",
    "email": "newuser@example.com"
  },
  "message": "User updated successfully"
}
```

### Delete User

**Endpoint:**

```
DELETE /api/users/{id}
```

**Response:**

```json
{
  "status": "SUCCESS",
  "message": "User deleted successfully"
}
```

## Notes
- All requests and responses are in JSON format.
- Ensure that a valid token is provided for authentication-protected endpoints.
- Use appropriate HTTP methods (`GET`, `POST`, `PATCH`, `DELETE`) for interacting with the API.

## Features

- Automated build and test pipeline
- Documentation generation with Doxygen
- Test coverage reporting with JaCoCo and ReportGenerator
- Documentation coverage reporting with Coverxygen
- Maven site generation
- GitHub Actions CI/CD workflow

## Prerequisites

The following tools will be automatically installed by the setup scripts:

- Java Development Kit (JDK)
- Maven
- Doxygen
- Graphviz
- Python & pip
- .NET SDK (for ReportGenerator)
- LCOV
- Various documentation tools

## Setup Instructions

1. Clone this repository
2. Run the setup scripts in order:

### Setup Scripts

1. **1-create-git-ignore.bat**
   - Creates a comprehensive .gitignore file for the project
   - No need to edit this script

2. **2-install-package-manager.bat**
   - Installs Chocolatey and Scoop package managers
   - Edit if you need additional package sources

3. **3-install-required-apps.bat**
   - Installs all required development tools and dependencies:
     - Doxygen
     - Graphviz
     - Python tools
     - .NET SDK
     - Java Development Kit
     - Documentation tools
   - Edit if you need additional tools or different versions

4. **4-build-app.bat**
   - Cleans previous builds
   - Runs Maven build
   - Generates documentation
   - Creates test coverage reports
   - Packages outputs
   - Edit paths if project structure changes

5. **5-run-app.bat**
   - Runs the compiled application
   - Edit Java arguments if needed

6. **6-run-documentation-webpage.bat**
   - Starts a local server to view the documentation at http://localhost:9000
   - Edit port number if needed

## Project Configuration

### Edit `.bat` Files

1. **2-install-package-manager.bat**
   - Edit if you need additional package sources

2. **3-install-required-apps.bat**
   - Edit if you need additional tools or different versions

3. **4-build-app.bat**
   - Edit paths if project structure changes

4. **5-run-app.bat**
   - Edit Java arguments if needed

5. **6-run-documentation-webpage.bat**
   - Edit port number if needed

### Directory Structure

```
project-root/
├── .github/  # GitHub configuration files
├── assets/  # Project assets (e.g., images, badges)
├── user-auth-system/  # Main application directory
│   ├── src/  # Source code directory
│   │   ├── main/  # Main application source code
│   │   │   ├── java/  # Java source files
│   │   │   ├── resources/  # Application resources
│   │   ├── test/  # Test source code
│   │   │   ├── java/  # Java test files
│   │   │   ├── resources/  # Test resources
│   ├── site/  # Documentation site content
│   │   ├── markdown/  # Markdown files for site
│   │   ├── resources/  # Resources for site
│   ├── pom.xml  # Maven project configuration file
├── 1-create-git-ignore.bat
├── 2-install-package-manager.bat
├── 3-install-required-apps.bat
├── 4-build-app.bat
├── 5-run-app.bat
├── 6-run-documentation-webpage.bat
├── Doxyfile  # Doxygen configuration file
├── README.md  # Project README file
```

## Build Output

The build process (`4-build-app.bat`) generates:

- Compiled JAR files
- JaCoCo test coverage reports
- ReportGenerator coverage reports
- Doxygen documentation
- Coverxygen documentation coverage reports
- Maven site

## Documentation

Access the documentation locally:
1. Run `6-run-documentation-webpage.bat`
2. Open `http://localhost:9000/` in your browser
