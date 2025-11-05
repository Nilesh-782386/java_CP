# OCR Setup Guide for Report Analyzer

The Report Analyzer uses OCR (Optical Character Recognition) to extract blood test values from uploaded images.

## Prerequisites

### 1. Install Tesseract OCR

**Windows:**
1. Download Tesseract installer from: https://github.com/UB-Mannheim/tesseract/wiki
2. Run the installer (recommended: `tesseract-ocr-w64-setup-5.3.0.20221214.exe`)
3. Install to default location: `C:\Program Files\Tesseract-OCR`
4. Add to PATH (or the Python code will auto-detect common locations)

**Linux:**
```bash
sudo apt-get update
sudo apt-get install tesseract-ocr
```

**macOS:**
```bash
brew install tesseract
```

### 2. Install Python Dependencies

```bash
pip install -r requirements.txt
```

This will install:
- `pytesseract` - Python wrapper for Tesseract OCR
- `opencv-python` - Image preprocessing
- `Pillow` - Image handling

## Usage

1. **Upload Image**: Click "📤 Upload & Scan Image" button in Report Analyzer
2. **Select Image**: Choose a clear photo/scan of your blood test report
3. **Auto-extract**: The system will automatically:
   - Process the image
   - Extract parameter names and values using OCR
   - Populate the input fields
   - Automatically analyze the report

## Tips for Best Results

1. **Image Quality:**
   - Use clear, well-lit images
   - Ensure text is readable and not blurry
   - Avoid shadows and reflections

2. **Report Format:**
   - Reports in English work best
   - Standard lab report formats are preferred
   - Ensure parameter names are visible

3. **Supported Formats:**
   - PNG, JPG, JPEG, BMP, GIF

## Troubleshooting

**If OCR fails:**
- Verify Tesseract is installed: `tesseract --version`
- Check image quality and clarity
- Try manual entry as fallback (always available)

**If values are incorrect:**
- Review extracted values in input fields
- Manually correct any errors before analyzing
- The system will still analyze correctly

## Manual Entry (Always Available)

Even if OCR doesn't work, you can always:
1. Enter values manually in the input fields
2. Click "📊 Analyze Report" to get analysis

