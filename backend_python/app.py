"""
SMART Health Guide+ - Python Backend
Flask REST API for AI-Based Personal Medical Advisor System
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import os
from dotenv import load_dotenv
from datetime import datetime
import pandas as pd

# Import modules
from symptom_predictor import SymptomPredictor
from chatbot import MedicalChatbot
from test_optimizer import TestOptimizer
from cost_forecast import CostForecaster
from report_analyzer import ReportAnalyzer
from report_ocr import ReportOCR
from risk_predictor import RiskPredictor
from advanced_risk_predictor import advanced_predictor
from health_coach_ai import health_coach

# Load environment variables
load_dotenv()

app = Flask(__name__)
CORS(app)  # Enable CORS for JavaFX frontend

# Initialize modules
print("Initializing ML models and modules...")
symptom_predictor = SymptomPredictor()
chatbot = MedicalChatbot()
test_optimizer = TestOptimizer()
cost_forecaster = CostForecaster()
report_analyzer = ReportAnalyzer()

# Initialize Risk Predictor with error handling
try:
    risk_predictor = RiskPredictor()
    print("✅ Risk Predictor module initialized successfully!")
except Exception as e:
    print(f"⚠️  Warning: Risk Predictor module failed to initialize: {e}")
    print("   Install dependencies: pip install xgboost")
    risk_predictor = None

# Initialize OCR module with error handling
try:
    report_ocr = ReportOCR()
    print("✅ OCR module initialized successfully!")
except Exception as e:
    print(f"⚠️  Warning: OCR module failed to initialize: {e}")
    print("   Image upload feature will not be available.")
    print("   Install dependencies: pip install pytesseract opencv-python Pillow")
    print("   Install Tesseract OCR: https://github.com/UB-Mannheim/tesseract/wiki")
    report_ocr = None

print("All modules initialized successfully!")


@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        "status": "ok",
        "message": "SMART Health Guide+ Backend is running",
        "version": "1.0.0"
    })


@app.route('/api/symptoms', methods=['GET'])
def get_symptoms():
    """Get all available symptoms"""
    try:
        symptoms = symptom_predictor.get_all_symptoms()
        return jsonify(symptoms)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/diseases', methods=['GET'])
def get_diseases():
    """Get all available diseases"""
    try:
        diseases = symptom_predictor.get_all_diseases()
        return jsonify(diseases)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/check-symptoms', methods=['POST'])
def check_symptoms():
    """
    Predict disease based on symptoms
    Request body: {"symptomIds": ["symptom1", "symptom2", ...]}
    """
    try:
        data = request.get_json()
        symptom_ids = data.get('symptomIds', [])
        
        if not symptom_ids:
            return jsonify({"error": "No symptoms provided"}), 400
        
        print(f"Received symptom IDs: {symptom_ids}")  # Debug log
        
        # Get predictions
        results = symptom_predictor.predict_disease(symptom_ids)
        
        print(f"Prediction results: {len(results) if results else 0} diseases found")  # Debug log
        
        if not results:
            print("WARNING: No results returned from predictor")
        
        return jsonify(results)
    except Exception as e:
        print(f"ERROR in check_symptoms: {str(e)}")  # Debug log
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route('/api/chat', methods=['POST'])
def chat():
    """
    Chat with ML-enhanced medical chatbot
    Request body: {"message": "user question"}
    """
    try:
        data = request.get_json()
        message = data.get('message', '')
        
        if not message:
            return jsonify({"error": "No message provided"}), 400
        
        # Get chatbot response using ML
        response = chatbot.get_response(message)
        
        return jsonify({
            "response": response["answer"],
            "relatedTopics": response.get("relatedTopics", response.get("related_topics", [])),
            "confidence": response.get("confidence", 0.0)  # Include ML confidence score
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/tests/<disease_id>', methods=['GET'])
def get_test_recommendations(disease_id):
    """
    Get test recommendations for a disease
    """
    try:
        recommendations = test_optimizer.get_test_recommendations(disease_id)
        return jsonify(recommendations)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/treatments', methods=['GET'])
def get_treatments():
    """Get all available treatments"""
    try:
        treatments = cost_forecaster.get_all_treatments()
        return jsonify(treatments)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/estimate-cost', methods=['POST'])
def estimate_cost():
    """
    Estimate cost for a treatment
    Request body: {"treatmentType": "surgery name", "hospitalType": "private/government/semi-private"}
    """
    try:
        data = request.get_json()
        treatment_type = data.get('treatmentType', '')
        hospital_type = data.get('hospitalType', 'private')
        
        if not treatment_type:
            return jsonify({"error": "No treatment type provided"}), 400
        
        # Get cost estimation
        estimation = cost_forecaster.estimate_cost(treatment_type, hospital_type)
        
        return jsonify(estimation)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/analyze-report', methods=['POST'])
def analyze_report():
    """
    Analyze blood test report
    Request body: {"hemoglobin": 12.5, "wbc": 7000, "platelets": 250000, ...}
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "No report data provided"}), 400
        
        print(f"Received report data: {data}")
        print(f"Data type: {type(data)}, Keys: {list(data.keys()) if isinstance(data, dict) else 'N/A'}")
        
        # Analyze report
        analysis = report_analyzer.analyze(data)
        
        print(f"Analysis result - Overall status: {analysis.get('overallStatus')}, Parameters analyzed: {len(analysis.get('parameters', []))}")
        
        return jsonify(analysis)
    except Exception as e:
        print(f"ERROR in analyze_report: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


def preprocess_user_data(user_data):
    """Convert user data payload into model-ready features."""
    if not isinstance(user_data, dict):
        return pd.DataFrame([{}])

    features = {}
    feature_mapping = {
        'age': 'age',
        'bmi': 'bmi',
        'systolic_bp': 'blood_pressure',
        'glucose': 'glucose',
        'cholesterol': 'cholesterol',
        'weight': 'weight',
        'height': 'height',
        'sleep_hours': 'sleep_hours',
        'stress_level': 'stress_level',
        'diet_quality': 'diet_quality',
        'exercise': 'exercise',
        'smoking': 'smoking',
        'alcohol': 'alcohol',
        'vegetable_servings': 'vegetable_servings',
        'processed_meals_per_week': 'processed_meals_per_week',
        'hydration_glasses': 'hydration_glasses',
        'caffeine_intake': 'caffeine_intake',
        'moderate_activity_minutes': 'moderate_activity_minutes',
        'vigorous_activity_minutes': 'vigorous_activity_minutes',
        'strength_training_sessions': 'strength_training_sessions',
        'sedentary_hours': 'sedentary_hours',
        'sleep_quality': 'sleep_quality',
        'sleep_consistency': 'sleep_consistency',
        'snoring': 'snoring',
        'stress_coping': 'stress_coping',
        'work_hours': 'work_hours',
        'mood_stability': 'mood_stability',
        'medication_adherence': 'medication_adherence',
        'medication_side_effects': 'medication_side_effects',
        'smoking_intensity': 'smoking_intensity',
        'vaping': 'vaping',
        'environmental_exposure': 'environmental_exposure',
        'shift_work': 'shift_work',
        'last_checkup_months': 'last_checkup_months',
        'vaccination_status': 'vaccination_status'
    }

    for incoming, model_key in feature_mapping.items():
        value = user_data.get(incoming)
        if isinstance(value, bool):
            value = int(value)
        features[model_key] = value if value is not None else None

    baseline_mapping = {
        'baseline_diabetes_risk': 'baseline_diabetes_risk',
        'baseline_heart_disease_risk': 'baseline_heart_disease_risk',
        'baseline_hypertension_risk': 'baseline_hypertension_risk',
        'baseline_health_score': 'baseline_health_score'
    }

    for incoming, model_key in baseline_mapping.items():
        value = user_data.get(incoming)
        features[model_key] = value if value is not None else None

    return pd.DataFrame([features])


@app.route('/api/advanced-risk-assessment', methods=['POST'])
def advanced_risk_assessment():
    """Advanced risk assessment with ensemble models and confidence intervals."""
    try:
        payload = request.get_json()
        if not payload:
            return jsonify({"error": "No data provided"}), 400

        features = preprocess_user_data(payload)
        result = advanced_predictor.predict_with_confidence(features)
        predictions = result.get('predictions', {})

        response = {
            'predictions': predictions,
            'model_type': 'ensemble_advanced',
            'timestamp': datetime.now().isoformat(),
            'metadata': {
                'confidence_calculation': 'bootstrap_95CI',
                'uncertainty_threshold': 0.3,
                'ensemble_weights': {'xgb': 0.4, 'lgb': 0.4, 'nn': 0.2}
            },
            'lifestyle_summary': result.get('lifestyle_summary', {}),
            'disease_impacts': result.get('disease_impacts', {}),
            'overall_focus': result.get('overall_focus', []),
            'protective_factors': result.get('protective_factors', []),
            'data_quality': result.get('data_quality', {})
        }
        return jsonify(response)
    except Exception as exc:
        print(f"Advanced risk assessment error: {exc}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(exc)}), 500


@app.route('/api/health-coach-plan', methods=['POST'])
def health_coach_plan():
    """Generate personalised 30-day health improvement plan."""
    try:
        payload = request.get_json() or {}

        user_profile = {
            'age': payload.get('age'),
            'bmi': payload.get('bmi'),
            'health_score': payload.get('health_score', 65),
            'existing_conditions': payload.get('existing_conditions', [])
        }
        risk_predictions = payload.get('risk_predictions', {})

        plan = health_coach.generate_personalized_plan(user_profile, risk_predictions)

        return jsonify({
            'success': True,
            'plan': plan,
            'generated_at': datetime.now().isoformat()
        })
    except Exception as exc:
        print(f"Health coach plan error: {exc}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(exc)}), 500


@app.route('/api/risk-assessment', methods=['POST'])
def risk_assessment():
    """
    Assess personal disease risks (diabetes, heart disease, hypertension)
    Request body: {
        "age": 45,
        "weight": 75,
        "height": 170,
        "symptoms": ["fatigue", "headache"],
        "family_history": ["diabetes", "heart disease"],
        "smoking": false,
        "exercise": 1,
        "alcohol": false
    }
    """
    try:
        if risk_predictor is None:
            return jsonify({
                "error": "Risk Predictor module not available",
                "message": "Risk prediction feature is not available. Please install xgboost: pip install xgboost"
            }), 503
        
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "No data provided"}), 400
        
        # Extract required fields
        age = data.get('age')
        weight = data.get('weight')
        height = data.get('height')
        
        if age is None or weight is None or height is None:
            return jsonify({"error": "Missing required fields: age, weight, height"}), 400
        
        # Extract optional fields
        symptoms = data.get('symptoms', [])
        family_history = data.get('family_history', [])
        smoking = data.get('smoking', False)
        exercise = data.get('exercise', 1)  # 0 = none, 1 = moderate, 2 = high
        alcohol = data.get('alcohol', False)
        sleep_hours = data.get('sleep_hours', None)  # Optional: hours of sleep
        stress_level = data.get('stress_level', None)  # Optional: 0=low, 1=moderate, 2=high
        diet_quality = data.get('diet_quality', None)  # Optional: 0=poor, 1=moderate, 2=good
        
        # Validate inputs
        if not isinstance(age, (int, float)) or age < 18 or age > 100:
            return jsonify({"error": "Age must be a number between 18 and 100"}), 400
        if not isinstance(weight, (int, float)) or weight < 30 or weight > 200:
            return jsonify({"error": "Weight must be a number between 30 and 200 kg"}), 400
        if not isinstance(height, (int, float)) or height < 100 or height > 250:
            return jsonify({"error": "Height must be a number between 100 and 250 cm"}), 400
        
        # Predict risks with enhanced features
        result = risk_predictor.predict_risks(
            age=int(age),
            weight=float(weight),
            height=float(height),
            symptoms=symptoms if isinstance(symptoms, list) else [],
            family_history=family_history if isinstance(family_history, list) else [],
            smoking=bool(smoking),
            exercise=int(exercise) if exercise in [0, 1, 2] else 1,
            alcohol=bool(alcohol) if not isinstance(alcohol, (int, float)) else int(alcohol),
            sleep_hours=float(sleep_hours) if sleep_hours is not None else None,
            stress_level=int(stress_level) if stress_level is not None else None,
            diet_quality=int(diet_quality) if diet_quality is not None else None
        )
        
        return jsonify(result)
        
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        print(f"ERROR in risk_assessment: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route('/api/upload-report-image', methods=['POST'])
def upload_report_image():
    """
    Upload and process blood test report image using OCR
    Request body: {"image": "base64_encoded_image_string"}
    """
    try:
        if report_ocr is None:
            return jsonify({
                "success": False,
                "error": "OCR module not available",
                "message": "OCR dependencies not installed. Please install: pip install pytesseract opencv-python Pillow and Tesseract OCR"
            }), 503
        
        data = request.get_json()
        
        if not data or 'image' not in data:
            return jsonify({"error": "No image data provided"}), 400
        
        image_base64 = data['image']
        
        print("Processing uploaded report image...")
        
        # Process image using OCR
        result = report_ocr.process_base64_image(image_base64)
        
        if result['success']:
            print(f"Successfully extracted {len(result['extractedValues'])} parameter(s) from image")
            return jsonify(result)
        else:
            return jsonify(result), 400
            
    except Exception as e:
        print(f"ERROR in upload_report_image: {str(e)}")
        import traceback
        traceback.print_exc()
        return jsonify({
            "success": False,
            "error": str(e),
            "message": f"Failed to process image: {str(e)}"
        }), 500


if __name__ == '__main__':
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('DEBUG', 'False').lower() == 'true'
    
    print(f"\n{'='*60}")
    print("SMART Health Guide+ Backend Server")
    print(f"{'='*60}")
    print(f"Server starting on http://localhost:{port}")
    print(f"Debug mode: {debug}")
    print(f"{'='*60}\n")
    
    app.run(host='0.0.0.0', port=port, debug=debug)

