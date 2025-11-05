"""
Symptom-Based Disease Prediction Module
Uses ML model (RandomForest) to predict diseases from symptoms
"""

import pandas as pd
import numpy as np
import joblib
import os
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
import json


class SymptomPredictor:
    """Predict diseases based on symptoms using ML model"""
    
    def __init__(self, model_path='models/disease_model.pkl', dataset_path='datasets/disease_symptoms.csv'):
        self.model_path = model_path
        self.dataset_path = dataset_path
        self.model = None
        self.symptom_encoder = None
        self.disease_encoder = None
        self.symptoms_list = []
        self.diseases_list = []
        self.disease_info = {}
        
        # Load or train model
        if os.path.exists(model_path):
            self.load_model()
        else:
            print("Model not found. Training new model...")
            self.train_model()
        
        # Load disease information
        self.load_disease_info()
    
    def load_disease_info(self):
        """Load disease information from JSON"""
        info_path = 'datasets/disease_info.json'
        if os.path.exists(info_path):
            with open(info_path, 'r', encoding='utf-8') as f:
                self.disease_info = json.load(f)
        else:
            # Create default disease info
            self.disease_info = self._create_default_disease_info()
    
    def _create_default_disease_info(self):
        """Create default disease information for all diseases in the model"""
        disease_symptoms_data = {
            'Common Cold': {'severity': 'low', 'description': 'A viral infection that affects the upper respiratory tract', 'symptoms': ['Cough', 'Runny Nose', 'Sore Throat', 'Sneezing', 'Fatigue', 'Nasal Congestion', 'Mild Headache'], 'treatments': ['Rest', 'Hydration', 'Over-the-counter medications'], 'whenToSeekHelp': 'If symptoms persist for more than 10 days or worsen significantly'},
            'Influenza': {'severity': 'moderate', 'description': 'A contagious respiratory illness caused by influenza viruses', 'symptoms': ['Fever', 'Body Aches', 'Fatigue', 'Cough', 'Headache', 'Chills', 'Sore Throat', 'Runny Nose'], 'treatments': ['Antiviral medication', 'Rest', 'Fluids'], 'whenToSeekHelp': 'If you experience difficulty breathing or persistent high fever'},
            'Migraine': {'severity': 'moderate', 'description': 'A neurological condition characterized by severe headaches', 'symptoms': ['Headache', 'Nausea', 'Sensitivity to Light', 'Sensitivity to Sound', 'Dizziness', 'Blurred Vision'], 'treatments': ['Pain medication', 'Rest in dark room', 'Avoid triggers'], 'whenToSeekHelp': 'If headaches are severe, sudden, or accompanied by vision changes'},
            'Sinusitis': {'severity': 'moderate', 'description': 'Inflammation of the sinuses', 'symptoms': ['Headache', 'Facial Pain', 'Nasal Congestion', 'Runny Nose', 'Fever', 'Cough', 'Fatigue'], 'treatments': ['Nasal decongestants', 'Steam inhalation', 'Pain relievers'], 'whenToSeekHelp': 'If symptoms persist for more than a week or worsen'},
            'Bronchitis': {'severity': 'moderate', 'description': 'Inflammation of the bronchial tubes', 'symptoms': ['Cough', 'Shortness of Breath', 'Chest Discomfort', 'Fatigue', 'Wheezing', 'Fever', 'Mucus Production'], 'treatments': ['Rest', 'Increase fluid intake', 'Cough suppressants'], 'whenToSeekHelp': 'If you have difficulty breathing or high fever'},
            'Pneumonia': {'severity': 'high', 'description': 'Infection that inflames air sacs in one or both lungs', 'symptoms': ['Fever', 'Cough', 'Shortness of Breath', 'Chest Pain', 'Fatigue', 'Chills', 'Sweating', 'Nausea'], 'treatments': ['Antibiotics', 'Rest', 'Fluids', 'Oxygen therapy if needed'], 'whenToSeekHelp': 'Seek immediate medical attention if you have difficulty breathing'},
            'Gastroenteritis': {'severity': 'moderate', 'description': 'Inflammation of the stomach and intestines', 'symptoms': ['Nausea', 'Vomiting', 'Diarrhea', 'Abdominal Pain', 'Fever', 'Loss of Appetite', 'Dehydration'], 'treatments': ['Stay hydrated', 'BRAT diet', 'Rest'], 'whenToSeekHelp': 'If you have signs of dehydration or severe pain'},
            'Urinary Tract Infection': {'severity': 'moderate', 'description': 'Infection in any part of the urinary system', 'symptoms': ['Frequent Urination', 'Burning Sensation', 'Abdominal Pain', 'Fever', 'Cloudy Urine', 'Pelvic Pain'], 'treatments': ['Antibiotics', 'Increased fluid intake', 'Pain relievers'], 'whenToSeekHelp': 'If you have fever, back pain, or blood in urine'},
            'Arthritis': {'severity': 'moderate', 'description': 'Inflammation of one or more joints', 'symptoms': ['Joint Pain', 'Stiffness', 'Swelling', 'Reduced Range of Motion', 'Warm Joints', 'Morning Stiffness'], 'treatments': ['Anti-inflammatory medications', 'Physical therapy', 'Exercise'], 'whenToSeekHelp': 'If joint pain is severe or persistent'},
            'Hypertension': {'severity': 'high', 'description': 'High blood pressure', 'symptoms': ['Headache', 'Dizziness', 'Fatigue', 'Chest Pain', 'Shortness of Breath', 'Vision Problems'], 'treatments': ['Medication', 'Lifestyle changes', 'Regular monitoring'], 'whenToSeekHelp': 'If you experience severe headache or chest pain'},
            'Diabetes': {'severity': 'high', 'description': 'A metabolic disorder affecting blood sugar regulation', 'symptoms': ['Increased Thirst', 'Frequent Urination', 'Fatigue', 'Blurred Vision', 'Slow Healing', 'Weight Loss'], 'treatments': ['Medication', 'Diet management', 'Regular exercise', 'Blood sugar monitoring'], 'whenToSeekHelp': 'If you have very high or very low blood sugar'},
            'Asthma': {'severity': 'moderate', 'description': 'Chronic condition affecting airways', 'symptoms': ['Wheezing', 'Shortness of Breath', 'Chest Tightness', 'Cough', 'Difficulty Breathing'], 'treatments': ['Inhalers', 'Avoid triggers', 'Medication'], 'whenToSeekHelp': 'If you have severe difficulty breathing'},
            'Allergies': {'severity': 'low', 'description': 'Immune system reaction to foreign substances', 'symptoms': ['Sneezing', 'Runny Nose', 'Itchy Eyes', 'Watery Eyes', 'Nasal Congestion', 'Skin Rash'], 'treatments': ['Antihistamines', 'Avoid allergens', 'Nasal sprays'], 'whenToSeekHelp': 'If you have severe allergic reactions or difficulty breathing'},
            'Anemia': {'severity': 'moderate', 'description': 'Condition where blood lacks enough healthy red blood cells', 'symptoms': ['Fatigue', 'Weakness', 'Dizziness', 'Shortness of Breath', 'Pale Skin', 'Cold Hands'], 'treatments': ['Iron supplements', 'Diet changes', 'Treat underlying cause'], 'whenToSeekHelp': 'If you have severe fatigue or shortness of breath'},
            'Hypothyroidism': {'severity': 'moderate', 'description': 'Underactive thyroid gland', 'symptoms': ['Fatigue', 'Weight Gain', 'Cold Intolerance', 'Depression', 'Dry Skin', 'Hair Loss'], 'treatments': ['Thyroid hormone replacement', 'Regular monitoring'], 'whenToSeekHelp': 'If you have severe symptoms or changes in mental state'},
            'Hyperthyroidism': {'severity': 'moderate', 'description': 'Overactive thyroid gland', 'symptoms': ['Weight Loss', 'Rapid Heartbeat', 'Anxiety', 'Sweating', 'Tremors', 'Fatigue'], 'treatments': ['Medication', 'Radioactive iodine', 'Surgery'], 'whenToSeekHelp': 'If you have rapid heartbeat or severe anxiety'},
            'Depression': {'severity': 'moderate', 'description': 'Mental health disorder affecting mood', 'symptoms': ['Persistent Sadness', 'Loss of Interest', 'Fatigue', 'Sleep Problems', 'Appetite Changes', 'Difficulty Concentrating'], 'treatments': ['Therapy', 'Medication', 'Lifestyle changes'], 'whenToSeekHelp': 'If you have thoughts of self-harm or suicide'},
            'Anxiety': {'severity': 'moderate', 'description': 'Excessive worry and fear', 'symptoms': ['Excessive Worry', 'Restlessness', 'Rapid Heartbeat', 'Sweating', 'Trembling', 'Difficulty Sleeping'], 'treatments': ['Therapy', 'Medication', 'Relaxation techniques'], 'whenToSeekHelp': 'If anxiety is interfering with daily life'},
            'Gastroesophageal Reflux Disease': {'severity': 'moderate', 'description': 'Chronic digestive disorder', 'symptoms': ['Heartburn', 'Chest Pain', 'Regurgitation', 'Difficulty Swallowing', 'Chronic Cough'], 'treatments': ['Lifestyle changes', 'Medication', 'Diet modifications'], 'whenToSeekHelp': 'If you have severe chest pain or difficulty swallowing'},
            'Irritable Bowel Syndrome': {'severity': 'low', 'description': 'Common disorder affecting the large intestine', 'symptoms': ['Abdominal Pain', 'Bloating', 'Diarrhea', 'Constipation', 'Gas', 'Cramping'], 'treatments': ['Diet changes', 'Stress management', 'Medication'], 'whenToSeekHelp': 'If you have severe pain or blood in stool'},
            'Osteoarthritis': {'severity': 'moderate', 'description': 'Degenerative joint disease', 'symptoms': ['Joint Pain', 'Stiffness', 'Swelling', 'Reduced Flexibility', 'Bone Spurs'], 'treatments': ['Pain relievers', 'Physical therapy', 'Exercise'], 'whenToSeekHelp': 'If joint pain is severe or disabling'},
            'Rheumatoid Arthritis': {'severity': 'high', 'description': 'Autoimmune disorder affecting joints', 'symptoms': ['Joint Pain', 'Swelling', 'Morning Stiffness', 'Fatigue', 'Fever', 'Weight Loss'], 'treatments': ['DMARDs', 'Anti-inflammatory drugs', 'Physical therapy'], 'whenToSeekHelp': 'If you have severe joint pain or signs of infection'},
            'Fibromyalgia': {'severity': 'moderate', 'description': 'Chronic pain condition', 'symptoms': ['Widespread Pain', 'Fatigue', 'Sleep Problems', 'Cognitive Difficulties', 'Headaches', 'Stiffness'], 'treatments': ['Pain medication', 'Exercise', 'Stress management'], 'whenToSeekHelp': 'If pain is severe or affecting daily activities'},
            'Chronic Fatigue Syndrome': {'severity': 'moderate', 'description': 'Long-term condition with extreme fatigue', 'symptoms': ['Severe Fatigue', 'Sleep Problems', 'Muscle Pain', 'Joint Pain', 'Headaches', 'Memory Problems'], 'treatments': ['Lifestyle changes', 'Graded exercise', 'Cognitive behavioral therapy'], 'whenToSeekHelp': 'If fatigue is severe and persistent'},
            'Sleep Apnea': {'severity': 'moderate', 'description': 'Sleep disorder causing breathing interruptions', 'symptoms': ['Loud Snoring', 'Daytime Sleepiness', 'Morning Headaches', 'Difficulty Concentrating', 'Irritability'], 'treatments': ['CPAP therapy', 'Lifestyle changes', 'Weight loss'], 'whenToSeekHelp': 'If you have excessive daytime sleepiness'},
            'Chronic Obstructive Pulmonary Disease': {'severity': 'high', 'description': 'Chronic lung disease', 'symptoms': ['Shortness of Breath', 'Chronic Cough', 'Wheezing', 'Chest Tightness', 'Fatigue'], 'treatments': ['Bronchodilators', 'Oxygen therapy', 'Pulmonary rehabilitation'], 'whenToSeekHelp': 'If you have severe difficulty breathing'},
            'Chronic Kidney Disease': {'severity': 'high', 'description': 'Progressive loss of kidney function', 'symptoms': ['Fatigue', 'Swelling', 'Nausea', 'Loss of Appetite', 'Itchy Skin', 'Muscle Cramps'], 'treatments': ['Medication', 'Diet changes', 'Dialysis if needed'], 'whenToSeekHelp': 'If you have severe symptoms or changes in urination'},
            'Liver Disease': {'severity': 'high', 'description': 'Various conditions affecting the liver', 'symptoms': ['Fatigue', 'Jaundice', 'Abdominal Pain', 'Nausea', 'Loss of Appetite', 'Dark Urine'], 'treatments': ['Medication', 'Lifestyle changes', 'Treatment of underlying cause'], 'whenToSeekHelp': 'If you have jaundice or severe abdominal pain'},
            'Heart Disease': {'severity': 'high', 'description': 'Various conditions affecting the heart', 'symptoms': ['Chest Pain', 'Shortness of Breath', 'Fatigue', 'Dizziness', 'Swelling', 'Irregular Heartbeat'], 'treatments': ['Medication', 'Lifestyle changes', 'Surgery if needed'], 'whenToSeekHelp': 'Seek immediate help for chest pain or irregular heartbeat'},
            'Stroke': {'severity': 'high', 'description': 'Medical emergency when blood supply to brain is interrupted', 'symptoms': ['Sudden Weakness', 'Numbness', 'Confusion', 'Difficulty Speaking', 'Vision Problems', 'Severe Headache'], 'treatments': ['Emergency treatment', 'Rehabilitation', 'Medication'], 'whenToSeekHelp': 'Seek immediate emergency medical attention'}
        }
        
        disease_info = {}
        for idx, (disease_name, info) in enumerate(disease_symptoms_data.items(), 1):
            disease_info[str(idx)] = {
                "id": str(idx),
                "name": disease_name,
                "description": info['description'],
                "severity": info['severity'],
                "symptoms": info['symptoms'],
                "treatments": info['treatments'],
                "whenToSeekHelp": info['whenToSeekHelp']
            }
        
        return disease_info
    
    def train_model(self):
        """Train the disease prediction model"""
        # Load or create dataset
        if os.path.exists(self.dataset_path):
            df = pd.read_csv(self.dataset_path)
        else:
            print("Creating synthetic dataset...")
            df = self._create_synthetic_dataset()
            os.makedirs(os.path.dirname(self.dataset_path), exist_ok=True)
            df.to_csv(self.dataset_path, index=False)
        
        # Prepare features (symptoms as binary features)
        symptom_columns = [col for col in df.columns if col not in ['disease', 'disease_id']]
        X = df[symptom_columns].values
        y = df['disease'].values
        
        # Encode labels
        self.disease_encoder = LabelEncoder()
        y_encoded = self.disease_encoder.fit_transform(y)
        
        self.symptoms_list = symptom_columns
        self.diseases_list = self.disease_encoder.classes_.tolist()
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y_encoded, test_size=0.2, random_state=42
        )
        
        # Train model
        self.model = RandomForestClassifier(n_estimators=100, random_state=42, max_depth=10)
        self.model.fit(X_train, y_train)
        
        # Calculate accuracy
        accuracy = self.model.score(X_test, y_test)
        print(f"Model trained successfully! Accuracy: {accuracy:.2%}")
        
        # Save model
        os.makedirs(os.path.dirname(self.model_path), exist_ok=True)
        joblib.dump({
            'model': self.model,
            'symptoms_list': self.symptoms_list,
            'disease_encoder': self.disease_encoder
        }, self.model_path)
        print(f"Model saved to {self.model_path}")
    
    def _create_synthetic_dataset(self):
        """Create synthetic disease-symptom dataset"""
        # Define diseases and their common symptoms - Expanded with more symptoms
        disease_symptoms = {
            'Common Cold': ['Cough', 'Runny Nose', 'Sore Throat', 'Sneezing', 'Fatigue', 'Nasal Congestion', 'Mild Headache'],
            'Influenza': ['Fever', 'Body Aches', 'Fatigue', 'Cough', 'Headache', 'Chills', 'Sore Throat', 'Runny Nose'],
            'Migraine': ['Headache', 'Nausea', 'Sensitivity to Light', 'Sensitivity to Sound', 'Dizziness', 'Blurred Vision'],
            'Sinusitis': ['Headache', 'Facial Pain', 'Nasal Congestion', 'Runny Nose', 'Fever', 'Cough', 'Fatigue'],
            'Bronchitis': ['Cough', 'Shortness of Breath', 'Chest Discomfort', 'Fatigue', 'Wheezing', 'Fever', 'Mucus Production'],
            'Pneumonia': ['Fever', 'Cough', 'Shortness of Breath', 'Chest Pain', 'Fatigue', 'Chills', 'Sweating', 'Nausea'],
            'Gastroenteritis': ['Nausea', 'Vomiting', 'Diarrhea', 'Abdominal Pain', 'Fever', 'Loss of Appetite', 'Dehydration'],
            'Urinary Tract Infection': ['Frequent Urination', 'Burning Sensation', 'Abdominal Pain', 'Fever', 'Cloudy Urine', 'Pelvic Pain'],
            'Arthritis': ['Joint Pain', 'Stiffness', 'Swelling', 'Reduced Range of Motion', 'Warm Joints', 'Morning Stiffness'],
            'Hypertension': ['Headache', 'Dizziness', 'Fatigue', 'Chest Pain', 'Shortness of Breath', 'Vision Problems'],
            'Diabetes': ['Increased Thirst', 'Frequent Urination', 'Fatigue', 'Blurred Vision', 'Slow Healing', 'Weight Loss'],
            'Asthma': ['Wheezing', 'Shortness of Breath', 'Chest Tightness', 'Cough', 'Difficulty Breathing'],
            'Allergies': ['Sneezing', 'Runny Nose', 'Itchy Eyes', 'Watery Eyes', 'Nasal Congestion', 'Skin Rash'],
            'Anemia': ['Fatigue', 'Weakness', 'Dizziness', 'Shortness of Breath', 'Pale Skin', 'Cold Hands'],
            'Hypothyroidism': ['Fatigue', 'Weight Gain', 'Cold Intolerance', 'Depression', 'Dry Skin', 'Hair Loss'],
            'Hyperthyroidism': ['Weight Loss', 'Rapid Heartbeat', 'Anxiety', 'Sweating', 'Tremors', 'Fatigue'],
            'Depression': ['Persistent Sadness', 'Loss of Interest', 'Fatigue', 'Sleep Problems', 'Appetite Changes', 'Difficulty Concentrating'],
            'Anxiety': ['Excessive Worry', 'Restlessness', 'Rapid Heartbeat', 'Sweating', 'Trembling', 'Difficulty Sleeping'],
            'Gastroesophageal Reflux Disease': ['Heartburn', 'Chest Pain', 'Regurgitation', 'Difficulty Swallowing', 'Chronic Cough'],
            'Irritable Bowel Syndrome': ['Abdominal Pain', 'Bloating', 'Diarrhea', 'Constipation', 'Gas', 'Cramping'],
            'Osteoarthritis': ['Joint Pain', 'Stiffness', 'Swelling', 'Reduced Flexibility', 'Bone Spurs'],
            'Rheumatoid Arthritis': ['Joint Pain', 'Swelling', 'Morning Stiffness', 'Fatigue', 'Fever', 'Weight Loss'],
            'Fibromyalgia': ['Widespread Pain', 'Fatigue', 'Sleep Problems', 'Cognitive Difficulties', 'Headaches', 'Stiffness'],
            'Chronic Fatigue Syndrome': ['Severe Fatigue', 'Sleep Problems', 'Muscle Pain', 'Joint Pain', 'Headaches', 'Memory Problems'],
            'Sleep Apnea': ['Loud Snoring', 'Daytime Sleepiness', 'Morning Headaches', 'Difficulty Concentrating', 'Irritability'],
            'Chronic Obstructive Pulmonary Disease': ['Shortness of Breath', 'Chronic Cough', 'Wheezing', 'Chest Tightness', 'Fatigue'],
            'Chronic Kidney Disease': ['Fatigue', 'Swelling', 'Nausea', 'Loss of Appetite', 'Itchy Skin', 'Muscle Cramps'],
            'Liver Disease': ['Fatigue', 'Jaundice', 'Abdominal Pain', 'Nausea', 'Loss of Appetite', 'Dark Urine'],
            'Heart Disease': ['Chest Pain', 'Shortness of Breath', 'Fatigue', 'Dizziness', 'Swelling', 'Irregular Heartbeat'],
            'Stroke': ['Sudden Weakness', 'Numbness', 'Confusion', 'Difficulty Speaking', 'Vision Problems', 'Severe Headache']
        }
        
        # Get all unique symptoms
        all_symptoms = set()
        for symptoms in disease_symptoms.values():
            all_symptoms.update(symptoms)
        all_symptoms = sorted(list(all_symptoms))
        
        # Create dataset
        rows = []
        for disease, symptoms in disease_symptoms.items():
            # Create multiple samples per disease with variations
            for _ in range(50):
                row = {symptom: 0 for symptom in all_symptoms}
                # Add main symptoms (always present)
                for symptom in symptoms:
                    row[symptom] = 1
                # Add some random additional symptoms (noise)
                other_symptoms = [s for s in all_symptoms if s not in symptoms]
                num_additional = np.random.randint(0, 3)
                for _ in range(num_additional):
                    if np.random.random() < 0.3:  # 30% chance
                        row[np.random.choice(other_symptoms)] = 1
                row['disease'] = disease
                rows.append(row)
        
        df = pd.DataFrame(rows)
        return df
    
    def load_model(self):
        """Load trained model"""
        model_data = joblib.load(self.model_path)
        self.model = model_data['model']
        self.symptoms_list = model_data['symptoms_list']
        self.disease_encoder = model_data['disease_encoder']
        self.diseases_list = self.disease_encoder.classes_.tolist()
        print(f"Model loaded from {self.model_path}")
    
    def predict_disease(self, symptom_ids):
        """
        Predict disease from symptom IDs
        Args:
            symptom_ids: List of symptom IDs (strings)
        Returns:
            List of predictions with disease info and match percentage
        """
        try:
            # Convert symptom IDs to symptom names (assuming IDs map to names)
            symptom_names = self._ids_to_names(symptom_ids)
            print(f"Converted symptom IDs to names: {symptom_names}")  # Debug
            
            if not symptom_names:
                print("WARNING: No symptom names after conversion")
                return []
            
            # Create feature vector
            feature_vector = np.zeros(len(self.symptoms_list))
            matched_count = 0
            for symptom_name in symptom_names:
                if symptom_name in self.symptoms_list:
                    idx = self.symptoms_list.index(symptom_name)
                    feature_vector[idx] = 1
                    matched_count += 1
            
            print(f"Matched {matched_count} out of {len(symptom_names)} symptoms in model")  # Debug
            
            # Predict probabilities
            if feature_vector.sum() == 0:
                print("WARNING: No valid symptoms found in model - feature vector is all zeros")
                print(f"Available symptoms in model: {len(self.symptoms_list)}")
                print(f"Looking for symptoms: {symptom_names}")
                return []  # No valid symptoms
            
            # Check if model is initialized
            if self.model is None:
                print("ERROR: Model is not initialized!")
                return []
            
            # Get predictions
            probabilities = self.model.predict_proba([feature_vector])[0]
            
            # Get top predictions
            top_indices = np.argsort(probabilities)[::-1][:5]
            results = []
            
            for idx in top_indices:
                if probabilities[idx] > 0.01:  # Only include if probability > 1%
                    disease_name = self.diseases_list[idx]
                    match_percentage = probabilities[idx] * 100
                    
                    # Get disease info
                    disease_id = self._get_disease_id(disease_name)
                    disease_info = self.disease_info.get(disease_id, {
                        "id": disease_id,
                        "name": disease_name,
                        "description": f"Medical condition: {disease_name}",
                        "severity": "moderate",
                        "symptoms": symptom_names,
                        "treatments": ["Consult a healthcare professional"],
                        "whenToSeekHelp": "Please consult with a qualified healthcare provider"
                    })
                    
                    # Find matched symptoms
                    matched_symptoms = [s for s in symptom_names if s in disease_info.get('symptoms', [])]
                    
                    results.append({
                        "disease": {
                            "id": disease_info["id"],
                            "name": disease_info["name"],
                            "description": disease_info["description"],
                            "severity": disease_info["severity"],
                            "symptoms": disease_info.get("symptoms", []),
                            "treatments": disease_info.get("treatments", []),
                            "whenToSeekHelp": disease_info.get("whenToSeekHelp", "")
                        },
                        "matchPercentage": round(match_percentage, 2),
                        "matchedSymptoms": matched_symptoms
                    })
            
            print(f"Returning {len(results)} prediction results")  # Debug
            return results
        except Exception as e:
            print(f"ERROR in predict_disease: {str(e)}")
            import traceback
            traceback.print_exc()
            return []
    
    def _ids_to_names(self, symptom_ids):
        """Convert symptom IDs to names"""
        # Get all symptoms from the API response format
        all_symptoms_list = self.get_all_symptoms()
        # Create a proper ID to name mapping
        symptom_map = {symptom['id']: symptom['name'] for symptom in all_symptoms_list}
        
        # Convert IDs to names, fallback to ID itself if not found
        symptom_names = []
        for sid in symptom_ids:
            if sid in symptom_map:
                symptom_names.append(symptom_map[sid])
            else:
                # If ID not found, try to use as name directly
                symptom_names.append(str(sid))
        
        return symptom_names
    
    def _get_symptom_map(self):
        """Get mapping of symptom IDs to names"""
        # Use the get_all_symptoms method to get proper mapping
        all_symptoms_list = self.get_all_symptoms()
        return {symptom['id']: symptom['name'] for symptom in all_symptoms_list}
    
    def _get_disease_id(self, disease_name):
        """Get disease ID from name"""
        for disease_id, info in self.disease_info.items():
            if info['name'] == disease_name:
                return disease_id
        return str(len(self.disease_info) + 1)
    
    def get_all_symptoms(self):
        """Get all available symptoms"""
        all_symptoms = set()
        for info in self.disease_info.values():
            all_symptoms.update(info.get('symptoms', []))
        
        # Also include symptoms from model
        all_symptoms.update(self.symptoms_list)
        
        return [{"id": str(i+1), "name": symptom, "category": "General"} 
                for i, symptom in enumerate(sorted(all_symptoms))]
    
    def get_all_diseases(self):
        """Get all available diseases"""
        return [info for info in self.disease_info.values()]

