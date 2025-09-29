package com.smarttodo.controller;

import com.smarttodo.service.ComputerVisionService;
import com.smarttodo.service.IotService;
import com.smarttodo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smart")
@CrossOrigin(origins = "*")
public class SmartDashboardController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private ComputerVisionService cvService;

    @Autowired
    private IotService iotService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(){
        Map<String, Object> dashboard = new HashMap<>();

        long totalActive = todoService.findActiveTasks().size();
        long autoAdjusted = todoService.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getAutoAdjusted()))
                .count();

        dashboard.put("totalActiveTasks", totalActive);
        dashboard.put("autoAdjustedTasks", autoAdjusted);
        dashboard.put("topSuggestions", todoService.findActiveTasks().stream().limit(3).toList());
        dashboard.put("adjustmentEnabled", true);

        return ResponseEntity.ok(dashboard);
    }
}
