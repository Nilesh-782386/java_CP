import type { Symptom } from "@shared/schema";

export const symptomsDatabase: Symptom[] = [
  // Respiratory Symptoms
  { id: "s001", name: "Fever", category: "General", description: "Body temperature above 100.4°F (38°C)" },
  { id: "s002", name: "Cough", category: "Respiratory", description: "Persistent or dry cough" },
  { id: "s003", name: "Shortness of breath", category: "Respiratory", description: "Difficulty breathing or feeling breathless" },
  { id: "s004", name: "Chest pain", category: "Respiratory", description: "Pain or discomfort in chest area" },
  { id: "s005", name: "Sore throat", category: "Respiratory", description: "Pain or irritation in throat" },
  { id: "s006", name: "Runny nose", category: "Respiratory", description: "Nasal discharge or congestion" },
  { id: "s007", name: "Sneezing", category: "Respiratory", description: "Frequent sneezing" },

  // Digestive Symptoms
  { id: "s008", name: "Nausea", category: "Digestive", description: "Feeling of wanting to vomit" },
  { id: "s009", name: "Vomiting", category: "Digestive", description: "Expelling stomach contents" },
  { id: "s010", name: "Diarrhea", category: "Digestive", description: "Loose or watery stools" },
  { id: "s011", name: "Abdominal pain", category: "Digestive", description: "Pain in stomach area" },
  { id: "s012", name: "Constipation", category: "Digestive", description: "Difficulty passing stools" },
  { id: "s013", name: "Loss of appetite", category: "Digestive", description: "Reduced desire to eat" },
  { id: "s014", name: "Bloating", category: "Digestive", description: "Feeling of fullness in abdomen" },

  // Neurological Symptoms
  { id: "s015", name: "Headache", category: "Neurological", description: "Pain in head region" },
  { id: "s016", name: "Dizziness", category: "Neurological", description: "Feeling lightheaded or unsteady" },
  { id: "s017", name: "Fatigue", category: "General", description: "Extreme tiredness or weakness" },
  { id: "s018", name: "Confusion", category: "Neurological", description: "Difficulty thinking clearly" },
  { id: "s019", name: "Numbness", category: "Neurological", description: "Loss of sensation in body parts" },
  { id: "s020", name: "Tingling", category: "Neurological", description: "Pins and needles sensation" },

  // Musculoskeletal Symptoms
  { id: "s021", name: "Joint pain", category: "Musculoskeletal", description: "Pain in joints" },
  { id: "s022", name: "Muscle ache", category: "Musculoskeletal", description: "Pain in muscles" },
  { id: "s023", name: "Back pain", category: "Musculoskeletal", description: "Pain in back region" },
  { id: "s024", name: "Stiffness", category: "Musculoskeletal", description: "Reduced flexibility in joints" },
  { id: "s025", name: "Swelling", category: "Musculoskeletal", description: "Inflammation or fluid retention" },

  // Skin Symptoms
  { id: "s026", name: "Rash", category: "Dermatological", description: "Red or irritated skin patches" },
  { id: "s027", name: "Itching", category: "Dermatological", description: "Desire to scratch skin" },
  { id: "s028", name: "Bruising", category: "Dermatological", description: "Discoloration from injury" },
  { id: "s029", name: "Dry skin", category: "Dermatological", description: "Flaky or rough skin" },

  // Cardiovascular Symptoms
  { id: "s030", name: "Palpitations", category: "Cardiovascular", description: "Irregular heartbeat sensation" },
  { id: "s031", name: "High blood pressure", category: "Cardiovascular", description: "Elevated blood pressure readings" },
  { id: "s032", name: "Rapid heartbeat", category: "Cardiovascular", description: "Unusually fast heart rate" },

  // General Symptoms
  { id: "s033", name: "Weight loss", category: "General", description: "Unintentional weight reduction" },
  { id: "s034", name: "Weight gain", category: "General", description: "Unintentional weight increase" },
  { id: "s035", name: "Chills", category: "General", description: "Feeling cold with shivering" },
  { id: "s036", name: "Night sweats", category: "General", description: "Excessive sweating during sleep" },
  { id: "s037", name: "Excessive thirst", category: "General", description: "Unusual increase in thirst" },
  { id: "s038", name: "Frequent urination", category: "General", description: "Need to urinate more often" },
  { id: "s039", name: "Blurred vision", category: "Sensory", description: "Unclear or foggy vision" },
  { id: "s040", name: "Anxiety", category: "Psychological", description: "Feeling of worry or unease" },
];
