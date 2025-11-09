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
        self.recommended_tests_map = self._build_recommended_tests()
        self.risk_factor_map = self._build_risk_factors()
        self.lifestyle_guidance = self._build_lifestyle_guidance()
        self.monitoring_tips_map = self._build_monitoring_tips()
        self.precaution_map = self._build_precaution_guidance()
        self.red_flag_symptoms = {
            'Chest Pain', 'Shortness of Breath', 'Sudden Weakness', 'Numbness',
            'Confusion', 'Severe Headache', 'Difficulty Speaking', 'Rapid Heartbeat',
            'High Fever', 'Severe Abdominal Pain', 'Bloody Vomit', 'Bloody Stool',
            'Vision Problems', 'Difficulty Breathing', 'Loss of Consciousness'
        }
        
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
    
    def _build_recommended_tests(self):
        """Suggested investigations per condition."""
        return {
            'Common Cold': ['Rapid influenza antigen test (if high fever)', 'Covid-19 antigen test (if exposed)'],
            'Influenza': ['Rapid influenza diagnostic test', 'Complete blood count'],
            'Migraine': ['Neurological evaluation', 'MRI brain (if red flags present)'],
            'Sinusitis': ['CT scan of sinuses (if recurrent)', 'Nasal endoscopy'],
            'Bronchitis': ['Chest X-ray', 'Sputum culture (if productive cough)'],
            'Pneumonia': ['Chest X-ray', 'Pulse oximetry', 'Complete blood count'],
            'Gastroenteritis': ['Stool analysis', 'Electrolyte panel'],
            'Urinary Tract Infection': ['Urine routine & microscopic exam', 'Urine culture & sensitivity'],
            'Arthritis': ['ESR / CRP', 'X-ray of affected joints'],
            'Hypertension': ['Blood pressure monitoring', 'Kidney function tests', 'ECG'],
            'Diabetes': ['Fasting blood glucose', 'HbA1c', 'Urine microalbumin'],
            'Asthma': ['Spirometry', 'Peak flow meter monitoring'],
            'Allergies': ['Serum IgE', 'Skin prick test'],
            'Anemia': ['Complete blood count', 'Serum ferritin'],
            'Hypothyroidism': ['TSH, T3, T4', 'Lipid profile'],
            'Hyperthyroidism': ['TSH, T3, T4', 'Thyroid uptake scan'],
            'Depression': ['Psychiatric evaluation', 'Thyroid profile (rule-out)'],
            'Anxiety': ['Psychological assessment', 'ECG (if palpitations)'],
            'Gastroesophageal Reflux Disease': ['Upper GI endoscopy', 'Esophageal pH monitoring'],
            'Irritable Bowel Syndrome': ['Stool routine', 'Colonoscopy (if alarm features)'],
            'Osteoarthritis': ['X-ray of affected joints', 'Vitamin D levels'],
            'Rheumatoid Arthritis': ['Rheumatoid factor', 'Anti-CCP antibodies'],
            'Fibromyalgia': ['Sleep study', 'Vitamin D & B12 levels'],
            'Chronic Fatigue Syndrome': ['Thyroid profile', 'CBC & metabolic panel'],
            'Sleep Apnea': ['Polysomnography (sleep study)', 'Epworth sleepiness scale'],
            'Chronic Obstructive Pulmonary Disease': ['Spirometry', 'Chest CT (if severe)'],
            'Chronic Kidney Disease': ['Serum creatinine & eGFR', 'Urine albumin/creatinine ratio'],
            'Liver Disease': ['Liver function tests', 'Abdominal ultrasound'],
            'Heart Disease': ['ECG', 'Echocardiogram', 'Lipid profile'],
            'Stroke': ['CT/MRI brain', 'Carotid Doppler']
        }

    def _build_risk_factors(self):
        """Common risk factors for each condition."""
        return {
            'Common Cold': ['Recent exposure to infected individuals', 'Weakened immune system'],
            'Influenza': ['Lack of vaccination', 'Chronic illnesses', 'Elderly or very young age'],
            'Migraine': ['Family history of migraine', 'Irregular sleep patterns', 'Stress'],
            'Sinusitis': ['Allergic rhinitis', 'Deviated nasal septum', 'Smoking'],
            'Bronchitis': ['Smoking history', 'Exposure to pollutants', 'Weak immunity'],
            'Pneumonia': ['Ages <5 or >65', 'Chronic lung disease', 'Smoking'],
            'Gastroenteritis': ['Contaminated food/water', 'Travel history', 'Poor hygiene'],
            'Urinary Tract Infection': ['Poor hydration', 'Female gender', 'Diabetes'],
            'Arthritis': ['Age >45', 'Joint injuries', 'Obesity'],
            'Hypertension': ['Family history', 'High sodium diet', 'Sedentary lifestyle'],
            'Diabetes': ['Family history', 'Obesity', 'Sedentary lifestyle'],
            'Asthma': ['Family history of allergies', 'Exposure to smoke', 'Cold weather'],
            'Allergies': ['Family history', 'High pollen seasons'],
            'Anemia': ['Iron deficient diet', 'Chronic blood loss'],
            'Hypothyroidism': ['Autoimmune disorders', 'Family history'],
            'Hyperthyroidism': ['Family history', 'Stress'],
            'Depression': ['Chronic stress', 'Chemical imbalance', 'Family history'],
            'Anxiety': ['Stressful life events', 'Family history', 'Caffeine intake'],
            'Gastroesophageal Reflux Disease': ['Obesity', 'Late-night meals', 'Smoking'],
            'Irritable Bowel Syndrome': ['Stress', 'Dietary triggers'],
            'Osteoarthritis': ['Ageing', 'Obesity', 'Joint injuries'],
            'Rheumatoid Arthritis': ['Autoimmune tendency', 'Female gender'],
            'Fibromyalgia': ['Sleep disorders', 'Infections', 'Trauma'],
            'Chronic Fatigue Syndrome': ['Recent infection', 'Mental stress'],
            'Sleep Apnea': ['Obesity', 'Neck circumference > 40 cm'],
            'Chronic Obstructive Pulmonary Disease': ['Smoking', 'Occupational exposure'],
            'Chronic Kidney Disease': ['Diabetes', 'Hypertension', 'Family history'],
            'Liver Disease': ['Alcohol use', 'Viral hepatitis', 'Obesity'],
            'Heart Disease': ['High cholesterol', 'Hypertension', 'Smoking'],
            'Stroke': ['Hypertension', 'Atrial fibrillation', 'Smoking']
        }

    def _build_lifestyle_guidance(self):
        """Self-care and lifestyle suggestions."""
        return {
            'Common Cold': ['Rest and stay hydrated', 'Humidify the air', 'Over-the-counter symptom relief'],
            'Influenza': ['Stay home and rest', 'Hydrate frequently', 'Monitor fever regularly'],
            'Migraine': ['Maintain headache diary', 'Avoid triggers', 'Ensure adequate sleep'],
            'Sinusitis': ['Use saline nasal rinse', 'Steam inhalation twice daily', 'Avoid allergens'],
            'Bronchitis': ['Avoid smoking', 'Hydrate to loosen mucus', 'Use prescribed inhalers'],
            'Pneumonia': ['Follow antibiotic regimen', 'Practice breathing exercises', 'Rest adequately'],
            'Gastroenteritis': ['Follow BRAT diet', 'Take oral rehydration salts', 'Avoid dairy temporarily'],
            'Urinary Tract Infection': ['Increase water intake', 'Avoid caffeine & alcohol', 'Complete antibiotic course'],
            'Arthritis': ['Engage in low-impact exercise', 'Maintain healthy weight', 'Use warm compresses'],
            'Hypertension': ['Reduce salt intake', 'Exercise 30 minutes daily', 'Monitor BP at home'],
            'Diabetes': ['Monitor blood glucose', 'Adopt balanced carbohydrate diet', 'Exercise regularly'],
            'Asthma': ['Use spacers/inhalers properly', 'Avoid known triggers', 'Monitor peak flow'],
            'Allergies': ['Use protective masks outdoors', 'Keep living area dust-free', 'Use antihistamines as prescribed'],
            'Anemia': ['Eat iron-rich foods', 'Combine iron with vitamin C sources', 'Avoid tea/coffee near meals'],
            'Hypothyroidism': ['Take thyroid medication on empty stomach', 'Maintain consistent timing', 'Exercise regularly'],
            'Hyperthyroidism': ['Reduce caffeine intake', 'Practice relaxation techniques'],
            'Depression': ['Maintain routine', 'Engage in physical activity', 'Reach out to support system'],
            'Anxiety': ['Practice relaxation breathing', 'Limit stimulants', 'Maintain sleep hygiene'],
            'Gastroesophageal Reflux Disease': ['Eat small frequent meals', 'Elevate head of bed', 'Avoid late-night meals'],
            'Irritable Bowel Syndrome': ['Follow low-FODMAP diet', 'Manage stress', 'Keep food diary'],
            'Osteoarthritis': ['Regular stretching', 'Use supportive footwear', 'Manage body weight'],
            'Rheumatoid Arthritis': ['Follow medication schedule', 'Practice joint-protecting techniques'],
            'Fibromyalgia': ['Establish sleep schedule', 'Gentle aerobic exercise', 'Stress management'],
            'Chronic Fatigue Syndrome': ['Pace activities', 'Balanced diet', 'Gentle stretching'],
            'Sleep Apnea': ['Maintain healthy weight', 'Avoid alcohol before bed', 'Sleep on your side'],
            'Chronic Obstructive Pulmonary Disease': ['Participate in pulmonary rehab', 'Avoid smoke exposure'],
            'Chronic Kidney Disease': ['Limit sodium & potassium (as advised)', 'Avoid NSAIDs'],
            'Liver Disease': ['Avoid alcohol', 'Eat balanced low-fat diet'],
            'Heart Disease': ['Follow heart-healthy diet', 'Take medications as prescribed'],
            'Stroke': ['Adhere to rehabilitation plan', 'Monitor blood pressure', 'Follow antiplatelet therapy']
        }

    def _evaluate_triage(self, disease_info, selected_symptoms, missing_critical):
        severity = disease_info.get('severity', 'moderate')
        red_flags = sorted([sym for sym in selected_symptoms if sym in self.red_flag_symptoms])

        level = "Routine"
        color = "#0ea5e9"  # teal blue
        message = "Monitor symptoms and schedule a routine consultation for confirmation."

        if red_flags:
            level = "Emergency"
            color = "#dc2626"
            red_flag_text = ", ".join(red_flags)
            message = (
                f"Emergency warning signs detected ({red_flag_text}). Seek immediate medical attention or call emergency services."
            )
        elif severity == "high":
            level = "Urgent"
            color = "#f97316"
            message = "Potentially serious condition detected. Arrange urgent medical review within 24 hours."
        elif missing_critical:
            level = "Priority"
            color = "#facc15"
            missing_text = ", ".join(missing_critical)
            message = (
                f"Key hallmark symptoms ({missing_text}) are not reported. Consider medical visit soon to rule out complications."
            )
        elif severity == "moderate":
            level = "Priority"
            color = "#22c55e"
            message = "Arrange a consultation in the next few days to confirm diagnosis and start treatment."

        return {
            "level": level,
            "message": message,
            "color": color
        }, red_flags

    def _get_specialist_for_condition(self, disease_name):
        specialist_map = {
            'Common Cold': 'Primary Care Physician',
            'Influenza': 'Primary Care Physician',
            'Migraine': 'Neurologist',
            'Sinusitis': 'ENT Specialist',
            'Bronchitis': 'Pulmonologist',
            'Pneumonia': 'Pulmonologist / Emergency Physician',
            'Gastroenteritis': 'Gastroenterologist',
            'Urinary Tract Infection': 'Urologist',
            'Arthritis': 'Rheumatologist',
            'Hypertension': 'Cardiologist',
            'Diabetes': 'Endocrinologist',
            'Asthma': 'Pulmonologist',
            'Allergies': 'Allergist / Immunologist',
            'Anemia': 'Hematologist',
            'Hypothyroidism': 'Endocrinologist',
            'Hyperthyroidism': 'Endocrinologist',
            'Depression': 'Psychiatrist',
            'Anxiety': 'Psychiatrist',
            'Gastroesophageal Reflux Disease': 'Gastroenterologist',
            'Irritable Bowel Syndrome': 'Gastroenterologist',
            'Osteoarthritis': 'Orthopedic Specialist',
            'Rheumatoid Arthritis': 'Rheumatologist',
            'Fibromyalgia': 'Rheumatologist / Pain Specialist',
            'Chronic Fatigue Syndrome': 'Internal Medicine Specialist',
            'Sleep Apnea': 'Sleep Medicine Specialist',
            'Chronic Obstructive Pulmonary Disease': 'Pulmonologist',
            'Chronic Kidney Disease': 'Nephrologist',
            'Liver Disease': 'Hepatologist',
            'Heart Disease': 'Cardiologist',
            'Stroke': 'Neurologist'
        }
        return specialist_map.get(disease_name, 'Primary Care Physician')

    def _build_monitoring_tips(self):
        """Home monitoring guidance."""
        return {
            'Hypertension': ['Track blood pressure twice daily', 'Log readings for doctor review'],
            'Diabetes': ['Check fasting glucose daily', 'Monitor for hypo/hyperglycemia symptoms'],
            'Heart Disease': ['Monitor chest discomfort patterns', 'Record heart rate trends'],
            'Stroke': ['Monitor for new weakness or speech difficulty', 'Keep emergency numbers handy'],
            'Asthma': ['Use peak flow meter', 'Note inhaler usage frequency'],
            'COPD': ['Track oxygen saturation if available', 'Measure exercise tolerance'],
            'Chronic Kidney Disease': ['Monitor urine output changes', 'Track blood pressure daily'],
            'Liver Disease': ['Watch for jaundice or swelling', 'Track weight changes'],
            'Anemia': ['Monitor fatigue levels', 'Note shortness of breath on exertion'],
            'Depression': ['Track mood variations', 'Maintain sleep log'],
            'Anxiety': ['Monitor panic episodes', 'Practice calming routines']
        }

    def _build_precaution_guidance(self):
        """Precautions and follow up advice."""
        return {
            'Common Cold': ['Seek care if fever >38.5°C persists', 'Watch for breathing difficulty'],
            'Influenza': ['Avoid contact with high-risk individuals', 'Seek care if breathing worsens'],
            'Pneumonia': ['Complete antibiotics fully', 'Seek urgent care if oxygen <92%'],
            'Bronchitis': ['Avoid smoking environments', 'Seek care if symptoms >14 days'],
            'Hypertension': ['Emergency if BP >180/120 with symptoms', 'Routine follow up monthly'],
            'Heart Disease': ['Emergency if chest pain lasts >5 min', 'Follow cardiologist plan'],
            'Stroke': ['Call emergency services for sudden neurological changes'],
            'Diabetes': ['Seek care if blood sugar >300 mg/dL persistently', 'Follow diabetic diet'],
            'Asthma': ['Carry rescue inhaler at all times', 'Follow action plan'],
            'COPD': ['Use oxygen as prescribed', 'Seek care for severe breathlessness'],
            'Chronic Kidney Disease': ['Avoid dehydration', 'Maintain nephrologist visits'],
            'Liver Disease': ['Avoid hepatotoxic drugs', 'Seek care for abdominal swelling']
        }
    
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
            
            selected_symptom_set = set(symptom_names)

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
            top_candidates = [
                (self.diseases_list[i], float(probabilities[i]) * 100.0)
                for i in top_indices if probabilities[i] > 0.005
            ]
            results = []
            
            for idx in top_indices:
                probability = float(probabilities[idx])
                if probability > 0.01:  # Only include if probability > 1%
                    disease_name = self.diseases_list[idx]
                    match_percentage = probability * 100
                    
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
                    condition_symptoms = disease_info.get('symptoms', [])
                    missing_symptoms = [s for s in condition_symptoms if s not in matched_symptoms]
                    critical_symptoms = condition_symptoms[:min(3, len(condition_symptoms))]
                    missing_critical = [s for s in critical_symptoms if s not in matched_symptoms]
                    symptom_coverage = len(matched_symptoms) / max(len(condition_symptoms), 1)

                    confidence_score = (probability * 0.6) + (symptom_coverage * 0.4)
                    if probability < 0.05 and symptom_coverage < 0.25:
                        confidence_level = "Low"
                    elif confidence_score >= 0.7:
                        confidence_level = "High"
                    elif confidence_score >= 0.45:
                        confidence_level = "Moderate"
                    else:
                        confidence_level = "Low"

                    confidence_info = {
                        "score": round(confidence_score * 100, 1),
                        "level": confidence_level,
                        "modelProbability": round(probability * 100, 1),
                        "symptomCoverage": round(symptom_coverage * 100, 1),
                        "explanation": (
                            f"Matches {len(matched_symptoms)} of {len(condition_symptoms)} hallmark symptoms "
                            f"with model confidence {probability * 100:.1f}%."
                        )
                    }

                    triage_info, red_flags_triggered = self._evaluate_triage(
                        disease_info,
                        selected_symptom_set,
                        missing_critical
                    )

                    similar_conditions = []
                    for other_name, other_prob in top_candidates:
                        if other_name != disease_name and len(similar_conditions) < 3:
                            similar_conditions.append({
                                "name": other_name,
                                "matchPercentage": round(other_prob, 2)
                            })
                    
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
                        "matchedSymptoms": matched_symptoms,
                        "missingSymptoms": missing_symptoms,
                        "criticalSymptomsMissing": missing_critical,
                        "symptomCoverage": round(symptom_coverage * 100, 1),
                        "confidence": confidence_info,
                        "triage": {
                            "level": triage_info["level"],
                            "message": triage_info["message"],
                            "color": triage_info["color"],
                            "specialist": self._get_specialist_for_condition(disease_name)
                        },
                        "recommendedTests": self.recommended_tests_map.get(disease_name, []),
                        "riskFactors": self.risk_factor_map.get(disease_name, []),
                        "lifestyleAdvice": self.lifestyle_guidance.get(disease_name, []),
                        "monitoringTips": self.monitoring_tips_map.get(disease_name, []),
                        "precautions": self.precaution_map.get(disease_name, []),
                        "similarConditions": similar_conditions,
                        "redFlags": red_flags_triggered
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

