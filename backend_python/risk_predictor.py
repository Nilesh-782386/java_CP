"""
Health Risk Predictor Module
Uses XGBoost model to predict personal disease risks (diabetes, heart disease, hypertension)
"""

import pandas as pd
import numpy as np
import joblib
import os
import json
from xgboost import XGBClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score, classification_report


class RiskPredictor:
    """Predict personal disease risks using XGBoost model"""
    
    def __init__(self, model_dir='models'):
        self.model_dir = model_dir
        os.makedirs(model_dir, exist_ok=True)
        
        # Model paths
        self.diabetes_model_path = os.path.join(model_dir, 'diabetes_risk_model.pkl')
        self.heart_model_path = os.path.join(model_dir, 'heart_disease_risk_model.pkl')
        self.hypertension_model_path = os.path.join(model_dir, 'hypertension_risk_model.pkl')
        self.scaler_path = os.path.join(model_dir, 'risk_scaler.pkl')
        
        # Models and scaler
        self.diabetes_model = None
        self.heart_model = None
        self.hypertension_model = None
        self.scaler = StandardScaler()
        
        # Load or train models
        if (os.path.exists(self.diabetes_model_path) and 
            os.path.exists(self.heart_model_path) and 
            os.path.exists(self.hypertension_model_path)):
            self.load_models()
        else:
            print("Risk prediction models not found. Training new models...")
            self.train_models()
    
    def _create_synthetic_dataset(self, n_samples=5000):
        """Create synthetic dataset for training risk prediction models"""
        np.random.seed(42)
        
        data = []
        for _ in range(n_samples):
            age = np.random.randint(18, 80)
            weight = np.random.uniform(45, 120)  # kg
            height = np.random.uniform(150, 190)  # cm
            bmi = weight / ((height / 100) ** 2)
            
            # Lifestyle factors
            smoking = np.random.choice([0, 1], p=[0.7, 0.3])  # 0 = no, 1 = yes
            exercise = np.random.choice([0, 1, 2], p=[0.3, 0.5, 0.2])  # 0 = none, 1 = moderate, 2 = high
            alcohol = np.random.choice([0, 1], p=[0.6, 0.4])
            
            # Family history (0 = no, 1 = yes)
            family_diabetes = np.random.choice([0, 1], p=[0.7, 0.3])
            family_heart = np.random.choice([0, 1], p=[0.75, 0.25])
            family_hypertension = np.random.choice([0, 1], p=[0.7, 0.3])
            
            # Symptom count (0-10)
            symptom_count = np.random.randint(0, 11)
            
            # Blood pressure (systolic)
            systolic_bp = np.random.normal(120, 15)
            if family_hypertension:
                systolic_bp += np.random.normal(10, 5)
            
            # Blood sugar (fasting)
            fasting_glucose = np.random.normal(90, 15)
            if family_diabetes or bmi > 30:
                fasting_glucose += np.random.normal(15, 10)
            
            # Cholesterol
            cholesterol = np.random.normal(200, 30)
            if family_heart or smoking:
                cholesterol += np.random.normal(20, 10)
            
            # Risk factors for diabetes
            diabetes_risk_score = (
                (age > 45) * 2 +
                (bmi > 25) * 3 +
                (bmi > 30) * 2 +
                (family_diabetes) * 3 +
                (exercise == 0) * 2 +
                (fasting_glucose > 100) * 3 +
                (symptom_count > 5) * 1
            )
            diabetes_risk = 1 if diabetes_risk_score >= 8 else 0
            
            # Risk factors for heart disease
            heart_risk_score = (
                (age > 50) * 2 +
                (bmi > 25) * 2 +
                (smoking) * 3 +
                (family_heart) * 3 +
                (exercise == 0) * 2 +
                (cholesterol > 200) * 2 +
                (systolic_bp > 130) * 2 +
                (symptom_count > 5) * 1
            )
            heart_risk = 1 if heart_risk_score >= 8 else 0
            
            # Risk factors for hypertension
            hypertension_risk_score = (
                (age > 40) * 2 +
                (bmi > 25) * 3 +
                (family_hypertension) * 3 +
                (exercise == 0) * 2 +
                (smoking) * 1 +
                (alcohol) * 1 +
                (systolic_bp > 120) * 2 +
                (symptom_count > 4) * 1
            )
            hypertension_risk = 1 if hypertension_risk_score >= 7 else 0
            
            data.append({
                'age': age,
                'weight': weight,
                'height': height,
                'bmi': bmi,
                'smoking': smoking,
                'exercise': exercise,
                'alcohol': alcohol,
                'family_diabetes': family_diabetes,
                'family_heart': family_heart,
                'family_hypertension': family_hypertension,
                'symptom_count': symptom_count,
                'systolic_bp': systolic_bp,
                'fasting_glucose': fasting_glucose,
                'cholesterol': cholesterol,
                'diabetes_risk': diabetes_risk,
                'heart_risk': heart_risk,
                'hypertension_risk': hypertension_risk
            })
        
        return pd.DataFrame(data)
    
    def train_models(self):
        """Train XGBoost models for risk prediction"""
        print("Creating synthetic dataset...")
        df = self._create_synthetic_dataset(n_samples=5000)
        
        # Feature columns
        feature_cols = [
            'age', 'weight', 'height', 'bmi', 'smoking', 'exercise',
            'alcohol', 'family_diabetes', 'family_heart', 'family_hypertension',
            'symptom_count', 'systolic_bp', 'fasting_glucose', 'cholesterol'
        ]
        
        X = df[feature_cols].values
        y_diabetes = df['diabetes_risk'].values
        y_heart = df['heart_risk'].values
        y_hypertension = df['hypertension_risk'].values
        
        # Split data
        X_train, X_test, y_diabetes_train, y_diabetes_test = train_test_split(
            X, y_diabetes, test_size=0.2, random_state=42
        )
        _, _, y_heart_train, y_heart_test = train_test_split(
            X, y_heart, test_size=0.2, random_state=42
        )
        _, _, y_hypertension_train, y_hypertension_test = train_test_split(
            X, y_hypertension, test_size=0.2, random_state=42
        )
        
        # Scale features
        X_train_scaled = self.scaler.fit_transform(X_train)
        X_test_scaled = self.scaler.transform(X_test)
        
        # Train Diabetes model
        print("Training diabetes risk model...")
        self.diabetes_model = XGBClassifier(
            n_estimators=100,
            max_depth=5,
            learning_rate=0.1,
            random_state=42,
            eval_metric='logloss'
        )
        self.diabetes_model.fit(X_train_scaled, y_diabetes_train)
        diabetes_pred = self.diabetes_model.predict(X_test_scaled)
        diabetes_acc = accuracy_score(y_diabetes_test, diabetes_pred)
        print(f"Diabetes model accuracy: {diabetes_acc:.2%}")
        
        # Train Heart Disease model
        print("Training heart disease risk model...")
        self.heart_model = XGBClassifier(
            n_estimators=100,
            max_depth=5,
            learning_rate=0.1,
            random_state=42,
            eval_metric='logloss'
        )
        self.heart_model.fit(X_train_scaled, y_heart_train)
        heart_pred = self.heart_model.predict(X_test_scaled)
        heart_acc = accuracy_score(y_heart_test, heart_pred)
        print(f"Heart disease model accuracy: {heart_acc:.2%}")
        
        # Train Hypertension model
        print("Training hypertension risk model...")
        self.hypertension_model = XGBClassifier(
            n_estimators=100,
            max_depth=5,
            learning_rate=0.1,
            random_state=42,
            eval_metric='logloss'
        )
        self.hypertension_model.fit(X_train_scaled, y_hypertension_train)
        hypertension_pred = self.hypertension_model.predict(X_test_scaled)
        hypertension_acc = accuracy_score(y_hypertension_test, hypertension_pred)
        print(f"Hypertension model accuracy: {hypertension_acc:.2%}")
        
        # Save models
        joblib.dump(self.diabetes_model, self.diabetes_model_path)
        joblib.dump(self.heart_model, self.heart_model_path)
        joblib.dump(self.hypertension_model, self.hypertension_model_path)
        joblib.dump(self.scaler, self.scaler_path)
        
        print("✅ All risk prediction models trained and saved successfully!")
    
    def load_models(self):
        """Load trained models"""
        self.diabetes_model = joblib.load(self.diabetes_model_path)
        self.heart_model = joblib.load(self.heart_model_path)
        self.hypertension_model = joblib.load(self.hypertension_model_path)
        self.scaler = joblib.load(self.scaler_path)
        print("✅ Risk prediction models loaded successfully!")
    
    def _calculate_bmi(self, weight, height):
        """Calculate BMI from weight (kg) and height (cm)"""
        if weight <= 0 or height <= 0:
            return 22.0  # Default BMI
        height_m = height / 100.0
        return weight / (height_m ** 2)
    
    def _estimate_blood_pressure(self, age, bmi, family_history, smoking, exercise):
        """Estimate systolic blood pressure based on risk factors"""
        base_bp = 110
        base_bp += (age - 30) * 0.5  # Age factor
        base_bp += (bmi - 22) * 1.2  # BMI factor
        base_bp += family_history * 8  # Family history
        base_bp += smoking * 5  # Smoking
        base_bp -= exercise * 3  # Exercise reduces BP
        return max(90, min(180, base_bp))  # Clamp between 90-180
    
    def _estimate_glucose(self, age, bmi, family_history, exercise, symptom_count):
        """Estimate fasting glucose based on risk factors"""
        base_glucose = 85
        base_glucose += (age - 30) * 0.3
        base_glucose += (bmi - 22) * 1.5
        base_glucose += family_history * 12
        base_glucose -= exercise * 3
        base_glucose += (symptom_count > 5) * 8
        return max(70, min(150, base_glucose))  # Clamp between 70-150
    
    def _estimate_cholesterol(self, age, bmi, family_history, smoking, exercise):
        """Estimate cholesterol based on risk factors"""
        base_chol = 180
        base_chol += (age - 30) * 0.8
        base_chol += (bmi - 22) * 2.5
        base_chol += family_history * 25
        base_chol += smoking * 15
        base_chol -= exercise * 10
        return max(120, min(300, base_chol))  # Clamp between 120-300
    
    def predict_risks(self, age, weight, height, symptoms=None, family_history=None, 
                     smoking=False, exercise=1, alcohol=False, sleep_hours=None, stress_level=None, diet_quality=None):
        """
        Predict disease risks for a person (Enhanced with more features)
        
        Args:
            age: Age in years
            weight: Weight in kg
            height: Height in cm
            symptoms: List of symptom names (optional)
            family_history: List of family history conditions (optional)
            smoking: Boolean (0 or 1)
            exercise: 0 = none, 1 = moderate, 2 = high
            alcohol: Boolean (0 or 1) or int (0=none, 1=moderate, 2=heavy)
            sleep_hours: Hours of sleep per night (optional, default 7)
            stress_level: 0=low, 1=moderate, 2=high (optional, default 1)
            diet_quality: 0=poor, 1=moderate, 2=good (optional, default 1)
        
        Returns:
            Dictionary with risk percentages, recommendations, and enhanced AI features
        """
        try:
            # Validate inputs
            if age < 18 or age > 100:
                raise ValueError("Age must be between 18 and 100")
            if weight < 30 or weight > 200:
                raise ValueError("Weight must be between 30 and 200 kg")
            if height < 100 or height > 250:
                raise ValueError("Height must be between 100 and 250 cm")
            
            # Process symptoms
            symptom_list = symptoms if symptoms else []
            symptom_count = len(symptom_list)
            
            # Process family history
            family_list = family_history if family_history else []
            family_diabetes = 1 if any('diabetes' in str(h).lower() for h in family_list) else 0
            family_heart = 1 if any('heart' in str(h).lower() for h in family_list) else 0
            family_hypertension = 1 if any('hypertension' in str(h).lower() or 'blood pressure' in str(h).lower() for h in family_list) else 0
            
            # Convert boolean inputs with defaults for enhanced features
            smoking_int = 1 if smoking else 0
            alcohol_int = int(alcohol) if isinstance(alcohol, (int, float)) else (1 if alcohol else 0)
            exercise_int = int(exercise) if exercise in [0, 1, 2] else 1
            sleep_hours_val = sleep_hours if sleep_hours is not None else 7.0
            stress_level_val = stress_level if stress_level is not None else 1
            diet_quality_val = diet_quality if diet_quality is not None else 1
            
            # Calculate BMI
            bmi = self._calculate_bmi(weight, height)
            
            # Enhanced estimation with new factors
            systolic_bp = self._estimate_blood_pressure(age, bmi, family_hypertension, smoking_int, exercise_int)
            systolic_bp += (stress_level_val - 1) * 3  # Stress increases BP
            systolic_bp -= (sleep_hours_val - 7) * 0.5  # Good sleep reduces BP
            systolic_bp = max(90, min(180, systolic_bp))
            
            fasting_glucose = self._estimate_glucose(age, bmi, family_diabetes, exercise_int, symptom_count)
            fasting_glucose += (stress_level_val - 1) * 2  # Stress affects glucose
            fasting_glucose -= (diet_quality_val - 1) * 3  # Good diet helps
            fasting_glucose = max(70, min(150, fasting_glucose))
            
            cholesterol = self._estimate_cholesterol(age, bmi, family_heart, smoking_int, exercise_int)
            cholesterol += (stress_level_val - 1) * 5  # Stress affects cholesterol
            cholesterol -= (diet_quality_val - 1) * 8  # Good diet helps
            cholesterol = max(120, min(300, cholesterol))
            
            # Prepare feature vector
            features = np.array([[
                age, weight, height, bmi, smoking_int, exercise_int,
                alcohol_int, family_diabetes, family_heart, family_hypertension,
                symptom_count, systolic_bp, fasting_glucose, cholesterol
            ]])
            
            # Scale features
            features_scaled = self.scaler.transform(features)
            
            # Predict probabilities
            diabetes_proba = self.diabetes_model.predict_proba(features_scaled)[0][1]
            heart_proba = self.heart_model.predict_proba(features_scaled)[0][1]
            hypertension_proba = self.hypertension_model.predict_proba(features_scaled)[0][1]
            
            # Convert to percentages
            diabetes_risk = int(diabetes_proba * 100)
            heart_risk = int(heart_proba * 100)
            hypertension_risk = int(hypertension_proba * 100)
            
            # Get feature importance for each disease
            feature_importance = self._get_feature_importance(features_scaled)
            
            # Generate enhanced recommendations with new factors
            recommendations = self._generate_recommendations(
                diabetes_risk, heart_risk, hypertension_risk,
                bmi, age, smoking_int, exercise_int, family_diabetes, family_heart, family_hypertension,
                sleep_hours_val, stress_level_val, diet_quality_val
            )
            
            # Calculate overall health score
            avg_risk = (diabetes_risk + heart_risk + hypertension_risk) / 3
            health_score = max(0, min(100, int(100 - avg_risk)))
            
            # Risk trend prediction (5 years)
            age_factor = 1 + (5 / max(1, 100 - age))
            lifestyle_factor = 1.0
            if exercise_int == 0:
                lifestyle_factor *= 1.15
            if smoking_int:
                lifestyle_factor *= 1.2
            if stress_level_val > 1:
                lifestyle_factor *= 1.1
            if sleep_hours_val < 6:
                lifestyle_factor *= 1.08
            
            risk_trend = {
                'diabetes': {
                    'current': diabetes_risk,
                    'predicted_5_years': min(100, int(diabetes_risk * age_factor * lifestyle_factor)),
                    'trend': 'increasing' if lifestyle_factor > 1.1 else 'stable'
                },
                'heart_disease': {
                    'current': heart_risk,
                    'predicted_5_years': min(100, int(heart_risk * age_factor * lifestyle_factor)),
                    'trend': 'increasing' if lifestyle_factor > 1.1 else 'stable'
                },
                'hypertension': {
                    'current': hypertension_risk,
                    'predicted_5_years': min(100, int(hypertension_risk * age_factor * lifestyle_factor)),
                    'trend': 'increasing' if lifestyle_factor > 1.1 else 'stable'
                }
            }
            
            # Risk reduction calculator - show potential improvements
            risk_reduction = self._calculate_risk_reduction(
                diabetes_risk, heart_risk, hypertension_risk,
                smoking_int, exercise_int, stress_level_val, sleep_hours_val, diet_quality_val, bmi
            )
            
            # Generate personalized action plan
            action_plan = self._generate_action_plan(
                diabetes_risk, heart_risk, hypertension_risk,
                smoking_int, exercise_int, stress_level_val, sleep_hours_val, diet_quality_val, bmi, age
            )
            
            # Risk explanations
            risk_explanations = self._explain_risks(
                diabetes_risk, heart_risk, hypertension_risk,
                bmi, age, smoking_int, exercise_int, family_diabetes, family_heart, family_hypertension,
                sleep_hours_val, stress_level_val, diet_quality_val, feature_importance
            )
            
            # Preventive screening recommendations
            screening_recommendations = self._get_screening_recommendations(
                diabetes_risk, heart_risk, hypertension_risk, age, bmi
            )
            
            # Population comparison
            population_comparison = self._get_population_comparison(
                diabetes_risk, heart_risk, hypertension_risk, age, bmi
            )
            
            return {
                'diabetes_risk': diabetes_risk,
                'heart_risk': heart_risk,
                'hypertension_risk': hypertension_risk,
                'bmi': round(bmi, 1),
                'health_score': health_score,
                'recommendations': recommendations,
                'risk_trend': risk_trend,
                'feature_importance': feature_importance,
                'risk_reduction': risk_reduction,
                'action_plan': action_plan,
                'risk_explanations': risk_explanations,
                'screening_recommendations': screening_recommendations,
                'population_comparison': population_comparison,
                'estimated_values': {
                    'systolic_bp': round(systolic_bp, 0),
                    'fasting_glucose': round(fasting_glucose, 1),
                    'cholesterol': round(cholesterol, 1)
                }
            }
            
        except Exception as e:
            raise ValueError(f"Error predicting risks: {str(e)}")
    
    def _generate_recommendations(self, diabetes_risk, heart_risk, hypertension_risk,
                                 bmi, age, smoking, exercise, family_diabetes, family_heart, family_hypertension,
                                 sleep_hours=7, stress_level=1, diet_quality=1):
        """Generate enhanced personalized recommendations based on risk levels and lifestyle factors"""
        recommendations = []
        
        # High risk recommendations
        if diabetes_risk >= 70:
            recommendations.append({
                'priority': 'high',
                'category': 'diabetes',
                'message': 'High diabetes risk detected. Consider regular blood sugar monitoring and consult a healthcare provider.'
            })
        elif diabetes_risk >= 50:
            recommendations.append({
                'priority': 'medium',
                'category': 'diabetes',
                'message': 'Moderate diabetes risk. Maintain healthy diet and regular exercise.'
            })
        
        if heart_risk >= 70:
            recommendations.append({
                'priority': 'high',
                'category': 'heart',
                'message': 'High heart disease risk detected. Consider cardiac screening and lifestyle modifications.'
            })
        elif heart_risk >= 50:
            recommendations.append({
                'priority': 'medium',
                'category': 'heart',
                'message': 'Moderate heart disease risk. Focus on heart-healthy lifestyle choices.'
            })
        
        if hypertension_risk >= 70:
            recommendations.append({
                'priority': 'high',
                'category': 'hypertension',
                'message': 'High hypertension risk detected. Monitor blood pressure regularly and reduce sodium intake.'
            })
        elif hypertension_risk >= 50:
            recommendations.append({
                'priority': 'medium',
                'category': 'hypertension',
                'message': 'Moderate hypertension risk. Maintain healthy weight and regular exercise.'
            })
        
        # Lifestyle recommendations
        if bmi > 30:
            recommendations.append({
                'priority': 'high',
                'category': 'lifestyle',
                'message': 'BMI indicates obesity. Consider weight management program with diet and exercise.'
            })
        elif bmi > 25:
            recommendations.append({
                'priority': 'medium',
                'category': 'lifestyle',
                'message': 'BMI indicates overweight. Focus on balanced diet and increased physical activity.'
            })
        
        if smoking:
            recommendations.append({
                'priority': 'high',
                'category': 'lifestyle',
                'message': 'Smoking significantly increases health risks. Consider smoking cessation programs.'
            })
        
        if exercise == 0:
            recommendations.append({
                'priority': 'medium',
                'category': 'lifestyle',
                'message': 'Regular exercise can significantly reduce health risks. Aim for at least 150 minutes per week.'
            })
        
        if family_diabetes or family_heart or family_hypertension:
            recommendations.append({
                'priority': 'medium',
                'category': 'screening',
                'message': 'Family history of chronic diseases. Consider regular health screenings and preventive care.'
            })
        
        # Enhanced lifestyle recommendations
        if sleep_hours < 6:
            recommendations.append({
                'priority': 'medium',
                'category': 'sleep',
                'message': f'Inadequate sleep ({sleep_hours:.1f} hours) increases health risks. Aim for 7-9 hours of quality sleep per night for optimal health.'
            })
        elif sleep_hours > 9:
            recommendations.append({
                'priority': 'low',
                'category': 'sleep',
                'message': f'Excessive sleep ({sleep_hours:.1f} hours) may indicate underlying health issues. Consult a healthcare provider if this is consistent.'
            })
        
        if stress_level > 1:
            recommendations.append({
                'priority': 'medium',
                'category': 'stress',
                'message': 'High stress levels can contribute to multiple health risks. Practice stress management techniques like meditation, yoga, or deep breathing exercises.'
            })
        
        if diet_quality < 1:
            recommendations.append({
                'priority': 'high',
                'category': 'diet',
                'message': 'Poor diet quality significantly impacts health. Focus on whole foods, fruits, vegetables, lean proteins, and limit processed foods and sugars.'
            })
        elif diet_quality == 1:
            recommendations.append({
                'priority': 'medium',
                'category': 'diet',
                'message': 'Improving diet quality can further reduce health risks. Consider consulting a nutritionist for personalized dietary recommendations.'
            })
        
        # General recommendations if no specific risks
        if not recommendations:
            recommendations.append({
                'priority': 'low',
                'category': 'general',
                'message': 'Your risk levels are relatively low. Continue maintaining a healthy lifestyle with regular exercise, balanced diet, adequate sleep, and stress management.'
            })
        
        return recommendations
    
    def _get_feature_importance(self, features_scaled):
        """Get feature importance for each disease model"""
        feature_names = [
            'age', 'weight', 'height', 'bmi', 'smoking', 'exercise',
            'alcohol', 'family_diabetes', 'family_heart', 'family_hypertension',
            'symptom_count', 'systolic_bp', 'fasting_glucose', 'cholesterol'
        ]
        
        importance_data = {}
        
        for disease_name, model in [('diabetes', self.diabetes_model), 
                                    ('heart_disease', self.heart_model),
                                    ('hypertension', self.hypertension_model)]:
            if hasattr(model, 'feature_importances_'):
                importances = model.feature_importances_
                # Get top 5 most important features
                indices = np.argsort(importances)[::-1][:5]
                top_features = []
                for idx in indices:
                    top_features.append({
                        'feature': feature_names[idx],
                        'importance': float(importances[idx]),
                        'value': float(features_scaled[0][idx])
                    })
                importance_data[disease_name] = top_features
        
        return importance_data
    
    def _calculate_risk_reduction(self, diabetes_risk, heart_risk, hypertension_risk,
                                 smoking, exercise, stress, sleep, diet, bmi):
        """Calculate potential risk reduction from lifestyle improvements"""
        reductions = {}
        
        # Scenario 1: Quit smoking
        if smoking:
            reductions['quit_smoking'] = {
                'diabetes': min(15, int(diabetes_risk * 0.1)),
                'heart_disease': min(25, int(heart_risk * 0.2)),
                'hypertension': min(10, int(hypertension_risk * 0.08)),
                'description': 'Quitting smoking can significantly reduce cardiovascular risks'
            }
        
        # Scenario 2: Increase exercise
        if exercise == 0:
            reductions['increase_exercise'] = {
                'diabetes': min(20, int(diabetes_risk * 0.15)),
                'heart_disease': min(18, int(heart_risk * 0.15)),
                'hypertension': min(15, int(hypertension_risk * 0.12)),
                'description': 'Regular exercise (150+ min/week) can reduce multiple health risks'
            }
        elif exercise == 1:
            reductions['increase_exercise'] = {
                'diabetes': min(10, int(diabetes_risk * 0.08)),
                'heart_disease': min(8, int(heart_risk * 0.08)),
                'hypertension': min(7, int(hypertension_risk * 0.06)),
                'description': 'Increasing exercise intensity can provide additional benefits'
            }
        
        # Scenario 3: Improve diet
        if diet < 2:
            reductions['improve_diet'] = {
                'diabetes': min(18, int(diabetes_risk * 0.12)),
                'heart_disease': min(15, int(heart_risk * 0.12)),
                'hypertension': min(12, int(hypertension_risk * 0.10)),
                'description': 'Improving diet quality (whole foods, less processed) reduces risks'
            }
        
        # Scenario 4: Reduce stress
        if stress > 1:
            reductions['reduce_stress'] = {
                'diabetes': min(8, int(diabetes_risk * 0.06)),
                'heart_disease': min(10, int(heart_risk * 0.08)),
                'hypertension': min(12, int(hypertension_risk * 0.10)),
                'description': 'Stress management can help control blood pressure and overall health'
            }
        
        # Scenario 5: Improve sleep
        if sleep < 7:
            reductions['improve_sleep'] = {
                'diabetes': min(10, int(diabetes_risk * 0.08)),
                'heart_disease': min(8, int(heart_risk * 0.07)),
                'hypertension': min(10, int(hypertension_risk * 0.08)),
                'description': 'Adequate sleep (7-9 hours) supports metabolic and cardiovascular health'
            }
        
        # Scenario 6: Weight management
        if bmi > 25:
            target_bmi = 25 if bmi <= 30 else 28
            weight_loss_impact = min(25, int((bmi - target_bmi) * 2))
            reductions['weight_management'] = {
                'diabetes': min(weight_loss_impact, int(diabetes_risk * 0.20)),
                'heart_disease': min(weight_loss_impact - 3, int(heart_risk * 0.18)),
                'hypertension': min(weight_loss_impact - 2, int(hypertension_risk * 0.15)),
                'description': f'Reducing BMI from {bmi:.1f} to {target_bmi:.1f} can significantly lower risks'
            }
        
        return reductions
    
    def _generate_action_plan(self, diabetes_risk, heart_risk, hypertension_risk,
                             smoking, exercise, stress, sleep, diet, bmi, age):
        """Generate step-by-step personalized action plan"""
        action_plan = []
        priority = 1
        
        # High priority actions for high risks
        if max(diabetes_risk, heart_risk, hypertension_risk) >= 70:
            action_plan.append({
                'priority': priority,
                'category': 'immediate',
                'title': 'Consult Healthcare Provider',
                'description': 'Schedule an appointment with your healthcare provider for comprehensive evaluation',
                'timeline': 'Within 1 week',
                'impact': 'High'
            })
            priority += 1
        
        # Lifestyle modifications
        if smoking:
            action_plan.append({
                'priority': priority,
                'category': 'lifestyle',
                'title': 'Quit Smoking',
                'description': 'Enroll in smoking cessation program or use nicotine replacement therapy',
                'timeline': 'Start immediately',
                'impact': 'Very High',
                'steps': [
                    'Set a quit date',
                    'Remove all tobacco products',
                    'Consider nicotine replacement therapy',
                    'Seek support from family/friends',
                    'Join a support group'
                ]
            })
            priority += 1
        
        if exercise == 0:
            action_plan.append({
                'priority': priority,
                'category': 'exercise',
                'title': 'Start Regular Exercise',
                'description': 'Begin with 30 minutes of moderate activity, 5 days per week',
                'timeline': 'Start this week',
                'impact': 'High',
                'steps': [
                    'Week 1-2: 15 min/day, 3 days/week (walking)',
                    'Week 3-4: 20 min/day, 4 days/week',
                    'Week 5+: 30 min/day, 5 days/week',
                    'Include both cardio and strength training',
                    'Track progress with fitness app'
                ]
            })
            priority += 1
        elif exercise == 1:
            action_plan.append({
                'priority': priority,
                'category': 'exercise',
                'title': 'Increase Exercise Intensity',
                'description': 'Add 2-3 high-intensity sessions per week',
                'timeline': 'Within 2 weeks',
                'impact': 'Medium',
                'steps': [
                    'Add 1-2 HIIT sessions per week',
                    'Increase duration to 45 minutes',
                    'Include resistance training 2x/week'
                ]
            })
            priority += 1
        
        if bmi > 25:
            action_plan.append({
                'priority': priority,
                'category': 'nutrition',
                'title': 'Weight Management Plan',
                'description': f'Target: Reduce BMI from {bmi:.1f} to {25 if bmi <= 30 else 28:.1f}',
                'timeline': '3-6 months',
                'impact': 'High',
                'steps': [
                    'Calculate daily calorie needs',
                    'Create 500-750 calorie deficit',
                    'Focus on whole foods',
                    'Track meals and weight weekly',
                    'Consider consulting a nutritionist'
                ]
            })
            priority += 1
        
        if diet < 2:
            action_plan.append({
                'priority': priority,
                'category': 'nutrition',
                'title': 'Improve Diet Quality',
                'description': 'Transition to whole foods, reduce processed foods',
                'timeline': 'Start immediately',
                'impact': 'High',
                'steps': [
                    'Increase fruits and vegetables (5+ servings/day)',
                    'Choose whole grains over refined',
                    'Include lean proteins',
                    'Limit processed foods and sugars',
                    'Stay hydrated (8+ glasses water/day)'
                ]
            })
            priority += 1
        
        if stress > 1:
            action_plan.append({
                'priority': priority,
                'category': 'wellness',
                'title': 'Stress Management',
                'description': 'Implement stress reduction techniques',
                'timeline': 'Start this week',
                'impact': 'Medium',
                'steps': [
                    'Practice 10 min meditation daily',
                    'Try deep breathing exercises',
                    'Schedule regular breaks',
                    'Consider yoga or tai chi',
                    'Ensure work-life balance'
                ]
            })
            priority += 1
        
        if sleep < 7:
            action_plan.append({
                'priority': priority,
                'category': 'wellness',
                'title': 'Improve Sleep Quality',
                'description': f'Increase sleep from {sleep:.1f} to 7-9 hours per night',
                'timeline': 'Within 2 weeks',
                'impact': 'Medium',
                'steps': [
                    'Set consistent sleep schedule',
                    'Create relaxing bedtime routine',
                    'Limit screen time 1 hour before bed',
                    'Keep bedroom cool and dark',
                    'Avoid caffeine after 2 PM'
                ]
            })
            priority += 1
        
        # Monitoring actions
        if diabetes_risk >= 50:
            action_plan.append({
                'priority': priority,
                'category': 'monitoring',
                'title': 'Blood Sugar Monitoring',
                'description': 'Regular monitoring of fasting and post-meal glucose',
                'timeline': 'Start immediately',
                'impact': 'High',
                'steps': [
                    'Get HbA1c test every 3-6 months',
                    'Monitor fasting glucose weekly',
                    'Track post-meal glucose',
                    'Keep a glucose log',
                    'Review with healthcare provider'
                ]
            })
            priority += 1
        
        if heart_risk >= 50 or hypertension_risk >= 50:
            action_plan.append({
                'priority': priority,
                'category': 'monitoring',
                'title': 'Cardiovascular Monitoring',
                'description': 'Regular blood pressure and cholesterol checks',
                'timeline': 'Start immediately',
                'impact': 'High',
                'steps': [
                    'Monitor BP at home (2x daily)',
                    'Get lipid profile every 6 months',
                    'Track BP readings in log',
                    'Consider ECG if recommended',
                    'Regular follow-ups with doctor'
                ]
            })
            priority += 1
        
        return action_plan
    
    def _explain_risks(self, diabetes_risk, heart_risk, hypertension_risk,
                      bmi, age, smoking, exercise, family_diabetes, family_heart, family_hypertension,
                      sleep, stress, diet, feature_importance):
        """Explain why risks are at current levels"""
        explanations = {}
        
        # Diabetes explanation
        diabetes_factors = []
        if bmi > 25:
            diabetes_factors.append(f"BMI of {bmi:.1f} (overweight/obese)")
        if age > 45:
            diabetes_factors.append(f"Age {age} (increased risk after 45)")
        if family_diabetes:
            diabetes_factors.append("Family history of diabetes")
        if exercise == 0:
            diabetes_factors.append("Lack of physical activity")
        if sleep < 6:
            diabetes_factors.append(f"Inadequate sleep ({sleep:.1f} hours)")
        if stress > 1:
            diabetes_factors.append("High stress levels")
        if diet < 1:
            diabetes_factors.append("Poor diet quality")
        
        explanations['diabetes'] = {
            'risk_level': diabetes_risk,
            'level': 'high' if diabetes_risk >= 70 else 'moderate' if diabetes_risk >= 50 else 'low',
            'primary_factors': diabetes_factors[:3] if diabetes_factors else ["Low risk factors"],
            'explanation': f"Your diabetes risk is {diabetes_risk}%. " + 
                         ("Primary contributing factors: " + ", ".join(diabetes_factors[:3]) if diabetes_factors else 
                          "Your risk factors are relatively low. Continue maintaining healthy lifestyle.")
        }
        
        # Heart disease explanation
        heart_factors = []
        if age > 50:
            heart_factors.append(f"Age {age} (increased risk after 50)")
        if smoking:
            heart_factors.append("Smoking (major risk factor)")
        if bmi > 25:
            heart_factors.append(f"BMI of {bmi:.1f}")
        if family_heart:
            heart_factors.append("Family history of heart disease")
        if exercise == 0:
            heart_factors.append("Lack of exercise")
        if stress > 1:
            heart_factors.append("High stress")
        if diet < 1:
            heart_factors.append("Poor diet")
        
        explanations['heart_disease'] = {
            'risk_level': heart_risk,
            'level': 'high' if heart_risk >= 70 else 'moderate' if heart_risk >= 50 else 'low',
            'primary_factors': heart_factors[:3] if heart_factors else ["Low risk factors"],
            'explanation': f"Your heart disease risk is {heart_risk}%. " +
                         ("Primary contributing factors: " + ", ".join(heart_factors[:3]) if heart_factors else
                          "Your risk factors are relatively low.")
        }
        
        # Hypertension explanation
        hypertension_factors = []
        if age > 40:
            hypertension_factors.append(f"Age {age} (increased risk after 40)")
        if bmi > 25:
            hypertension_factors.append(f"BMI of {bmi:.1f}")
        if family_hypertension:
            hypertension_factors.append("Family history of hypertension")
        if exercise == 0:
            hypertension_factors.append("Lack of exercise")
        if stress > 1:
            hypertension_factors.append("High stress levels")
        if sleep < 6:
            hypertension_factors.append(f"Inadequate sleep")
        if diet < 1:
            hypertension_factors.append("Poor diet (high sodium)")
        
        explanations['hypertension'] = {
            'risk_level': hypertension_risk,
            'level': 'high' if hypertension_risk >= 70 else 'moderate' if hypertension_risk >= 50 else 'low',
            'primary_factors': hypertension_factors[:3] if hypertension_factors else ["Low risk factors"],
            'explanation': f"Your hypertension risk is {hypertension_risk}%. " +
                         ("Primary contributing factors: " + ", ".join(hypertension_factors[:3]) if hypertension_factors else
                          "Your risk factors are relatively low.")
        }
        
        return explanations
    
    def _get_screening_recommendations(self, diabetes_risk, heart_risk, hypertension_risk, age, bmi):
        """Get preventive screening recommendations based on risk levels"""
        screenings = []
        
        # Diabetes screening
        if diabetes_risk >= 50 or (age > 45 and bmi > 25):
            screenings.append({
                'test': 'HbA1c (Glycated Hemoglobin)',
                'frequency': 'Every 3-6 months' if diabetes_risk >= 70 else 'Every 6-12 months',
                'purpose': 'Monitor long-term blood sugar control',
                'priority': 'high' if diabetes_risk >= 70 else 'medium'
            })
            screenings.append({
                'test': 'Fasting Blood Glucose',
                'frequency': 'Every 3-6 months' if diabetes_risk >= 70 else 'Annually',
                'purpose': 'Check current blood sugar levels',
                'priority': 'high' if diabetes_risk >= 70 else 'medium'
            })
        
        # Heart disease screening
        if heart_risk >= 50 or age > 50:
            screenings.append({
                'test': 'Lipid Profile (Cholesterol Panel)',
                'frequency': 'Every 6 months' if heart_risk >= 70 else 'Annually',
                'purpose': 'Check cholesterol and triglyceride levels',
                'priority': 'high' if heart_risk >= 70 else 'medium'
            })
            if heart_risk >= 70 or age > 55:
                screenings.append({
                    'test': 'ECG (Electrocardiogram)',
                    'frequency': 'Annually or as recommended',
                    'purpose': 'Check heart rhythm and electrical activity',
                    'priority': 'high'
                })
        
        # Hypertension screening
        if hypertension_risk >= 50:
            screenings.append({
                'test': 'Blood Pressure Monitoring',
                'frequency': 'Daily at home, monthly at clinic' if hypertension_risk >= 70 else 'Weekly at home',
                'purpose': 'Track blood pressure trends',
                'priority': 'high' if hypertension_risk >= 70 else 'medium'
            })
        
        # General health screenings
        if age > 40:
            screenings.append({
                'test': 'Complete Blood Count (CBC)',
                'frequency': 'Annually',
                'purpose': 'General health checkup',
                'priority': 'medium'
            })
        
        if bmi > 30:
            screenings.append({
                'test': 'Liver Function Tests',
                'frequency': 'Annually',
                'purpose': 'Check for fatty liver disease',
                'priority': 'medium'
            })
        
        return screenings
    
    def _get_population_comparison(self, diabetes_risk, heart_risk, hypertension_risk, age, bmi):
        """Compare user's risks with population averages"""
        # Age-adjusted population averages (simulated based on demographics)
        age_group = '18-30' if age < 30 else '30-45' if age < 45 else '45-60' if age < 60 else '60+'
        
        # Base population averages by age group
        pop_averages = {
            '18-30': {'diabetes': 8, 'heart_disease': 5, 'hypertension': 12},
            '30-45': {'diabetes': 15, 'heart_disease': 12, 'hypertension': 22},
            '45-60': {'diabetes': 28, 'heart_disease': 25, 'hypertension': 38},
            '60+': {'diabetes': 35, 'heart_disease': 40, 'hypertension': 55}
        }
        
        base_avg = pop_averages.get(age_group, pop_averages['45-60'])
        
        # Adjust for BMI
        bmi_factor = 1.0
        if bmi > 30:
            bmi_factor = 1.4
        elif bmi > 25:
            bmi_factor = 1.2
        
        pop_diabetes = int(base_avg['diabetes'] * bmi_factor)
        pop_heart = int(base_avg['heart_disease'] * bmi_factor)
        pop_hypertension = int(base_avg['hypertension'] * bmi_factor)
        
        comparison = {
            'age_group': age_group,
            'diabetes': {
                'your_risk': diabetes_risk,
                'population_avg': pop_diabetes,
                'difference': diabetes_risk - pop_diabetes,
                'percentile': min(100, max(0, int(50 + (diabetes_risk - pop_diabetes) * 1.5))),
                'status': 'above_average' if diabetes_risk > pop_diabetes + 5 else 'below_average' if diabetes_risk < pop_diabetes - 5 else 'average'
            },
            'heart_disease': {
                'your_risk': heart_risk,
                'population_avg': pop_heart,
                'difference': heart_risk - pop_heart,
                'percentile': min(100, max(0, int(50 + (heart_risk - pop_heart) * 1.5))),
                'status': 'above_average' if heart_risk > pop_heart + 5 else 'below_average' if heart_risk < pop_heart - 5 else 'average'
            },
            'hypertension': {
                'your_risk': hypertension_risk,
                'population_avg': pop_hypertension,
                'difference': hypertension_risk - pop_hypertension,
                'percentile': min(100, max(0, int(50 + (hypertension_risk - pop_hypertension) * 1.5))),
                'status': 'above_average' if hypertension_risk > pop_hypertension + 5 else 'below_average' if hypertension_risk < pop_hypertension - 5 else 'average'
            }
        }
        
        return comparison

