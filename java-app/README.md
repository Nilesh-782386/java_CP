# SmartHeal Desktop Application

JavaFX desktop application for the SMART Health Guide+ system. This application connects to the Python backend (Flask) and provides a native desktop interface for all health information modules.

## Features

- **Symptom Checker**: Select symptoms and get educational information about possible conditions
- **Health Chatbot**: Ask health-related questions and get instant educational answers
- **Cost Estimator**: Get estimated costs for medical treatments at different hospital types
- **Report Analyzer**: Input blood test values and receive educational feedback
- **Risk Assessment**: Calculate your personal disease risks using AI

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Python backend running on `http://localhost:5000`

## Quick Start

### Step 1: Setup Python Backend (One-time setup)
```bash
cd backend_python
pip install -r requirements.txt
```

### Step 2: Start Python Backend (Terminal 1)
```bash
cd backend_python
python app.py
```
Wait until you see: `Server starting on http://localhost:5000`

### Step 3: Run Java Application (Terminal 2)
```bash
cd java-app
mvn javafx:run
```

The application window will open automatically!

### Windows Users
**⚠️ IMPORTANT:** Use **Command Prompt**, NOT PowerShell for Maven commands.
See [WINDOWS_SETUP.md](WINDOWS_SETUP.md) for detailed Windows instructions.

### Running in VS Code
See [VSCODE_SETUP.md](VSCODE_SETUP.md) for detailed VS Code setup instructions.

## Setup

### Prerequisites
- **Java 17+** installed (`java -version`)
- **Maven 3.6+** installed (`mvn -version`)
- **Python backend** running on `http://localhost:5000` (see main README.md for setup)

### Build
```bash
cd java-app
mvn clean compile
```

### Run
```bash
mvn javafx:run
```

## Project Structure

```
java-app/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/smartheal/
│   │   │       ├── SmartHealApp.java          # Main application class
│   │   │       ├── api/
│   │   │       │   └── ApiClient.java         # HTTP client for backend API
│   │   │       ├── models/                    # Data models (POJOs)
│   │   │       │   ├── Symptom.java
│   │   │       │   ├── Disease.java
│   │   │       │   ├── SymptomCheckResult.java
│   │   │       │   ├── ChatMessage.java
│   │   │       │   ├── ChatResponse.java
│   │   │       │   ├── MedicalTest.java
│   │   │       │   ├── TestRecommendation.java
│   │   │       │   ├── CostEstimation.java
│   │   │       │   ├── BloodParameter.java
│   │   │       │   └── ReportAnalysis.java
│   │   │       └── views/                     # JavaFX views
│   │   │           ├── DisclaimerBanner.java
│   │   │           ├── SymptomCheckerView.java
│   │   │           ├── HealthChatbotView.java
│   │   │           ├── TestLookupView.java
│   │   │           ├── CostEstimatorView.java
│   │   │           └── ReportAnalyzerView.java
│   │   └── resources/
│   │       └── styles.css                     # Application styling
└── README.md
```

## API Integration

The application connects to the following backend endpoints:

- `GET /api/symptoms` - Get all symptoms
- `GET /api/diseases` - Get all diseases
- `GET /api/treatments` - Get all treatments
- `POST /api/check-symptoms` - Analyze symptoms
- `POST /api/chat` - Send chat message
- `GET /api/tests/:diseaseId` - Get test recommendations
- `POST /api/estimate-cost` - Estimate treatment cost
- `POST /api/analyze-report` - Analyze blood report

## UI Theme

The application uses a medical theme with:
- Primary color: Teal (#0F766E)
- Secondary color: Light Teal (#14B8A6)
- Success: Green (#22C55E)
- Warning: Orange (#F59E0B)
- Danger: Red (#EF4444)

## Build for Distribution

To create a standalone JAR file:

```bash
mvn clean package
```

The compiled JAR will be in `target/smart-heal-desktop-1.0.0.jar`.

Note: For a native executable, you'll need to use tools like jlink or jpackage (Java 14+).

## Troubleshooting

1. **Backend not found**: Ensure the Python backend is running on `http://localhost:5000` (see main README.md for setup)
2. **JavaFX not found**: Make sure you have JavaFX installed or use the Maven JavaFX plugin
3. **Module errors**: If you get module-related errors, ensure Java 17+ is being used

## License

Same as the main SmartHeal project.

