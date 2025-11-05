# Database Setup Guide for SMART Health Guide+

## Prerequisites

1. **MySQL Server** installed and running
2. **MySQL Workbench** or **MySQL Command Line Client**

## Step 1: Create Database

### Option A: Using SQL Script (Recommended)
1. Open MySQL Workbench or Command Line
2. Run the script: `database_setup.sql`
   ```bash
   mysql -u root -p < database_setup.sql
   ```

### Option B: Manual Setup
1. Connect to MySQL:
   ```sql
   mysql -u root -p
   ```

2. Create database:
   ```sql
   CREATE DATABASE smartheal_db;
   USE smartheal_db;
   ```

3. Run all CREATE TABLE statements from `database_setup.sql`

## Step 2: Configure Database Connection

Edit `DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/smartheal_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USERNAME = "root";  // Change if needed
private static final String PASSWORD = "";      // Change to your MySQL password
```

## Step 3: Verify Connection

The application will automatically:
- Test connection on startup
- Initialize all tables if they don't exist
- Show warning if database is unavailable

## Database Tables Created

1. **users** - User accounts for login/register
2. **user_preferences** - User settings and preferences
3. **user_health_profiles** - User medical information
4. **symptom_history** - Symptom analysis history
5. **chat_history** - Chatbot conversation history
6. **report_history** - Blood report analysis history
7. **cost_estimation_history** - Cost estimation history

## Features Enabled with Database

✅ User login/registration
✅ Password hashing (BCrypt)
✅ User profile management
✅ Automatic history saving:
   - Symptom analyses
   - Chat conversations
   - Report analyses
   - Cost estimations
✅ User preferences storage
✅ Health profile management

## Troubleshooting

### Connection Refused
- Check MySQL server is running
- Verify port 3306 is open
- Check firewall settings

### Access Denied
- Verify username and password in `DatabaseConnection.java`
- Check MySQL user permissions

### Tables Not Created
- Check MySQL user has CREATE privileges
- Run `database_setup.sql` manually

## Security Notes

- Passwords are hashed using BCrypt (12 rounds)
- All user data is stored securely
- SQL injection protection via PreparedStatements
- Connection pooling for efficiency

