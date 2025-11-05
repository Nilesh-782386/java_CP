# 🚀 SMART Health Guide+ - Complete Setup Guide

Follow these steps to set up and run the complete system.

---

## 📋 Prerequisites

### Required Software
- **Python 3.8+** - [Download Python](https://www.python.org/downloads/)
- **Java 17+** - [Download Java](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)

### Verify Installation

```bash
# Check Python version
python --version  # Should be 3.8 or higher

# Check Java version
java -version  # Should be 17 or higher

# Check Maven version
mvn --version  # Should be 3.6 or higher
```

---

## 🔧 Step 1: Setup Python Backend

### 1.1 Navigate to Backend Directory

```bash
cd backend_python
```

### 1.2 Install Python Dependencies

```bash
# Install all required packages
pip install -r requirements.txt
```

**Note for Windows users:**
```cmd
python -m pip install -r requirements.txt
```

### 1.3 Run Setup Script (Optional)

```bash
python setup.py
```

This creates necessary directories and downloads NLTK data.

### 1.4 Start the Backend Server

```bash
python app.py
```

**Expected Output:**
```
============================================================
SMART Health Guide+ Backend Server
============================================================
Server starting on http://localhost:5000
Debug mode: False
============================================================

Initializing ML models and modules...
Model not found. Training new model...
Creating synthetic dataset...
Model trained successfully! Accuracy: 87.50%
Model saved to models/disease_model.pkl
Cost model not found. Training new model...
Creating synthetic cost dataset...
Cost model trained successfully! R² Score: 95.00%
Cost model saved to models/cost_model.pkl
All modules initialized successfully!
 * Running on http://0.0.0.0:5000
```

**⚠️ Important:** Keep this terminal open. The backend must be running for the frontend to work.

### 1.5 Verify Backend is Running

Open your browser and visit:
- http://localhost:5000/health
- http://localhost:5000/api/symptoms

You should see JSON responses.

---

## 🖥️ Step 2: Setup JavaFX Frontend

### 2.1 Open a New Terminal/Command Prompt

**Keep the backend running in the first terminal!**

### 2.2 Navigate to Java App Directory

```bash
cd java-app
```

### 2.3 Build the Project

**Windows (Command Prompt):**
```cmd
mvn clean compile
```

**Linux/Mac:**
```bash
mvn clean compile
```

### 2.4 Run the Application

**Windows (Command Prompt):**
```cmd
mvn javafx:run
```

**Linux/Mac:**
```bash
mvn javafx:run
```

**Expected Output:**
```
[INFO] Scanning for projects...
[INFO] Building SmartHeal Desktop 1.0.0
[INFO] Starting JavaFX application...
```

The JavaFX application window should open automatically!

---

## ✅ Step 3: Verify Everything Works

### Test Backend
1. Open browser: http://localhost:5000/health
2. Should see: `{"status":"ok","message":"..."}`

### Test Frontend
1. Application window should open
2. All 5 tabs should be visible:
   - Symptom Checker
   - Health Chatbot
   - Test Lookup
   - Cost Estimator
   - Report Analyzer

### Test Features
1. **Symptom Checker**: Select symptoms → Click "Analyze Symptoms"
2. **Chatbot**: Type a question → Click "Send"
3. **Test Lookup**: Select a disease → View recommendations
4. **Cost Estimator**: Select treatment → Click "Estimate"
5. **Report Analyzer**: Enter blood values → Click "Analyze"

---

## 🐛 Troubleshooting

### Backend Issues

**Problem: Port 5000 already in use**
```bash
# Option 1: Kill process using port 5000
# Windows:
netstat -ano | findstr :5000
taskkill /PID <PID> /F

# Linux/Mac:
lsof -ti:5000 | xargs kill

# Option 2: Change port in app.py
# Edit: port = int(os.getenv('PORT', 5001))  # Change 5000 to 5001
```

**Problem: Module not found errors**
```bash
pip install -r requirements.txt
```

**Problem: Model training fails**
- Ensure `models/` and `datasets/` directories exist
- Check disk space (needs ~50MB)
- First run may take 1-2 minutes

### Frontend Issues

**Problem: Cannot connect to backend**
- Ensure Python backend is running on port 5000
- Check: http://localhost:5000/health in browser
- Verify firewall isn't blocking port 5000

**Problem: Maven build fails**
- Ensure Java 17+ is installed
- Windows: Use Command Prompt, NOT PowerShell
- Check Maven is in PATH

**Problem: JavaFX not found**
- Maven should download JavaFX automatically
- Check internet connection
- Try: `mvn clean install`

---

## 🔄 Running Again

### Quick Start (After Initial Setup)

**Terminal 1 - Backend:**
```bash
cd backend_python
python app.py
```

**Terminal 2 - Frontend:**
```bash
cd java-app
mvn javafx:run
```

---

## 📁 Project Structure

```
SMART_Health_Guide/
├── backend_python/          # Python backend
│   ├── app.py              # Start here: python app.py
│   ├── requirements.txt    # Install: pip install -r requirements.txt
│   └── ...
│
├── java-app/               # JavaFX frontend
│   ├── pom.xml            # Maven config
│   └── src/               # Java source code
│
└── docs/                  # Documentation
    ├── API_Documentation.md
    └── research_paper_outline.md
```

---

## 🎯 Next Steps

1. **Read Documentation**
   - [Backend README](backend_python/README.md)
   - [API Documentation](docs/API_Documentation.md)
   - [Main README](README.md)

2. **Explore Features**
   - Try all 5 modules
   - Test different symptoms
   - Ask chatbot questions
   - Analyze sample reports

3. **Customize**
   - Add your own datasets
   - Modify ML models
   - Customize UI
   - Add new features

---

## ⚠️ Important Notes

- **Backend must be running** before starting frontend
- **First run** may take 1-2 minutes (model training)
- **Educational purposes only** - Not for actual medical diagnosis
- **Port 5000** must be available for backend

---

## 🆘 Getting Help

1. Check error messages carefully
2. Verify all prerequisites are installed
3. Ensure ports are not blocked
4. Check firewall settings
5. Review troubleshooting section above

---

**Setup Complete! Enjoy using SMART Health Guide+** 🎉

