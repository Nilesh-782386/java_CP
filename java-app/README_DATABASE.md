# MySQL Database Setup - Quick Start

## 🚀 Quick Setup (5 minutes)

### 1. Install MySQL
- Download from: https://dev.mysql.com/downloads/mysql/
- Install with default settings
- Remember your root password

### 2. Create Database
Open MySQL Command Line or Workbench and run:
```sql
CREATE DATABASE smartheal_db;
```

### 3. Update Connection Settings
Edit `java-app/src/main/java/com/smartheal/database/DatabaseConnection.java`:
- Change `USERNAME` if not using 'root'
- Change `PASSWORD` to your MySQL root password

### 4. Run Application
The application will automatically:
- ✅ Create all tables on first run
- ✅ Initialize database structure
- ✅ Enable login/register features

## 📋 Database Schema

### Tables Created Automatically:
- `users` - User accounts
- `user_preferences` - Settings
- `user_health_profiles` - Medical info
- `symptom_history` - Analysis history
- `chat_history` - Chat history
- `report_history` - Report history
- `cost_estimation_history` - Cost history

## 🔐 Default Configuration

```java
URL: jdbc:mysql://localhost:3306/smartheal_db
Username: root
Password: (empty by default - change this!)
```

## ✅ Features Enabled

With database connected:
- Login/Register system
- User profiles
- Automatic history saving
- User preferences
- Health profiles

Without database:
- Application still works
- Login/register disabled
- History not saved
- All other features work normally

## 🎯 That's It!

Just ensure MySQL is running and the application handles the rest automatically!

