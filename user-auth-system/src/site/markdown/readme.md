# User Auth System With Spring Boot

| Coverage Type | Windows OS                                                             |
| ------------- | ---------------------------------------------------------------------- |
| Line Based    | ![Line Coverage](assets/badge_linecoverage.svg)     |
| Branch Based  | ![Branch Coverage](assets/badge_branchcoverage.svg) |
| Method Based  | ![Method Coverage](assets/badge_methodcoverage.svg) |

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

### Required Modifications

1. **Update `user-auth-system/pom.xml`:**
   - Change `groupId`, `artifactId`, and `version`
   - Update project description
   - Modify SCM, developer, and organization information

2. **Update `Doxyfile`:**
   - Modify `PROJECT_NAME`, `PROJECT_BRIEF`, and `PROJECT_NUMBER`
   - Adjust `INPUT` paths if needed

3. **Update `user-auth-system/src/site/site.xml`:**
   - Modify banner and navigation menu items
   - Update project links and documentation structure

4. **Create content in:**
   - `user-auth-system/src/site/markdown/overview.md`
   - `README.md`

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
