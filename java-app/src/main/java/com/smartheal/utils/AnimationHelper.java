package com.smartheal.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationHelper {
    public static void fadeIn(Node node, double durationMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        node.setOpacity(0);
        fade.play();
    }

    public static void scaleIn(Node node, double durationMs) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    public static void pulse(Node node) {
        ScaleTransition scale1 = new ScaleTransition(Duration.millis(200), node);
        scale1.setToX(1.1);
        scale1.setToY(1.1);
        
        ScaleTransition scale2 = new ScaleTransition(Duration.millis(200), node);
        scale2.setToX(1.0);
        scale2.setToY(1.0);
        
        scale1.setOnFinished(e -> scale2.play());
        scale1.play();
    }
}

