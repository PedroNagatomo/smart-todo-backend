package com.smarttodo.controller;

import com.smarttodo.entity.SensorData;
import com.smarttodo.repository.SensorDataRepository;
import com.smarttodo.service.IotService;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/iot")
@CrossOrigin(origins = "*")
public class IoTController {

    private static final Logger log = LoggerFactory.getLogger(IoTController.class);
    @Autowired
    private IotService iotService;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getIoTStatus(){
        Map<String, Object> status = new HashMap<>();

        status.put("currentLocation", iotService.getCurrentLocation());
        status.put("environtmentalConditions", iotService.getCurrentEnvironmentalConditions());
        status.put("latestSensorData", iotService.getAllLatestSensorData());
        status.put("timestamp", LocalDateTime.now());

        List<String> sensorTypes = sensorDataRepository.findAllSensorTypes();
        status.put("activeSensorTypes", sensorTypes);
        status.put("totalSensors", sensorTypes.size());

        return ResponseEntity.ok(status);
    }

    @GetMapping("/sensor/{type}")
    public ResponseEntity<Map<String, Object>> getSensorData(
            @PathVariable String type,
            @RequestParam(defaultValue = "24") int hours){

        SensorData latest = iotService.getLatestSensorData(type);
        List<SensorData> recent = iotService.getRecentSensorData(type, hours);

        Map<String, Object> response = new HashMap<>();
        response.put("sensorType", type);
        response.put("latestReading", latest);
        response.put("recentReadings", recent);
        response.put("readingCount", recent.size());

        if (latest != null){
            response.put("currentValue", latest.getValue());
            response.put("unit", latest.getUnit());
            response.put("quality", latest.getQuality());
            response.put("lastUpdate", latest.getTimestamp());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSensorHistory(
            @RequestParam(defaultValue = "24") int hours){

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<SensorData> recentData = sensorDataRepository.findRecentData(since);

        Map<String, Object> response = new HashMap<>();
        response.put("since", since);
        response.put("totalReadings", recentData.size());
        response.put("readings", recentData);

        Map<String, Long> countBySensor = recentData.stream()
                .collect(Collectors.groupingBy(
                        SensorData::getSensorType,
                        Collectors.counting()
                ));

        response.put("readingsBySensorType", countBySensor);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/command/{device}")
    public ResponseEntity<Map<String, String>> sendCommand(
            @PathVariable String device,
            @RequestParam String action,
            @RequestBody(required = false) Map<String, Object> parameters){

        if(parameters == null){
            parameters = new HashMap<>();
        }

        iotService.sendSmartHomeCommand(device, action, parameters);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Comando enviado com sucesso");
        response.put("device", device);
        response.put("action", action);
        response.put("status", "sent");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/simulate/{sensorType}")
    public ResponseEntity<Map<String, String>> simulateSensorData(
            @PathVariable String sensorType,
            @RequestParam Double value,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String location){

        SensorData simulatedData = new SensorData();
        simulatedData.setSensorId(sensorType + "_sim_001");
        simulatedData.setSensorType(sensorType);
        simulatedData.setValue(value);
        simulatedData.setUnit(unit != null ? unit : getDefaultUnit(sensorType));
        simulatedData.setLocation(location != null ? location : "test_location");
        simulatedData.setQuality("good");

        sensorDataRepository.save(simulatedData);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Dados de sensor simulados criados");
        response.put("sensorType", sensorType);
        response.put("value", value.toString());
        response.put("status", "simulated");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getEnvironmentalDashboard(){
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("currentLocation", iotService.getCurrentLocation());
        dashboard.put("timestamp", LocalDateTime.now());

        Map<String, Object> currentConditions = new HashMap<>();
        Map<String, SensorData> latestData = iotService.getAllLatestSensorData();

        for (Map.Entry<String, SensorData> entry : latestData.entrySet()){
            String sensorType = entry.getKey();
            SensorData data = entry.getValue();

            Map<String, Object> sensorInfo = new HashMap<>();
            sensorInfo.put("value", data.getValue());
            sensorInfo.put("unit", data.getUnit());
            sensorInfo.put("quality", data.getQuality());
            sensorInfo.put("lastUpdate", data.getTimestamp());
            sensorInfo.put("status", evaluateSensorStatus(sensorType, data.getValue()));
            currentConditions.put(sensorType, sensorInfo);
        }

        dashboard.put("currentConditions", currentConditions);

        List<String> activeSensors = sensorDataRepository.findAllSensorTypes();
        dashboard.put("activeSensors", activeSensors);
        dashboard.put("totalSensorTypes", activeSensors.size());

        return ResponseEntity.ok(dashboard);
    }

    private String getDefaultUnit(String sensorType){
        return switch (sensorType){
            case "temperature" -> "°C";
            case "humidity" -> "%";
            case "light" -> "lux";
            case "noise" -> "dB";
            case "air_quality" -> "AQI";
            case "motion" -> "detection";
            case "presence" -> "boolean";
            default -> "";
        };
    }

    private String evaluateSensorStatus(String sensorType, Double value){
        return switch (sensorType){
            case "temperature" -> {
                if (value < 18 || value > 26) yield "warning";
                else if (value >= 20 && value <= 24) yield "optimal";
                else yield "good";
            }
            case "humidity" -> {
                if (value < 30 || value > 70) yield "warning";
                else if (value >= 40 && value <= 60) yield "optimal";
                else yield "good";
            }
            case "light" -> {
                if (value < 200) yield "warning";
                else if (value >= 300 && value <= 800) yield "optimal";
                else yield "good";
            }
            case "noise" -> {
                if (value > 60) yield "warning";
                else if (value <= 40) yield "optimal";
                else yield "good";
            }
            case "air_quality" -> {
                if (value > 100) yield "warning";
                else if (value <= 50) yield "optimal";
                else yield "good";
            }
            default -> "unknown";
        };
    }
}
