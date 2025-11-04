import type { MedicalTest } from "@shared/schema";

export const medicalTestsDatabase: Record<string, {
  recommended: MedicalTest[];
  toAvoid: MedicalTest[];
  reasoning: string;
}> = {
  d001: { // Common Cold
    recommended: [
      {
        id: "t001",
        name: "Rapid Strep Test",
        description: "Quick test to check for streptococcal bacteria if severe sore throat is present",
        purpose: "To rule out strep throat which requires antibiotic treatment",
        costRange: "₹200 - ₹500",
        preparation: "No special preparation required",
      },
    ],
    toAvoid: [
      {
        id: "t002",
        name: "Chest X-ray",
        description: "Imaging of the chest to view lungs and heart",
        purpose: "Generally unnecessary for uncomplicated common cold unless pneumonia is suspected",
        costRange: "₹300 - ₹800",
      },
      {
        id: "t003",
        name: "Complete Blood Count (CBC)",
        description: "Blood test measuring various components",
        purpose: "Not typically needed for common cold unless symptoms are severe or prolonged",
        costRange: "₹200 - ₹600",
      },
    ],
    reasoning: "Common cold is viral and self-limiting. Most tests are unnecessary unless complications are suspected or symptoms persist beyond 10 days.",
  },
  d002: { // Influenza
    recommended: [
      {
        id: "t004",
        name: "Rapid Influenza Diagnostic Test",
        description: "Quick test to detect influenza viruses from nasal swab",
        purpose: "Confirms flu diagnosis to guide antiviral treatment, especially if caught early",
        costRange: "₹500 - ₹1,200",
        preparation: "No special preparation required",
      },
      {
        id: "t005",
        name: "Pulse Oximetry",
        description: "Non-invasive measurement of blood oxygen levels",
        purpose: "To check for respiratory complications",
        costRange: "₹100 - ₹300",
      },
    ],
    toAvoid: [
      {
        id: "t006",
        name: "Routine Blood Culture",
        description: "Growing bacteria from blood sample",
        purpose: "Not needed for uncomplicated flu without signs of bacterial infection",
        costRange: "₹800 - ₹2,000",
      },
    ],
    reasoning: "Rapid flu test helps guide early antiviral treatment. Most other tests are only needed if complications develop.",
  },
  d003: { // Type 2 Diabetes
    recommended: [
      {
        id: "t007",
        name: "HbA1c Test",
        description: "Measures average blood sugar levels over the past 2-3 months",
        purpose: "Primary test for diagnosing and monitoring diabetes management",
        costRange: "₹300 - ₹800",
        preparation: "No fasting required",
      },
      {
        id: "t008",
        name: "Fasting Blood Glucose",
        description: "Measures blood sugar after 8-hour fast",
        purpose: "Diagnose and monitor diabetes",
        costRange: "₹50 - ₹200",
        preparation: "8-12 hour fasting required",
      },
      {
        id: "t009",
        name: "Lipid Profile",
        description: "Measures cholesterol and triglyceride levels",
        purpose: "Diabetics have higher cardiovascular risk; monitoring is essential",
        costRange: "₹300 - ₹800",
        preparation: "9-12 hour fasting recommended",
      },
      {
        id: "t010",
        name: "Kidney Function Tests (Creatinine, eGFR)",
        description: "Measures kidney health markers",
        purpose: "Diabetes can damage kidneys; early detection is crucial",
        costRange: "₹200 - ₹600",
      },
    ],
    toAvoid: [
      {
        id: "t011",
        name: "Insulin Levels (for Type 2 without specific indication)",
        description: "Measures insulin in blood",
        purpose: "Usually not needed for routine Type 2 diabetes management",
        costRange: "₹400 - ₹1,000",
      },
    ],
    reasoning: "Regular monitoring of blood sugar, kidney function, and cardiovascular risk factors is essential for diabetes management.",
  },
  d004: { // Hypertension
    recommended: [
      {
        id: "t012",
        name: "24-Hour Blood Pressure Monitoring",
        description: "Continuous BP monitoring over 24 hours",
        purpose: "Provides comprehensive picture of blood pressure patterns",
        costRange: "₹1,000 - ₹3,000",
        preparation: "Normal daily activities while wearing monitor",
      },
      {
        id: "t013",
        name: "Electrocardiogram (ECG)",
        description: "Records electrical activity of the heart",
        purpose: "Checks for heart damage or abnormalities from high blood pressure",
        costRange: "₹150 - ₹500",
      },
      {
        id: "t014",
        name: "Kidney Function Tests",
        description: "Blood tests for kidney health",
        purpose: "High BP can damage kidneys; monitoring is important",
        costRange: "₹200 - ₹600",
      },
    ],
    toAvoid: [
      {
        id: "t015",
        name: "CT Scan of Adrenal Glands",
        description: "Imaging test of adrenal glands",
        purpose: "Only needed if secondary hypertension is suspected, not for routine cases",
        costRange: "₹3,000 - ₹8,000",
      },
    ],
    reasoning: "Focus on confirming diagnosis, assessing target organ damage, and ruling out secondary causes if indicated.",
  },
  d005: { // Gastroenteritis
    recommended: [
      {
        id: "t016",
        name: "Stool Culture",
        description: "Lab test to identify bacteria or parasites in stool",
        purpose: "Identifies causative organism if bloody diarrhea or severe symptoms",
        costRange: "₹300 - ₹800",
        preparation: "Fresh stool sample required",
      },
      {
        id: "t017",
        name: "Electrolyte Panel",
        description: "Measures sodium, potassium, and other electrolytes",
        purpose: "Checks for dehydration and electrolyte imbalances",
        costRange: "₹200 - ₹600",
      },
    ],
    toAvoid: [
      {
        id: "t018",
        name: "Abdominal X-ray",
        description: "Imaging of abdomen",
        purpose: "Not typically needed for uncomplicated gastroenteritis",
        costRange: "₹300 - ₹800",
      },
      {
        id: "t019",
        name: "Endoscopy",
        description: "Camera inserted through mouth to view digestive tract",
        purpose: "Excessive for simple gastroenteritis; only for persistent or severe cases",
        costRange: "₹3,000 - ₹10,000",
      },
    ],
    reasoning: "Most gastroenteritis is viral and self-limiting. Tests are mainly for severe cases or to identify specific pathogens.",
  },
  d006: { // Migraine
    recommended: [
      {
        id: "t020",
        name: "Headache Diary/Log",
        description: "Patient tracking of headache patterns, triggers, and severity",
        purpose: "Helps identify triggers and patterns for better management",
        costRange: "Free",
      },
      {
        id: "t021",
        name: "MRI Brain (if indicated)",
        description: "Detailed brain imaging",
        purpose: "To rule out structural causes if red flags present or unusual presentation",
        costRange: "₹4,000 - ₹12,000",
        preparation: "Remove metal objects; may require contrast",
      },
    ],
    toAvoid: [
      {
        id: "t022",
        name: "Routine CT or MRI for typical migraine",
        description: "Brain imaging without specific red flags",
        purpose: "Not needed for typical migraine with normal neurological exam",
        costRange: "₹3,000 - ₹12,000",
      },
      {
        id: "t023",
        name: "EEG (Electroencephalogram)",
        description: "Records brain electrical activity",
        purpose: "Not useful for diagnosing migraines unless seizures suspected",
        costRange: "₹1,500 - ₹4,000",
      },
    ],
    reasoning: "Migraine is a clinical diagnosis. Imaging is only needed if there are concerning features or abnormal neurological findings.",
  },
  d007: { // Asthma
    recommended: [
      {
        id: "t024",
        name: "Spirometry",
        description: "Lung function test measuring air flow",
        purpose: "Diagnose and monitor asthma severity and response to treatment",
        costRange: "₹500 - ₹1,500",
        preparation: "Avoid bronchodilators before test as directed",
      },
      {
        id: "t025",
        name: "Peak Flow Monitoring",
        description: "Simple test measuring maximum air flow rate",
        purpose: "Home monitoring of asthma control",
        costRange: "₹200 - ₹800 (one-time device purchase)",
      },
      {
        id: "t026",
        name: "Allergy Testing",
        description: "Skin or blood tests for specific allergens",
        purpose: "Identify triggers to avoid for better asthma control",
        costRange: "₹1,000 - ₹5,000",
      },
    ],
    toAvoid: [
      {
        id: "t027",
        name: "Routine Chest X-ray",
        description: "Imaging of chest",
        purpose: "Not needed for routine asthma monitoring unless complications suspected",
        costRange: "₹300 - ₹800",
      },
    ],
    reasoning: "Lung function tests are key for asthma diagnosis and monitoring. Imaging is only for complications.",
  },
  d008: { // Arthritis
    recommended: [
      {
        id: "t028",
        name: "X-ray of Affected Joints",
        description: "Imaging to visualize joint damage",
        purpose: "Assess severity and type of arthritis",
        costRange: "₹200 - ₹600 per joint",
      },
      {
        id: "t029",
        name: "Rheumatoid Factor & Anti-CCP",
        description: "Blood tests for rheumatoid arthritis markers",
        purpose: "Help diagnose rheumatoid arthritis specifically",
        costRange: "₹500 - ₹1,500",
      },
      {
        id: "t030",
        name: "ESR & CRP",
        description: "Blood tests measuring inflammation",
        purpose: "Assess level of inflammation in the body",
        costRange: "₹200 - ₹600",
      },
    ],
    toAvoid: [
      {
        id: "t031",
        name: "MRI for every arthritic joint",
        description: "Detailed soft tissue imaging",
        purpose: "Expensive and usually unnecessary unless planning surgery or diagnosis unclear",
        costRange: "₹4,000 - ₹12,000 per area",
      },
    ],
    reasoning: "Basic imaging and blood tests help diagnose type and severity of arthritis. Advanced imaging reserved for specific indications.",
  },
  d009: { // Allergic Rhinitis
    recommended: [
      {
        id: "t032",
        name: "Allergy Skin Prick Test",
        description: "Small amounts of allergens applied to skin to check reactions",
        purpose: "Identify specific allergens causing symptoms",
        costRange: "₹1,000 - ₹3,000",
        preparation: "Stop antihistamines 3-7 days before test",
      },
      {
        id: "t033",
        name: "IgE Blood Test",
        description: "Measures allergy antibodies in blood",
        purpose: "Alternative to skin testing, identifies allergens",
        costRange: "₹800 - ₹3,000",
      },
    ],
    toAvoid: [
      {
        id: "t034",
        name: "Sinus CT Scan",
        description: "Detailed imaging of sinuses",
        purpose: "Not needed for simple allergic rhinitis unless chronic sinusitis suspected",
        costRange: "₹2,000 - ₹6,000",
      },
      {
        id: "t035",
        name: "Complete Immunology Panel",
        description: "Comprehensive immune system testing",
        purpose: "Excessive for allergic rhinitis; basic allergy testing is sufficient",
        costRange: "₹5,000 - ₹15,000",
      },
    ],
    reasoning: "Identifying specific allergens helps with avoidance and targeted treatment. Extensive testing is rarely needed.",
  },
  d010: { // Anxiety Disorder
    recommended: [
      {
        id: "t036",
        name: "Thyroid Function Tests",
        description: "Blood tests for thyroid hormones",
        purpose: "Rule out thyroid problems that can mimic anxiety",
        costRange: "₹300 - ₹800",
      },
      {
        id: "t037",
        name: "Vitamin B12 & D Levels",
        description: "Blood tests for vitamin levels",
        purpose: "Deficiencies can contribute to anxiety symptoms",
        costRange: "₹400 - ₹1,200",
      },
      {
        id: "t038",
        name: "ECG (if palpitations present)",
        description: "Heart rhythm check",
        purpose: "Rule out heart rhythm problems if palpitations are a symptom",
        costRange: "₹150 - ₹500",
      },
    ],
    toAvoid: [
      {
        id: "t039",
        name: "Brain MRI for anxiety alone",
        description: "Detailed brain imaging",
        purpose: "Not indicated for anxiety without neurological symptoms",
        costRange: "₹4,000 - ₹12,000",
      },
      {
        id: "t040",
        name: "Full Hormone Panel",
        description: "Comprehensive hormone testing",
        purpose: "Excessive for simple anxiety; targeted testing is more appropriate",
        costRange: "₹3,000 - ₹10,000",
      },
    ],
    reasoning: "Testing focuses on ruling out medical causes of anxiety symptoms. Anxiety is primarily a clinical diagnosis.",
  },
};
