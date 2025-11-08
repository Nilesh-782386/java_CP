# SmartHeal Deployment Guide

## 1) Backend (Flask)

### Requirements
- Python 3.8+
- pip
- (Optional for OCR feature) Tesseract OCR binary

### Install dependencies
```bash
cd backend_python
pip install -r requirements.txt
```

### OCR (Image Upload) Setup
- Install Tesseract OCR:
  - Windows: https://github.com/UB-Mannheim/tesseract/wiki
  - Linux: sudo apt-get install tesseract-ocr
  - macOS: brew install tesseract
- If not on PATH, set env in a `.env` file (backend_python/.env):
```
TESSERACT_CMD=C:\\Program Files\\Tesseract-OCR\\tesseract.exe
PORT=5000
```

### Run backend
```bash
cd backend_python
python app.py
```
Server starts on http://localhost:5000.

---

## 2) Frontend (JavaFX)

### Requirements
- Java 17+
- Maven 3.6+

### Build & Run
```bash
cd java-app
mvn clean compile
mvn javafx:run
```

### Package as JAR
```bash
cd java-app
mvn clean package
java -jar target/smart-heal-desktop-1.0.0.jar
```

---

## 3) Environment & Config
- Backend URL used by frontend: http://localhost:5000
- CORS is enabled in backend
- Database config (if used) should be set via appropriate config/ENV

---

## 4) Common Issues
- OCR upload returns 400
  - Tesseract is not installed or not on PATH
  - Set TESSERACT_CMD in .env or install Tesseract
- Maven errors from root folder
  - Run Maven inside java-app or use -f java-app/pom.xml
- JavaFX plugin not found
  - Ensure internet connectivity for Maven, run from java-app

---

## 5) Health Check
```bash
curl http://localhost:5000/health
```

---

## 6) Production Notes
- Flask dev server is for development; use a WSGI server (gunicorn + waitress/uvicorn) behind a reverse proxy in production
- Ensure Tesseract is installed on the host if OCR is required


