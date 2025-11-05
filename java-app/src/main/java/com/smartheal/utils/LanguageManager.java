package com.smartheal.utils;

import java.util.*;

public class LanguageManager {
    public enum Language {
        ENGLISH("English", "en"),
        HINDI("हिंदी", "hi"),
        MARATHI("मराठी", "mr");
        
        private final String displayName;
        private final String code;
        
        Language(String displayName, String code) {
            this.displayName = displayName;
            this.code = code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    private static Language currentLanguage = Language.ENGLISH;
    private static final Map<String, Map<Language, String>> translations = new HashMap<>();
    
    static {
        initializeTranslations();
    }
    
    public static void setLanguage(Language language) {
        currentLanguage = language;
    }
    
    public static Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static String translate(String englishText) {
        if (currentLanguage == Language.ENGLISH) {
            return englishText;
        }
        
        Map<Language, String> translations = LanguageManager.translations.get(englishText.toLowerCase());
        if (translations != null && translations.containsKey(currentLanguage)) {
            return translations.get(currentLanguage);
        }
        
        // If translation not found, return original
        return englishText;
    }
    
    public static String translateSymptom(String englishSymptom) {
        return translate(englishSymptom);
    }
    
    private static void initializeTranslations() {
        // Common Symptoms Translations
        addTranslation("Fever", "बुखार", "ताप");
        addTranslation("Headache", "सिरदर्द", "डोकेदुखी");
        addTranslation("Cough", "खांसी", "खोकला");
        addTranslation("Runny Nose", "नाक बहना", "नाक वाहणे");
        addTranslation("Sore Throat", "गले में खराश", "घशात खराश");
        addTranslation("Sneezing", "छींक आना", "शिंकणे");
        addTranslation("Fatigue", "थकान", "थकवा");
        addTranslation("Body Aches", "शरीर में दर्द", "शरीरात वेदना");
        addTranslation("Nausea", "मतली", "मळमळ");
        addTranslation("Vomiting", "उल्टी", "ओकारी");
        addTranslation("Diarrhea", "दस्त", "अतिसार");
        addTranslation("Abdominal Pain", "पेट में दर्द", "पोटात वेदना");
        addTranslation("Chest Pain", "छाती में दर्द", "छातीत वेदना");
        addTranslation("Chest Discomfort", "छाती में बेचैनी", "छातीत अस्वस्थता");
        addTranslation("Shortness of Breath", "सांस लेने में तकलीफ", "श्वास घेण्यात अडचण");
        addTranslation("Wheezing", "घरघराहट", "घरघर");
        addTranslation("Nasal Congestion", "नाक बंद", "नाक बंद");
        addTranslation("Chills", "ठंड लगना", "थंडी");
        addTranslation("Sweating", "पसीना आना", "घाम");
        addTranslation("Dizziness", "चक्कर आना", "चक्कर");
        addTranslation("Blurred Vision", "धुंधली दृष्टि", "अंधुक दृष्टी");
        addTranslation("Joint Pain", "जोड़ों में दर्द", "सांधे दुखणे");
        addTranslation("Swelling", "सूजन", "सूज");
        addTranslation("Stiffness", "अकड़न", "ताठरपणा");
        addTranslation("Back Pain", "पीठ दर्द", "पाठीचा वेदना");
        addTranslation("Muscle Pain", "मांसपेशियों में दर्द", "स्नायूंमध्ये वेदना");
        addTranslation("Loss of Appetite", "भूख न लगना", "क्षुधा नसणे");
        addTranslation("Weight Loss", "वजन कम होना", "वजन कमी");
        addTranslation("Weight Gain", "वजन बढ़ना", "वजन वाढ");
        addTranslation("Increased Thirst", "प्यास लगना", "तहान लागणे");
        addTranslation("Frequent Urination", "बार-बार पेशाब आना", "वारंवार लघवी");
        addTranslation("Burning Sensation", "जलन", "जळजळ");
        addTranslation("Rash", "चकत्ते", "पुरळ");
        addTranslation("Itching", "खुजली", "तुरटी");
        addTranslation("Redness", "लाली", "लाली");
        addTranslation("Confusion", "भ्रम", "गोंधळ");
        addTranslation("Memory Problems", "याददाश्त की समस्या", "स्मृती समस्या");
        addTranslation("Difficulty Concentrating", "ध्यान केंद्रित करने में कठिनाई", "लक्ष केंद्रित करण्यात अडचण");
        addTranslation("Anxiety", "चिंता", "चिंता");
        addTranslation("Depression", "अवसाद", "नैराश्य");
        addTranslation("Insomnia", "अनिद्रा", "अनिद्रा");
        addTranslation("Irregular Heartbeat", "अनियमित दिल की धड़कन", "अनियमित हृदय गती");
        addTranslation("High Blood Pressure", "उच्च रक्तचाप", "उच्च रक्तदाब");
        addTranslation("Low Blood Pressure", "निम्न रक्तचाप", "कमी रक्तदाब");
        addTranslation("Chest Tightness", "छाती में जकड़न", "छातीत घट्टपणा");
        addTranslation("Chronic Cough", "पुरानी खांसी", "जुन्या खोकला");
        addTranslation("Mucus Production", "कफ बनना", "श्लेष्मा उत्पादन");
        addTranslation("Facial Pain", "चेहरे में दर्द", "चेहर्यात वेदना");
        addTranslation("Sensitivity to Light", "प्रकाश के प्रति संवेदनशीलता", "प्रकाशाकडे संवेदनशीलता");
        addTranslation("Sensitivity to Sound", "ध्वनि के प्रति संवेदनशीलता", "आवाजाकडे संवेदनशीलता");
        addTranslation("Loss of Balance", "संतुलन खोना", "संतुलन गमावणे");
        addTranslation("Numbness", "सुन्नता", "बधीरपणा");
        addTranslation("Tingling", "झुनझुनी", "चुरचुर");
        addTranslation("Weakness", "कमजोरी", "अशक्तपणा");
        addTranslation("Pale Skin", "पीली त्वचा", "फिक्का त्वचा");
        addTranslation("Yellow Skin", "पीली त्वचा", "पिवळी त्वचा");
        addTranslation("Dark Urine", "गहरा मूत्र", "गडद मूत्र");
        addTranslation("Cloudy Urine", "बादल जैसा मूत्र", "ढगाळ मूत्र");
        addTranslation("Pelvic Pain", "श्रोणि में दर्द", "श्रोणी वेदना");
        addTranslation("Bleeding", "रक्तस्राव", "रक्तस्राव");
        addTranslation("Bruising", "चोट लगना", "निळेपणा");
        addTranslation("Hair Loss", "बाल झड़ना", "केस गळणे");
        addTranslation("Dry Skin", "सूखी त्वचा", "कोरडी त्वचा");
        addTranslation("Cold Hands", "ठंडे हाथ", "थंड हात");
        addTranslation("Hot Flashes", "गर्मी लगना", "उष्णता");
        addTranslation("Night Sweats", "रात में पसीना", "रात्री घाम");
        addTranslation("Constipation", "कब्ज", "मलबद्धता");
        addTranslation("Bloating", "पेट फूलना", "पोट फुगणे");
        addTranslation("Gas", "गैस", "वायू");
        addTranslation("Cramping", "मरोड़", "खेच");
        addTranslation("Heartburn", "सीने में जलन", "हृदयज्वाला");
        addTranslation("Indigestion", "अपच", "अपचन");
        addTranslation("Loss of Taste", "स्वाद न आना", "चव नसणे");
        addTranslation("Loss of Smell", "गंध न आना", "वास नसणे");
        addTranslation("Ear Pain", "कान में दर्द", "कानात वेदना");
        addTranslation("Ear Discharge", "कान से पानी", "कानातून स्त्राव");
        addTranslation("Sore Eyes", "आंखों में खराश", "डोळ्यात खराश");
        addTranslation("Watery Eyes", "आंसू आना", "डोळे पाणी");
        addTranslation("Red Eyes", "लाल आंखें", "लाल डोळे");
        addTranslation("Itchy Eyes", "आंखों में खुजली", "डोळ्यात तुरटी");
        addTranslation("Sensitivity to Light", "प्रकाश के प्रति संवेदनशीलता", "प्रकाशाकडे संवेदनशीलता");
        addTranslation("Blurred Vision", "धुंधली दृष्टि", "अंधुक दृष्टी");
        addTranslation("Double Vision", "दोहरी दृष्टि", "दुहेरी दृष्टी");
        addTranslation("Eye Floaters", "आंखों में धब्बे", "डोळ्यात तरंग");
        addTranslation("Neck Pain", "गर्दन में दर्द", "मानेचा वेदना");
        addTranslation("Neck Stiffness", "गर्दन में अकड़न", "मानेचा ताठरपणा");
        addTranslation("Shoulder Pain", "कंधे में दर्द", "खांद्यात वेदना");
        addTranslation("Elbow Pain", "कोहनी में दर्द", "कोपरात वेदना");
        addTranslation("Wrist Pain", "कलाई में दर्द", "मनगटात वेदना");
        addTranslation("Hip Pain", "कूल्हे में दर्द", "नितंब वेदना");
        addTranslation("Knee Pain", "घुटने में दर्द", "गुडघ्यात वेदना");
        addTranslation("Ankle Pain", "टखने में दर्द", "घोट्यात वेदना");
        addTranslation("Foot Pain", "पैर में दर्द", "पायात वेदना");
        addTranslation("Toe Pain", "पैर की उंगली में दर्द", "पायाच्या बोटात वेदना");
        addTranslation("Finger Pain", "उंगली में दर्द", "बोटात वेदना");
        addTranslation("Hand Pain", "हाथ में दर्द", "हातात वेदना");
        addTranslation("Arm Pain", "बांह में दर्द", "हातात वेदना");
        addTranslation("Leg Pain", "पैर में दर्द", "पायात वेदना");
        addTranslation("Lower Back Pain", "कमर दर्द", "कमरेचा वेदना");
        addTranslation("Upper Back Pain", "ऊपरी पीठ दर्द", "वरच्या पाठीचा वेदना");
        addTranslation("Spine Pain", "रीढ़ में दर्द", "पाठीचा कणा वेदना");
        addTranslation("Rib Pain", "पसली में दर्द", "बरगड्यात वेदना");
        addTranslation("Groin Pain", "जांघ में दर्द", "जांघीचा वेदना");
        addTranslation("Bone Spurs", "हड्डी का उभार", "हाडांचा उभार");
        addTranslation("Reduced Range of Motion", "गति सीमित होना", "हालचाल मर्यादित");
        addTranslation("Warm Joints", "गर्म जोड़", "उबदार सांधे");
        addTranslation("Morning Stiffness", "सुबह की अकड़न", "सकाळचा ताठरपणा");
        addTranslation("Reduced Flexibility", "लचीलापन कम होना", "लवचिकता कमी");
        addTranslation("Widespread Pain", "व्यापक दर्द", "व्यापक वेदना");
        addTranslation("Sleep Problems", "नींद की समस्या", "झोपेची समस्या");
        addTranslation("Cognitive Difficulties", "संज्ञानात्मक कठिनाइयाँ", "संज्ञानात्मक अडचणी");
        addTranslation("Severe Fatigue", "गंभीर थकान", "गंभीर थकवा");
        addTranslation("Sleep Problems", "नींद की समस्या", "झोपेची समस्या");
        addTranslation("Muscle Pain", "मांसपेशियों में दर्द", "स्नायूंमध्ये वेदना");
        addTranslation("Memory Problems", "याददाश्त की समस्या", "स्मृती समस्या");
        addTranslation("Loud Snoring", "जोर से खर्राटे", "मोठ्याने घोरतो");
        addTranslation("Daytime Sleepiness", "दिन में नींद आना", "दिवसा झोप");
        addTranslation("Morning Headaches", "सुबह सिरदर्द", "सकाळी डोकेदुखी");
        addTranslation("Difficulty Concentrating", "ध्यान केंद्रित करने में कठिनाई", "लक्ष केंद्रित करण्यात अडचण");
        addTranslation("Irritability", "चिड़चिड़ापन", "चिडचिडेपणा");
        addTranslation("Swelling", "सूजन", "सूज");
        addTranslation("Itchy Skin", "त्वचा में खुजली", "त्वचेत तुरटी");
        addTranslation("Muscle Cramps", "मांसपेशियों में ऐंठन", "स्नायूंमध्ये खेच");
        addTranslation("Jaundice", "पीलिया", "कावीळ");
        addTranslation("Nausea", "मतली", "मळमळ");
        addTranslation("Loss of Appetite", "भूख न लगना", "क्षुधा नसणे");
        addTranslation("Dark Urine", "गहरा मूत्र", "गडद मूत्र");
        addTranslation("Sudden Weakness", "अचानक कमजोरी", "अचानक अशक्तपणा");
        addTranslation("Numbness", "सुन्नता", "बधीरपणा");
        addTranslation("Confusion", "भ्रम", "गोंधळ");
        addTranslation("Difficulty Speaking", "बोलने में कठिनाई", "बोलण्यात अडचण");
        addTranslation("Vision Problems", "दृष्टि समस्या", "दृष्टी समस्या");
        addTranslation("Severe Headache", "तीव्र सिरदर्द", "तीव्र डोकेदुखी");
        addTranslation("Dehydration", "निर्जलीकरण", "निर्जलीकरण");
        addTranslation("Loss of Interest", "रुचि न होना", "आवड नसणे");
        addTranslation("Appetite Changes", "भूख में बदलाव", "क्षुधा बदल");
        addTranslation("Persistent Sadness", "लगातार उदासी", "सतत उदासी");
        addTranslation("Excessive Worry", "अत्यधिक चिंता", "अत्यधिक चिंता");
        addTranslation("Restlessness", "बेचैनी", "अस्वस्थता");
        addTranslation("Rapid Heartbeat", "तेज दिल की धड़कन", "वेगवान हृदय गती");
        addTranslation("Trembling", "कंपन", "थरथरणे");
        addTranslation("Difficulty Sleeping", "सोने में कठिनाई", "झोपण्यात अडचण");
        addTranslation("Regurgitation", "उल्टी आना", "उलटी");
        addTranslation("Difficulty Swallowing", "निगलने में कठिनाई", "गिळण्यात अडचण");
        addTranslation("Difficulty Breathing", "सांस लेने में कठिनाई", "श्वास घेण्यात अडचण");
        addTranslation("Slow Healing", "घाव धीरे भरना", "जखम हळू भरणे");
    }
    
    private static void addTranslation(String english, String hindi, String marathi) {
        Map<Language, String> trans = new HashMap<>();
        trans.put(Language.ENGLISH, english);
        trans.put(Language.HINDI, hindi);
        trans.put(Language.MARATHI, marathi);
        translations.put(english.toLowerCase(), trans);
    }
    
    public static String getUILabel(String key) {
        Map<String, Map<Language, String>> uiLabels = new HashMap<>();
        
        // UI Labels
        uiLabels.put("select_symptoms", createLabelMap("Select Your Symptoms", "अपने लक्षण चुनें", "तुमची लक्षणे निवडा"));
        uiLabels.put("analysis_results", createLabelMap("Analysis Results", "विश्लेषण परिणाम", "विश्लेषण परिणाम"));
        uiLabels.put("analyze", createLabelMap("Analyze Symptoms", "लक्षणों का विश्लेषण करें", "लक्षणांचे विश्लेषण करा"));
        uiLabels.put("clear_all", createLabelMap("Clear All", "सभी साफ करें", "सर्व साफ करा"));
        uiLabels.put("search_symptoms", createLabelMap("Search symptoms...", "लक्षण खोजें...", "लक्षण शोधा..."));
        uiLabels.put("selected_symptoms", createLabelMap("Selected Symptoms", "चयनित लक्षण", "निवडलेली लक्षणे"));
        uiLabels.put("description", createLabelMap("Description", "विवरण", "वर्णन"));
        uiLabels.put("matched_symptoms", createLabelMap("Matched Symptoms", "मेल खाने वाले लक्षण", "जुळलेली लक्षणे"));
        uiLabels.put("common_treatments", createLabelMap("Common Treatments", "सामान्य उपचार", "सामान्य उपचार"));
        uiLabels.put("when_to_seek_help", createLabelMap("When to Seek Medical Help", "चिकित्सकीय सहायता कब लें", "वैद्यकीय मदत कधी घ्यावी"));
        uiLabels.put("severity", createLabelMap("Severity", "गंभीरता", "गंभीरता"));
        uiLabels.put("match", createLabelMap("Match", "मेल", "जुळणे"));
        uiLabels.put("no_results", createLabelMap("No matching conditions found", "कोई मेल खाने वाली स्थिति नहीं मिली", "जुळणारी स्थिती सापडली नाही"));
        
        Map<Language, String> labelMap = uiLabels.get(key);
        if (labelMap != null && labelMap.containsKey(currentLanguage)) {
            return labelMap.get(currentLanguage);
        }
        return key;
    }
    
    private static Map<Language, String> createLabelMap(String en, String hi, String mr) {
        Map<Language, String> map = new HashMap<>();
        map.put(Language.ENGLISH, en);
        map.put(Language.HINDI, hi);
        map.put(Language.MARATHI, mr);
        return map;
    }
}

