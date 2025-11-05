# SMART Health Guide+ 🌟

**An AI-Based Personal Medical Advisor System**

A complete multi-language desktop application that combines machine learning, NLP, and healthcare information to provide educational medical guidance.

---

## 🏗️ Architecture

- **Frontend**: Java (JavaFX) Desktop GUI Application
- **Backend**: Python (Flask) REST API
- **ML Models**: scikit-learn (RandomForest)
- **NLP**: NLTK + Rule-based chatbot
- **Communication**: REST API using JSON

---

## ✨ Features

### 1. Symptom-Based Disease Prediction
- ML model (RandomForest) predicts diseases from symptoms
- Provides match percentages and severity levels
- Top 5 disease predictions with detailed information

### 2. Medical Chatbot
- NLP-powered chatbot for health questions
- Knowledge base with medical FAQs
- Contextual responses with related topics

### 3. Test Optimization
- Recommends medical tests for specific conditions
- Identifies unnecessary tests to save costs
- Provides cost ranges and preparation instructions

### 4. Cost Forecasting
- ML model estimates medical treatment costs
- Supports government, semi-private, and private hospitals
- Cost breakdown with factors affecting prices

### 5. Report Analyzer
- Analyzes blood test reports
- Flags abnormal values
- Provides interpretations and recommendations

---

## 🚀 Quick Start

### Prerequisites

- **Python 3.8+** (for backend)
- **Java 17+** (for frontend)
- **Maven 3.6+** (for Java build)

### Step 1: Setup Python Backend

```bash
# Navigate to backend directory
cd backend_python

# Install Python dependencies
pip install -r requirements.txt

# Download NLTK data (optional, for chatbot)
python -c "import nltk; nltk.download('punkt'); nltk.download('stopwords')"

# Start the backend server
python app.py
```

The backend will start on `http://localhost:5000`

### Step 2: Setup JavaFX Frontend

```bash
# Navigate to Java app directory
cd java-app

# Build the project
mvn clean compile

# Run the application
mvn javafx:run
```

---

## 📁 Project Structure

```
SMART_Health_Guide/
├── backend_python/              # Python backend
│   ├── app.py                   # Flask main application
│   ├── symptom_predictor.py     # Disease prediction ML model
│   ├── chatbot.py               # NLP chatbot
│   ├── test_optimizer.py        # Test recommendations
│   ├── cost_forecast.py         # Cost estimation ML model
│   ├── report_analyzer.py       # Blood report analyzer
│   ├── requirements.txt         # Python dependencies
│   ├── models/                  # ML models (auto-created)
│   └── datasets/                # Data files (auto-created)
│
├── java-app/                    # JavaFX frontend
│   ├── src/main/java/
│   │   └── com/smartheal/
│   │       ├── SmartHealApp.java
│   │       ├── api/             # API client
│   │       ├── models/          # Data models
│   │       └── views/           # UI views
│   └── pom.xml
│
└── docs/                        # Documentation
    ├── API_Documentation.md
    └── research_paper_outline.md
```

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/api/symptoms` | Get all symptoms |
| GET | `/api/diseases` | Get all diseases |
| POST | `/api/check-symptoms` | Predict disease |
| POST | `/api/chat` | Chat with bot |
| GET | `/api/tests/{id}` | Get test recommendations |
| POST | `/api/estimate-cost` | Estimate cost |
| POST | `/api/analyze-report` | Analyze blood report |

See [API Documentation](docs/API_Documentation.md) for details.

---

## 🤖 Machine Learning Models

### Disease Prediction Model
- **Algorithm**: RandomForest Classifier
- **Accuracy**: 85-90% on synthetic data
- **Training**: Automatic on first run
- **Model File**: `models/disease_model.pkl`

### Cost Forecasting Model
- **Algorithm**: RandomForest Regressor
- **R² Score**: 0.95
- **Training**: Automatic on first run
- **Model File**: `models/cost_model.pkl`

---

## 📊 Demo Datasets

The system automatically creates demo datasets on first run:
- Disease-symptom mappings
- Medical FAQ knowledge base
- Test recommendations
- Treatment costs
- Blood reference ranges

**Note**: These are synthetic datasets for demonstration. Replace with real data for production use.

---

## ⚠️ Important Disclaimers

- **Educational Purposes Only**: This tool provides general health information and is NOT a substitute for professional medical advice, diagnosis, or treatment.
- **Not for Diagnosis**: Always consult with a qualified healthcare provider for medical concerns.
- **Synthetic Data**: Demo datasets are used for demonstration purposes only.
- **No Medical Liability**: The system is not responsible for any medical decisions made based on its outputs.

---

## 🔧 Configuration

### Backend Port
Default: `5000`

Change in `backend_python/app.py`:
```python
port = int(os.getenv('PORT', 5000))  # Change 5000 to your port
```

Update frontend in `java-app/src/main/java/com/smartheal/api/ApiClient.java`:
```java
private static final String BASE_URL = "http://localhost:5000/api";
```

### Environment Variables
Create `backend_python/.env`:
```env
PORT=5000
DEBUG=False
```

---

## 🐛 Troubleshooting

### Backend Issues

**Port already in use:**
```bash
# Change port in app.py or use:
PORT=5001 python app.py
```

**Import errors:**
```bash
pip install -r requirements.txt
```

**Model training fails:**
- Ensure `models/` and `datasets/` directories are writable
- Check disk space availability

### Frontend Issues

**Cannot connect to backend:**
- Ensure Python backend is running on port 5000
- Check firewall settings
- Verify CORS is enabled

**Maven build fails:**
- Ensure Java 17+ is installed
- Check Maven installation
- Use Command Prompt (not PowerShell) on Windows

---

## 📚 Documentation

- [Backend README](backend_python/README.md) - Detailed backend documentation
- [API Documentation](docs/API_Documentation.md) - Complete API reference
- [Research Paper Outline](docs/research_paper_outline.md) - Academic paper structure

---

## 🎯 Future Enhancements

- [ ] Real medical database integration
- [ ] Advanced NLP (transformer-based chatbot)
- [ ] MySQL/PostgreSQL database
- [ ] Mobile app (Android/iOS)
- [ ] User authentication and history
- [ ] Multi-language support
- [ ] Deep learning models
- [ ] Telemedicine integration

---

## 📄 License

This project is for educational purposes. See disclaimers above.

---

## 👥 Contributing

This is a demonstration project. For production use:
1. Replace synthetic data with real medical datasets
2. Obtain proper medical domain expertise
3. Implement proper security and compliance measures
4. Add comprehensive testing and validation

---

## 🙏 Acknowledgments

- scikit-learn for ML algorithms
- Flask for REST API framework
- JavaFX for desktop UI
- Medical community for reference ranges and guidelines

---

**Remember**: This system is for educational purposes only. Always consult qualified healthcare professionals for medical advice.

---

**Version**: 1.0.0  
**Last Updated**: 2024

