# Windows Setup Instructions - SmartHeal JavaFX

## ⚠️ IMPORTANT: Use Command Prompt, NOT PowerShell

Maven works in Command Prompt but may have issues in PowerShell due to PATH variable differences.

---

## Step-by-Step Instructions

### Step 1: Setup Python Backend (One-time setup)

1. **Press Windows + R**
2. Type: `cmd` and press Enter
3. Run these commands:

```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\backend_python
pip install -r requirements.txt
```

### Step 2: Start Python Backend (Command Prompt 1)

1. **Press Windows + R**
2. Type: `cmd` and press Enter
3. Run these commands:

```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\backend_python
python app.py
```

Wait until you see: `Server starting on http://localhost:5000`

**Keep this window open!** (Backend must stay running)

---

### Step 3: Run JavaFX App (Command Prompt 2)

1. **Press Windows + R** again
2. Type: `cmd` and press Enter (opens new window)
3. Run these commands:

```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\java-app
mvn clean compile
mvn javafx:run
```

The application window should open!

---

## Alternative: If Maven Path Issues Persist

If you get "mvn is not recognized", use the full path to Maven:

```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\java-app

REM Replace with your actual Maven path
"D:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd" clean compile
"D:\Program Files\apache-maven-3.9.11-bin\apache-maven-3.9.11\bin\mvn.cmd" javafx:run
```

To find your Maven path:
1. Search for "mvn.cmd" in Windows File Explorer
2. Copy the full path
3. Use it in the commands above

---

## Quick Commands Summary

### Terminal 1 (Python Backend):
```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\backend_python
python app.py
```
✅ Wait for: `Server starting on http://localhost:5000`

### Terminal 2 (JavaFX):
```cmd
cd C:\Users\Multimedia\Downloads\SmartHeal\java-app
mvn clean compile
mvn javafx:run
```
✅ Application window opens!

---

## Troubleshooting

### "mvn is not recognized"
- Use full path to mvn.cmd (see Alternative above)
- Or add Maven to Windows PATH environment variable

### "Cannot connect to backend"
- Make sure Python backend is running (Terminal 1)
- Check: http://localhost:5000/api/symptoms in browser
- Should see JSON data if backend is running
- Ensure Python backend started successfully: `python app.py`

### Port 5000 already in use
- Python backend might already be running
- Check Task Manager for Python processes
- Or change port in `backend_python/app.py`

---

## Why Command Prompt?

- PowerShell has different PATH variable handling
- Maven scripts (`.cmd` files) work better in Command Prompt
- More reliable for Windows Java development

---

**That's it! Use Command Prompt for best results! 🚀**

