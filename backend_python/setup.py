"""
Setup script for SMART Health Guide+ Backend
Creates necessary directories and downloads NLTK data
"""

import os
import sys

def create_directories():
    """Create necessary directories"""
    directories = [
        'models',
        'datasets'
    ]
    
    for directory in directories:
        os.makedirs(directory, exist_ok=True)
        print(f"✓ Created directory: {directory}/")

def download_nltk_data():
    """Download required NLTK data"""
    try:
        import nltk
        print("\nDownloading NLTK data...")
        nltk.download('punkt', quiet=True)
        nltk.download('stopwords', quiet=True)
        print("✓ NLTK data downloaded successfully")
    except ImportError:
        print("⚠ NLTK not installed. Skipping NLTK data download.")
        print("  Install with: pip install nltk")

def main():
    print("=" * 60)
    print("SMART Health Guide+ Backend Setup")
    print("=" * 60)
    print()
    
    print("Creating directories...")
    create_directories()
    
    print("\nSetting up NLTK...")
    download_nltk_data()
    
    print("\n" + "=" * 60)
    print("Setup complete!")
    print("=" * 60)
    print("\nNext steps:")
    print("1. Install dependencies: pip install -r requirements.txt")
    print("2. Run the server: python app.py")
    print("\nNote: ML models and datasets will be created automatically")
    print("      on first run (this may take a few minutes).")
    print()

if __name__ == '__main__':
    main()

