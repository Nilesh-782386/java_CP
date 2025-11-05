# Running SmartHeal JavaFX in VS Code

## Step 1: Install VS Code Extensions

1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Install these extensions:
   - **Extension Pack for Java** (by Microsoft)
     - Includes: Language Support, Debugger, Test Runner, Maven, Project Manager
   - **JavaFX** (optional, for better JavaFX support)

## Step 2: Open the Project

1. Open VS Code
2. File → Open Folder
3. Navigate to: `C:\Users\Multimedia\Downloads\SmartHeal\java-app`
4. Click "Select Folder"

## Step 3: Configure Java

1. VS Code should detect Java automatically
2. If prompted, select your Java 17+ installation
3. Wait for Maven to download dependencies (first time only)
   - Check bottom-right status bar for progress

## Step 4: Start Backend Server

**IMPORTANT:** You need the Python backend running first!

### Option A: Terminal in VS Code
1. Terminal → New Terminal (Ctrl+`)
2. Navigate to backend directory:
   ```bash
   cd ../backend_python
   ```
3. Start backend:
   ```bash
   python app.py
   ```
4. Keep this terminal open (backend must stay running)

### Option B: External Terminal
Open a separate terminal/command prompt:
```bash
cd C:\Users\Multimedia\Downloads\SmartHeal\backend_python
python app.py
```

## Step 5: Run the Application

### Method 1: Using VS Code Run Button
1. Open `src/main/java/com/smartheal/SmartHealApp.java`
2. Click the "Run" button above `main()` method
3. Or press F5 to debug

### Method 2: Using Terminal in VS Code
1. Terminal → New Terminal (Ctrl+`)
2. Run:
   ```bash
   mvn javafx:run
   ```

### Method 3: Using Maven Command
1. Open Command Palette (Ctrl+Shift+P)
2. Type: "Java: Run Maven Goal"
3. Select: `javafx:run`

## Step 6: Verify It Works

✅ Application window should open
✅ All 5 tabs visible
✅ No error dialogs

---

## Troubleshooting

### Java Not Detected
- Check: File → Preferences → Settings → Search "java.home"
- Set path to your Java 17+ installation
- Example: `C:\Program Files\Java\jdk-17`

### Maven Dependencies Not Downloading
- View → Output → Select "Java" from dropdown
- Check for errors
- Try: Terminal → `mvn clean install`

### Cannot Connect to Backend
- Make sure Python backend is running (Step 4)
- Check: http://localhost:5000/api/symptoms in browser
- If not working, start Python backend first: `cd backend_python && python app.py`

### JavaFX Not Found
- VS Code should use Maven to download JavaFX
- If issues, check `pom.xml` has JavaFX dependencies
- Try: `mvn clean compile` in terminal

---

## Quick Commands

**New Terminal:** `Ctrl + `` (backtick)

**Run Maven Goal:** `Ctrl+Shift+P` → "Java: Run Maven Goal" → `javafx:run`

**Debug:** Open `SmartHealApp.java` → Press `F5`

---

## VS Code Workspace Settings (Optional)

Create `.vscode/settings.json` in `java-app` folder:

```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "maven.executable.path": "mvn"
}
```

---

That's it! The application should now run in VS Code! 🚀

