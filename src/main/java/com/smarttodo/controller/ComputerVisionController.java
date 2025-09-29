package com.smarttodo.controller;

import com.smarttodo.service.ComputerVisionService;
import com.smarttodo.service.MoodAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cv")
@CrossOrigin(origins = "*")
public class ComputerVisionController {

    @Autowired
    private ComputerVisionService cvService;

    @Autowired
    private MoodAnalysisService moodService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(){
        Map<String, Object> status = new HashMap<>();
        status.put("isRunning", cvService.isAnalysisRunning());
        status.put("currentMood", cvService.getCurrentMood());
        status.put("moodDescription", moodService.getMoodDescription(cvService.getCurrentMood()));
        status.put("lastAnalysis", cvService.getLastAnalysis());
        status.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(status);
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startAnalysis(){
        cvService.startAnalysis();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Analise de Computer Vision iniciada");
        response.put("status", "started");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopAnalysis(){
        cvService. stopAnalysis();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Analise de Computer Vision pausada");
        response.put("status", "stopped");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mood/current")
    public ResponseEntity<Map<String, Object>> getCurrentMood(){
        String mood = cvService.getCurrentMood();

        Map<String, Object> response = new HashMap<>();
        response.put("mood", mood);
        response.put("description", moodService.getMoodDescription(mood));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mood/analyze")
    public ResponseEntity<Map<String, Object>> analyzeNow(){
        String mood = cvService.analyzeCurrentMoodAsync().join();

        Map<String, Object> response = new HashMap<>();
        response.put("mood", mood);
        response.put("description", moodService.getMoodDescription(mood));
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Analise realizada com sucesso");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/moods")
    public ResponseEntity<Map<String, Object>> getAlllMoods(){
        Map<String, Object> response = new HashMap<>();

        String[] moods = moodService.getAllMoods();
        Map<String, String> moodDescriptions = new HashMap<>();

        for (String mood : moods){
            moodDescriptions.put(mood, moodService.getMoodDescription(mood));
        }

        response.put("availableMoods", moods);
        response.put("descriptions", moodDescriptions);

        return ResponseEntity.ok(response);
    }
}
