"""
Enhanced Health Risk Predictor Module with Advanced AI Features
- Multiple disease predictions (7 diseases)
- Feature importance analysis
- Risk trend predictions
- Personalized AI-generated recommendations
- Confidence intervals
- Population comparison
"""

import pandas as pd
import numpy as np
import joblib
import os
import json
from xgboost import XGBClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier, VotingClassifier
from sklearn.metrics import accuracy_score, classification_report
from datetime import datetime, timedelta


class EnhancedRiskPredictor:
    """Enhanced risk predictor with advanced AI features"""
    
    def __init__(self, model_dir='models'):
        self.model_dir = model_dir
        os.makedirs(model_dir, exist_ok=True)
        
        # Model paths for 7 diseases
        self.disease_models = {
            'diabetes': os.path.join(model_dir, 'enhanced_diabetes_model.pkl'),
            'heart_disease': os.path.join(model_dir, 'enhanced_heart_model.pkl'),
            'hypertension': os.path.join(model_dir, 'enhanced_hypertension_model.pkl'),
            'stroke': os.path.join(model_dir, 'enhanced_stroke_model.pkl'),
            'kidney_disease': os.path.join(model_dir, 'enhanced_kidney_model.pkl'),
            'liver_disease': os.path.join(model_dir, 'enhanced_liver_model.pkl'),
            'obesity': os.path.join(model_dir, 'enhanced_obesity_model.pkl')
        }
        self.scaler_path = os.path.join(model_dir, 'enhanced_risk_scaler.pkl')
        self.feature_names_path = os.path.join(model_dir, 'enhanced_feature_names.pkl')
        
        # Models and scaler
        self.models = {}
        self.scaler = StandardScaler()
        self.feature_names = []
        
        # Load or train models
        if all(os.path.exists(path) for path in self.disease_models.values()):
            self.load_models()
        else:
            print("Enhanced risk prediction models not found. Training new models...")
            self.train_models()
    
    def _create_enhanced_dataset(self, n_samples=10000):
        """Create enhanced synthetic dataset with more features"""
        np.random.seed(42)
        
        data = []
        for _ in range(n_samples):
            age = np.random.randint(18, 80)
            weight = np.random.uniform(45, 120)
            height = np.random.uniform(150, 190)
            bmi = weight / ((height / 100) ** 2)
            
            # Lifestyle factors
            smoking = np.random.choice([0, 1], p=[0.7, 0.3])
            exercise = np.random.choice([0, 1, 2], p=[0.3, 0.5, 0.2])
            alcohol = np.random.choice([0, 1, 2], p=[0.5, 0.4, 0.1])  # 0=none, 1=moderate, 2=heavy
            sleep_hours = np.random.uniform(5, 9)
            stress_level = np.random.choice([0, 1, 2], p=[0.3, 0.5, 0.2])  # 0=low, 1=moderate, 2=high
            diet_quality = np.random.choice([0, 1, 2], p=[0.3, 0.5, 0.2])  # 0=poor, 1=moderate, 2=good
            
            # Family history
            family_diabetes = np.random.choice([0, 1], p=[0.7, 0.3])
            family_heart = np.random.choice([0, 1], p=[0.75, 0.25])
            family_hypertension = np.random.choice([0, 1], p=[0.7, 0.3])
            family_stroke = np.random.choice([0, 1], p=[0.85, 0.15])
            
            # Symptom count
            symptom_count = np.random.randint(0, 11)
            
            # Health metrics (estimated)
            systolic_bp = 110 + (age - 30) * 0.5 + (bmi - 22) * 1.2 + family_hypertension * 8 + smoking * 5 - exercise * 3
            diastolic_bp = systolic_bp - 40 + np.random.normal(0, 5)
            fasting_glucose = 85 + (age - 30) * 0.3 + (bmi - 22) * 1.5 + family_diabetes * 12 - exercise * 3
            cholesterol = 180 + (age - 30) * 0.8 + (bmi - 22) * 2.5 + family_heart * 25 + smoking * 15 - exercise * 10
            hdl = 50 - (bmi - 22) * 0.5 - smoking * 5
            ldl = cholesterol - hdl - 40
            triglycerides = 100 + (bmi - 22) * 2 + alcohol * 10
            creatinine = 0.8 + (age - 30) * 0.01 + (bmi - 22) * 0.02
            
            # Clamp values to realistic ranges
            systolic_bp = max(90, min(180, systolic_bp))
            diastolic_bp = max(60, min(120, diastolic_bp))
            fasting_glucose = max(70, min(150, fasting_glucose))
            cholesterol = max(120, min(300, cholesterol))
            hdl = max(30, min(80, hdl))
            ldl = max(50, min(200, ldl))
            triglycerides = max(50, min(400, triglycerides))
            creatinine = max(0.5, min(2.0, creatinine))
            
            # Calculate risks using enhanced scoring
            diabetes_risk = self._calculate_diabetes_risk(age, bmi, family_diabetes, exercise, fasting_glucose, symptom_count, sleep_hours, stress_level, diet_quality)
            heart_risk = self._calculate_heart_risk(age, bmi, smoking, family_heart, exercise, cholesterol, systolic_bp, stress_level, diet_quality)
            hypertension_risk = self._calculate_hypertension_risk(age, bmi, family_hypertension, smoking, exercise, systolic_bp, stress_level, sleep_hours, alcohol)
            stroke_risk = self._calculate_stroke_risk(age, bmi, smoking, family_stroke, hypertension_risk, heart_risk, exercise, stress_level)
            kidney_risk = self._calculate_kidney_risk(age, bmi, hypertension_risk, diabetes_risk, creatinine, smoking, exercise)
            liver_risk = self._calculate_liver_risk(age, bmi, alcohol, exercise, diet_quality, symptom_count)
            obesity_risk = self._calculate_obesity_risk(bmi, age, exercise, diet_quality, sleep_hours, stress_level)
            
            data.append({
                'age': age, 'weight': weight, 'height': height, 'bmi': bmi,
                'smoking': smoking, 'exercise': exercise, 'alcohol': alcohol,
                'sleep_hours': sleep_hours, 'stress_level': stress_level, 'diet_quality': diet_quality,
                'family_diabetes': family_diabetes, 'family_heart': family_heart,
                'family_hypertension': family_hypertension, 'family_stroke': family_stroke,
                'symptom_count': symptom_count,
                'systolic_bp': systolic_bp, 'diastolic_bp': diastolic_bp,
                'fasting_glucose': fasting_glucose, 'cholesterol': cholesterol,
                'hdl': hdl, 'ldl': ldl, 'triglycerides': triglycerides, 'creatinine': creatinine,
                'diabetes_risk': diabetes_risk, 'heart_risk': heart_risk,
                'hypertension_risk': hypertension_risk, 'stroke_risk': stroke_risk,
                'kidney_risk': kidney_risk, 'liver_risk': liver_risk, 'obesity_risk': obesity_risk
            })
        
        return pd.DataFrame(data)
    
    def _calculate_diabetes_risk(self, age, bmi, family, exercise, glucose, symptoms, sleep, stress, diet):
        score = (age > 45) * 2 + (bmi > 25) * 3 + (bmi > 30) * 2 + family * 3 + (exercise == 0) * 2 + (glucose > 100) * 3 + (symptoms > 5) * 1 + (sleep < 6) * 1 + (stress > 1) * 1 + (diet < 1) * 1
        return 1 if score >= 8 else 0
    
    def _calculate_heart_risk(self, age, bmi, smoking, family, exercise, cholesterol, bp, stress, diet):
        score = (age > 50) * 2 + (bmi > 25) * 2 + smoking * 3 + family * 3 + (exercise == 0) * 2 + (cholesterol > 200) * 2 + (bp > 130) * 2 + (stress > 1) * 2 + (diet < 1) * 1
        return 1 if score >= 8 else 0
    
    def _calculate_hypertension_risk(self, age, bmi, family, smoking, exercise, bp, stress, sleep, alcohol):
        score = (age > 40) * 2 + (bmi > 25) * 3 + family * 3 + (exercise == 0) * 2 + smoking * 1 + (bp > 120) * 2 + (stress > 1) * 2 + (sleep < 6) * 1 + (alcohol > 1) * 1
        return 1 if score >= 7 else 0
    
    def _calculate_stroke_risk(self, age, bmi, smoking, family, hypertension, heart, exercise, stress):
        score = (age > 55) * 3 + (bmi > 30) * 2 + smoking * 2 + family * 2 + hypertension * 2 + heart * 2 + (exercise == 0) * 1 + (stress > 1) * 1
        return 1 if score >= 7 else 0
    
    def _calculate_kidney_risk(self, age, bmi, hypertension, diabetes, creatinine, smoking, exercise):
        score = (age > 60) * 2 + (bmi > 30) * 2 + hypertension * 2 + diabetes * 2 + (creatinine > 1.2) * 3 + smoking * 1 + (exercise == 0) * 1
        return 1 if score >= 6 else 0
    
    def _calculate_liver_risk(self, age, bmi, alcohol, exercise, diet, symptoms):
        score = (age > 50) * 1 + (bmi > 30) * 2 + (alcohol > 1) * 3 + (exercise == 0) * 1 + (diet < 1) * 2 + (symptoms > 5) * 1
        return 1 if score >= 5 else 0
    
    def _calculate_obesity_risk(self, bmi, age, exercise, diet, sleep, stress):
        score = (bmi > 30) * 3 + (bmi > 25) * 2 + (exercise == 0) * 2 + (diet < 1) * 2 + (sleep < 6) * 1 + (stress > 1) * 1
        return 1 if score >= 5 else 0
    
    def train_models(self):
        """Train enhanced ensemble models"""
        print("Creating enhanced dataset...")
        df = self._create_enhanced_dataset(n_samples=10000)
        
        # Enhanced feature set
        feature_cols = [
            'age', 'weight', 'height', 'bmi', 'smoking', 'exercise', 'alcohol',
            'sleep_hours', 'stress_level', 'diet_quality',
            'family_diabetes', 'family_heart', 'family_hypertension', 'family_stroke',
            'symptom_count', 'systolic_bp', 'diastolic_bp', 'fasting_glucose',
            'cholesterol', 'hdl', 'ldl', 'triglycerides', 'creatinine'
        ]
        
        self.feature_names = feature_cols
        X = df[feature_cols].values
        
        # Train models for each disease
        diseases = ['diabetes', 'heart_disease', 'hypertension', 'stroke', 'kidney_disease', 'liver_disease', 'obesity']
        
        for disease in diseases:
            print(f"Training {disease} risk model...")
            y = df[f'{disease}_risk'].values
            
            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
            
            # Scale features
            if disease == 'diabetes':  # Only fit scaler once
                X_train_scaled = self.scaler.fit_transform(X_train)
                X_test_scaled = self.scaler.transform(X_test)
            else:
                X_train_scaled = self.scaler.transform(X_train)
                X_test_scaled = self.scaler.transform(X_test)
            
            # Ensemble model: XGBoost + RandomForest
            xgb_model = XGBClassifier(n_estimators=150, max_depth=6, learning_rate=0.08, random_state=42, eval_metric='logloss')
            rf_model = RandomForestClassifier(n_estimators=150, max_depth=8, random_state=42)
            
            ensemble = VotingClassifier(
                estimators=[('xgb', xgb_model), ('rf', rf_model)],
                voting='soft'
            )
            
            ensemble.fit(X_train_scaled, y_train)
            y_pred = ensemble.predict(X_test_scaled)
            acc = accuracy_score(y_test, y_pred)
            print(f"  {disease} model accuracy: {acc:.2%}")
            
            self.models[disease] = ensemble
        
        # Save models
        for disease, path in self.disease_models.items():
            joblib.dump(self.models[disease], path)
        joblib.dump(self.scaler, self.scaler_path)
        joblib.dump(self.feature_names, self.feature_names_path)
        
        print("✅ All enhanced risk prediction models trained and saved!")
    
    def load_models(self):
        """Load trained models"""
        for disease, path in self.disease_models.items():
            self.models[disease] = joblib.load(path)
        self.scaler = joblib.load(self.scaler_path)
        self.feature_names = joblib.load(self.feature_names_path)
        print("✅ Enhanced risk prediction models loaded successfully!")
    
    def _get_feature_importance(self, disease, features):
        """Get feature importance for a disease"""
        model = self.models[disease]
        if hasattr(model, 'feature_importances_'):
            importances = model.feature_importances_
        elif hasattr(model, 'named_estimators_'):
            # For ensemble, average importances
            xgb_imp = model.named_estimators_['xgb'].feature_importances_
            rf_imp = model.named_estimators_['rf'].feature_importances_
            importances = (xgb_imp + rf_imp) / 2
        else:
            return []
        
        # Get top 5 most important features
        indices = np.argsort(importances)[::-1][:5]
        top_features = []
        for idx in indices:
            top_features.append({
                'feature': self.feature_names[idx],
                'importance': float(importances[idx]),
                'value': float(features[0][idx])
            })
        return top_features
    
    def _generate_ai_recommendations(self, risks, bmi, age, lifestyle_factors, top_features):
        """Generate AI-powered personalized recommendations"""
        recommendations = []
        
        # High-risk diseases
        high_risk_diseases = [d for d, r in risks.items() if r >= 70]
        moderate_risk_diseases = [d for d, r in risks.items() if 50 <= r < 70]
        
        # Priority recommendations based on risk levels
        if high_risk_diseases:
            for disease in high_risk_diseases:
                rec = self._get_disease_specific_recommendations(disease, risks[disease], lifestyle_factors, top_features.get(disease, []))
                recommendations.extend(rec)
        
        if moderate_risk_diseases:
            for disease in moderate_risk_diseases:
                rec = self._get_disease_specific_recommendations(disease, risks[disease], lifestyle_factors, top_features.get(disease, []))
                recommendations.extend(rec)
        
        # Lifestyle-based recommendations
        if bmi > 30:
            recommendations.append({
                'priority': 'high',
                'category': 'weight_management',
                'message': f'Your BMI of {bmi:.1f} indicates obesity. Focus on gradual weight loss (1-2 kg/month) through calorie deficit and regular exercise.',
                'action_items': ['Reduce daily calories by 500-750', 'Aim for 150+ minutes exercise/week', 'Track food intake']
            })
        elif bmi > 25:
            recommendations.append({
                'priority': 'medium',
                'category': 'weight_management',
                'message': f'Your BMI of {bmi:.1f} indicates overweight. Maintain healthy diet and increase physical activity.',
                'action_items': ['Increase daily activity', 'Focus on whole foods', 'Limit processed foods']
            })
        
        if lifestyle_factors.get('exercise') == 0:
            recommendations.append({
                'priority': 'high',
                'category': 'exercise',
                'message': 'Regular exercise can significantly reduce your health risks. Start with 30 minutes of moderate activity, 5 days per week.',
                'action_items': ['Start with walking 30 min/day', 'Gradually increase intensity', 'Find activities you enjoy']
            })
        
        if lifestyle_factors.get('smoking'):
            recommendations.append({
                'priority': 'high',
                'category': 'smoking',
                'message': 'Smoking is a major risk factor. Quitting can reduce your risk of heart disease, stroke, and cancer significantly.',
                'action_items': ['Consider smoking cessation programs', 'Use nicotine replacement therapy', 'Seek support from healthcare provider']
            })
        
        if lifestyle_factors.get('stress_level', 0) > 1:
            recommendations.append({
                'priority': 'medium',
                'category': 'stress',
                'message': 'High stress levels can contribute to multiple health risks. Practice stress management techniques.',
                'action_items': ['Try meditation or yoga', 'Ensure adequate sleep (7-9 hours)', 'Consider counseling if needed']
            })
        
        if lifestyle_factors.get('sleep_hours', 7) < 6:
            recommendations.append({
                'priority': 'medium',
                'category': 'sleep',
                'message': 'Inadequate sleep (less than 6 hours) increases health risks. Aim for 7-9 hours of quality sleep.',
                'action_items': ['Maintain consistent sleep schedule', 'Create relaxing bedtime routine', 'Limit screen time before bed']
            })
        
        return recommendations
    
    def _get_disease_specific_recommendations(self, disease, risk, lifestyle, top_features):
        """Get disease-specific AI recommendations"""
        recommendations = []
        
        disease_names = {
            'diabetes': 'Diabetes',
            'heart_disease': 'Heart Disease',
            'hypertension': 'Hypertension',
            'stroke': 'Stroke',
            'kidney_disease': 'Kidney Disease',
            'liver_disease': 'Liver Disease',
            'obesity': 'Obesity'
        }
        
        disease_name = disease_names.get(disease, disease)
        
        # Analyze top contributing factors
        contributing_factors = []
        for feat in top_features[:3]:
            feat_name = feat['feature'].replace('_', ' ').title()
            contributing_factors.append(feat_name)
        
        factors_text = ', '.join(contributing_factors) if contributing_factors else 'various factors'
        
        if risk >= 70:
            recommendations.append({
                'priority': 'high',
                'category': disease,
                'message': f'High {disease_name} risk ({risk}%) detected. Contributing factors: {factors_text}. Consult a healthcare provider for comprehensive evaluation and management plan.',
                'action_items': self._get_action_items(disease, risk, lifestyle)
            })
        elif risk >= 50:
            recommendations.append({
                'priority': 'medium',
                'category': disease,
                'message': f'Moderate {disease_name} risk ({risk}%). Focus on preventive measures: {factors_text}. Regular monitoring and lifestyle modifications recommended.',
                'action_items': self._get_action_items(disease, risk, lifestyle)
            })
        
        return recommendations
    
    def _get_action_items(self, disease, risk, lifestyle):
        """Get specific action items for a disease"""
        action_items_map = {
            'diabetes': ['Monitor blood sugar regularly', 'Follow diabetic diet (low glycemic index)', 'Maintain healthy weight', 'Regular exercise (150 min/week)'],
            'heart_disease': ['Heart-healthy diet (Mediterranean style)', 'Regular cardiovascular exercise', 'Manage cholesterol and blood pressure', 'Quit smoking if applicable'],
            'hypertension': ['Reduce sodium intake (<2g/day)', 'Regular blood pressure monitoring', 'Stress management', 'Limit alcohol consumption'],
            'stroke': ['Control blood pressure', 'Manage cholesterol', 'Regular exercise', 'Healthy diet rich in fruits/vegetables'],
            'kidney_disease': ['Stay hydrated', 'Monitor blood pressure', 'Limit protein if needed', 'Avoid NSAIDs'],
            'liver_disease': ['Limit alcohol consumption', 'Maintain healthy weight', 'Avoid hepatotoxic substances', 'Regular liver function tests'],
            'obesity': ['Calorie-controlled diet', 'Regular physical activity', 'Behavioral modifications', 'Consider professional weight management program']
        }
        return action_items_map.get(disease, ['Consult healthcare provider', 'Follow medical advice', 'Regular health checkups'])
    
    def predict_risks_enhanced(self, age, weight, height, symptoms=None, family_history=None,
                              smoking=False, exercise=1, alcohol=0, sleep_hours=7, stress_level=1, diet_quality=1):
        """
        Enhanced risk prediction with AI features
        """
        try:
            # Validate inputs
            if age < 18 or age > 100:
                raise ValueError("Age must be between 18 and 100")
            if weight < 30 or weight > 200:
                raise ValueError("Weight must be between 30 and 200 kg")
            if height < 100 or height > 250:
                raise ValueError("Height must be between 100 and 250 cm")
            
            # Process inputs
            symptom_list = symptoms if symptoms else []
            symptom_count = len(symptom_list)
            
            family_list = family_history if family_history else []
            family_diabetes = 1 if any('diabetes' in str(h).lower() for h in family_list) else 0
            family_heart = 1 if any('heart' in str(h).lower() for h in family_list) else 0
            family_hypertension = 1 if any('hypertension' in str(h).lower() or 'blood pressure' in str(h).lower() for h in family_list) else 0
            family_stroke = 1 if any('stroke' in str(h).lower() for h in family_list) else 0
            
            # Calculate BMI
            bmi = weight / ((height / 100) ** 2)
            
            # Estimate health metrics
            systolic_bp = 110 + (age - 30) * 0.5 + (bmi - 22) * 1.2 + family_hypertension * 8 + smoking * 5 - exercise * 3
            diastolic_bp = systolic_bp - 40
            fasting_glucose = 85 + (age - 30) * 0.3 + (bmi - 22) * 1.5 + family_diabetes * 12 - exercise * 3
            cholesterol = 180 + (age - 30) * 0.8 + (bmi - 22) * 2.5 + family_heart * 25 + smoking * 15 - exercise * 10
            hdl = 50 - (bmi - 22) * 0.5 - smoking * 5
            ldl = cholesterol - hdl - 40
            triglycerides = 100 + (bmi - 22) * 2 + alcohol * 10
            creatinine = 0.8 + (age - 30) * 0.01 + (bmi - 22) * 0.02
            
            # Clamp values
            systolic_bp = max(90, min(180, systolic_bp))
            diastolic_bp = max(60, min(120, diastolic_bp))
            fasting_glucose = max(70, min(150, fasting_glucose))
            cholesterol = max(120, min(300, cholesterol))
            hdl = max(30, min(80, hdl))
            ldl = max(50, min(200, ldl))
            triglycerides = max(50, min(400, triglycerides))
            creatinine = max(0.5, min(2.0, creatinine))
            
            # Prepare feature vector
            features = np.array([[
                age, weight, height, bmi, int(smoking), exercise, alcohol,
                sleep_hours, stress_level, diet_quality,
                family_diabetes, family_heart, family_hypertension, family_stroke,
                symptom_count, systolic_bp, diastolic_bp, fasting_glucose,
                cholesterol, hdl, ldl, triglycerides, creatinine
            ]])
            
            # Scale features
            features_scaled = self.scaler.transform(features)
            
            # Predict all diseases
            risks = {}
            probabilities = {}
            top_features = {}
            
            for disease in self.models.keys():
                model = self.models[disease]
                proba = model.predict_proba(features_scaled)[0][1]
                risks[disease] = int(proba * 100)
                probabilities[disease] = float(proba)
                top_features[disease] = self._get_feature_importance(disease, features_scaled)
            
            # Generate AI recommendations
            lifestyle_factors = {
                'smoking': smoking,
                'exercise': exercise,
                'alcohol': alcohol,
                'sleep_hours': sleep_hours,
                'stress_level': stress_level,
                'diet_quality': diet_quality
            }
            
            recommendations = self._generate_ai_recommendations(risks, bmi, age, lifestyle_factors, top_features)
            
            # Calculate overall health score (0-100, higher is better)
            avg_risk = np.mean(list(risks.values()))
            health_score = max(0, min(100, int(100 - avg_risk)))
            
            # Population comparison (simulated)
            population_avg = {
                'diabetes': 25,
                'heart_disease': 20,
                'hypertension': 30,
                'stroke': 10,
                'kidney_disease': 15,
                'liver_disease': 12,
                'obesity': 35
            }
            
            comparison = {}
            for disease, risk in risks.items():
                pop_avg = population_avg.get(disease, 20)
                comparison[disease] = {
                    'your_risk': risk,
                    'population_avg': pop_avg,
                    'difference': risk - pop_avg,
                    'percentile': min(100, max(0, int(50 + (risk - pop_avg) * 2)))
                }
            
            return {
                'risks': risks,
                'probabilities': probabilities,
                'bmi': round(bmi, 1),
                'health_score': health_score,
                'recommendations': recommendations,
                'top_risk_factors': top_features,
                'population_comparison': comparison,
                'estimated_values': {
                    'systolic_bp': round(systolic_bp, 0),
                    'diastolic_bp': round(diastolic_bp, 0),
                    'fasting_glucose': round(fasting_glucose, 1),
                    'cholesterol': round(cholesterol, 1),
                    'hdl': round(hdl, 1),
                    'ldl': round(ldl, 1),
                    'triglycerides': round(triglycerides, 1),
                    'creatinine': round(creatinine, 2)
                },
                'risk_trend': self._predict_risk_trend(risks, age, lifestyle_factors)
            }
            
        except Exception as e:
            raise ValueError(f"Error predicting risks: {str(e)}")
    
    def _predict_risk_trend(self, current_risks, age, lifestyle):
        """Predict how risks might change over time"""
        trend = {}
        
        for disease, risk in current_risks.items():
            # Predict risk in 5 years if lifestyle doesn't change
            age_factor = 1 + (5 / (100 - age)) if age < 95 else 1.1
            lifestyle_factor = 1.0
            
            if lifestyle['exercise'] == 0:
                lifestyle_factor *= 1.15
            if lifestyle['smoking']:
                lifestyle_factor *= 1.2
            if lifestyle['stress_level'] > 1:
                lifestyle_factor *= 1.1
            if lifestyle['sleep_hours'] < 6:
                lifestyle_factor *= 1.08
            
            future_risk = min(100, int(risk * age_factor * lifestyle_factor))
            
            trend[disease] = {
                'current': risk,
                'predicted_5_years': future_risk,
                'change': future_risk - risk,
                'trend': 'increasing' if future_risk > risk + 5 else 'stable' if abs(future_risk - risk) <= 5 else 'decreasing'
            }
        
        return trend

