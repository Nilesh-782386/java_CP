# SMART Health Guide+ API Documentation

REST API for AI-Based Personal Medical Advisor System

**Base URL**: `http://localhost:5000`

---

## Health Check

### GET /health

Check if the server is running.

**Response:**
```json
{
  "status": "ok",
  "message": "SMART Health Guide+ Backend is running",
  "version": "1.0.0"
}
```

---

## Symptoms & Diseases

### GET /api/symptoms

Get all available symptoms.

**Response:**
```json
[
  {
    "id": "1",
    "name": "Headache",
    "category": "General"
  },
  {
    "id": "2",
    "name": "Fever",
    "category": "General"
  }
]
```

### GET /api/diseases

Get all available diseases.

**Response:**
```json
[
  {
    "id": "1",
    "name": "Common Cold",
    "description": "A viral infection...",
    "severity": "low",
    "symptoms": ["Cough", "Runny Nose"],
    "treatments": ["Rest", "Hydration"],
    "whenToSeekHelp": "If symptoms persist..."
  }
]
```

### POST /api/check-symptoms

Predict disease based on symptoms.

**Request Body:**
```json
{
  "symptomIds": ["1", "2", "3"]
}
```

**Response:**
```json
[
  {
    "disease": {
      "id": "1",
      "name": "Common Cold",
      "description": "...",
      "severity": "low",
      "symptoms": [...],
      "treatments": [...],
      "whenToSeekHelp": "..."
    },
    "matchPercentage": 75.5,
    "matchedSymptoms": ["Cough", "Fever"]
  }
]
```

---

## Chatbot

### POST /api/chat

Chat with medical chatbot.

**Request Body:**
```json
{
  "message": "What is fever?"
}
```

**Response:**
```json
{
  "response": "Fever is a temporary increase in body temperature...",
  "relatedTopics": ["General Health", "Symptoms"]
}
```

---

## Test Recommendations

### GET /api/tests/{disease_id}

Get test recommendations for a disease.

**Response:**
```json
{
  "diseaseId": "1",
  "diseaseName": "Common Cold",
  "reasoning": "For uncomplicated colds, minimal testing is needed...",
  "recommendedTests": [
    {
      "id": "1",
      "name": "Complete Blood Count (CBC)",
      "description": "Measures various components of blood",
      "purpose": "To check for signs of bacterial infection",
      "costRange": "₹300 - ₹500",
      "preparation": "Fasting not required"
    }
  ],
  "testsToAvoid": [
    {
      "id": "3",
      "name": "Chest X-Ray",
      "description": "...",
      "purpose": "Not needed for uncomplicated colds",
      "costRange": "₹800 - ₹1200"
    }
  ]
}
```

---

## Cost Estimation

### GET /api/treatments

Get all available treatments.

**Response:**
```json
[
  "Appendectomy",
  "Surgery",
  "Medication",
  "Physical Therapy"
]
```

### POST /api/estimate-cost

Estimate cost for a treatment.

**Request Body:**
```json
{
  "treatmentType": "Appendectomy",
  "hospitalType": "private"
}
```

**Note**: `hospitalType` can be: `"government"`, `"semi-private"`, or `"private"`

**Response:**
```json
{
  "treatmentName": "Appendectomy",
  "hospitalType": "Private Hospital",
  "averageCost": 90000,
  "minCost": 72000,
  "maxCost": 108000,
  "currency": "INR",
  "factors": [
    {
      "name": "Hospital Location",
      "impact": "Costs vary significantly by city and region..."
    }
  ],
  "disclaimer": "These are estimated costs..."
}
```

---

## Report Analysis

### POST /api/analyze-report

Analyze blood test report.

**Request Body:**
```json
{
  "hemoglobin": 12.5,
  "wbc": 7000,
  "platelets": 250000,
  "rbc": 4.8,
  "blood_sugar_fasting": 95,
  "total_cholesterol": 180
}
```

**Response:**
```json
{
  "overallStatus": "normal",
  "summary": "All analyzed parameters are within normal ranges...",
  "parameters": [
    {
      "name": "Hemoglobin",
      "value": 12.5,
      "status": "normal",
      "unit": "g/dL",
      "normalMin": 12.0,
      "normalMax": 16.0,
      "interpretation": "Within normal range"
    }
  ],
  "flaggedParameters": [],
  "recommendations": [
    "Continue regular health checkups",
    "Maintain a balanced diet and regular exercise"
  ]
}
```

---

## Error Responses

All endpoints may return error responses:

**400 Bad Request:**
```json
{
  "error": "No symptoms provided"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Error message here"
}
```

---

## CORS

CORS is enabled for all origins to allow JavaFX frontend integration.

---

## Rate Limiting

Currently no rate limiting. Consider adding for production use.

---

## Notes

- All costs are in Indian Rupees (INR)
- All timestamps are in ISO 8601 format
- All IDs are strings
- Percentages are numbers (0-100)

