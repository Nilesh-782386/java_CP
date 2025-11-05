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
            "vitamin", "supplement", "sleep", "stress", "immunity", "cure", "therapy"
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
        
        # If good match found (threshold: 0.25 for ML, lower than keyword matching)
        if best_score > 0.25:
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
            for keyword in keywords:
                if keyword.lower() in user_message_lower:
                    score += 1.0
            
            if keywords:
                score = score / len(keywords)
            
            if score > best_score:
                best_score = score
                best_match = entry
        
        if best_match and best_score > 0.3:
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
            }
        ]
