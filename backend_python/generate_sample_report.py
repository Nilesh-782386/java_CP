"""
Generate Sample Blood Test Report Image for Testing OCR
Creates a realistic-looking blood test report image that can be uploaded and tested
"""

from PIL import Image, ImageDraw, ImageFont
import os

def generate_sample_report_image(output_path='sample_blood_report.png'):
    """Generate a sample blood test report image"""
    
    # Create image with white background
    width, height = 800, 1000
    image = Image.new('RGB', (width, height), color='white')
    draw = ImageDraw.Draw(image)
    
    # Try to use a nice font, fallback to default if not available
    try:
        # Try to use a system font (Windows)
        title_font = ImageFont.truetype("arial.ttf", 32)
        header_font = ImageFont.truetype("arial.ttf", 20)
        normal_font = ImageFont.truetype("arial.ttf", 16)
        small_font = ImageFont.truetype("arial.ttf", 14)
    except:
        # Fallback to default font
        title_font = ImageFont.load_default()
        header_font = ImageFont.load_default()
        normal_font = ImageFont.load_default()
        small_font = ImageFont.load_default()
    
    # Colors
    black = (0, 0, 0)
    dark_gray = (50, 50, 50)
    gray = (100, 100, 100)
    blue = (0, 100, 200)
    
    y_position = 40
    
    # Header
    draw.text((width // 2 - 150, y_position), "MEDICAL LABORATORY REPORT", fill=blue, font=title_font)
    y_position += 60
    
    draw.text((50, y_position), "Patient Name: John Doe", fill=black, font=normal_font)
    y_position += 30
    draw.text((50, y_position), "Date: 05-Nov-2025", fill=black, font=normal_font)
    y_position += 30
    draw.text((50, y_position), "Lab ID: LAB-2025-001234", fill=gray, font=small_font)
    y_position += 50
    
    # Draw a line
    draw.line([(50, y_position), (width - 50, y_position)], fill=gray, width=2)
    y_position += 30
    
    # Section Header
    draw.text((50, y_position), "COMPLETE BLOOD COUNT (CBC)", fill=dark_gray, font=header_font)
    y_position += 40
    
    # Table Header
    draw.rectangle([(50, y_position), (width - 50, y_position + 35)], outline=gray, width=1)
    draw.text((60, y_position + 8), "Parameter", fill=black, font=normal_font)
    draw.text((350, y_position + 8), "Value", fill=black, font=normal_font)
    draw.text((500, y_position + 8), "Unit", fill=black, font=normal_font)
    draw.text((600, y_position + 8), "Reference Range", fill=black, font=normal_font)
    y_position += 35
    
    # Sample values - these are the ones we want OCR to extract
    test_data = [
        ("Hemoglobin", "14.5", "g/dL", "12.0 - 16.0"),
        ("WBC Count", "7500", "cells/µL", "4000 - 11000"),
        ("RBC Count", "4.8", "million cells/µL", "4.5 - 5.5"),
        ("Platelet Count", "280000", "cells/µL", "150000 - 450000"),
    ]
    
    for i, (param, value, unit, ref_range) in enumerate(test_data):
        # Alternate row background
        if i % 2 == 0:
            draw.rectangle([(50, y_position), (width - 50, y_position + 40)], fill=(245, 245, 245), outline=gray, width=1)
        else:
            draw.rectangle([(50, y_position), (width - 50, y_position + 40)], fill=(255, 255, 255), outline=gray, width=1)
        
        draw.text((60, y_position + 10), param, fill=black, font=normal_font)
        draw.text((350, y_position + 10), value, fill=blue, font=normal_font)
        draw.text((500, y_position + 10), unit, fill=gray, font=small_font)
        draw.text((600, y_position + 10), ref_range, fill=gray, font=small_font)
        y_position += 40
    
    y_position += 30
    
    # Draw a line
    draw.line([(50, y_position), (width - 50, y_position)], fill=gray, width=2)
    y_position += 30
    
    # Biochemistry Section
    draw.text((50, y_position), "BIOCHEMISTRY", fill=dark_gray, font=header_font)
    y_position += 40
    
    # Table Header
    draw.rectangle([(50, y_position), (width - 50, y_position + 35)], outline=gray, width=1)
    draw.text((60, y_position + 8), "Parameter", fill=black, font=normal_font)
    draw.text((350, y_position + 8), "Value", fill=black, font=normal_font)
    draw.text((500, y_position + 8), "Unit", fill=black, font=normal_font)
    draw.text((600, y_position + 8), "Reference Range", fill=black, font=normal_font)
    y_position += 35
    
    # Biochemistry values
    bio_data = [
        ("Blood Sugar (Fasting)", "95.0", "mg/dL", "70 - 100"),
        ("Total Cholesterol", "185.0", "mg/dL", "< 200"),
        ("HDL Cholesterol", "55.0", "mg/dL", "40 - 100"),
        ("LDL Cholesterol", "110.0", "mg/dL", "< 100"),
        ("Creatinine", "0.9", "mg/dL", "0.6 - 1.2"),
        ("SGOT (AST)", "28.0", "U/L", "10 - 40"),
        ("SGPT (ALT)", "32.0", "U/L", "10 - 40"),
    ]
    
    for i, (param, value, unit, ref_range) in enumerate(bio_data):
        # Alternate row background
        if i % 2 == 0:
            draw.rectangle([(50, y_position), (width - 50, y_position + 40)], fill=(245, 245, 245), outline=gray, width=1)
        else:
            draw.rectangle([(50, y_position), (width - 50, y_position + 40)], fill=(255, 255, 255), outline=gray, width=1)
        
        draw.text((60, y_position + 10), param, fill=black, font=normal_font)
        draw.text((350, y_position + 10), value, fill=blue, font=normal_font)
        draw.text((500, y_position + 10), unit, fill=gray, font=small_font)
        draw.text((600, y_position + 10), ref_range, fill=gray, font=small_font)
        y_position += 40
    
    y_position += 30
    
    # Footer
    draw.line([(50, y_position), (width - 50, y_position)], fill=gray, width=2)
    y_position += 20
    draw.text((50, y_position), "Note: Please consult with your healthcare provider for interpretation.", fill=gray, font=small_font)
    y_position += 25
    draw.text((50, y_position), "Report Generated by: SmartHeal Medical Lab System", fill=gray, font=small_font)
    
    # Save image
    image.save(output_path, 'PNG', quality=95)
    print(f"✅ Sample blood test report image generated: {output_path}")
    print(f"   File location: {os.path.abspath(output_path)}")
    print(f"   You can now upload this image to test OCR functionality!")
    
    return output_path

if __name__ == "__main__":
    output_file = generate_sample_report_image()
    print(f"\n📄 Sample report ready at: {os.path.abspath(output_file)}")
    print("   Upload this image using the 'Upload & Scan Image' button in Report Analyzer!")

