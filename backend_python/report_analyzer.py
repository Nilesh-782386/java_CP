"""
Report Analyzer Module
Analyzes blood test reports and flags abnormal values
"""

import json
import os


class ReportAnalyzer:
    """Analyze blood test reports"""
    
    def __init__(self, reference_ranges_path='datasets/blood_reference_ranges.json'):
        self.reference_ranges_path = reference_ranges_path
        self.reference_ranges = {}
        self.load_reference_ranges()
    
    def load_reference_ranges(self):
        """Load normal reference ranges for blood parameters"""
        if os.path.exists(self.reference_ranges_path):
            with open(self.reference_ranges_path, 'r', encoding='utf-8') as f:
                self.reference_ranges = json.load(f)
        else:
            print("Creating default reference ranges...")
            self.reference_ranges = self._create_default_ranges()
            os.makedirs(os.path.dirname(self.reference_ranges_path), exist_ok=True)
            with open(self.reference_ranges_path, 'w', encoding='utf-8') as f:
                json.dump(self.reference_ranges, f, indent=2, ensure_ascii=False)
    
    def _create_default_ranges(self):
        """Create default blood parameter reference ranges"""
        return {
            "hemoglobin": {
                "name": "Hemoglobin",
                "normalMin": 12.0,
                "normalMax": 16.0,
                "unit": "g/dL",
                "low_threshold": 12.0,
                "high_threshold": 16.0
            },
            "wbc": {
                "name": "WBC Count",
                "normalMin": 4000,
                "normalMax": 11000,
                "unit": "cells/µL",
                "low_threshold": 4000,
                "high_threshold": 11000
            },
            "platelets": {
                "name": "Platelet Count",
                "normalMin": 150000,
                "normalMax": 450000,
                "unit": "cells/µL",
                "low_threshold": 150000,
                "high_threshold": 450000
            },
            "rbc": {
                "name": "RBC Count",
                "normalMin": 4.5,
                "normalMax": 5.5,
                "unit": "million cells/µL",
                "low_threshold": 4.5,
                "high_threshold": 5.5
            },
            "blood_sugar_fasting": {
                "name": "Blood Sugar (Fasting)",
                "normalMin": 70,
                "normalMax": 100,
                "unit": "mg/dL",
                "low_threshold": 70,
                "high_threshold": 100
            },
            "total_cholesterol": {
                "name": "Total Cholesterol",
                "normalMin": 0,
                "normalMax": 200,
                "unit": "mg/dL",
                "low_threshold": 0,
                "high_threshold": 200
            },
            "hdl_cholesterol": {
                "name": "HDL Cholesterol",
                "normalMin": 40,
                "normalMax": 100,
                "unit": "mg/dL",
                "low_threshold": 40,
                "high_threshold": 100
            },
            "ldl_cholesterol": {
                "name": "LDL Cholesterol",
                "normalMin": 0,
                "normalMax": 100,
                "unit": "mg/dL",
                "low_threshold": 0,
                "high_threshold": 100
            },
            "creatinine": {
                "name": "Creatinine",
                "normalMin": 0.6,
                "normalMax": 1.2,
                "unit": "mg/dL",
                "low_threshold": 0.6,
                "high_threshold": 1.2
            },
            "sgot": {
                "name": "SGOT (AST)",
                "normalMin": 10,
                "normalMax": 40,
                "unit": "U/L",
                "low_threshold": 10,
                "high_threshold": 40
            },
            "sgpt": {
                "name": "SGPT (ALT)",
                "normalMin": 10,
                "normalMax": 40,
                "unit": "U/L",
                "low_threshold": 10,
                "high_threshold": 40
            }
        }
    
    def analyze(self, report_data: dict) -> dict:
        """
        Analyze blood test report
        Args:
            report_data: Dictionary with blood test values
        Returns:
            Dictionary with analysis results
        """
        parameters = []
        flagged_parameters = []
        
        # Parameter name mapping (Java frontend -> Python backend)
        param_name_mapping = {
            "bloodSugar": "blood_sugar_fasting",
            "cholesterol": "total_cholesterol",
            "blood_sugar": "blood_sugar_fasting",
            "totalCholesterol": "total_cholesterol"
        }
        
        # Analyze each parameter
        for param_key, value in report_data.items():
            # Skip None, empty strings, or empty values
            if value is None:
                continue
            if isinstance(value, str) and value.strip() == '':
                continue
            
            # Convert to float (handles both string and numeric inputs)
            try:
                if isinstance(value, str):
                    value = float(value.strip())
                else:
                    value = float(value)
            except (ValueError, TypeError) as e:
                print(f"Warning: Cannot convert value '{value}' for parameter '{param_key}': {e}")
                continue
            
            # Map parameter name if needed
            mapped_key = param_name_mapping.get(param_key, param_key)
            
            # Get reference range
            ref_range = self.reference_ranges.get(mapped_key)
            if not ref_range:
                # Try original key if mapped key not found
                ref_range = self.reference_ranges.get(param_key)
                if not ref_range:
                    print(f"Warning: Unknown parameter '{param_key}' - skipping")
                    continue
            
            # Determine status
            status = self._determine_status(value, ref_range)
            
            # Debug logging
            print(f"Analyzing parameter: {param_key} -> {mapped_key}")
            print(f"  Value: {value}, Range: {ref_range['normalMin']}-{ref_range['normalMax']}, Status: {status}")
            
            # Use mapped key for interpretation lookup
            interpretation_key = mapped_key if mapped_key in param_name_mapping.values() else param_key
            
            # Create parameter result
            param_result = {
                "name": ref_range["name"],
                "value": value,
                "status": status,
                "unit": ref_range["unit"],
                "normalMin": ref_range["normalMin"],
                "normalMax": ref_range["normalMax"],
                "interpretation": self._get_interpretation(interpretation_key, value, status, ref_range)
            }
            
            parameters.append(param_result)
            
            # Flag if abnormal
            if status != "normal":
                flagged_parameters.append(ref_range["name"])
        
        # Determine overall status
        if not parameters:
            return {
                "overallStatus": "error",
                "summary": "No valid blood test parameters provided. Please enter at least one parameter.",
                "parameters": [],
                "flaggedParameters": [],
                "recommendations": ["Please enter valid blood test values"]
            }
        
        overall_status = self._determine_overall_status(parameters)
        
        # Generate summary
        summary = self._generate_summary(parameters, flagged_parameters, overall_status)
        
        # Generate recommendations
        recommendations = self._generate_recommendations(parameters, flagged_parameters, overall_status)
        
        return {
            "overallStatus": overall_status,
            "summary": summary,
            "parameters": parameters,
            "flaggedParameters": flagged_parameters,
            "recommendations": recommendations
        }
    
    def _determine_status(self, value: float, ref_range: dict) -> str:
        """Determine if value is normal, low, or high"""
        normal_min = ref_range.get("normalMin", 0)
        normal_max = ref_range.get("normalMax", 0)
        
        # Debug logging
        print(f"  Status check: value={value}, min={normal_min}, max={normal_max}")
        
        if value < normal_min:
            print(f"  -> LOW (value {value} < min {normal_min})")
            return "low"
        elif value > normal_max:
            print(f"  -> HIGH (value {value} > max {normal_max})")
            return "high"
        else:
            print(f"  -> NORMAL (value {value} between {normal_min} and {normal_max})")
            return "normal"
    
    def _get_interpretation(self, param_key: str, value: float, status: str, ref_range: dict) -> str:
        """Get interpretation for a parameter"""
        if status == "normal":
            return "Within normal range"
        
        interpretations = {
            "hemoglobin": {
                "low": "May indicate anemia. Consider iron supplements and consult a healthcare provider.",
                "high": "May indicate polycythemia or dehydration. Consult a healthcare provider."
            },
            "wbc": {
                "low": "May indicate immune suppression or bone marrow issues. Consult a healthcare provider.",
                "high": "May indicate infection or inflammation. Consult a healthcare provider."
            },
            "platelets": {
                "low": "May indicate bleeding risk. Consult a healthcare provider immediately.",
                "high": "May indicate clotting risk. Consult a healthcare provider."
            },
            "rbc": {
                "low": "May indicate anemia. Consult a healthcare provider.",
                "high": "May indicate polycythemia. Consult a healthcare provider."
            },
            "blood_sugar_fasting": {
                "low": "May indicate hypoglycemia. Monitor and consult a healthcare provider.",
                "high": "May indicate diabetes or pre-diabetes. Consult a healthcare provider for further testing."
            },
            "total_cholesterol": {
                "low": "Generally considered healthy, but consult if very low.",
                "high": "May increase cardiovascular risk. Consider lifestyle changes and consult a healthcare provider."
            },
            "hdl_cholesterol": {
                "low": "Low HDL may increase cardiovascular risk. Consider lifestyle changes and consult a healthcare provider.",
                "high": "High HDL is generally considered beneficial for cardiovascular health."
            },
            "ldl_cholesterol": {
                "low": "Low LDL is generally considered beneficial for cardiovascular health.",
                "high": "High LDL may increase cardiovascular risk. Consider lifestyle changes and consult a healthcare provider."
            },
            "creatinine": {
                "low": "Low creatinine is usually not a concern and may indicate low muscle mass.",
                "high": "High creatinine may indicate kidney function issues. Consult a healthcare provider."
            },
            "sgot": {
                "low": "Low SGOT/AST is usually not a concern.",
                "high": "High SGOT/AST may indicate liver damage or other conditions. Consult a healthcare provider."
            },
            "sgpt": {
                "low": "Low SGPT/ALT is usually not a concern.",
                "high": "High SGPT/ALT may indicate liver damage or other conditions. Consult a healthcare provider."
            }
        }
        
        param_interpretations = interpretations.get(param_key, {})
        return param_interpretations.get(status, f"Value is {status}. Please consult a healthcare provider.")
    
    def _determine_overall_status(self, parameters: list) -> str:
        """Determine overall report status"""
        abnormal_count = sum(1 for p in parameters if p["status"] != "normal")
        total_count = len(parameters)
        
        if abnormal_count == 0:
            return "normal"
        elif abnormal_count <= total_count * 0.3:
            return "requires-attention"
        else:
            return "abnormal"
    
    def _generate_summary(self, parameters: list, flagged: list, status: str) -> str:
        """Generate summary text"""
        if status == "normal":
            return "All analyzed parameters are within normal ranges. Continue maintaining a healthy lifestyle."
        elif status == "requires-attention":
            return f"Some parameters require attention: {', '.join(flagged)}. Please consult with a healthcare professional for further evaluation."
        else:
            return f"Multiple parameters are outside normal ranges: {', '.join(flagged)}. It is recommended to consult with a healthcare professional promptly for proper evaluation and guidance."
    
    def _generate_recommendations(self, parameters: list, flagged: list, status: str) -> list:
        """Generate recommendations"""
        recommendations = []
        
        if status == "normal":
            recommendations.append("Continue regular health checkups")
            recommendations.append("Maintain a balanced diet and regular exercise")
        else:
            recommendations.append("Consult with a healthcare professional for proper evaluation")
            
            if "Hemoglobin" in flagged or "RBC Count" in flagged:
                recommendations.append("Consider iron-rich foods and supplements if recommended by your doctor")
            
            if "Blood Sugar (Fasting)" in flagged:
                recommendations.append("Monitor blood sugar levels and follow a diabetes-friendly diet if needed")
            
            if "Total Cholesterol" in flagged or "LDL Cholesterol" in flagged:
                recommendations.append("Consider dietary changes and regular exercise to improve cholesterol levels")
            
            recommendations.append("Follow up with additional tests if recommended by your healthcare provider")
        
        return recommendations

