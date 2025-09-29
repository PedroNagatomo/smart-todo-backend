package com.smarttodo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "smarttodo.computer-vision")
public class ComputerVisionProperties {

    private boolean enabled = true;
    private int cameraIndex = 0;
    private long analysisInterval = 5000;
    private boolean faceDetectionEnabled = true;
    private boolean moodAnalysisEnabled = true;
    private boolean debugMode = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCameraIndex() {
        return cameraIndex;
    }

    public void setCameraIndex(int cameraIndex) {
        this.cameraIndex = cameraIndex;
    }

    public long getAnalysisInterval() {
        return analysisInterval;
    }

    public void setAnalysisInterval(long analysisInterval) {
        this.analysisInterval = analysisInterval;
    }

    public boolean isFaceDetectionEnabled() {
        return faceDetectionEnabled;
    }

    public void setFaceDetectionEnabled(boolean faceDetectionEnabled) {
        this.faceDetectionEnabled = faceDetectionEnabled;
    }

    public boolean isMoodAnalysisEnabled() {
        return moodAnalysisEnabled;
    }

    public void setMoodAnalysisEnabled(boolean moodAnalysisEnabled) {
        this.moodAnalysisEnabled = moodAnalysisEnabled;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
