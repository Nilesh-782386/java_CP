# SMART Health Guide+ - Python Backend

AI-Based Personal Medical Advisor System - Python Backend with ML Models

## 🚀 Features

- **Symptom-Based Disease Prediction**: ML model (RandomForest) predicts diseases from symptoms
- **Medical Chatbot**: NLP-based chatbot for health questions
- **Test Optimization**: Recommends medical tests and identifies unnecessary ones
- **Cost Forecasting**: ML model estimates medical treatment costs
- **Report Analyzer**: Analyzes blood test reports and flags abnormalities

## 📋 Requirements

- Python 3.8 or higher
- pip (Python package manager)

## 🔧 Installation

### Step 1: Install Python Dependencies

```bash
cd backend_python
pip install -r requirements.txt
```

**Note**: If you encounter issues with `transformers` or `torch`, you can install them separately:

```bash
pip install torch --index-url https://download.pytorch.org/whl/cpu
pip install transformers
```

### Step 2: Download NLTK Data (for chatbot)

```bash
python -c "import nltk; nltk.download('punkt'); nltk.download('stopwords')"
```

## 🏃 Running the Backend

### Start the Flask Server

```bash
python app.py
```

The server will start on `http://localhost:5000`

You should see:
```
============================================================
SMART Health Guide+ Backend Server
============================================================
Server starting on http://localhost:5000
Debug mode: False
============================================================
```

### Test the Backend

Open your browser and visit:
- Health check: http://localhost:5000/health
- Get symptoms: http://localhost:5000/api/symptoms
- Get diseases: http://localhost:5000/api/diseases

## 📁 Project Structure

```
backend_python/
├── app.py                  # Flask main application
├── symptom_predictor.py    # Disease prediction ML model
├── chatbot.py              # NLP chatbot module
├── test_optimizer.py       # Test recommendation module
├── cost_forecast.py        # Cost estimation ML model
├── report_analyzer.py      # Blood report analyzer
├── requirements.txt        # Python dependencies
├── models/                 # ML models (created on first run)
│   ├── disease_model.pkl
│   └── cost_model.pkl
└── datasets/               # Data files (created on first run)
    ├── disease_symptoms.csv
    ├── disease_info.json
    ├── medical_faq.json
    ├── test_mapping.json
    ├── treatment_costs.json
    └── blood_reference_ranges.json
```

## 🔌 API Endpoints

### Health Check
- **GET** `/health` - Server health status

### Symptoms & Diseases
- **GET** `/api/symptoms` - Get all available symptoms
- **GET** `/api/diseases` - Get all available diseases
- **POST** `/api/check-symptoms` - Predict disease from symptoms
  ```json
  {
    "symptomIds": ["1", "2", "3"]
  }
  ```

### Chatbot
- **POST** `/api/chat` - Chat with medical chatbot
  ```json
  {
    "message": "What is fever?"
  }
  ```

### Test Recommendations
- **GET** `/api/tests/<disease_id>` - Get test recommendations for a disease

### Cost Estimation
- **GET** `/api/treatments` - Get all available treatments
- **POST** `/api/estimate-cost` - Estimate treatment cost
  ```json
  {
    "treatmentType": "Appendectomy",
    "hospitalType": "private"
  }
  ```

### Report Analysis
- **POST** `/api/analyze-report` - Analyze blood test report
  ```json
  {
    "hemoglobin": 12.5,
    "wbc": 7000,
    "platelets": 250000
  }
  ```

## 🤖 Machine Learning Models

### Disease Prediction Model
- **Algorithm**: RandomForest Classifier
- **Training**: Automatic on first run (creates synthetic dataset)
- **Accuracy**: ~85-90% on synthetic data
- **Model File**: `models/disease_model.pkl`

### Cost Forecasting Model
- **Algorithm**: RandomForest Regressor
- **Training**: Automatic on first run
- **R² Score**: ~95% on synthetic data
- **Model File**: `models/cost_model.pkl`

## 📊 Datasets

The system automatically creates demo datasets on first run:
- `disease_symptoms.csv` - Disease-symptom mapping for ML training
- `medical_faq.json` - Medical FAQ knowledge base
- `test_mapping.json` - Disease-to-test recommendations
- `treatment_costs.json` - Treatment cost data
- `blood_reference_ranges.json` - Normal blood parameter ranges

## 🔧 Configuration

### Environment Variables

Create a `.env` file (optional):

```env
PORT=5000
DEBUG=False
```

### Customizing Models

To retrain models with your own data:
1. Replace CSV files in `datasets/` with your data
2. Delete existing `.pkl` files in `models/`
3. Restart the server - models will retrain automatically

## 🐛 Troubleshooting

### Port Already in Use
If port 5000 is busy, change it:
```python
# In app.py, change:
port = int(os.getenv('PORT', 5000))  # Change 5000 to another port
```

### Model Training Errors
- Ensure you have sufficient disk space
- Check that `models/` and `datasets/` directories are writable
- Models are created automatically on first run

### Import Errors
- Make sure all dependencies are installed: `pip install -r requirements.txt`
- Use Python 3.8 or higher

## 📝 Notes

- **First Run**: Models and datasets are created automatically, which may take a few minutes
- **Demo Mode**: Uses synthetic data for demonstration purposes
- **Production**: Replace synthetic data with real medical datasets for production use

## 🔗 Integration with JavaFX Frontend

The JavaFX frontend connects to this backend on port 5000. Make sure:
1. Python backend is running before starting JavaFX app
2. Port 5000 is not blocked by firewall
3. CORS is enabled (already configured in `app.py`)

## 📚 Next Steps

- Connect to real medical databases
- Train models on real patient data
- Add authentication and authorization
- Implement rate limiting
- Add logging and monitoring

