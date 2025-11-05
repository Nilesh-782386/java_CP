# 🚀 EXACT COMMANDS TO RUN IN VS CODE

## ⚡ QUICK START (3 Steps)

### Step 1: Start Backend (Terminal 1)
Open VS Code Terminal (`Ctrl + ``) and run:
```bash
cd ../backend_python
python app.py
```
**Keep this terminal open!** Wait until you see: `Server starting on http://localhost:5000`

### Step 2: Run JavaFX App (Terminal 2)
Open **NEW Terminal** in VS Code (`Ctrl + Shift + ``) and run:
```bash
cd java-app
mvn javafx:run
```

### Step 3: Done! ✅
The application window should open!

---

## 📋 DETAILED OPTIONS

### Option A: Using Maven Command (Recommended)
```bash
# In VS Code Terminal (Ctrl + `)
cd C:\Users\Multimedia\Downloads\SmartHeal\java-app
mvn javafx:run
```

### Option B: Using VS Code Run Button
1. Open file: `src/main/java/com/smartheal/SmartHealApp.java`
2. Click **▶ Run** button above `main()` method
3. Or press **F5** to debug

### Option C: Using Command Palette
1. Press `Ctrl + Shift + P`
2. Type: `Java: Run Maven Goal`
3. Select: `javafx:run`
4. Press Enter

### Option D: Using Tasks
1. Press `Ctrl + Shift + P`
2. Type: `Tasks: Run Task`
3. Select: `Maven: Run JavaFX App`

---

## 🔧 BUILD COMMANDS

### Clean and Compile
```bash
mvn clean compile
```

### Clean and Install (Download Dependencies)
```bash
mvn clean install
```

### Just Compile
```bash
mvn compile
```

---

## 🎯 EXACT VS CODE STEPS

### Full Process:
1. **Open VS Code**
   - File → Open Folder
   - Navigate to: `C:\Users\Multimedia\Downloads\SmartHeal\java-app`

2. **Install Extensions** (if not installed):
   - `Ctrl + Shift + X`
   - Search: "Extension Pack for Java"
   - Install it

3. **Wait for Maven Sync**:
   - Bottom-right status bar shows "Maven: Downloading..."
   - Wait until it finishes

4. **Start Python Backend** (Terminal 1):
   ```bash
   cd ../backend_python
   python app.py
   ```

5. **Run Application** (Terminal 2):
   ```bash
   mvn javafx:run
   ```

---

## ✅ VERIFY IT'S WORKING

**Check Console Output:**
- Should see: `Starting SmartHeal Application...`
- Should see: `✓ Loaded CSS from: /styles.css`
- Application window opens
- All 5 tabs visible

**Check Backend:**
- Open browser: http://localhost:5000/api/symptoms
- Should return JSON data

---

## 🐛 TROUBLESHOOTING

### Error: "mvn is not recognized"
**Fix:** Use Command Prompt instead of PowerShell, or add Maven to PATH

### Error: "JavaFX not found"
**Fix:** Run:
```bash
mvn clean install
```

### Error: "Port 5000 already in use"
**Fix:** Python backend is already running, or change port in `backend_python/app.py`

### Error: "Cannot connect to backend"
**Fix:** Make sure backend is running first (Step 1)

---

## 📝 COMMAND REFERENCE

| Action | Command |
|--------|---------|
| **Run App** | `mvn javafx:run` |
| **Compile** | `mvn compile` |
| **Clean Build** | `mvn clean compile` |
| **Install Dependencies** | `mvn clean install` |
| **Start Backend** | `cd ../backend_python && python app.py` |

---

## 🎯 ONE-LINER (If Backend Already Running)

```bash
cd C:\Users\Multimedia\Downloads\SmartHeal\java-app && mvn javafx:run
```

---

**That's it! Just run `mvn javafx:run` in VS Code Terminal!** 🚀

