package com.smartheal.utils;

import javafx.application.Platform;
import java.util.concurrent.atomic.AtomicInteger;

public class UsageTracker {
    private static final AtomicInteger symptomsChecked = new AtomicInteger(0);
    private static final AtomicInteger analysesDone = new AtomicInteger(0);
    private static final AtomicInteger reportsAnalyzed = new AtomicInteger(0);
    private static final AtomicInteger costsEstimated = new AtomicInteger(0);
    
    private static com.smartheal.components.QuickStatsPanel statsPanel;

    public static void setStatsPanel(com.smartheal.components.QuickStatsPanel panel) {
        statsPanel = panel;
    }

    public static void incrementSymptomsChecked() {
        symptomsChecked.incrementAndGet();
        if (statsPanel != null) {
            Platform.runLater(() -> statsPanel.incrementSymptomsChecked());
        }
    }

    public static void incrementAnalysesDone() {
        analysesDone.incrementAndGet();
        if (statsPanel != null) {
            Platform.runLater(() -> statsPanel.incrementAnalysesDone());
        }
    }

    public static void incrementReportsAnalyzed() {
        reportsAnalyzed.incrementAndGet();
        if (statsPanel != null) {
            Platform.runLater(() -> statsPanel.incrementReportsAnalyzed());
        }
    }

    public static void incrementCostsEstimated() {
        costsEstimated.incrementAndGet();
        if (statsPanel != null) {
            Platform.runLater(() -> statsPanel.incrementCostsEstimated());
        }
    }

    public static int getSymptomsChecked() {
        return symptomsChecked.get();
    }

    public static int getAnalysesDone() {
        return analysesDone.get();
    }

    public static int getReportsAnalyzed() {
        return reportsAnalyzed.get();
    }

    public static int getCostsEstimated() {
        return costsEstimated.get();
    }

    public static void reset() {
        symptomsChecked.set(0);
        analysesDone.set(0);
        reportsAnalyzed.set(0);
        costsEstimated.set(0);
        if (statsPanel != null) {
            Platform.runLater(() -> statsPanel.resetStats());
        }
    }
}

