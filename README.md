# NexRide: Carpooling Application

A lightweight carpooling application with a Java backend, H2 embedded database, and a vanilla JavaScript frontend.

## Prerequisites
- Java Development Kit (JDK) 8 or higher.

## How to Run

### 1. Compile the Project
Use the following command to compile all Java source files:
```bash
javac -d bin -cp "lib/*" @sources.txt
```

### 2. Start the Server
Run the `WebAppServer` class to start the HTTP server:
```bash
java -cp "bin:lib/*" com.carpool.server.WebAppServer
```

### 3. Access the Application
Once the server is started, open your browser and navigate to:
[http://localhost:8080](http://localhost:8080)

## Demo Credentials
The server automatically seeds the database with the following test accounts:

| Role | Email | Password |
| :--- | :--- | :--- |
| **Chauffeur** | `marc@test.com` | `pass123` |
| **Passager** | `sophie@test.com` | `pass456` |

## Project Structure
- `src/`: Java source files.
- `lib/`: External libraries (H2 Database).
- `bin/`: Compiled class files.
- `frontend/`: HTML, CSS, and JS files for the web interface.
- `sources.txt`: List of source files for compilation.
