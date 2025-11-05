package com.smartheal.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class JSONExporter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void exportToJSON(Object data, String defaultFileName, String title, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(defaultFileName + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                String json = objectMapper.writeValueAsString(data);
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json);
                }
                
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Export Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("JSON file saved successfully to:\n" + file.getAbsolutePath());
                    alert.showAndWait();
                });
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Export Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to save JSON file:\n" + e.getMessage());
                    alert.showAndWait();
                });
            }
        }
    }
}

