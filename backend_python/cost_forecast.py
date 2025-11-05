"""
Cost Forecasting Module
Estimates medical treatment costs using regression model
"""

import pandas as pd
import numpy as np
import joblib
import os
import json
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import LabelEncoder


class CostForecaster:
    """Forecast medical treatment costs"""
    
    def __init__(self, model_path='models/cost_model.pkl', dataset_path='datasets/surgery_costs.csv'):
        self.model_path = model_path
        self.dataset_path = dataset_path
        self.model = None
        self.treatment_encoder = None
        self.hospital_encoder = None
        self.treatment_costs = {}
        
        # Load or train model
        if os.path.exists(model_path):
            self.load_model()
        else:
            print("Cost model not found. Training new model...")
            self.train_model()
        
        # Load treatment costs data
        self.load_treatment_costs()
    
    def load_treatment_costs(self):
        """Load treatment cost data"""
        costs_path = 'datasets/treatment_costs.json'
        if os.path.exists(costs_path):
            with open(costs_path, 'r', encoding='utf-8') as f:
                self.treatment_costs = json.load(f)
        else:
            self.treatment_costs = self._create_default_costs()
            os.makedirs(os.path.dirname(costs_path), exist_ok=True)
            with open(costs_path, 'w', encoding='utf-8') as f:
                json.dump(self.treatment_costs, f, indent=2, ensure_ascii=False)
    
    def _create_default_costs(self):
        """Create default treatment cost data"""
        return {
            "Appendectomy": {
                "government": 15000,
                "semi-private": 45000,
                "private": 90000
            },
            "Surgery": {
                "government": 20000,
                "semi-private": 60000,
                "private": 120000
            },
            "Medication": {
                "government": 5000,
                "semi-private": 12000,
                "private": 25000
            },
            "Physical Therapy": {
                "government": 3000,
                "semi-private": 8000,
                "private": 15000
            },
            "Chemotherapy": {
                "government": 50000,
                "semi-private": 150000,
                "private": 300000
            },
            "Cardiac Surgery": {
                "government": 100000,
                "semi-private": 300000,
                "private": 600000
            },
            "Orthopedic Surgery": {
                "government": 40000,
                "semi-private": 120000,
                "private": 250000
            }
        }
    
    def train_model(self):
        """Train cost forecasting model"""
        # Load or create dataset
        if os.path.exists(self.dataset_path):
            df = pd.read_csv(self.dataset_path)
        else:
            print("Creating synthetic cost dataset...")
            df = self._create_synthetic_dataset()
            os.makedirs(os.path.dirname(self.dataset_path), exist_ok=True)
            df.to_csv(self.dataset_path, index=False)
        
        # Prepare features
        self.treatment_encoder = LabelEncoder()
        self.hospital_encoder = LabelEncoder()
        
        df['treatment_encoded'] = self.treatment_encoder.fit_transform(df['treatment'])
        df['hospital_encoded'] = self.hospital_encoder.fit_transform(df['hospital_type'])
        
        X = df[['treatment_encoded', 'hospital_encoded']].values
        y = df['cost'].values
        
        # Train model
        self.model = RandomForestRegressor(n_estimators=100, random_state=42, max_depth=10)
        self.model.fit(X, y)
        
        # Calculate R² score
        score = self.model.score(X, y)
        print(f"Cost model trained successfully! R² Score: {score:.2%}")
        
        # Save model
        os.makedirs(os.path.dirname(self.model_path), exist_ok=True)
        joblib.dump({
            'model': self.model,
            'treatment_encoder': self.treatment_encoder,
            'hospital_encoder': self.hospital_encoder
        }, self.model_path)
        print(f"Cost model saved to {self.model_path}")
    
    def _create_synthetic_dataset(self):
        """Create synthetic treatment cost dataset"""
        rows = []
        
        # Base costs for different treatments
        base_costs = {
            'Appendectomy': 30000,
            'Surgery': 40000,
            'Medication': 8000,
            'Physical Therapy': 5000,
            'Chemotherapy': 100000,
            'Cardiac Surgery': 200000,
            'Orthopedic Surgery': 80000,
            'Dental Surgery': 15000,
            'Eye Surgery': 25000,
            'Gynecological Surgery': 35000
        }
        
        # Hospital type multipliers
        multipliers = {
            'government': 0.3,
            'semi-private': 1.0,
            'private': 2.0
        }
        
        # Generate synthetic data
        for treatment, base_cost in base_costs.items():
            for hospital_type, multiplier in multipliers.items():
                # Add some variation
                cost = base_cost * multiplier * np.random.uniform(0.8, 1.2)
                rows.append({
                    'treatment': treatment,
                    'hospital_type': hospital_type,
                    'cost': int(cost)
                })
        
        return pd.DataFrame(rows)
    
    def load_model(self):
        """Load trained cost model"""
        model_data = joblib.load(self.model_path)
        self.model = model_data['model']
        self.treatment_encoder = model_data['treatment_encoder']
        self.hospital_encoder = model_data['hospital_encoder']
        print(f"Cost model loaded from {self.model_path}")
    
    def estimate_cost(self, treatment_type: str, hospital_type: str) -> dict:
        """
        Estimate cost for a treatment
        Args:
            treatment_type: Name of treatment/surgery
            hospital_type: 'government', 'semi-private', or 'private'
        Returns:
            Dictionary with cost estimation details
        """
        # Normalize hospital type
        hospital_type = hospital_type.lower().replace(' ', '-')
        if hospital_type not in ['government', 'semi-private', 'private']:
            hospital_type = 'private'
        
        # Try to get cost from lookup table first
        treatment_lower = treatment_type.lower()
        estimated_cost = None
        
        for treatment, costs in self.treatment_costs.items():
            if treatment.lower() in treatment_lower or treatment_lower in treatment.lower():
                estimated_cost = costs.get(hospital_type)
                if estimated_cost:
                    break
        
        # If not found, use model prediction
        if estimated_cost is None:
            try:
                # Encode treatment and hospital type
                if treatment_type in self.treatment_encoder.classes_:
                    treatment_encoded = self.treatment_encoder.transform([treatment_type])[0]
                else:
                    # Use average of similar treatments
                    treatment_encoded = 0
                
                if hospital_type in self.hospital_encoder.classes_:
                    hospital_encoded = self.hospital_encoder.transform([hospital_type])[0]
                else:
                    hospital_encoded = 1  # Default to semi-private
                
                # Predict cost
                predicted_cost = self.model.predict([[treatment_encoded, hospital_encoded]])[0]
                estimated_cost = max(5000, int(predicted_cost))  # Minimum 5000
            except:
                # Fallback to default estimation
                estimated_cost = 50000 if hospital_type == 'private' else 20000
        
        # Calculate min/max range (20% variation)
        min_cost = int(estimated_cost * 0.8)
        max_cost = int(estimated_cost * 1.2)
        
        # Format hospital type for display
        hospital_type_display = hospital_type.replace('-', ' ').title()
        if hospital_type == 'semi-private':
            hospital_type_display = 'Semi-Private Hospital'
        elif hospital_type == 'government':
            hospital_type_display = 'Government Hospital'
        else:
            hospital_type_display = 'Private Hospital'
        
        return {
            "treatmentName": treatment_type,
            "hospitalType": hospital_type_display,
            "averageCost": estimated_cost,
            "minCost": min_cost,
            "maxCost": max_cost,
            "currency": "INR",
            "factors": [
                {
                    "name": "Hospital Location",
                    "impact": "Costs vary significantly by city and region. Metropolitan areas typically have higher costs."
                },
                {
                    "name": "Doctor Experience",
                    "impact": "Senior doctors and specialists charge higher consultation and procedure fees."
                },
                {
                    "name": "Facility Type",
                    "impact": "Private hospitals generally cost 2-3x more than government hospitals. Semi-private offers a middle ground."
                },
                {
                    "name": "Insurance Coverage",
                    "impact": "Health insurance can significantly reduce out-of-pocket expenses. Check your policy coverage."
                },
                {
                    "name": "Additional Services",
                    "impact": "Post-operative care, medications, and follow-up visits add to the total cost."
                }
            ],
            "disclaimer": "These are estimated costs based on average data. Actual costs may vary significantly based on location, specific hospital, doctor fees, and individual circumstances. Please consult with the specific hospital for accurate pricing and check your insurance coverage."
        }
    
    def get_all_treatments(self) -> list:
        """Get all available treatments"""
        treatments = list(self.treatment_costs.keys())
        # Add some common treatments
        common_treatments = ['Surgery', 'Medication', 'Physical Therapy', 'Chemotherapy']
        all_treatments = list(set(treatments + common_treatments))
        return sorted(all_treatments)

