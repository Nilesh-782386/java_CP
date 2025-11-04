export interface ChatKnowledge {
  keywords: string[];
  response: string;
  category: string;
}

export const chatbotKnowledgeBase: ChatKnowledge[] = [
  // General Health
  {
    keywords: ["hello", "hi", "hey", "greet"],
    category: "greeting",
    response: "Hello! I'm your Health Assistant. I can provide educational information about health topics, symptoms, and general wellness. How can I help you today? (Remember: I provide educational information only, not medical advice)",
  },
  {
    keywords: ["help", "what can you do", "how to use"],
    category: "help",
    response: "I can help you with:\n\n• General health information and wellness tips\n• Explanation of common symptoms and conditions\n• Healthy lifestyle guidance\n• Basic preventive healthcare information\n• Understanding medical terms\n\nPlease ask me any health-related question! Note: I provide educational information only and am not a substitute for professional medical advice.",
  },

  // Symptoms
  {
    keywords: ["fever", "temperature", "high fever"],
    category: "symptoms",
    response: "Fever is a temporary increase in body temperature, often due to illness. Normal body temperature is around 98.6°F (37°C). A fever is generally considered 100.4°F (38°C) or higher.\n\nCommon causes:\n• Viral or bacterial infections\n• Inflammatory conditions\n• Heat exhaustion\n• Certain medications\n\nHome care: Rest, stay hydrated, use fever-reducing medications like acetaminophen. Seek medical attention if fever exceeds 103°F, lasts more than 3 days, or is accompanied by severe symptoms.",
  },
  {
    keywords: ["headache", "head pain", "migraine"],
    category: "symptoms",
    response: "Headaches are very common and can range from mild to severe. Types include:\n\n• Tension headaches: Most common, feel like a tight band\n• Migraines: Intense, throbbing pain often with nausea\n• Cluster headaches: Severe pain around one eye\n• Sinus headaches: Facial pain with sinus pressure\n\nRelief measures: Rest in quiet dark room, adequate hydration, over-the-counter pain relievers, stress management. Seek immediate care for sudden severe headache, headache with fever/stiff neck, or after head injury.",
  },
  {
    keywords: ["cough", "coughing", "persistent cough"],
    category: "symptoms",
    response: "Coughing is a reflex that helps clear airways. Can be:\n\n• Acute: Lasts less than 3 weeks (often viral)\n• Chronic: Lasts more than 8 weeks\n• Dry or productive (with mucus)\n\nCommon causes: Common cold, flu, allergies, asthma, acid reflux.\n\nHome remedies: Stay hydrated, use humidifier, honey (for adults), throat lozenges. See a doctor if cough lasts more than 3 weeks, produces blood, or causes breathing difficulty.",
  },

  // Conditions
  {
    keywords: ["diabetes", "blood sugar", "glucose"],
    category: "conditions",
    response: "Diabetes is a chronic condition affecting blood sugar regulation:\n\n• Type 1: Body doesn't produce insulin\n• Type 2: Body doesn't use insulin properly\n\nSymptoms: Increased thirst, frequent urination, fatigue, blurred vision, slow healing.\n\nManagement: Regular blood sugar monitoring, healthy diet, exercise, medications/insulin as prescribed. Prevention (Type 2): Maintain healthy weight, exercise regularly, balanced diet. Regular check-ups are essential.",
  },
  {
    keywords: ["blood pressure", "hypertension", "high bp"],
    category: "conditions",
    response: "Blood pressure measures force of blood against artery walls:\n\n• Normal: Less than 120/80 mmHg\n• Elevated: 120-129/<80\n• Hypertension Stage 1: 130-139/80-89\n• Hypertension Stage 2: 140+/90+\n\nRisks: Heart disease, stroke, kidney disease.\n\nLifestyle changes: Reduce sodium, exercise regularly, maintain healthy weight, limit alcohol, manage stress, quit smoking. Regular monitoring is important. Many people need medication to control blood pressure.",
  },
  {
    keywords: ["cold", "common cold", "flu", "influenza"],
    category: "conditions",
    response: "Cold vs Flu:\n\nCold: Gradual onset, runny nose, sore throat, mild symptoms\nFlu: Sudden onset, fever, body aches, fatigue, more severe\n\nBoth are viral infections. Treatment:\n• Rest and fluids\n• Over-the-counter symptom relievers\n• Flu antivirals (if started early)\n\nPrevention: Wash hands frequently, avoid close contact with sick people, flu vaccine annually. See doctor if: high fever, difficulty breathing, chest pain, or symptoms lasting >10 days.",
  },

  // Prevention & Wellness
  {
    keywords: ["prevent", "prevention", "avoid disease"],
    category: "prevention",
    response: "Key disease prevention strategies:\n\n1. Vaccinations: Stay up-to-date\n2. Healthy diet: Fruits, vegetables, whole grains\n3. Regular exercise: 150 minutes moderate activity weekly\n4. Maintain healthy weight\n5. Don't smoke, limit alcohol\n6. Regular health screenings\n7. Good sleep (7-9 hours)\n8. Stress management\n9. Hand hygiene\n10. Safe practices (seatbelts, safe sex)\n\nRegular check-ups help catch problems early!",
  },
  {
    keywords: ["exercise", "physical activity", "workout"],
    category: "wellness",
    response: "Benefits of regular exercise:\n\n• Strengthens heart and improves circulation\n• Helps maintain healthy weight\n• Reduces risk of chronic diseases\n• Improves mood and mental health\n• Strengthens bones and muscles\n• Improves sleep quality\n\nRecommendations:\n• Adults: 150 minutes moderate OR 75 minutes vigorous activity weekly\n• Include strength training twice weekly\n• Start slowly if new to exercise\n• Choose activities you enjoy\n\nConsult your doctor before starting a new exercise program, especially if you have health conditions.",
  },
  {
    keywords: ["diet", "nutrition", "healthy eating", "food"],
    category: "wellness",
    response: "Healthy eating guidelines:\n\n• Eat variety of fruits and vegetables (5+ servings daily)\n• Choose whole grains over refined\n• Include lean proteins (fish, poultry, beans)\n• Limit saturated fats, sugar, and salt\n• Stay hydrated (8 glasses water daily)\n• Control portion sizes\n• Limit processed foods\n\nA balanced diet provides:\n• Energy for daily activities\n• Nutrients for growth and repair\n• Disease prevention\n\nConsider consulting a registered dietitian for personalized nutrition advice.",
  },
  {
    keywords: ["sleep", "insomnia", "rest", "tired"],
    category: "wellness",
    response: "Importance of good sleep:\n\n• Adults need 7-9 hours nightly\n• Essential for physical and mental health\n• Helps memory consolidation\n• Supports immune function\n• Regulates hormones and appetite\n\nBetter sleep tips:\n• Consistent sleep schedule\n• Cool, dark, quiet bedroom\n• Avoid screens 1 hour before bed\n• Limit caffeine and alcohol\n• Regular exercise (but not close to bedtime)\n• Relaxation techniques\n\nChronic sleep problems may indicate a sleep disorder—consult a doctor if insomnia persists.",
  },

  // Medical Tests
  {
    keywords: ["blood test", "lab test", "medical test"],
    category: "tests",
    response: "Common blood tests:\n\n• Complete Blood Count (CBC): Checks for anemia, infection, blood disorders\n• Metabolic Panel: Kidney function, electrolytes, blood sugar\n• Lipid Panel: Cholesterol levels\n• Thyroid Function: TSH, T3, T4 levels\n• Hemoglobin A1C: Average blood sugar over 3 months\n\nPreparation:\n• Some tests require fasting (usually 8-12 hours)\n• Inform doctor of medications\n• Stay hydrated\n\nYour doctor will explain what tests you need and why. Always discuss results with your healthcare provider.",
  },
  {
    keywords: ["cholesterol", "lipid", "hdl", "ldl"],
    category: "tests",
    response: "Understanding cholesterol:\n\n• Total cholesterol: <200 mg/dL desirable\n• LDL (bad): <100 mg/dL optimal\n• HDL (good): 60+ mg/dL protective\n• Triglycerides: <150 mg/dL normal\n\nHigh cholesterol increases heart disease risk. Management:\n• Heart-healthy diet (low saturated fat)\n• Regular exercise\n• Maintain healthy weight\n• Don't smoke\n• Medications if needed (statins)\n\nGet cholesterol checked regularly, especially if you have family history or risk factors.",
  },

  // Emergency & When to Seek Help
  {
    keywords: ["emergency", "urgent", "911", "serious"],
    category: "emergency",
    response: "Call emergency services (911/108) immediately for:\n\n• Chest pain or pressure\n• Difficulty breathing or shortness of breath\n• Sudden severe headache\n• Sudden weakness/numbness\n• Severe bleeding\n• Loss of consciousness\n• Severe allergic reaction\n• Suicidal thoughts\n• Severe burns\n• Major trauma\n\nWhen in doubt, err on the side of caution. It's better to seek emergency care and not need it than to delay when it's critical.",
  },

  // Default fallback
  {
    keywords: ["default"],
    category: "fallback",
    response: "I'm here to provide general health education and information. While I can discuss common health topics, symptoms, and wellness, I don't have specific information about that topic in my current knowledge base.\n\nFor specific medical concerns or detailed information about your particular situation, please consult with a qualified healthcare provider. They can provide personalized medical advice based on your individual health history.\n\nIs there another health topic I can help explain?",
  },
];
