"""
Report OCR Module
Extracts blood test parameter values from uploaded report images using OCR
"""

import os
import re
import base64
from io import BytesIO
from PIL import Image
import pytesseract
import cv2
import numpy as np

# Resolve Tesseract path via env or common locations (Windows) for deployment
env_tess = os.getenv('TESSERACT_CMD')
if env_tess and os.path.exists(env_tess):
    pytesseract.pytesseract.tesseract_cmd = env_tess
elif os.name == 'nt':  # Windows common install paths
    possible_paths = [
        r'C:\\Program Files\\Tesseract-OCR\\tesseract.exe',
        r'C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe',
        r'C:\\Users\\{}\\AppData\\Local\\Tesseract-OCR\\tesseract.exe'.format(os.getenv('USERNAME', '')),
    ]
    for path in possible_paths:
        if os.path.exists(path):
            pytesseract.pytesseract.tesseract_cmd = path
            break


class ReportOCR:
    """OCR service for extracting blood test values from images"""
    
    def __init__(self):
        self.parameter_keywords = {
            'hemoglobin': ['hemoglobin', 'haemoglobin', 'hb', 'hgb'],
            'wbc': ['wbc', 'white blood cell', 'leukocyte', 'white cell count', 'w.b.c'],
            'platelets': ['platelet', 'plt', 'thrombocyte', 'platelet count'],
            'rbc': ['rbc', 'red blood cell', 'erythrocyte', 'red cell count'],
            'blood_sugar_fasting': [
                'fasting blood sugar', 'blood sugar fasting', 'fasting glucose', 'fbs',
                'blood glucose', 'sugar (fasting)', 'glucose fasting'
            ],
            'total_cholesterol': ['total cholesterol', 'cholesterol total', 'total chol', 'chol'],
            'hdl_cholesterol': ['hdl', 'hdl cholesterol', 'high density lipoprotein'],
            'ldl_cholesterol': ['ldl', 'ldl cholesterol', 'low density lipoprotein'],
            'creatinine': ['creatinine', 'serum creat'],
            'sgot': ['sgot', 'ast', 'aspartate aminotransferase'],
            'sgpt': ['sgpt', 'alt', 'alanine aminotransferase']
        }
        self.tesseract_ready = self._check_tesseract()
    
    def preprocess_image(self, image: Image.Image) -> Image.Image:
        """Preprocess image to improve OCR accuracy"""
        # Convert to numpy array
        img_array = np.array(image)
        
        # Convert to grayscale if needed
        if len(img_array.shape) == 3:
            gray = cv2.cvtColor(img_array, cv2.COLOR_RGB2GRAY)
        else:
            gray = img_array
        
        # Apply denoising
        denoised = cv2.fastNlMeansDenoising(gray, None, 10, 7, 21)
        
        # Apply thresholding to get binary image
        _, binary = cv2.threshold(denoised, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        
        # Enhance contrast
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(binary)
        
        # Convert back to PIL Image
        return Image.fromarray(enhanced)
    
    def extract_text(self, image: Image.Image) -> str:
        """Extract text from image using OCR"""
        # Preprocess image
        processed_image = self.preprocess_image(image)
        
        if not self.tesseract_ready:
            raise RuntimeError("Tesseract OCR engine not available. Install Tesseract or set TESSERACT_CMD env path.")

        custom_config = r'--oem 3 --psm 6'
        text = pytesseract.image_to_string(processed_image, config=custom_config)
        
        return text
    
    def parse_parameter_value(self, text: str, param_keywords: list) -> float:
        """Extract numeric value for a parameter from OCR text"""
        # Clean text
        text_lower = text.lower()
        normalized = text_lower.replace(',', '.')

        lines = normalized.split('\n')
        for line in lines:
            line_lower = line.strip()
            if not line_lower:
                continue
            
            if any(keyword in line_lower for keyword in param_keywords):
                cleaned_line = re.sub(r'[\|\t]+', ' ', line_lower)
                cleaned_line = cleaned_line.replace(' x10^3', '000').replace(' x10³', '000')
                matches = re.findall(r'([-+]?\d*\.\d+|\d+)', cleaned_line)
                if matches:
                    try:
                        for num_str in matches:
                            try:
                                value = float(num_str)
                            except ValueError:
                                continue
                            if 0.05 <= value <= 1000:
                                return value
                    except ValueError:
                        continue

        pattern = r'(' + '|'.join([re.escape(k) for k in param_keywords]) + r')[^\d]{0,10}([-+]?\d*\.\d+|\d+)'
        inline_matches = re.findall(pattern, normalized)
        for _, value_str in inline_matches:
            try:
                value = float(value_str)
                if 0.05 <= value <= 1000:
                    return value
            except ValueError:
                continue
        
        return None
    
    def extract_values_from_text(self, ocr_text: str) -> dict:
        """Extract all parameter values from OCR text"""
        extracted_values = {}
        
        print(f"OCR Text extracted:\n{ocr_text[:500]}...")  # Debug: print first 500 chars
        
        # Try to extract each parameter
        for param_key, keywords in self.parameter_keywords.items():
            value = self.parse_parameter_value(ocr_text, keywords)
            if value is not None:
                # Map to frontend parameter names
                frontend_key = param_key
                if param_key == 'blood_sugar_fasting':
                    frontend_key = 'bloodSugar'
                elif param_key == 'total_cholesterol':
                    frontend_key = 'cholesterol'
                
                extracted_values[frontend_key] = value
                print(f"Extracted {param_key}: {value}")
        
        return extracted_values
    
    def process_image(self, image_data: bytes) -> dict:
        """
        Process uploaded image and extract blood test values
        Args:
            image_data: Raw image bytes
        Returns:
            Dictionary with extracted parameter values
        """
        try:
            # Load image from bytes
            if not self.tesseract_ready:
                return {
                    "success": False,
                    "error": "TesseractNotFound",
                    "message": "Tesseract OCR engine not available. Install Tesseract or set TESSERACT_CMD environment variable."
                }

            image = Image.open(BytesIO(image_data))
            
            # Extract text using OCR
            ocr_text = self.extract_text(image)
            
            # Extract parameter values
            extracted_values = self.extract_values_from_text(ocr_text)
            
            return {
                "success": True,
                "extractedValues": extracted_values,
                "ocrText": ocr_text[:1000],  # Return first 1000 chars for debugging
                "message": f"Successfully extracted {len(extracted_values)} parameter(s) from image"
            }
        except Exception as e:
            print(f"Error processing image: {str(e)}")
            import traceback
            traceback.print_exc()
            return {
                "success": False,
                "error": str(e),
                "extractedValues": {},
                "message": f"Failed to process image: {str(e)}"
            }
    
    def process_base64_image(self, base64_string: str) -> dict:
        """Process base64-encoded image"""
        try:
            # Remove data URL prefix if present
            if ',' in base64_string:
                base64_string = base64_string.split(',')[1]
            
            # Decode base64
            image_data = base64.b64decode(base64_string)
            
            return self.process_image(image_data)
        except Exception as e:
            return {
                "success": False,
                "error": str(e),
                "extractedValues": {},
                "message": f"Failed to decode image: {str(e)}"
            }

    def _check_tesseract(self):
        try:
            pytesseract.get_tesseract_version()
            return True
        except Exception:
            print("WARNING: Tesseract OCR not found. Image scanning will be disabled until Tesseract is installed.")
            return False

