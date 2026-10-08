# RetroLab

**RetroLab — Console Repair Management and IoT Diagnostic System**

RetroLab is a full-stack web application designed to manage video game console repairs, organize diagnostic procedures, and record electronic measurements through an ESP32-based IoT multimeter.

The project combines software development, database management, web technologies, and embedded electronics into a single system aimed at improving the organization and traceability of console repair workflows.

Developed as a Final Degree Project (TFG) for the Bachelor's Degree in Software Application Development at the Universitat Oberta de Catalunya (UOC).

## 1. Project Overview

RetroLab addresses the challenges of managing console repairs, particularly when multiple devices, components, diagnostic tests, and technicians are involved.

Traditional repair workflows may rely on handwritten notes, spreadsheets, or disconnected measurement devices. RetroLab provides a centralized environment where technicians can manage consoles, register repair cases, document component tests, and associate electronic measurements with specific repairs.

One of its main features is the integration of a custom-built IoT multimeter based on the ESP32 microcontroller, allowing electronic measurements to be transmitted to the backend over Wi-Fi.

### Main Objectives

- Centralize video game console and repair case management.
- Maintain a structured database of manufacturers, console models, and electronic components.
- Organize diagnostic procedures and record component measurements.
- Integrate a custom ESP32-based electronic measurement device.
- Provide a responsive web interface for repair management.
- Implement secure authentication and role-based access control.
- Apply full-stack software engineering principles to a practical hardware-oriented project.

## 2. Technology Stack

### Backend

| Technology | Purpose |
|---|---|
| Java 21 | Main programming language |
| Spring Boot 3 | Backend application framework |
| Spring Web | REST API development |
| Spring Data JPA | Data persistence |
| Hibernate | Object-relational mapping |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| PostgreSQL | Relational database |
| Docker | Database containerization |

### Frontend

| Technology | Purpose |
|---|---|
| React | User interface |
| JavaScript | Frontend programming |
| Vite | Development server and build tool |
| Tailwind CSS | Styling and responsive layouts |
| React Router | Client-side navigation |
| Fetch / HTTP services | Backend communication |

### Embedded System

| Technology | Purpose |
|---|---|
| ESP32-WROOM | Main microcontroller |
| Arduino IDE | Firmware development |
| C++ | Embedded programming |
| INA3221 | Voltage and current monitoring |
| ADS1115 | Analog-to-digital conversion |
| SSD1306 OLED | Local measurement display |
| NTC thermistor | Temperature measurement |
| Rotary encoder | User input |
| Wi-Fi / HTTP | Communication with the backend |
| ArduinoJson | JSON serialization |

## 3. System Architecture

RetroLab follows a modular monolithic architecture for the backend, combined with a React Single Page Application (SPA) and an independent embedded measurement device.

The backend is organized into layers with clearly separated responsibilities.

### Architecture Overview

```text
                  RETROLAB SYSTEM
                         |
          +--------------+--------------+
          |                             |
     React Frontend                 ESP32 Device
          |                             |
     HTTP / REST API              Wi-Fi / HTTP
          |                             |
          +--------------+--------------+
                         |
                  Spring Boot API
                         |
                 Spring Security
                   JWT Validation
                         |
                    Controllers
                         |
                     Services
                         |
                   Repositories
                         |
                    Hibernate
                         |
                    PostgreSQL
```

### Backend Layers

**Controllers**

Expose REST endpoints and handle incoming HTTP requests.

**Services**

Contain business logic, validations, and application operations.

**Repositories**

Provide database access through Spring Data JPA.

**Entities**

Represent the domain model and its relationships.

**DTOs**

Transfer data between the API and its clients without exposing persistence entities directly.

### Architectural Decisions

The modular monolithic approach was selected to keep deployment and development manageable while maintaining separation between functional responsibilities.

The frontend communicates with the backend through REST endpoints, while the ESP32 operates as an independent client capable of transmitting measurements over Wi-Fi.

## 4. Main Features

### 4.1 Authentication and User Management

RetroLab includes authentication based on Spring Security and JSON Web Tokens.

The system supports:

- User authentication.
- Protected application routes.
- Role-based authorization.
- User profile management.
- Team member management.
- Administrative operations for user accounts and roles.

### 4.2 Dashboard

The dashboard provides an overview of the information stored in the system.

Rather than displaying static demonstration values, the interface retrieves information from the backend to present the current application state.

### 4.3 Console Management

Technicians can register and manage consoles using a structured database.

Console records are associated with their corresponding models and manufacturers.

Each console has a unique serial number, allowing individual devices to be identified throughout the repair process.

### 4.4 Manufacturers and Console Models

RetroLab maintains a catalog of console manufacturers and models.

Manufacturers can be associated with multiple console models, while each model can have multiple registered consoles and electronic components.

This structure makes it possible to reuse technical information across devices of the same model.

### 4.5 Repair Case Management

Repair cases represent individual repair workflows associated with registered consoles.

The system allows technicians to organize repair information, manage case records, and associate diagnostic tests with the corresponding repair.

This provides traceability between a physical device, its repair case, and the tests performed during diagnosis.

### 4.6 Component Management

Electronic components are associated with specific console models.

The application maintains a catalog of components that can be selected when performing diagnostic tests.

Each component is identified within the context of its console model.

### 4.7 Component Testing

Component tests allow diagnostic information and measurements to be recorded against a repair case.

Tests are associated with both the corresponding repair case and the component being examined.

The integration with the ESP32 measurement device provides a connection between physical electronic measurements and the software-based repair workflow.

## 5. IoT Multimeter

One of the most distinctive parts of RetroLab is its custom-built ESP32 multimeter.

The device was designed to support basic electronic diagnostics during console repair operations.

It combines several sensors, analog measurement circuits, a local display, and wireless communication.

### 5.1 Hardware Components

- ESP32-WROOM development board.
- INA3221 voltage/current monitoring module.
- ADS1115 16-bit analog-to-digital converter.
- SSD1306 0.96-inch I2C OLED display.
- NTC thermistor.
- Rotary encoder with push button.
- Piezoelectric buzzer.
- Measurement resistors and voltage divider.
- Shunt resistor for current measurements.
- Banana jack connectors.
- Fuse protection for the current measurement circuit.

### 5.2 Measurement Capabilities

The prototype includes the following diagnostic functions:

| Measurement | Implementation |
|---|---|
| DC voltage | ADS1115 and voltage divider |
| DC current | INA3221 and shunt resistor |
| Resistance | Reference resistor and ADC |
| Continuity | Resistance threshold and buzzer |
| Temperature | NTC thermistor |

### 5.3 Local User Interface

The OLED display provides measurement information directly on the device.

A rotary encoder allows the user to navigate between measurement modes and interact with the embedded interface.

The buzzer provides audible feedback during continuity testing.

### 5.4 Communication

The ESP32 connects to the local network using Wi-Fi and communicates with the Spring Boot backend through HTTP requests.

Measurements can be serialized as JSON and associated with repair-related records.

This allows electronic measurements obtained from the physical device to become part of the application's diagnostic history.

### 5.5 Firmware Libraries

The embedded software uses Arduino-compatible libraries, including:

- `Wire.h`
- `U8g2lib.h`
- `Adafruit_ADS1X15.h`
- `WiFi.h`
- `HTTPClient.h`
- `ArduinoJson.h`

Additional sensor libraries may be used depending on the firmware configuration.

## 6. Database Design

RetroLab uses PostgreSQL as its relational database.

The persistence layer is implemented with Spring Data JPA and Hibernate.

### Main Entities

| Entity | Description |
|---|---|
| User | Application user and associated role |
| Manufacturer | Console manufacturer |
| ConsoleModel | Console model and technical information |
| Console | Individual physical console |
| RepairCase | Repair workflow associated with a console |
| Component | Electronic component belonging to a console model |
| ComponentTest | Diagnostic test associated with a repair case and component |

### Entity Relationships

```text
Manufacturer
    |
    +----< ConsoleModel
                |
                +----< Console
                |        |
                |        +----< RepairCase
                |                    |
                |                    +----< ComponentTest
                |
                +----< Component
                            |
                            +----< ComponentTest

User
  |
  +----< Console
```

The main relationships are implemented using JPA annotations such as `@OneToMany` and `@ManyToOne`.

### Data Integrity

The database includes uniqueness constraints for:

- Console serial numbers.
- Console model names.
- Component names within the same console model.

Entity relationships and foreign key constraints help maintain consistency between consoles, repair cases, and diagnostic records.

## 7. REST API

The backend exposes RESTful endpoints for managing the application's main resources.

### Main Resource Endpoints

| Resource | Base Endpoint |
|---|---|
| Manufacturers | `/manufacturers` |
| Console Models | `/console-models` |
| Consoles | `/consoles` |
| Repair Cases | `/repair-cases` |
| Components | `/components` |
| Component Tests | `/component-tests` |
| Users | `/users` |

### Additional Endpoints

```http
GET /repair-cases/console/{consoleId}
```

Retrieves repair cases associated with a specific console.

```http
GET /component-tests/repair-case/{repairCaseId}
```

Retrieves diagnostic tests associated with a specific repair case.

```http
PATCH /users/{id}/role
```

Updates a user's role, subject to the application's authorization rules.

The API uses JSON for communication between the backend, frontend, and embedded device.

## 8. Frontend Application

The frontend is developed with React and Tailwind CSS, using Vite as the development and build tool.

### Single Page Application

RetroLab behaves as a Single Page Application.

Instead of loading a new HTML document for each section, React updates the displayed content according to the current route.

React Router handles navigation between the main sections.

### Main Sections

- Dashboard
- Consoles
- Repair Cases
- Team
- Profile

### Routing and Layout

The application uses nested routing to share common layout components.

The main layout contains a sidebar, navigation bar, and content area rendered through React Router's `Outlet` component.

Protected routes restrict access to authenticated users.

### State Management

React's built-in hooks are used for local state management.

The application relies on:

- `useState` for component state.
- `useEffect` for lifecycle-related operations.
- Asynchronous functions for API communication.
- `Promise.all` for concurrent independent requests.

A separate global state management library was not introduced, as the application's scope could be handled using React's existing capabilities.

### API Communication

The frontend communicates with the Spring Boot backend through a service layer.

Components request data through reusable functions rather than implementing HTTP requests directly throughout the interface.

The application also manages loading states, error messages, and data refreshes after CRUD operations.

## 9. Security

RetroLab implements authentication and authorization using Spring Security.

### Security Features

- JWT-based authentication.
- Protected REST endpoints.
- Role-based access control.
- Restricted administrative operations.
- Protected frontend navigation.
- User-specific authentication state.

The backend is responsible for enforcing authorization rules. Frontend route protection improves the user experience but does not replace server-side security.

## 10. Running the Project

### Prerequisites

The development environment requires:

- Java Development Kit 21
- Node.js and npm
- Docker
- Git
- Arduino IDE (for the embedded device)
- ESP32 board support package

### Clone the Repository

```bash
git clone <repository-url>
cd RetroLab
```

### Database Setup

RetroLab uses PostgreSQL, which can be run using Docker.

Configure the database connection according to the backend's Spring Boot configuration.

Typical Spring Boot environment settings include:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Database credentials and other sensitive configuration values should not be committed to the repository.

### Backend Setup

Navigate to the backend directory:

```bash
cd backend
```

For a project using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Make sure PostgreSQL is running and the required environment variables are configured.

### Frontend Setup

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will be accessible at the local address displayed by Vite.

### ESP32 Setup

1. Install Arduino IDE.
2. Install the ESP32 board support package.
3. Install the required firmware libraries.
4. Open the ESP32 firmware project.
5. Configure Wi-Fi credentials and the backend address.
6. Select the correct ESP32 board and serial port.
7. Compile and upload the firmware.

The ESP32 and backend must be configured so that the device can reach the API over the network.

## 11. Testing

Testing was performed during development to verify backend behavior, persistence operations, and integration between application components.

### Backend Testing

The backend testing process covered:

- Authentication.
- CRUD operations.
- Entity relationships.
- Data validation.
- Uniqueness constraints.
- Foreign key integrity.
- HTTP response status codes.
- Repair case and component test operations.

### Hardware Testing

The ESP32 prototype was tested using known electronic components and reference measurements.

Tests included:

- Voltage measurements using known sources.
- Current measurements with resistive loads.
- Resistance measurements with reference resistors.
- Continuity detection.
- Temperature measurements.
- OLED display and rotary encoder operation.

Prototype measurements were compared with conventional multimeter readings to evaluate accuracy and identify calibration requirements.

## 12. Development Methodology

RetroLab was developed using an iterative approach inspired by Agile and Scrum principles.

Trello was used to organize tasks and monitor progress.

The development process involved several stages:

1. Requirements analysis and project planning.
2. Database and backend design.
3. REST API implementation.
4. Frontend development.
5. Electronic circuit design and prototyping.
6. ESP32 firmware development.
7. Hardware and software integration.
8. Testing, debugging, and documentation.

The iterative approach made it possible to adapt the implementation as new technical challenges appeared, particularly during the development of the electronic prototype.

## 13. Technical Challenges

### Hardware Integration

Integrating several I2C devices required careful configuration of communication addresses, pins, and electrical connections.

### Measurement Accuracy

The measurement circuits required calibration and validation against reference components and a conventional multimeter.

### Database Integrity

Managing relationships between consoles, repair cases, components, and tests required attention to entity lifecycle operations and foreign key constraints.

### Full-Stack Integration

Coordinating the frontend, backend, database, and ESP32 device required a consistent communication model and clear separation of responsibilities.

### Scope Management

Combining web application development with an embedded electronics prototype required prioritization and iterative development to keep the project manageable.

## 14. Future Improvements

Potential future enhancements include:

- Improved electronic measurement accuracy and calibration.
- Expanded support for additional measurement types.
- More advanced diagnostic history and reporting.
- Real-time measurement visualization.
- Enhanced IoT device management.
- Automated diagnostic workflows.
- Additional automated integration tests.
- Production deployment and monitoring.

## 15. Academic Context

RetroLab was developed as a Final Degree Project at the **Universitat Oberta de Catalunya (UOC)**.

The project demonstrates the practical application of knowledge acquired during software development studies, including:

- Object-oriented programming.
- Backend development.
- Web application development.
- Relational database design.
- REST API design.
- Software architecture.
- Authentication and authorization.
- Embedded programming.
- Basic electronics.
- Software testing.
- Project management.

## 16. Author

**Manuel Pérez Feria**

Software Development Student — Universitat Oberta de Catalunya (UOC)

Interests: Software Engineering, Embedded Systems, IoT, Electronics, and Video Game Console Repair.

---
