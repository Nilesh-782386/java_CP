"""
ML-Enhanced Test Optimization Module
Recommends medical tests for diseases using ML-based similarity matching
"""

import json
import os
import re
import numpy as np
from typing import Dict, List
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

# Download required NLTK data
try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt', quiet=True)

try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords', quiet=True)

try:
    nltk.data.find('corpora/wordnet')
except LookupError:
    nltk.download('wordnet', quiet=True)


class TestOptimizer:
    """ML-enhanced test optimizer using TF-IDF and cosine similarity"""
    
    def __init__(self, test_mapping_path='datasets/test_mapping.json'):
        self.test_mapping_path = test_mapping_path
        self.test_mapping = {}
        self.vectorizer = None
        self.disease_vectors = None
        self.disease_names = []
        self.lemmatizer = WordNetLemmatizer()
        self.stop_words = set(stopwords.words('english'))
        self.load_test_mapping()
        self._initialize_ml_models()
    
    def _preprocess_text(self, text: str) -> str:
        """Preprocess text for ML matching"""
        text = text.lower()
        text = re.sub(r'[^a-zA-Z\s]', '', text)
        tokens = word_tokenize(text)
        tokens = [
            self.lemmatizer.lemmatize(token)
            for token in tokens
            if token not in self.stop_words and len(token) > 2
        ]
        return ' '.join(tokens)
    
    def _initialize_ml_models(self):
        """Initialize TF-IDF vectorizer for disease matching"""
        disease_texts = []
        for disease_id, data in self.test_mapping.items():
            if disease_id != "default":
                disease_name = data.get("diseaseName", "")
                reasoning = data.get("reasoning", "")
                combined_text = f"{disease_name} {reasoning}"
                processed_text = self._preprocess_text(combined_text)
                disease_texts.append(processed_text)
                self.disease_names.append(disease_id)
        
        if disease_texts:
            self.vectorizer = TfidfVectorizer(
                max_features=1000,
                ngram_range=(1, 2),
                min_df=1,
                max_df=0.95
            )
            self.disease_vectors = self.vectorizer.fit_transform(disease_texts)
            print(f"✅ ML models initialized: {len(disease_texts)} diseases vectorized for test recommendations")
    
    def load_test_mapping(self):
        """Load disease-to-test mapping"""
        if os.path.exists(self.test_mapping_path):
            with open(self.test_mapping_path, 'r', encoding='utf-8') as f:
                self.test_mapping = json.load(f)
        else:
            print("Creating comprehensive test mapping with ML support...")
            self.test_mapping = self._create_comprehensive_mapping()
            os.makedirs(os.path.dirname(self.test_mapping_path), exist_ok=True)
            with open(self.test_mapping_path, 'w', encoding='utf-8') as f:
                json.dump(self.test_mapping, f, indent=2, ensure_ascii=False)
    
    def _create_comprehensive_mapping(self):
        """Create comprehensive ML-enhanced disease-to-test mapping for 50+ diseases"""
        return {
            "1": {  # Common Cold
                "diseaseName": "Common Cold",
                "reasoning": "For uncomplicated colds, minimal testing is needed. Tests help rule out complications like bacterial infections.",
                "recommendedTests": [
                    {
                        "id": "1",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Measures various components of blood including white blood cells",
                        "purpose": "To check for signs of bacterial infection or complications",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "2",
                        "name": "Throat Swab Culture",
                        "description": "Tests for bacterial infections like strep throat",
                        "purpose": "To rule out bacterial infections if symptoms persist",
                        "costRange": "₹400 - ₹600",
                        "preparation": "No preparation needed"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "3",
                        "name": "Chest X-Ray",
                        "description": "Imaging test for chest conditions",
                        "purpose": "Not needed for uncomplicated colds without chest symptoms",
                        "costRange": "₹800 - ₹1200"
                    }
                ]
            },
            "2": {  # Influenza
                "diseaseName": "Influenza",
                "reasoning": "Flu testing helps confirm diagnosis and guide treatment, especially for high-risk patients.",
                "recommendedTests": [
                    {
                        "id": "4",
                        "name": "Rapid Influenza Test",
                        "description": "Quick test to detect influenza virus",
                        "purpose": "To confirm flu diagnosis and guide antiviral treatment",
                        "costRange": "₹600 - ₹800",
                        "preparation": "Nasal or throat swab"
                    },
                    {
                        "id": "5",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Measures blood components",
                        "purpose": "To check for complications and monitor infection",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "6",
                        "name": "CT Scan",
                        "description": "Advanced imaging test",
                        "purpose": "Not necessary unless severe complications are suspected",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "3": {  # Migraine
                "diseaseName": "Migraine",
                "reasoning": "Migraines are typically diagnosed clinically. Tests help rule out other causes of headaches.",
                "recommendedTests": [
                    {
                        "id": "7",
                        "name": "MRI of Brain",
                        "description": "Imaging test to visualize brain structure",
                        "purpose": "To rule out other causes of headaches if symptoms are atypical",
                        "costRange": "₹4000 - ₹8000",
                        "preparation": "No preparation needed, but inform about metal implants"
                    },
                    {
                        "id": "8",
                        "name": "Blood Pressure Monitoring",
                        "description": "Monitoring blood pressure over time",
                        "purpose": "To check for hypertension as a contributing factor",
                        "costRange": "₹200 - ₹400",
                        "preparation": "No preparation needed"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "9",
                        "name": "Lumbar Puncture",
                        "description": "Spinal tap procedure",
                        "purpose": "Not needed for typical migraines",
                        "costRange": "₹2000 - ₹4000"
                    }
                ]
            },
            "4": {  # Diabetes
                "diseaseName": "Diabetes",
                "reasoning": "Diabetes requires comprehensive monitoring to manage blood sugar levels and prevent complications. Regular testing is essential for proper management.",
                "recommendedTests": [
                    {
                        "id": "11",
                        "name": "Fasting Blood Glucose (FBG)",
                        "description": "Measures blood sugar after 8+ hours of fasting",
                        "purpose": "Primary test for diabetes diagnosis and monitoring",
                        "costRange": "₹150 - ₹300",
                        "preparation": "Fast for 8-12 hours before test"
                    },
                    {
                        "id": "12",
                        "name": "HbA1c (Glycated Hemoglobin)",
                        "description": "Measures average blood sugar over 2-3 months",
                        "purpose": "Long-term diabetes control indicator, target <7%",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "13",
                        "name": "Lipid Profile",
                        "description": "Measures cholesterol and triglycerides",
                        "purpose": "Diabetes increases heart disease risk, monitor lipids",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fast for 12 hours"
                    },
                    {
                        "id": "14",
                        "name": "Kidney Function Test (Creatinine, eGFR)",
                        "description": "Measures kidney function",
                        "purpose": "Diabetes can damage kidneys, annual monitoring needed",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "15",
                        "name": "Urine Microalbumin",
                        "description": "Detects early kidney damage",
                        "purpose": "Early detection of diabetic nephropathy",
                        "costRange": "₹400 - ₹600",
                        "preparation": "First morning urine sample preferred"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "16",
                        "name": "CT Scan (unless complications)",
                        "description": "Advanced imaging",
                        "purpose": "Not routine for uncomplicated diabetes",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "5": {  # Hypertension
                "diseaseName": "Hypertension",
                "reasoning": "High blood pressure requires monitoring and tests to assess organ damage and cardiovascular risk.",
                "recommendedTests": [
                    {
                        "id": "17",
                        "name": "Blood Pressure Monitoring (24-hour)",
                        "description": "Continuous BP monitoring over 24 hours",
                        "purpose": "Accurate diagnosis and treatment response",
                        "costRange": "₹2000 - ₹3500",
                        "preparation": "Normal daily activities"
                    },
                    {
                        "id": "18",
                        "name": "ECG (Electrocardiogram)",
                        "description": "Heart electrical activity test",
                        "purpose": "Check for heart damage from hypertension",
                        "costRange": "₹300 - ₹500",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "19",
                        "name": "Echocardiogram",
                        "description": "Ultrasound of heart",
                        "purpose": "Assess heart function and structure",
                        "costRange": "₹1500 - ₹2500",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "20",
                        "name": "Kidney Function Test",
                        "description": "Measures kidney health",
                        "purpose": "Hypertension can damage kidneys",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "21",
                        "name": "Lipid Profile",
                        "description": "Cholesterol and triglyceride levels",
                        "purpose": "Assess cardiovascular risk",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fast for 12 hours"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "22",
                        "name": "CT Angiography (routine)",
                        "description": "Advanced heart imaging",
                        "purpose": "Only if specific complications suspected",
                        "costRange": "₹8000 - ₹15000"
                    }
                ]
            },
            "6": {  # Asthma
                "diseaseName": "Asthma",
                "reasoning": "Asthma diagnosis and monitoring requires lung function tests and allergy testing to identify triggers.",
                "recommendedTests": [
                    {
                        "id": "23",
                        "name": "Spirometry (PFT)",
                        "description": "Measures lung function and airflow",
                        "purpose": "Primary test for asthma diagnosis and monitoring",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Avoid bronchodilators 4-6 hours before"
                    },
                    {
                        "id": "24",
                        "name": "Peak Flow Meter Test",
                        "description": "Measures maximum airflow rate",
                        "purpose": "Daily monitoring of asthma control at home",
                        "costRange": "₹200 - ₹400",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "25",
                        "name": "Allergy Skin Test",
                        "description": "Tests for allergic triggers",
                        "purpose": "Identify asthma triggers (pollen, dust, etc.)",
                        "costRange": "₹800 - ₹1200",
                        "preparation": "Stop antihistamines 3-7 days before"
                    },
                    {
                        "id": "26",
                        "name": "Chest X-Ray",
                        "description": "Imaging of lungs and chest",
                        "purpose": "Rule out other lung conditions",
                        "costRange": "₹800 - ₹1200",
                        "preparation": "No preparation needed"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "27",
                        "name": "CT Scan (routine)",
                        "description": "Advanced chest imaging",
                        "purpose": "Not needed for routine asthma unless complications",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "7": {  # Pneumonia
                "diseaseName": "Pneumonia",
                "reasoning": "Pneumonia requires imaging to confirm diagnosis and identify the causative organism.",
                "recommendedTests": [
                    {
                        "id": "28",
                        "name": "Chest X-Ray",
                        "description": "Imaging to visualize lung infection",
                        "purpose": "Confirm pneumonia diagnosis and assess severity",
                        "costRange": "₹800 - ₹1200",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "29",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Blood cell count analysis",
                        "purpose": "Check for infection markers (elevated WBC)",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "30",
                        "name": "Sputum Culture",
                        "description": "Tests for bacterial infection",
                        "purpose": "Identify causative bacteria for targeted antibiotics",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Deep cough sample, morning sample preferred"
                    },
                    {
                        "id": "31",
                        "name": "Blood Culture",
                        "description": "Tests blood for bacterial infection",
                        "purpose": "Detect bacteremia (bacteria in blood)",
                        "costRange": "₹600 - ₹900",
                        "preparation": "Before starting antibiotics if possible"
                    },
                    {
                        "id": "32",
                        "name": "CRP (C-Reactive Protein)",
                        "description": "Inflammation marker",
                        "purpose": "Assess severity of infection",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "33",
                        "name": "CT Scan (unless complications)",
                        "description": "Advanced imaging",
                        "purpose": "X-ray usually sufficient, CT only if complications",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "8": {  # Heart Disease
                "diseaseName": "Heart Disease",
                "reasoning": "Heart disease requires comprehensive cardiac evaluation including imaging, stress tests, and blood markers.",
                "recommendedTests": [
                    {
                        "id": "34",
                        "name": "ECG (Electrocardiogram)",
                        "description": "Heart electrical activity",
                        "purpose": "Detect irregular heart rhythms and damage",
                        "costRange": "₹300 - ₹500",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "35",
                        "name": "Echocardiogram",
                        "description": "Heart ultrasound",
                        "purpose": "Assess heart structure and function",
                        "costRange": "₹1500 - ₹2500",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "36",
                        "name": "Stress Test (TMT)",
                        "description": "Exercise ECG",
                        "purpose": "Assess heart function under stress",
                        "costRange": "₹2000 - ₹3500",
                        "preparation": "Wear comfortable clothes, avoid heavy meals"
                    },
                    {
                        "id": "37",
                        "name": "Lipid Profile",
                        "description": "Cholesterol and triglycerides",
                        "purpose": "Assess cardiovascular risk factors",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fast for 12 hours"
                    },
                    {
                        "id": "38",
                        "name": "Troponin Test",
                        "description": "Cardiac enzyme marker",
                        "purpose": "Detect heart muscle damage (heart attack)",
                        "costRange": "₹800 - ₹1200",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "39",
                        "name": "Coronary Angiography (routine)",
                        "description": "Invasive heart imaging",
                        "purpose": "Only if other tests indicate blockage",
                        "costRange": "₹15000 - ₹30000"
                    }
                ]
            },
            "9": {  # Thyroid Disorder
                "diseaseName": "Thyroid Disorder",
                "reasoning": "Thyroid disorders require hormone level testing and sometimes imaging to assess thyroid function.",
                "recommendedTests": [
                    {
                        "id": "40",
                        "name": "TSH (Thyroid Stimulating Hormone)",
                        "description": "Primary thyroid function test",
                        "purpose": "Screen for thyroid dysfunction",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "41",
                        "name": "Free T4 (Thyroxine)",
                        "description": "Active thyroid hormone",
                        "purpose": "Assess thyroid hormone production",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "42",
                        "name": "Free T3 (Triiodothyronine)",
                        "description": "Active thyroid hormone",
                        "purpose": "Complete thyroid function assessment",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "43",
                        "name": "Thyroid Ultrasound",
                        "description": "Imaging of thyroid gland",
                        "purpose": "Check for nodules, enlargement, or abnormalities",
                        "costRange": "₹800 - ₹1500",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "44",
                        "name": "Anti-TPO Antibodies",
                        "description": "Autoimmune marker",
                        "purpose": "Detect autoimmune thyroid disease",
                        "costRange": "₹600 - ₹900",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "45",
                        "name": "CT Scan (unless specific indication)",
                        "description": "Advanced imaging",
                        "purpose": "Ultrasound is usually sufficient",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "10": {  # Kidney Disease
                "diseaseName": "Kidney Disease",
                "reasoning": "Kidney disease requires comprehensive renal function testing and imaging to assess damage and function.",
                "recommendedTests": [
                    {
                        "id": "46",
                        "name": "Serum Creatinine",
                        "description": "Kidney function marker",
                        "purpose": "Primary test for kidney function",
                        "costRange": "₹200 - ₹400",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "47",
                        "name": "eGFR (Estimated Glomerular Filtration Rate)",
                        "description": "Calculated kidney function",
                        "purpose": "Assess overall kidney filtering capacity",
                        "costRange": "₹200 - ₹400",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "48",
                        "name": "Urine Protein/Creatinine Ratio",
                        "description": "Detects protein leakage",
                        "purpose": "Detect kidney damage (proteinuria)",
                        "costRange": "₹300 - ₹500",
                        "preparation": "First morning urine sample"
                    },
                    {
                        "id": "49",
                        "name": "Kidney Ultrasound",
                        "description": "Imaging of kidneys",
                        "purpose": "Check kidney size, structure, and blockages",
                        "costRange": "₹800 - ₹1500",
                        "preparation": "Drink water 1 hour before (full bladder)"
                    },
                    {
                        "id": "50",
                        "name": "Complete Urine Analysis",
                        "description": "Comprehensive urine test",
                        "purpose": "Detect blood, protein, infection in urine",
                        "costRange": "₹200 - ₹400",
                        "preparation": "Mid-stream urine sample"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "51",
                        "name": "CT Scan with Contrast (unless indicated)",
                        "description": "Advanced imaging with dye",
                        "purpose": "Contrast can damage kidneys, use cautiously",
                        "costRange": "₹5000 - ₹8000"
                    }
                ]
            },
            "11": {  # Sinusitis
                "diseaseName": "Sinusitis",
                "reasoning": "Sinusitis requires imaging to confirm inflammation and identify whether it's bacterial or viral.",
                "recommendedTests": [
                    {
                        "id": "52",
                        "name": "Sinus X-Ray or CT Scan",
                        "description": "Imaging of sinuses",
                        "purpose": "Visualize sinus inflammation and blockage",
                        "costRange": "₹800 - ₹1500 (X-Ray) or ₹3000 - ₹5000 (CT)",
                        "preparation": "No preparation needed"
                    },
                    {
                        "id": "53",
                        "name": "Nasal Endoscopy",
                        "description": "Visual examination of nasal passages",
                        "purpose": "Check for polyps, inflammation, or structural issues",
                        "costRange": "₹1500 - ₹2500",
                        "preparation": "Topical anesthesia may be used"
                    },
                    {
                        "id": "54",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Blood cell analysis",
                        "purpose": "Check for signs of bacterial infection",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "55",
                        "name": "MRI (unless complications)",
                        "description": "Advanced imaging",
                        "purpose": "CT or X-Ray usually sufficient for sinusitis",
                        "costRange": "₹5000 - ₹8000"
                    }
                ]
            },
            "12": {  # Gastroenteritis
                "diseaseName": "Gastroenteritis",
                "reasoning": "Gastroenteritis requires stool tests to identify the causative organism and assess dehydration.",
                "recommendedTests": [
                    {
                        "id": "56",
                        "name": "Stool Culture",
                        "description": "Tests for bacterial infection in stool",
                        "purpose": "Identify causative bacteria (E.coli, Salmonella, etc.)",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Fresh stool sample in sterile container"
                    },
                    {
                        "id": "57",
                        "name": "Stool Ova and Parasites",
                        "description": "Tests for parasites",
                        "purpose": "Detect parasitic infections",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Stool sample"
                    },
                    {
                        "id": "58",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Blood analysis",
                        "purpose": "Check for infection and dehydration markers",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "59",
                        "name": "Electrolyte Panel",
                        "description": "Measures sodium, potassium, chloride",
                        "purpose": "Assess dehydration and electrolyte imbalance",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "60",
                        "name": "CT Scan (unless severe complications)",
                        "description": "Advanced imaging",
                        "purpose": "Not needed for uncomplicated gastroenteritis",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "13": {  # Urinary Tract Infection
                "diseaseName": "Urinary Tract Infection",
                "reasoning": "UTI requires urine tests to confirm infection and identify the causative bacteria.",
                "recommendedTests": [
                    {
                        "id": "61",
                        "name": "Urine Analysis (Urinalysis)",
                        "description": "Comprehensive urine test",
                        "purpose": "Detect white blood cells, bacteria, nitrites",
                        "costRange": "₹200 - ₹400",
                        "preparation": "Mid-stream clean catch urine sample"
                    },
                    {
                        "id": "62",
                        "name": "Urine Culture and Sensitivity",
                        "description": "Identifies bacteria and tests antibiotic sensitivity",
                        "purpose": "Confirm UTI and guide antibiotic treatment",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Clean catch urine sample"
                    },
                    {
                        "id": "63",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Blood analysis",
                        "purpose": "Check for systemic infection (if fever present)",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "64",
                        "name": "CT Scan (unless complications)",
                        "description": "Advanced imaging",
                        "purpose": "Not needed for uncomplicated UTI",
                        "costRange": "₹3000 - ₹5000"
                    }
                ]
            },
            "14": {  # Anemia
                "diseaseName": "Anemia",
                "reasoning": "Anemia requires blood tests to identify the type and cause, which guides treatment.",
                "recommendedTests": [
                    {
                        "id": "65",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Comprehensive blood cell analysis",
                        "purpose": "Primary test for anemia diagnosis (hemoglobin, hematocrit, RBC count)",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "66",
                        "name": "Iron Studies (Serum Iron, TIBC, Ferritin)",
                        "description": "Measures iron levels and storage",
                        "purpose": "Identify iron deficiency anemia (most common type)",
                        "costRange": "₹600 - ₹900",
                        "preparation": "Fasting recommended"
                    },
                    {
                        "id": "67",
                        "name": "Vitamin B12 and Folate Levels",
                        "description": "Measures B12 and folic acid",
                        "purpose": "Detect B12 or folate deficiency anemia",
                        "costRange": "₹800 - ₹1200",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "68",
                        "name": "Reticulocyte Count",
                        "description": "Measures young red blood cells",
                        "purpose": "Assess bone marrow function and response",
                        "costRange": "₹400 - ₹600",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "69",
                        "name": "Bone Marrow Biopsy (unless indicated)",
                        "description": "Invasive procedure",
                        "purpose": "Only if severe or unexplained anemia",
                        "costRange": "₹5000 - ₹8000"
                    }
                ]
            },
            "15": {  # Liver Disease
                "diseaseName": "Liver Disease",
                "reasoning": "Liver disease requires comprehensive liver function tests and imaging to assess damage and function.",
                "recommendedTests": [
                    {
                        "id": "70",
                        "name": "Liver Function Tests (LFT)",
                        "description": "ALT, AST, ALP, Bilirubin, Albumin",
                        "purpose": "Primary test for liver function and damage",
                        "costRange": "₹500 - ₹800",
                        "preparation": "Fasting recommended"
                    },
                    {
                        "id": "71",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Blood cell analysis",
                        "purpose": "Check for bleeding disorders (low platelets)",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    },
                    {
                        "id": "72",
                        "name": "Liver Ultrasound",
                        "description": "Imaging of liver",
                        "purpose": "Check liver size, structure, and detect fatty liver or cirrhosis",
                        "costRange": "₹800 - ₹1500",
                        "preparation": "Fast for 6-8 hours before"
                    },
                    {
                        "id": "73",
                        "name": "Hepatitis Panel",
                        "description": "Tests for hepatitis A, B, C",
                        "purpose": "Identify viral hepatitis as cause",
                        "costRange": "₹1000 - ₹1500",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": [
                    {
                        "id": "74",
                        "name": "Liver Biopsy (unless indicated)",
                        "description": "Invasive procedure",
                        "purpose": "Only if other tests are inconclusive",
                        "costRange": "₹10000 - ₹20000"
                    }
                ]
            },
            "default": {
                "diseaseName": "General Condition",
                "reasoning": "Consult with a healthcare provider to determine appropriate tests based on your specific symptoms and medical history.",
                "recommendedTests": [
                    {
                        "id": "10",
                        "name": "Complete Blood Count (CBC)",
                        "description": "Basic blood test measuring various components",
                        "purpose": "To check overall health and detect various conditions",
                        "costRange": "₹300 - ₹500",
                        "preparation": "Fasting not required"
                    }
                ],
                "testsToAvoid": []
            }
        }
    
    def get_test_recommendations(self, disease_id: str) -> dict:
        """
        Get ML-enhanced test recommendations for a disease
        Uses similarity matching if exact match not found
        Args:
            disease_id: Disease ID
        Returns:
            Dictionary with recommended tests and tests to avoid
        """
        # First try exact match
        if disease_id in self.test_mapping:
            recommendations = self.test_mapping[disease_id]
        else:
            # ML-based fallback: try to find similar disease
            recommendations = self._ml_based_recommendation(disease_id)
        
        return {
            "diseaseId": disease_id,
            "diseaseName": recommendations.get("diseaseName", "Unknown Disease"),
            "reasoning": recommendations.get("reasoning", "Consult with a healthcare provider for appropriate tests."),
            "recommendedTests": recommendations.get("recommendedTests", []),
            "testsToAvoid": recommendations.get("testsToAvoid", [])
        }
    
    def _ml_based_recommendation(self, disease_id: str) -> dict:
        """Use ML similarity to find best matching disease for test recommendations"""
        # Try to get disease name from symptom predictor
        try:
            from symptom_predictor import SymptomPredictor
            predictor = SymptomPredictor()
            diseases = predictor.get_all_diseases()
            target_disease = None
            for disease in diseases:
                if disease.get('id') == disease_id:
                    target_disease = disease.get('name', '')
                    break
            
            if target_disease and self.vectorizer and self.disease_vectors is not None:
                # Preprocess target disease name
                processed_query = self._preprocess_text(target_disease)
                query_vector = self.vectorizer.transform([processed_query])
                
                # Calculate similarity
                similarities = cosine_similarity(query_vector, self.disease_vectors)[0]
                best_match_idx = np.argmax(similarities)
                best_score = similarities[best_match_idx]
                
                # If good match found (threshold: 0.3)
                if best_score > 0.3:
                    matched_disease_id = self.disease_names[best_match_idx]
                    recommendations = self.test_mapping.get(matched_disease_id, {})
                    if recommendations:
                        # Modify reasoning to indicate ML-based matching
                        recommendations = recommendations.copy()
                        recommendations["reasoning"] = f"Based on similarity to {recommendations.get('diseaseName', 'similar condition')}. " + recommendations.get("reasoning", "")
                        return recommendations
        except Exception as e:
            print(f"ML matching failed: {e}")
        
        # Fallback to default
        return self.test_mapping.get("default", {})

