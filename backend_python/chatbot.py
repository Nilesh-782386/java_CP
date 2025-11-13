"""
Medical Chatbot Module with ML Enhancement
ML-based chatbot using TF-IDF vectorization and cosine similarity for better text matching
"""

import json
import os
import re
import numpy as np
from typing import Dict, List, Tuple
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

# Download required NLTK data (only once)
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


class MedicalChatbot:
    """ML-enhanced medical chatbot using TF-IDF and cosine similarity"""
    
    def __init__(self, knowledge_base_path='datasets/medical_faq.json'):
        self.knowledge_base_path = knowledge_base_path
        self.knowledge_base = []
        self.vectorizer = None
        self.kb_vectors = None
        self.lemmatizer = WordNetLemmatizer()
        self.stop_words = set(stopwords.words('english'))
        self.load_knowledge_base()
        self._initialize_ml_models()
    
    def load_knowledge_base(self):
        """Load medical FAQ knowledge base"""
        if os.path.exists(self.knowledge_base_path):
            with open(self.knowledge_base_path, 'r', encoding='utf-8') as f:
                self.knowledge_base = json.load(f)
        else:
            print("Creating default knowledge base...")
            self.knowledge_base = self._create_default_kb()
            os.makedirs(os.path.dirname(self.knowledge_base_path), exist_ok=True)
            with open(self.knowledge_base_path, 'w', encoding='utf-8') as f:
                json.dump(self.knowledge_base, f, indent=2, ensure_ascii=False)
        
        # Expand knowledge base with more entries
        if len(self.knowledge_base) < 50:
            self.knowledge_base.extend(self._get_extended_kb())
    
    def _preprocess_text(self, text: str) -> str:
        """Preprocess text: lowercase, remove special chars, lemmatize"""
        # Convert to lowercase
        text = text.lower()
        
        # Remove special characters and digits
        text = re.sub(r'[^a-zA-Z\s]', '', text)
        
        # Tokenize
        tokens = word_tokenize(text)
        
        # Remove stopwords and lemmatize
        tokens = [
            self.lemmatizer.lemmatize(token)
            for token in tokens
            if token not in self.stop_words and len(token) > 2
        ]
        
        return ' '.join(tokens)
    
    def _initialize_ml_models(self):
        """Initialize TF-IDF vectorizer and create knowledge base vectors"""
        # Prepare texts for vectorization (combine question + keywords + answer)
        texts = []
        for entry in self.knowledge_base:
            combined_text = f"{entry['question']} {' '.join(entry.get('keywords', []))} {entry['answer']}"
            processed_text = self._preprocess_text(combined_text)
            texts.append(processed_text)
        
        # Initialize TF-IDF vectorizer with better parameters
        self.vectorizer = TfidfVectorizer(
            max_features=5000,  # Limit vocabulary size for efficiency
            ngram_range=(1, 2),  # Use unigrams and bigrams
            min_df=1,  # Minimum document frequency
            max_df=0.95,  # Maximum document frequency (remove very common words)
            sublinear_tf=True  # Use sublinear TF scaling
        )
        
        # Fit and transform knowledge base
        self.kb_vectors = self.vectorizer.fit_transform(texts)
        print(f"✅ ML models initialized: {len(self.knowledge_base)} entries vectorized")
    
    def validate_question(self, user_message: str) -> Dict[str, any]:
        """
        Validate if question is within chatbot's scope and provide guidance
        Returns validation result with guidance
        """
        user_lower = user_message.lower()
        
        # Check for inappropriate questions (emergency, personal diagnosis, etc.)
        emergency_keywords = ["emergency", "dying", "suicide", "kill myself", "heart attack", "stroke", "severe pain"]
        if any(keyword in user_lower for keyword in emergency_keywords):
            return {
                "valid": False,
                "message": "⚠️ **EMERGENCY SITUATION DETECTED**\n\nThis appears to be a medical emergency. Please:\n• Call emergency services immediately (112 or 911)\n• Go to the nearest emergency room\n• Do not delay seeking professional medical help\n\nI cannot provide emergency medical advice. Please seek immediate professional medical attention.",
                "should_respond": False
            }
        
        # Check for personal diagnosis requests
        diagnosis_keywords = ["diagnose me", "what do i have", "do i have", "am i sick", "am i dying"]
        if any(keyword in user_lower for keyword in diagnosis_keywords):
            return {
                "valid": False,
                "message": "I cannot provide personal diagnoses. I can share general health information and education about symptoms, conditions, and treatments. For a proper diagnosis, please consult with a qualified healthcare provider who can examine you and review your medical history.",
                "should_respond": True  # Still respond but with guidance
            }
        
        # Check for prescription requests
        prescription_keywords = ["prescribe", "give me medication", "give me medicine", "need prescription"]
        if any(keyword in user_lower for keyword in prescription_keywords):
            return {
                "valid": False,
                "message": "I cannot prescribe medications. Medication prescriptions require a doctor's evaluation, medical history review, and consideration of potential interactions and side effects. Please consult with a licensed healthcare provider for prescriptions.",
                "should_respond": True
            }
        
        # Valid question types we can handle
        valid_categories = [
            "symptoms", "treatment", "prevention", "medicine", "medication", "disease",
            "condition", "health", "fever", "cough", "headache", "pain", "fatigue",
            "diabetes", "hypertension", "asthma", "allergy", "exercise", "diet",
            "vitamin", "supplement", "sleep", "stress", "immunity", "cure", "therapy",
            "lifestyle", "improve", "wellness", "healthy living", "better health"
        ]
        
        is_valid = any(category in user_lower for category in valid_categories)
        
        if not is_valid and len(user_message.strip()) < 10:
            return {
                "valid": False,
                "message": "Please provide a more detailed question. I can help with:\n• General health information\n• Symptoms and their meanings\n• Treatment options and medications\n• Preventive measures\n• Lifestyle and wellness advice\n\nPlease ask a specific health-related question.",
                "should_respond": True
            }
        
        return {
            "valid": True,
            "message": None,
            "should_respond": True
        }
    
    def get_response(self, user_message: str) -> Dict[str, any]:
        """
        Get chatbot response using ML-based similarity matching with question validation
        Args:
            user_message: User's question
        Returns:
            Dictionary with answer, related topics, and validation info
        """
        # Validate question first
        validation = self.validate_question(user_message)
        
        if not validation["should_respond"]:
            return {
                "answer": validation["message"],
                "relatedTopics": [],
                "confidence": 0.0,
                "questionValid": False
            }
        
        if not validation["valid"] and validation["message"]:
            # Return guidance but also try to help
            guidance = validation["message"] + "\n\n"
        else:
            guidance = ""
        
        # Preprocess user message
        processed_message = self._preprocess_text(user_message)
        
        # Vectorize user message
        user_vector = self.vectorizer.transform([processed_message])
        
        # Calculate cosine similarity with all KB entries
        similarities = cosine_similarity(user_vector, self.kb_vectors)[0]
        
        # Get best match
        best_match_idx = np.argmax(similarities)
        best_score = similarities[best_match_idx]
        
        # Get top 3 matches for context
        top_indices = np.argsort(similarities)[-3:][::-1]
        top_scores = similarities[top_indices]
        
        # If good match found (threshold: 0.20 for ML to catch more lifestyle queries)
        if best_score > 0.20:
            best_match = self.knowledge_base[best_match_idx]
            
            # Enhance answer with context from similar entries if score is high
            enhanced_answer = best_match['answer']
            
            # If very high similarity, add related info
            if best_score > 0.6 and len(top_indices) > 1:
                # Add context from second best match if relevant
                second_match = self.knowledge_base[top_indices[1]]
                if top_scores[1] > 0.3 and second_match['category'] == best_match['category']:
                    enhanced_answer += f"\n\n**Related information:** {second_match['answer'][:150]}..."
            
            # Find related topics
            related_topics = self._find_related_topics(best_match['category'])
            
            return {
                "answer": guidance + enhanced_answer,
                "relatedTopics": related_topics,
                "confidence": float(best_score),
                "questionValid": validation["valid"]
            }
        else:
            # Fallback: Try keyword-based matching for very low confidence
            keyword_match = self._fallback_keyword_match(user_message)
            if keyword_match:
                keyword_match["answer"] = guidance + keyword_match["answer"]
                keyword_match["questionValid"] = validation["valid"]
                return keyword_match
            
            # Default response if no good match
            default_answer = guidance + "I understand you're asking about health. While I can provide general health information, I'm not a substitute for professional medical advice. For specific medical concerns, symptoms, or diagnoses, please consult with a qualified healthcare provider.\n\n**I can help you with:**\n• General health information and education\n• Common symptoms and their meanings\n• Treatment options and medications (general information)\n• Preventive measures and lifestyle advice\n• Questions about vitamins, supplements, and wellness\n\nPlease ask a specific health-related question, and I'll do my best to help! 💙"
            return {
                "answer": default_answer,
                "relatedTopics": ["General Health", "Symptoms", "Prevention", "Treatment"],
                "confidence": 0.0,
                "questionValid": validation["valid"]
            }
    
    def _fallback_keyword_match(self, user_message: str) -> Dict[str, any]:
        """Fallback to keyword matching if ML similarity is too low"""
        user_message_lower = user_message.lower()
        best_match = None
        best_score = 0
        
        for entry in self.knowledge_base:
            # Check keywords
            keywords = entry.get('keywords', [])
            score = 0
            matched_keywords = 0
            
            for keyword in keywords:
                keyword_lower = keyword.lower()
                # Exact match gets full score
                if keyword_lower in user_message_lower:
                    score += 1.0
                    matched_keywords += 1
                # Partial match (word boundary) gets partial score
                elif any(word in user_message_lower for word in keyword_lower.split()):
                    score += 0.5
                    matched_keywords += 1
            
            # Also check question text for matches
            question_lower = entry.get('question', '').lower()
            question_words = set(question_lower.split())
            user_words = set(user_message_lower.split())
            common_words = question_words.intersection(user_words)
            if len(common_words) > 0 and len(user_words) > 0:
                question_score = len(common_words) / max(len(user_words), 1)
                score += question_score * 0.3
            
            if keywords:
                # Normalize by number of keywords, but reward partial matches
                normalized_score = score / max(len(keywords), 1)
            else:
                normalized_score = score
            
            if normalized_score > best_score:
                best_score = normalized_score
                best_match = entry
        
        # Lower threshold to 0.2 to catch more lifestyle queries
        if best_match and best_score > 0.2:
            related_topics = self._find_related_topics(best_match['category'])
            return {
                "answer": best_match['answer'],
                "relatedTopics": related_topics,
                "confidence": float(best_score)
            }
        
        return None
    
    def _find_related_topics(self, category: str, limit: int = 3) -> List[str]:
        """Find related topics from the same category"""
        related = []
        for entry in self.knowledge_base:
            if entry['category'] == category and entry['question'] not in related:
                related.append(entry['question'])
                if len(related) >= limit:
                    break
        return related
    
    def _create_default_kb(self):
        """Create comprehensive medical FAQ knowledge base with medicine information"""
        return [
            {
                "category": "General Health",
                "keywords": ["fever", "temperature", "body temperature", "high temperature"],
                "question": "What is fever and how to treat it?",
                "answer": "Fever is a temporary increase in body temperature, often a sign that your body is fighting an infection. Normal body temperature is around 98.6°F (37°C).\n\n**Treatment:**\n• Rest and stay hydrated with water, herbal teas, or electrolyte solutions\n• Over-the-counter medications: Paracetamol (Acetaminophen) 500-1000mg every 4-6 hours OR Ibuprofen 200-400mg every 6-8 hours (for adults)\n• Lukewarm sponge bath can help reduce temperature\n• Wear light clothing and keep room temperature comfortable\n\n**Preventive Medicine:**\n• Maintain good hygiene to prevent infections\n• Get recommended vaccinations (flu, pneumonia)\n• Boost immunity with Vitamin C, Zinc supplements\n• Stay hydrated and eat nutritious foods\n\n⚠️ **Consult a doctor if:** Fever persists more than 3 days, temperature above 103°F (39.4°C), or accompanied by severe headache, rash, or difficulty breathing."
            },
            {
                "category": "General Health",
                "keywords": ["headache", "head pain", "migraine", "head ache"],
                "question": "What causes headaches and how to treat them?",
                "answer": "Headaches can be caused by stress, dehydration, lack of sleep, eye strain, sinus problems, or underlying medical conditions.\n\n**Treatment:**\n• Rest in a quiet, dark room\n• Apply cold or warm compress to forehead/neck\n• Stay hydrated - drink water\n• Over-the-counter medications:\n  - For tension headaches: Paracetamol 500-1000mg OR Ibuprofen 200-400mg\n  - For migraines: Paracetamol + Caffeine combination OR Aspirin 300-600mg\n  - For sinus headaches: Decongestants (Pseudoephedrine) + Pain relievers\n\n**Preventive Medicine:**\n• Regular sleep schedule (7-9 hours)\n• Magnesium supplements (200-400mg daily) may prevent migraines\n• Stress management techniques (meditation, yoga)\n• Regular exercise\n• Stay hydrated throughout the day\n• Avoid triggers: caffeine, alcohol, processed foods\n\n⚠️ **See a doctor if:** Headaches are severe, sudden, frequent, or accompanied by vision changes, fever, or neck stiffness."
            },
            {
                "category": "General Health",
                "keywords": ["cough", "coughing", "persistent cough", "dry cough"],
                "question": "How to treat a cough?",
                "answer": "Coughs can be caused by colds, allergies, or infections.\n\n**Treatment:**\n• Stay hydrated - drink warm water, herbal teas (honey + lemon), or warm broth\n• Use a humidifier or steam inhalation\n• Gargle with warm salt water\n• Over-the-counter medications:\n  - For dry cough: Dextromethorphan (Delsym, Robitussin) 10-20mg every 4-6 hours\n  - For productive cough: Guaifenesin (Mucinex) 200-400mg every 4 hours\n  - For cough with congestion: Combination of Dextromethorphan + Guaifenesin\n  - For allergic cough: Antihistamines (Cetirizine 10mg or Loratadine 10mg once daily)\n\n**Preventive Medicine:**\n• Avoid irritants: smoke, dust, allergens\n• Use air purifiers if prone to allergies\n• Vitamin C supplements (500-1000mg daily) during cold season\n• Zinc lozenges (10-15mg every 2-3 hours) when symptoms start\n• Maintain good hand hygiene\n• Consider annual flu vaccination\n\n⚠️ **Consult a doctor if:** Cough persists more than 2-3 weeks, produces blood, accompanied by fever, difficulty breathing, or chest pain."
            },
            {
                "category": "Symptoms",
                "keywords": ["nausea", "feeling sick", "vomiting", "queasy"],
                "question": "What causes nausea and how to treat it?",
                "answer": "Nausea can be caused by food poisoning, motion sickness, pregnancy, medications, or underlying medical conditions.\n\n**Treatment:**\n• **Home remedies:**\n  - Eat small, bland meals (crackers, toast, rice)\n  - Ginger: ginger tea, ginger candies, or ginger supplements (250-500mg)\n  - Peppermint tea\n  - Stay hydrated with small sips of clear liquids\n  - Avoid strong odors and fatty/spicy foods\n  - Rest in a well-ventilated room\n\n• **Over-the-counter medications:**\n  - Dimenhydrinate (Dramamine) 50-100mg for motion sickness\n  - Meclizine (Bonine) 25-50mg for motion sickness\n  - Bismuth subsalicylate (Pepto-Bismol) for stomach upset\n  - For severe cases: Prescription Ondansetron (Zofran) 4-8mg as needed\n\n**Preventive Medicine:**\n• For motion sickness: Take medication 30-60 mins before travel\n• Eat light meals before travel\n• Avoid alcohol and heavy meals\n• Stay hydrated\n• For morning sickness: Take prenatal vitamins with food\n\n⚠️ **See a doctor if:** Nausea persists more than 2 days, severe vomiting, signs of dehydration, or blood in vomit."
            },
            {
                "category": "Symptoms",
                "keywords": ["fatigue", "tiredness", "exhaustion", "feeling tired"],
                "question": "Why am I always tired?",
                "answer": "Fatigue can result from lack of sleep, poor diet, stress, anemia, thyroid problems, or other medical conditions. Ensure you get 7-9 hours of sleep, maintain a balanced diet, exercise regularly, and manage stress. If fatigue persists despite lifestyle changes, consult a healthcare provider."
            },
            {
                "category": "Prevention",
                "keywords": ["immunity", "immune system", "boost immunity", "strengthen immune"],
                "question": "How to boost immunity?",
                "answer": "To support your immune system: maintain a balanced diet rich in fruits and vegetables, get regular exercise, ensure adequate sleep (7-9 hours), manage stress, stay hydrated, avoid smoking, and maintain good hygiene practices like handwashing."
            },
            {
                "category": "Prevention",
                "keywords": ["vitamins", "supplements", "nutrition", "vitamin supplements"],
                "question": "What vitamins should I take?",
                "answer": "Most people can get necessary vitamins from a balanced diet. However, some may benefit from supplements like Vitamin D, B12, or multivitamins. It's best to consult with a healthcare provider before starting any supplements, as they can assess your specific needs and avoid potential interactions."
            },
            {
                "category": "Emergency",
                "keywords": ["chest pain", "heart attack", "emergency", "heart pain"],
                "question": "When to seek emergency care?",
                "answer": "Seek immediate emergency care for: severe chest pain, difficulty breathing, sudden severe headache, loss of consciousness, severe allergic reactions, or any life-threatening symptoms. Call emergency services immediately for these conditions."
            },
            {
                "category": "General Health",
                "keywords": ["cold", "common cold", "flu", "influenza"],
                "question": "Cold vs Flu and how to treat them?",
                "answer": "The common cold and flu are both respiratory illnesses but caused by different viruses. Flu symptoms are typically more severe.\n\n**Treatment for Common Cold:**\n• Rest and stay hydrated\n• Medications:\n  - Nasal congestion: Phenylephrine or Pseudoephedrine (decongestants)\n  - Runny nose: Antihistamines (Diphenhydramine 25-50mg or Cetirizine 10mg)\n  - Pain/fever: Paracetamol 500-1000mg or Ibuprofen 200-400mg\n  - Sore throat: Throat lozenges with Benzocaine or Menthol\n\n**Treatment for Flu:**\n• Antiviral medications (if started early): Oseltamivir (Tamiflu) 75mg twice daily for 5 days - **Prescription required**\n• Same symptomatic treatment as cold\n• More rest needed\n\n**Preventive Medicine:**\n• Annual flu vaccination (most important prevention)\n• Vitamin C (1000mg daily) and Zinc (10-15mg daily) supplements\n• Echinacea supplements (may reduce cold duration)\n• Hand hygiene and avoiding close contact with sick people\n• Maintain strong immunity through healthy diet and exercise\n\n⚠️ **See a doctor if:** Symptoms are severe, high fever, difficulty breathing, or symptoms worsen after initial improvement."
            },
            {
                "category": "General Health",
                "keywords": ["hypertension", "high blood pressure", "bp", "blood pressure"],
                "question": "What is high blood pressure and how to manage it?",
                "answer": "High blood pressure (hypertension) is when your BP is consistently above 130/80 mmHg. It's often called a 'silent killer' because it may not show symptoms.\n\n**Treatment (Prescription Required):**\nCommon medications prescribed by doctors:\n• ACE Inhibitors: Lisinopril, Enalapril\n• ARBs: Losartan, Valsartan\n• Beta-blockers: Atenolol, Metoprolol\n• Diuretics: Hydrochlorothiazide, Furosemide\n• Calcium Channel Blockers: Amlodipine, Diltiazem\n\n**Preventive Medicine & Lifestyle:**\n• Reduce sodium intake (less than 2g/day)\n• Maintain healthy weight (BMI 18.5-25)\n• Regular exercise (30 mins daily)\n• DASH diet: fruits, vegetables, whole grains, low-fat dairy\n• Limit alcohol (1-2 drinks/day max)\n• Stop smoking\n• Stress management\n• Supplements: Omega-3 fatty acids, CoQ10 (may help, consult doctor)\n• Regular BP monitoring at home\n\n⚠️ **Important:** Never self-medicate for hypertension. Always consult a doctor for proper diagnosis and medication. Regular checkups are essential."
            }
        ]
    
    def _get_extended_kb(self):
        """Extended knowledge base with more medical topics"""
        return [
            {
                "category": "Symptoms",
                "keywords": ["diabetes", "blood sugar", "diabetic", "sugar level"],
                "question": "What are the symptoms of diabetes and how to manage it?",
                "answer": "Diabetes symptoms include increased thirst, frequent urination, extreme fatigue, blurred vision, slow-healing wounds, and unexplained weight loss. Type 1 diabetes often develops quickly, while Type 2 may develop gradually.\n\n**Treatment (Prescription Required):**\n• Type 1: Insulin therapy (various types: rapid-acting, long-acting)\n• Type 2: Oral medications:\n  - Metformin (first-line treatment) 500-2000mg daily\n  - Sulfonylureas: Glipizide, Glyburide\n  - DPP-4 inhibitors: Sitagliptin, Linagliptin\n  - GLP-1 agonists: Liraglutide, Semaglutide\n  - SGLT2 inhibitors: Empagliflozin, Canagliflozin\n• Insulin may be needed in advanced Type 2\n\n**Preventive Medicine & Management:**\n• Monitor blood sugar regularly (target: 80-130 mg/dL fasting)\n• HbA1c target: <7%\n• Maintain healthy weight (BMI 18.5-25)\n• Low-carb, high-fiber diet\n• Regular exercise (150 mins/week)\n• Foot care (check daily for wounds)\n• Supplements: Alpha-lipoic acid, Chromium (consult doctor)\n• Annual eye exams and kidney function tests\n\n⚠️ **Critical:** Diabetes requires medical supervision. Never skip medications. Follow doctor's prescription exactly. Monitor blood sugar as advised."
            },
            {
                "category": "Symptoms",
                "keywords": ["asthma", "breathing difficulty", "wheezing", "shortness of breath"],
                "question": "What are asthma symptoms and how to treat them?",
                "answer": "Asthma symptoms include wheezing, coughing (especially at night), shortness of breath, and chest tightness. Triggers include allergens, exercise, cold air, or stress.\n\n**Treatment (Prescription Required):**\n• **Quick-relief inhalers (rescue):**\n  - Short-acting beta-agonists: Albuterol (Ventolin) 2-4 puffs as needed\n  - Anticholinergics: Ipratropium (Atrovent)\n\n• **Long-term control medications:**\n  - Inhaled corticosteroids: Fluticasone, Budesonide (daily)\n  - Long-acting beta-agonists: Salmeterol (combined with corticosteroids)\n  - Leukotriene modifiers: Montelukast 10mg daily\n  - Theophylline (oral, less common now)\n\n**Preventive Medicine:**\n• Avoid triggers: dust, pollen, smoke, pet dander\n• Use air purifiers and maintain clean indoor air\n• Annual flu vaccination\n• Pneumococcal vaccination\n• Regular exercise (swimming is excellent)\n• Maintain healthy weight\n• Monitor peak flow readings\n• Keep rescue inhaler always accessible\n\n⚠️ **Emergency:** If symptoms worsen rapidly or inhaler doesn't help, seek immediate medical care. Use rescue inhaler as prescribed."
            },
            {
                "category": "Prevention",
                "keywords": ["exercise", "physical activity", "workout", "fitness"],
                "question": "What are the benefits of regular exercise?",
                "answer": "Regular exercise benefits include improved cardiovascular health, stronger muscles and bones, better mental health, weight management, improved sleep, increased energy, and reduced risk of chronic diseases. Aim for at least 150 minutes of moderate-intensity exercise per week."
            },
            {
                "category": "Prevention",
                "keywords": ["cholesterol", "high cholesterol", "cholesterol level", "ldl"],
                "question": "How to manage high cholesterol?",
                "answer": "Adults should have cholesterol checked every 4-6 years starting at age 20. Target: LDL <100 mg/dL, HDL >40 mg/dL (men) or >50 mg/dL (women), Total <200 mg/dL.\n\n**Treatment (Prescription Required):**\n• **Statins (primary treatment):**\n  - Atorvastatin 10-80mg daily\n  - Simvastatin 10-40mg daily\n  - Rosuvastatin 5-40mg daily\n• **Other options:**\n  - Ezetimibe (cholesterol absorption inhibitor)\n  - Bile acid sequestrants: Cholestyramine\n  - PCSK9 inhibitors (for severe cases)\n\n**Preventive Medicine & Lifestyle:**\n• Heart-healthy diet: low saturated/trans fats, high fiber\n• Foods: Oats, fish (omega-3), nuts, olive oil, avocados\n• Limit: red meat, full-fat dairy, processed foods\n• Regular exercise (30-45 mins, 5 days/week)\n• Maintain healthy weight\n• Supplements: Omega-3 fatty acids (1000-2000mg daily), Plant sterols/stanols\n• Quit smoking\n• Limit alcohol\n\n⚠️ **Important:** Statins require regular monitoring of liver function. Follow doctor's prescription. Never stop statins without consulting doctor."
            },
            {
                "category": "General Health",
                "keywords": ["sore throat", "throat pain", "throat infection", "throat ache"],
                "question": "What causes sore throat and how to treat it?",
                "answer": "Sore throats are commonly caused by viral infections (colds, flu), bacterial infections (strep throat), allergies, or environmental factors.\n\n**Treatment:**\n• **Home remedies:**\n  - Warm salt water gargle (1/2 tsp salt in warm water)\n  - Warm liquids: tea with honey, warm broth\n  - Throat lozenges with Menthol or Benzocaine\n  - Rest and stay hydrated\n\n• **Over-the-counter medications:**\n  - Pain relievers: Paracetamol 500-1000mg or Ibuprofen 200-400mg\n  - Throat sprays: Phenol-based sprays (Chloraseptic)\n  - For bacterial infections (strep): Antibiotics prescribed by doctor (Penicillin, Amoxicillin)\n\n**Preventive Medicine:**\n• Avoid irritants: smoke, pollutants\n• Maintain good hand hygiene\n• Don't share utensils or drinks\n• Annual flu vaccination\n• Stay hydrated\n• Use humidifier in dry environments\n• Avoid allergens if allergic\n\n⚠️ **See a doctor if:** Sore throat lasts more than a week, severe pain, fever above 101°F, difficulty swallowing, white patches, or swollen lymph nodes."
            },
            {
                "category": "Symptoms",
                "keywords": ["dizziness", "vertigo", "lightheaded", "feeling dizzy"],
                "question": "What causes dizziness?",
                "answer": "Dizziness can result from dehydration, low blood pressure, inner ear problems, medication side effects, or underlying conditions. Stay hydrated, avoid sudden movements, and sit or lie down if feeling dizzy. Consult a doctor if dizziness is frequent or severe."
            },
            {
                "category": "Prevention",
                "keywords": ["sleep", "insomnia", "sleeping problems", "poor sleep"],
                "question": "How to improve sleep quality?",
                "answer": "Improve sleep by maintaining a regular sleep schedule, creating a comfortable sleep environment, avoiding screens before bed, limiting caffeine and alcohol, exercising regularly, and managing stress. Most adults need 7-9 hours of sleep per night."
            },
            {
                "category": "General Health",
                "keywords": ["allergy", "allergic reaction", "allergies", "allergen"],
                "question": "What are common allergy symptoms?",
                "answer": "Allergy symptoms include sneezing, runny or stuffy nose, itchy eyes, skin rashes, or hives. Common allergens include pollen, dust, pet dander, and certain foods. Avoid triggers when possible, use antihistamines for mild symptoms, and see a doctor for severe reactions."
            },
            {
                "category": "Symptoms",
                "keywords": ["joint pain", "arthritis", "joint ache", "stiff joints"],
                "question": "What causes joint pain and how to treat it?",
                "answer": "Joint pain can be caused by arthritis (osteoarthritis, rheumatoid arthritis), injury, overuse, or inflammation.\n\n**Treatment:**\n• **Over-the-counter medications:**\n  - Pain relievers: Paracetamol 500-1000mg every 4-6 hours\n  - NSAIDs: Ibuprofen 200-400mg every 6-8 hours OR Naproxen 220-440mg every 8-12 hours\n  - Topical creams: Capsaicin cream, Menthol-based gels (Bengay, Icy Hot)\n\n• **Prescription medications (for severe cases):**\n  - For rheumatoid arthritis: DMARDs (Methotrexate), Biologics\n  - Corticosteroids: Prednisone (short-term)\n  - Stronger NSAIDs: Diclofenac, Celecoxib\n\n**Preventive Medicine & Lifestyle:**\n• Maintain healthy weight (reduces stress on joints)\n• Low-impact exercise: swimming, cycling, yoga\n• Omega-3 supplements (1000-2000mg daily) - anti-inflammatory\n• Glucosamine + Chondroitin supplements (may help osteoarthritis)\n• Turmeric/Curcumin supplements (anti-inflammatory)\n• Vitamin D3 (1000-2000 IU daily) for bone health\n• Avoid repetitive joint stress\n• Use joint-friendly techniques during activities\n\n⚠️ **See a doctor if:** Joint pain is severe, persistent, accompanied by swelling/redness, or limits daily activities. Rheumatoid arthritis requires early medical treatment."
            },
            {
                "category": "Prevention",
                "keywords": ["diet", "healthy eating", "nutrition", "balanced diet"],
                "question": "What is a healthy diet?",
                "answer": "A healthy diet includes fruits, vegetables, whole grains, lean proteins, and healthy fats. Limit processed foods, sugar, and sodium. Stay hydrated with water. Portion control and variety are key. Consult a nutritionist for personalized dietary advice."
            },
            {
                "category": "General Health",
                "keywords": ["stress", "anxiety", "stress management", "mental health"],
                "question": "How to manage stress?",
                "answer": "Manage stress through regular exercise, meditation, deep breathing, adequate sleep, time management, and talking to friends or professionals. Identify stress triggers and develop coping strategies. Seek professional help if stress is overwhelming or affecting daily life."
            },
            {
                "category": "Emergency",
                "keywords": ["difficulty breathing", "breathing problem", "shortness of breath", "can't breathe"],
                "question": "What to do if having difficulty breathing?",
                "answer": "If experiencing severe difficulty breathing, seek immediate medical attention. Call emergency services. Sit upright, try to stay calm, and use any prescribed inhalers if available. Do not delay seeking help for breathing difficulties."
            },
            {
                "category": "Symptoms",
                "keywords": ["back pain", "lower back pain", "backache", "spine pain"],
                "question": "What causes back pain?",
                "answer": "Back pain can result from muscle strain, poor posture, injury, or underlying conditions. For mild pain, try rest, ice/heat therapy, and gentle stretching. Maintain good posture and exercise regularly. See a doctor if pain is severe, persistent, or radiates to legs."
            },
            {
                "category": "Prevention",
                "keywords": ["vaccination", "vaccine", "immunization", "vaccines"],
                "question": "Why are vaccinations important?",
                "answer": "Vaccinations protect against serious diseases, prevent outbreaks, and save lives. They work by training your immune system to recognize and fight specific pathogens. Follow recommended vaccination schedules for you and your family. Consult your healthcare provider about vaccination needs."
            },
            {
                "category": "General Health",
                "keywords": ["heart rate", "pulse", "heartbeat", "pulse rate"],
                "question": "What is a normal heart rate?",
                "answer": "Normal resting heart rate for adults is 60-100 beats per minute. Athletes may have lower rates (40-60 bpm). Factors like age, fitness level, and medications affect heart rate. If you notice irregular or concerning heart rates, consult a healthcare provider."
            },
            {
                "category": "Symptoms",
                "keywords": ["skin rash", "rash", "skin irritation", "dermatitis"],
                "question": "What causes skin rashes?",
                "answer": "Skin rashes can be caused by allergies, infections, irritants, or underlying conditions. Common causes include contact with allergens, medications, or infections. Keep the area clean and dry, avoid scratching, and use mild soaps. See a dermatologist if rash is severe or persistent."
            },
            {
                "category": "Prevention",
                "keywords": ["hydration", "water intake", "drink water", "dehydration"],
                "question": "How much water should I drink?",
                "answer": "Most adults need about 8 glasses (2 liters) of water daily, but needs vary based on activity level, climate, and health. Drink water throughout the day, more during exercise or hot weather. Urine color is a good indicator - pale yellow means adequate hydration."
            },
            {
                "category": "General Health",
                "keywords": ["blood test", "lab test", "medical test", "health checkup"],
                "question": "When should I get regular health checkups?",
                "answer": "Regular health checkups help detect problems early. Adults should have annual checkups, with more frequent visits if you have chronic conditions or risk factors. Screenings (blood pressure, cholesterol, blood sugar) are important for prevention. Discuss with your healthcare provider."
            },
            {
                "category": "Symptoms",
                "keywords": ["muscle pain", "muscle ache", "sore muscles", "muscle soreness"],
                "question": "What causes muscle pain?",
                "answer": "Muscle pain can result from overexertion, injury, tension, or underlying conditions. Rest, ice/heat therapy, gentle stretching, and over-the-counter pain relievers can help. If pain is severe, persistent, or accompanied by other symptoms, consult a healthcare provider."
            },
            {
                "category": "Prevention",
                "keywords": ["hand hygiene", "handwashing", "wash hands", "clean hands"],
                "question": "Why is hand hygiene important?",
                "answer": "Proper handwashing prevents the spread of infections and diseases. Wash hands with soap and water for at least 20 seconds, especially before eating, after using the bathroom, and after coughing or sneezing. Use hand sanitizer when soap isn't available."
            },
            {
                "category": "Prevention",
                "keywords": ["lifestyle", "improve lifestyle", "better lifestyle", "healthy lifestyle", "lifestyle improvement", "change lifestyle", "lifestyle tips", "wellness", "healthy living"],
                "question": "How to improve my lifestyle?",
                "answer": "Improving your lifestyle involves making positive changes across multiple areas of your life. Here's a comprehensive guide:\n\n**1. Nutrition & Diet:**\n• Eat a balanced diet with plenty of fruits, vegetables, whole grains, and lean proteins\n• Limit processed foods, sugar, and unhealthy fats\n• Stay hydrated - drink 8 glasses of water daily\n• Practice portion control and mindful eating\n• Consider consulting a nutritionist for personalized advice\n\n**2. Physical Activity:**\n• Aim for at least 150 minutes of moderate-intensity exercise per week (30 mins, 5 days)\n• Include both cardio (walking, cycling, swimming) and strength training\n• Find activities you enjoy to stay motivated\n• Start slowly and gradually increase intensity\n• Take breaks from sitting - move every hour\n\n**3. Sleep Quality:**\n• Maintain a regular sleep schedule (7-9 hours per night)\n• Create a comfortable sleep environment (dark, quiet, cool)\n• Avoid screens 1 hour before bed\n• Limit caffeine and alcohol, especially in the evening\n• Establish a relaxing bedtime routine\n\n**4. Stress Management:**\n• Practice relaxation techniques (meditation, deep breathing, yoga)\n• Exercise regularly to reduce stress\n• Maintain social connections with friends and family\n• Set realistic goals and priorities\n• Consider professional help if stress is overwhelming\n\n**5. Mental Health:**\n• Practice mindfulness and gratitude\n• Engage in hobbies and activities you enjoy\n• Limit social media and screen time\n• Seek professional help when needed\n• Build a support network\n\n**6. Preventive Care:**\n• Get regular health checkups and screenings\n• Stay up to date with vaccinations\n• Monitor your health metrics (blood pressure, weight, etc.)\n• Don't ignore symptoms - consult healthcare providers\n\n**7. Healthy Habits:**\n• Quit smoking and limit alcohol consumption\n• Practice good hygiene (handwashing, dental care)\n• Protect yourself from sun exposure\n• Maintain a healthy weight (BMI 18.5-25)\n• Avoid risky behaviors\n\n**8. Work-Life Balance:**\n• Set boundaries between work and personal time\n• Take regular breaks and vacations\n• Manage your time effectively\n• Prioritize self-care\n\n**Tips for Success:**\n• Start with small, achievable changes\n• Focus on one area at a time\n• Track your progress\n• Be patient - lifestyle changes take time\n• Celebrate small victories\n• Don't be too hard on yourself if you slip up\n\nRemember: Improving your lifestyle is a journey, not a destination. Make gradual, sustainable changes that you can maintain long-term. Consult healthcare providers for personalized advice based on your specific health needs."
            },
            {
                "category": "Symptoms",
                "keywords": ["heart disease", "coronary heart disease", "heart attack", "cardiac disease", "heart problem", "cardiovascular disease", "chest pain heart"],
                "question": "What is heart disease and how to prevent it?",
                "answer": "Heart disease (cardiovascular disease) refers to conditions affecting the heart and blood vessels, including coronary artery disease, heart attacks, and heart failure.\n\n**Common Symptoms:**\n• Chest pain or discomfort (angina)\n• Shortness of breath\n• Fatigue and weakness\n• Irregular heartbeat\n• Swelling in legs, ankles, or feet\n• Dizziness or fainting\n\n**Risk Factors:**\n• High blood pressure\n• High cholesterol\n• Smoking\n• Diabetes\n• Obesity\n• Family history\n• Age (men over 45, women over 55)\n• Sedentary lifestyle\n\n**Prevention & Lifestyle:**\n• Maintain healthy blood pressure (<120/80 mmHg)\n• Control cholesterol levels (LDL <100 mg/dL)\n• Quit smoking completely\n• Exercise regularly (150 mins/week)\n• Heart-healthy diet: fruits, vegetables, whole grains, lean proteins\n• Limit saturated fats, trans fats, and sodium\n• Maintain healthy weight (BMI 18.5-25)\n• Manage stress\n• Limit alcohol (1-2 drinks/day max)\n• Get regular checkups\n\n**Treatment (Prescription Required):**\n• Statins for cholesterol\n• Blood pressure medications (ACE inhibitors, beta-blockers)\n• Aspirin (low-dose, as prescribed)\n• Blood thinners if needed\n• Surgery (angioplasty, bypass) for severe cases\n\n⚠️ **Emergency:** If you experience severe chest pain, call emergency services immediately. Early treatment saves lives."
            },
            {
                "category": "Symptoms",
                "keywords": ["stroke", "brain stroke", "cerebral stroke", "stroke symptoms", "brain attack"],
                "question": "What are stroke symptoms and how to prevent stroke?",
                "answer": "A stroke occurs when blood flow to the brain is interrupted, causing brain cells to die. It's a medical emergency.\n\n**Stroke Symptoms (FAST):**\n• **F**ace: Drooping on one side\n• **A**rms: Weakness or numbness in one arm\n• **S**peech: Slurred or difficulty speaking\n• **T**ime: Call emergency immediately\n\n**Other Symptoms:**\n• Sudden severe headache\n• Vision problems\n• Dizziness or loss of balance\n• Confusion\n• Numbness on one side of body\n\n**Prevention:**\n• Control high blood pressure (most important)\n• Manage diabetes\n• Lower cholesterol\n• Quit smoking\n• Limit alcohol\n• Exercise regularly\n• Maintain healthy weight\n• Eat heart-healthy diet\n• Manage atrial fibrillation (irregular heartbeat)\n• Take prescribed medications (aspirin, blood thinners if needed)\n\n**Treatment (Emergency):**\n• Immediate medical care is critical\n• Clot-busting medications (if given within 3-4 hours)\n• Surgery may be needed\n• Rehabilitation after stroke\n\n⚠️ **CRITICAL:** If you suspect a stroke, call emergency services immediately. Every minute counts!"
            },
            {
                "category": "Symptoms",
                "keywords": ["obesity", "overweight", "weight problem", "excessive weight", "bmi high"],
                "question": "What is obesity and how to manage it?",
                "answer": "Obesity is a medical condition where excess body fat accumulates to the extent that it may negatively affect health. BMI ≥30 indicates obesity.\n\n**Health Risks:**\n• Heart disease and stroke\n• Type 2 diabetes\n• High blood pressure\n• Sleep apnea\n• Certain cancers\n• Joint problems\n• Fatty liver disease\n• Depression\n\n**Causes:**\n• Poor diet (high-calorie, processed foods)\n• Lack of physical activity\n• Genetics\n• Medical conditions (hypothyroidism, PCOS)\n• Medications\n• Psychological factors\n\n**Management & Treatment:**\n• **Diet:**\n  - Calorie-controlled, balanced diet\n  - More fruits, vegetables, whole grains\n  - Limit processed foods, sugar, unhealthy fats\n  - Portion control\n  - Consider consulting a dietitian\n\n• **Exercise:**\n  - Start with 150 mins/week moderate activity\n  - Gradually increase to 300 mins/week\n  - Include strength training\n  - Find activities you enjoy\n\n• **Lifestyle:**\n  - Get adequate sleep (7-9 hours)\n  - Manage stress\n  - Set realistic goals (1-2 lbs/week weight loss)\n  - Track food intake and activity\n\n• **Medical Treatment (Prescription):**\n  - Orlistat (fat blocker)\n  - GLP-1 agonists: Liraglutide, Semaglutide\n  - Phentermine-topiramate (appetite suppressant)\n  - Bariatric surgery for severe cases (BMI ≥40)\n\n⚠️ **Important:** Consult a healthcare provider for personalized weight management plan. Rapid weight loss can be dangerous."
            },
            {
                "category": "Symptoms",
                "keywords": ["depression", "depressed", "sadness", "feeling down", "mental depression", "mood disorder"],
                "question": "What are depression symptoms and how to treat it?",
                "answer": "Depression is a mental health disorder characterized by persistent sadness, loss of interest, and other symptoms that affect daily life.\n\n**Common Symptoms:**\n• Persistent sadness, anxiety, or emptiness\n• Loss of interest in activities once enjoyed\n• Fatigue and decreased energy\n• Difficulty concentrating or making decisions\n• Changes in sleep (insomnia or oversleeping)\n• Changes in appetite or weight\n• Feelings of worthlessness or guilt\n• Thoughts of death or suicide\n• Irritability or restlessness\n\n**Treatment:**\n• **Therapy:**\n  - Cognitive Behavioral Therapy (CBT)\n  - Interpersonal therapy\n  - Psychotherapy\n\n• **Medications (Prescription Required):**\n  - SSRIs: Sertraline, Fluoxetine, Escitalopram\n  - SNRIs: Venlafaxine, Duloxetine\n  - Atypical antidepressants: Bupropion, Mirtazapine\n  - May take 4-6 weeks to see effects\n\n• **Lifestyle:**\n  - Regular exercise (30 mins, 3-5 times/week)\n  - Healthy diet\n  - Adequate sleep\n  - Stress management\n  - Social support\n  - Avoid alcohol and drugs\n\n⚠️ **CRITICAL:** If you have thoughts of suicide, seek immediate help. Call a crisis hotline or go to emergency room. Depression is treatable - don't suffer alone."
            },
            {
                "category": "Symptoms",
                "keywords": ["anxiety", "anxiety disorder", "panic attack", "worried", "nervous", "anxious", "panic disorder"],
                "question": "What are anxiety symptoms and how to manage anxiety?",
                "answer": "Anxiety disorders involve excessive worry, fear, or nervousness that interferes with daily activities.\n\n**Common Symptoms:**\n• Excessive worry or fear\n• Restlessness or feeling on edge\n• Fatigue\n• Difficulty concentrating\n• Irritability\n• Muscle tension\n• Sleep problems\n• Panic attacks (rapid heartbeat, sweating, trembling)\n• Avoidance of anxiety-provoking situations\n\n**Types:**\n• Generalized Anxiety Disorder (GAD)\n• Panic Disorder\n• Social Anxiety Disorder\n• Phobias\n\n**Treatment:**\n• **Therapy:**\n  - Cognitive Behavioral Therapy (CBT)\n  - Exposure therapy\n  - Relaxation techniques\n\n• **Medications (Prescription):**\n  - SSRIs: Sertraline, Paroxetine, Escitalopram\n  - SNRIs: Venlafaxine, Duloxetine\n  - Benzodiazepines (short-term): Alprazolam, Lorazepam\n  - Buspirone (for GAD)\n\n• **Self-Help:**\n  - Deep breathing exercises\n  - Meditation and mindfulness\n  - Regular exercise\n  - Limit caffeine and alcohol\n  - Adequate sleep\n  - Stress management\n  - Support groups\n\n⚠️ **Seek professional help** if anxiety significantly impacts your daily life. Treatment is effective."
            },
            {
                "category": "Symptoms",
                "keywords": ["osteoporosis", "bone loss", "weak bones", "bone density", "fracture risk"],
                "question": "What is osteoporosis and how to prevent it?",
                "answer": "Osteoporosis is a condition where bones become weak and brittle, increasing fracture risk.\n\n**Risk Factors:**\n• Age (especially postmenopausal women)\n• Gender (women more at risk)\n• Family history\n• Low body weight\n• Smoking and excessive alcohol\n• Lack of exercise\n• Low calcium/vitamin D intake\n• Certain medications (steroids)\n\n**Prevention:**\n• **Calcium:** 1000-1200 mg daily (dairy, leafy greens, fortified foods)\n• **Vitamin D:** 600-800 IU daily (sunlight, supplements, fatty fish)\n• **Exercise:** Weight-bearing (walking, jogging) and strength training\n• **Lifestyle:** Quit smoking, limit alcohol\n• **Medications (if needed):**\n  - Bisphosphonates: Alendronate, Risedronate\n  - Hormone therapy (for postmenopausal women)\n  - Denosumab (injection)\n\n**Screening:**\n• Bone density scan (DEXA) recommended for:\n  - Women 65+ and men 70+\n  - Postmenopausal women with risk factors\n  - Anyone with fractures from minor trauma\n\n⚠️ **Important:** Early detection and treatment can prevent fractures. Consult your doctor about screening."
            },
            {
                "category": "Symptoms",
                "keywords": ["kidney disease", "kidney problem", "renal disease", "kidney failure", "chronic kidney disease", "ckd"],
                "question": "What is kidney disease and how to prevent it?",
                "answer": "Chronic kidney disease (CKD) is the gradual loss of kidney function over time.\n\n**Symptoms (often appear late):**\n• Fatigue and weakness\n• Swelling in legs, ankles, feet\n• Changes in urination (frequency, color)\n• Nausea and vomiting\n• Loss of appetite\n• Itchy skin\n• High blood pressure\n• Shortness of breath\n\n**Causes:**\n• Diabetes (most common)\n• High blood pressure\n• Glomerulonephritis\n• Polycystic kidney disease\n• Urinary tract obstruction\n• Recurrent kidney infections\n\n**Prevention:**\n• Control diabetes and blood sugar\n• Manage high blood pressure\n• Maintain healthy weight\n• Don't smoke\n• Limit NSAIDs (Ibuprofen, Naproxen)\n• Stay hydrated\n• Limit salt intake\n• Regular exercise\n• Get regular checkups\n\n**Treatment:**\n• Medications to control blood pressure and diabetes\n• Medications to reduce protein in urine\n• Dialysis (if kidney function severely impaired)\n• Kidney transplant (for end-stage disease)\n\n⚠️ **Important:** Early detection through regular checkups is crucial. Kidney disease is often silent in early stages."
            },
            {
                "category": "Symptoms",
                "keywords": ["liver disease", "liver problem", "hepatitis", "fatty liver", "liver damage", "cirrhosis"],
                "question": "What is liver disease and how to prevent it?",
                "answer": "Liver disease includes various conditions affecting the liver, such as hepatitis, fatty liver, and cirrhosis.\n\n**Common Types:**\n• Fatty liver disease (NAFLD)\n• Hepatitis (A, B, C)\n• Cirrhosis\n• Liver cancer\n\n**Symptoms:**\n• Fatigue and weakness\n• Jaundice (yellowing of skin/eyes)\n• Abdominal pain and swelling\n• Dark urine\n• Pale stools\n• Nausea and vomiting\n• Loss of appetite\n• Itchy skin\n\n**Prevention:**\n• Limit alcohol consumption\n• Maintain healthy weight\n• Eat balanced diet (limit processed foods)\n• Exercise regularly\n• Get vaccinated (Hepatitis A & B)\n• Practice safe sex\n• Don't share needles\n• Avoid excessive medications\n• Protect against toxins\n\n**Treatment:**\n• Depends on cause:\n  - Antiviral medications for hepatitis\n  - Lifestyle changes for fatty liver\n  - Medications to manage complications\n  - Liver transplant in severe cases\n\n⚠️ **Important:** Early detection and treatment can prevent progression. Regular checkups and liver function tests are important."
            },
            {
                "category": "Symptoms",
                "keywords": ["thyroid", "hypothyroidism", "hyperthyroidism", "underactive thyroid", "overactive thyroid", "thyroid problem"],
                "question": "What are thyroid problems and how to treat them?",
                "answer": "Thyroid disorders occur when the thyroid gland produces too much or too little hormone.\n\n**Hypothyroidism (Underactive Thyroid):**\n• **Symptoms:** Fatigue, weight gain, cold intolerance, dry skin, hair loss, depression, constipation\n• **Treatment:** Levothyroxine (Synthroid) - daily medication\n• **Causes:** Hashimoto's disease, iodine deficiency, medications\n\n**Hyperthyroidism (Overactive Thyroid):**\n• **Symptoms:** Weight loss, rapid heartbeat, anxiety, tremors, sweating, heat intolerance, sleep problems\n• **Treatment:**\n  - Antithyroid medications: Methimazole, Propylthiouracil\n  - Radioactive iodine therapy\n  - Surgery (in some cases)\n• **Causes:** Graves' disease, thyroid nodules\n\n**Prevention:**\n• Ensure adequate iodine intake (iodized salt, seafood)\n• Regular checkups\n• Be aware of family history\n\n**Diagnosis:**\n• Blood tests: TSH, T3, T4 levels\n• Physical examination\n• Imaging if needed\n\n⚠️ **Important:** Thyroid disorders require medical diagnosis and treatment. Don't self-medicate. Regular monitoring is essential."
            },
            {
                "category": "Symptoms",
                "keywords": ["copd", "chronic obstructive pulmonary disease", "emphysema", "chronic bronchitis", "lung disease", "breathing problem chronic"],
                "question": "What is COPD and how to manage it?",
                "answer": "COPD (Chronic Obstructive Pulmonary Disease) is a chronic lung disease that makes breathing difficult.\n\n**Symptoms:**\n• Shortness of breath (especially during activity)\n• Chronic cough with mucus\n• Wheezing\n• Chest tightness\n• Frequent respiratory infections\n• Fatigue\n\n**Causes:**\n• Smoking (primary cause)\n• Long-term exposure to lung irritants\n• Air pollution\n• Genetic factors (alpha-1 antitrypsin deficiency)\n\n**Prevention:**\n• **Most important:** Quit smoking immediately\n• Avoid secondhand smoke\n• Reduce exposure to air pollution\n• Use protective equipment if exposed to dust/chemicals\n• Get flu and pneumonia vaccinations\n\n**Treatment:**\n• **Medications (Prescription):**\n  - Bronchodilators: Albuterol, Ipratropium (inhalers)\n  - Inhaled corticosteroids: Fluticasone, Budesonide\n  - Combination inhalers\n  - Oral medications: Theophylline, Roflumilast\n  - Antibiotics for infections\n\n• **Lifestyle:**\n  - Pulmonary rehabilitation\n  - Oxygen therapy (if needed)\n  - Regular exercise (as tolerated)\n  - Healthy diet\n  - Avoid lung irritants\n\n⚠️ **Critical:** If you smoke, quitting is the most important step. COPD is progressive but manageable with proper treatment."
            },
            {
                "category": "Symptoms",
                "keywords": ["gerd", "acid reflux", "heartburn", "gastroesophageal reflux", "stomach acid", "reflux disease"],
                "question": "What is GERD and how to treat it?",
                "answer": "GERD (Gastroesophageal Reflux Disease) is chronic acid reflux where stomach acid flows back into the esophagus.\n\n**Symptoms:**\n• Heartburn (burning chest pain)\n• Regurgitation of food or sour liquid\n• Chest pain\n• Difficulty swallowing\n• Sensation of a lump in throat\n• Chronic cough\n• Hoarseness\n\n**Causes:**\n• Weak lower esophageal sphincter\n• Hiatal hernia\n• Obesity\n• Pregnancy\n• Certain foods and medications\n• Smoking\n\n**Treatment:**\n• **Lifestyle Changes:**\n  - Avoid trigger foods (spicy, fatty, acidic, chocolate, caffeine)\n  - Eat smaller, more frequent meals\n  - Don't lie down immediately after eating\n  - Elevate head of bed\n  - Lose weight if overweight\n  - Quit smoking\n  - Limit alcohol\n\n• **Medications:**\n  - Antacids: Tums, Rolaids (quick relief)\n  - H2 blockers: Ranitidine, Famotidine (reduce acid production)\n  - Proton pump inhibitors: Omeprazole, Esomeprazole, Pantoprazole (strongest, prescription)\n\n⚠️ **See a doctor if:** Symptoms persist despite lifestyle changes, you have difficulty swallowing, or experience chest pain (could be heart-related)."
            },
            {
                "category": "Symptoms",
                "keywords": ["ibs", "irritable bowel syndrome", "stomach cramps", "bowel problem", "digestive issue chronic"],
                "question": "What is IBS and how to manage it?",
                "answer": "IBS (Irritable Bowel Syndrome) is a common disorder affecting the large intestine.\n\n**Symptoms:**\n• Abdominal pain and cramping\n• Bloating and gas\n• Diarrhea or constipation (or alternating)\n• Mucus in stool\n• Urgency to have bowel movement\n\n**Triggers:**\n• Certain foods (dairy, beans, cabbage, carbonated drinks)\n• Stress\n• Hormonal changes\n• Other gastrointestinal infections\n\n**Management:**\n• **Diet (FODMAP diet may help):**\n  - Identify and avoid trigger foods\n  - Increase fiber gradually\n  - Stay hydrated\n  - Eat regular meals\n  - Limit caffeine and alcohol\n\n• **Lifestyle:**\n  - Regular exercise\n  - Stress management (meditation, yoga)\n  - Adequate sleep\n  - Keep a food diary\n\n• **Medications:**\n  - Fiber supplements: Psyllium, Methylcellulose\n  - Antispasmodics: Hyoscyamine, Dicyclomine\n  - Laxatives (for constipation): Polyethylene glycol\n  - Anti-diarrheal: Loperamide\n  - Antidepressants (low-dose, for pain): Amitriptyline\n\n⚠️ **See a doctor** for proper diagnosis. IBS symptoms can overlap with other conditions. There's no cure, but symptoms can be managed."
            },
            {
                "category": "Symptoms",
                "keywords": ["migraine", "migraine headache", "severe headache", "headache with aura", "chronic migraine"],
                "question": "What is a migraine and how to treat it?",
                "answer": "Migraine is a neurological condition causing severe, recurring headaches often with other symptoms.\n\n**Symptoms:**\n• Throbbing or pulsing pain (usually one side)\n• Sensitivity to light, sound, or smells\n• Nausea and vomiting\n• Visual disturbances (aura) - flashing lights, blind spots\n• Dizziness\n• Fatigue\n\n**Triggers:**\n• Stress\n• Hormonal changes\n• Certain foods (chocolate, cheese, processed meats)\n• Alcohol (especially red wine)\n• Caffeine (too much or withdrawal)\n• Sleep changes\n• Weather changes\n• Strong smells\n\n**Treatment:**\n• **Acute Treatment:**\n  - NSAIDs: Ibuprofen 400-600mg, Naproxen 500mg\n  - Triptans: Sumatriptan, Rizatriptan (prescription)\n  - Combination: Acetaminophen + Aspirin + Caffeine\n  - Anti-nausea: Metoclopramide, Prochlorperazine\n\n• **Prevention:**\n  - Beta-blockers: Propranolol, Metoprolol\n  - Antidepressants: Amitriptyline\n  - Anticonvulsants: Topiramate, Valproate\n  - CGRP inhibitors (newer, for chronic migraines)\n\n• **Lifestyle:**\n  - Identify and avoid triggers\n  - Regular sleep schedule\n  - Stress management\n  - Regular exercise\n  - Stay hydrated\n  - Magnesium supplements (400-600mg daily) may help\n\n⚠️ **See a doctor** if migraines are frequent, severe, or don't respond to over-the-counter medications."
            },
            {
                "category": "Symptoms",
                "keywords": ["pcos", "polycystic ovary syndrome", "irregular periods", "hormonal imbalance women", "ovarian cysts"],
                "question": "What is PCOS and how to manage it?",
                "answer": "PCOS (Polycystic Ovary Syndrome) is a hormonal disorder common in women of reproductive age.\n\n**Symptoms:**\n• Irregular or absent periods\n• Excess androgen (male hormone) - excess hair, acne\n• Polycystic ovaries\n• Weight gain or difficulty losing weight\n• Insulin resistance\n• Infertility\n• Thinning hair on scalp\n\n**Causes:**\n• Exact cause unknown\n• Insulin resistance\n• Genetics\n• Inflammation\n\n**Management:**\n• **Lifestyle:**\n  - Weight loss (even 5-10% can help)\n  - Low-carb or Mediterranean diet\n  - Regular exercise\n  - Stress management\n\n• **Medications:**\n  - Birth control pills (regulate periods, reduce androgen)\n  - Metformin (improve insulin sensitivity)\n  - Anti-androgens: Spironolactone (reduce excess hair)\n  - Fertility medications if trying to conceive\n\n• **Supplements:**\n  - Inositol (may improve insulin sensitivity)\n  - Vitamin D\n  - Omega-3 fatty acids\n\n⚠️ **Important:** PCOS requires medical diagnosis and management. Early treatment can prevent long-term complications like diabetes and heart disease."
            },
            {
                "category": "Symptoms",
                "keywords": ["anemia", "low iron", "iron deficiency", "low hemoglobin", "tired blood"],
                "question": "What is anemia and how to treat it?",
                "answer": "Anemia is a condition where you don't have enough healthy red blood cells to carry oxygen to your tissues.\n\n**Symptoms:**\n• Fatigue and weakness\n• Pale skin\n• Shortness of breath\n• Dizziness or lightheadedness\n• Cold hands and feet\n• Headaches\n• Irregular heartbeat\n• Brittle nails\n\n**Common Causes:**\n• Iron deficiency (most common)\n• Vitamin B12 deficiency\n• Folate deficiency\n• Chronic diseases\n• Blood loss\n\n**Treatment:**\n• **Iron Deficiency Anemia:**\n  - Iron supplements: Ferrous sulfate 325mg daily (with vitamin C for absorption)\n  - Iron-rich foods: red meat, spinach, beans, fortified cereals\n  - May take 2-3 months to correct\n\n• **Vitamin B12 Deficiency:**\n  - B12 supplements or injections\n  - B12-rich foods: meat, fish, dairy, fortified cereals\n\n• **Folate Deficiency:**\n  - Folic acid supplements\n  - Folate-rich foods: leafy greens, citrus fruits, beans\n\n**Prevention:**\n• Eat balanced diet with iron-rich foods\n• Include vitamin C with iron-rich meals\n• Consider supplements if at risk (pregnant women, vegetarians)\n\n⚠️ **See a doctor** for proper diagnosis. Don't self-treat with iron supplements without testing, as excess iron can be harmful."
            }
        ]
