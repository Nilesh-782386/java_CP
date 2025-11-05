package com.smartheal.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create users table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100),
                    date_of_birth DATE,
                    gender ENUM('Male', 'Female', 'Other'),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_username (username),
                    INDEX idx_email (email)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create user preferences table
            String createPreferencesTable = """
                CREATE TABLE IF NOT EXISTS user_preferences (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT UNIQUE NOT NULL,
                    theme VARCHAR(20) DEFAULT 'light',
                    language VARCHAR(10) DEFAULT 'en',
                    notifications_enabled BOOLEAN DEFAULT true,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create user health profiles table
            String createHealthProfilesTable = """
                CREATE TABLE IF NOT EXISTS user_health_profiles (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT UNIQUE NOT NULL,
                    blood_type VARCHAR(5),
                    allergies TEXT,
                    medications TEXT,
                    medical_conditions TEXT,
                    emergency_contact VARCHAR(100),
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create symptom history table
            String createSymptomHistoryTable = """
                CREATE TABLE IF NOT EXISTS symptom_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    selected_symptoms TEXT NOT NULL,
                    predicted_conditions TEXT,
                    analysis_result JSON,
                    analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_analysis_date (analysis_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create chat history table
            String createChatHistoryTable = """
                CREATE TABLE IF NOT EXISTS chat_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    user_message TEXT NOT NULL,
                    bot_response TEXT NOT NULL,
                    chat_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_chat_date (chat_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create report history table
            String createReportHistoryTable = """
                CREATE TABLE IF NOT EXISTS report_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    report_data JSON NOT NULL,
                    analysis_result TEXT,
                    flagged_parameters TEXT,
                    overall_status VARCHAR(50),
                    analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_analysis_date (analysis_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Create cost estimation history table
            String createCostHistoryTable = """
                CREATE TABLE IF NOT EXISTS cost_estimation_history (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    treatment_type VARCHAR(100) NOT NULL,
                    hospital_type VARCHAR(50) NOT NULL,
                    estimated_cost DECIMAL(10, 2),
                    min_cost DECIMAL(10, 2),
                    max_cost DECIMAL(10, 2),
                    estimation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    INDEX idx_user_id (user_id),
                    INDEX idx_estimation_date (estimation_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // Execute all CREATE TABLE statements
            stmt.execute(createUsersTable);
            System.out.println("✅ Users table created/verified");
            
            stmt.execute(createPreferencesTable);
            System.out.println("✅ User preferences table created/verified");
            
            stmt.execute(createHealthProfilesTable);
            System.out.println("✅ User health profiles table created/verified");
            
            stmt.execute(createSymptomHistoryTable);
            System.out.println("✅ Symptom history table created/verified");
            
            stmt.execute(createChatHistoryTable);
            System.out.println("✅ Chat history table created/verified");
            
            stmt.execute(createReportHistoryTable);
            System.out.println("✅ Report history table created/verified");
            
            stmt.execute(createCostHistoryTable);
            System.out.println("✅ Cost estimation history table created/verified");
            
            System.out.println("✅ Database initialization completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("❌ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

