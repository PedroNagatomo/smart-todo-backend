package com.smarttodo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttodo.config.IoTProperties;
import com.smarttodo.entity.SensorData;
import com.smarttodo.repository.SensorDataRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IotService {

    private static final Logger logger = LoggerFactory.getLogger(IotService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IoTProperties ioTProperties;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @Autowired
    private TaskAdjustmentService taskAdjustmentService;

    @Autowired(required = false)
    private MessageChannel mqttOutboundChannel;

    private final Map<String, SensorData> lastSensorReadings = new ConcurrentHashMap<>();
    private String currentUserLocation = "home";
    private final Map<String, Object> environmentalConditions = new ConcurrentHashMap<>();

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(@Payload String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic) {

        try {
            logger.debug("📡 Mensagem MQTT recebida - Tópico: {} | Payload: {}", topic, payload);

            if (topic.startsWith("sensors/")) {
                handleSensorMessage(topic, payload);
            } else if (topic.startsWith("location/")) {
                handleLocationMessage(topic, payload);
            }

        } catch (Exception e) {
            logger.error("❌ Erro ao processar mensagem MQTT do tópico: {}", topic, e);
        }
    }

    private void handleSensorMessage(String topic, String payload){
        try{
            Map<String, Object> rawData = objectMapper.readValue(payload, Map.class);
            String sensorType = extractSensorType(topic);

            SensorData sensorData = new SensorData();
            sensorData.setSensorId((String) rawData.get("sensor_id"));
            sensorData.setSensorType(sensorType);
            sensorData.setValue(getDoubleValue(rawData.get("value")));
            sensorData.setUnit((String) rawData.get("unit"));
            sensorData.setLocation((String) rawData.getOrDefault("location", currentUserLocation));
            sensorData.setQuality((String) rawData.getOrDefault("quality", "good"));

            if (rawData.get("timestamp") != null){
                sensorData.setTimestamp(LocalDateTime.parse((String) rawData.get("timestamp")));
            }

            lastSensorReadings.put(sensorType, sensorData);

            sensorDataRepository.save(sensorData);

            processEnvironmentalData(sensorData);

            logger.info("Sensor {} processado: {} {} (qualidade: {})",
                    sensorType, sensorData.getValue(), sensorData.getUnit(), sensorData.getQuality());
        } catch (Exception e){
            logger.error("Erro ao processar dados do sensor", e);
        }
    }

    private void handleLocationMessage(String topic, String payload){
        try {
            Map<String, Object> locationData = objectMapper.readValue(payload, Map.class);

            String newLocation = (String) locationData.get("location");
            Double confidence = getDoubleValue(locationData.get("confidence"));

            if(confidence != null && confidence > 0.7 && !newLocation.equals(currentUserLocation)){
                String previousLocation = currentUserLocation;
                currentUserLocation = newLocation;

                logger.info("Localização alterada: {} -> {} (confiança: {})",
                            previousLocation, newLocation, confidence);

                taskAdjustmentService.adjustTasksByLocation(newLocation);
            }
        } catch (Exception e){
            logger.error("Erro ao processar atualização de localização", e);
        }
    }

    private void processEnvironmentalData(SensorData data){
        String sensorType = data.getSensorType();
        Double value = data.getValue();

        environmentalConditions.put(sensorType, value);

        switch (sensorType){
            case "temperature" -> processTemperatureData(value);
            case "humidity" -> processHumidityData(value);
            case "light" -> processLightData(value);
            case "noise" -> processNoiseData(value);
            case "air_quality" -> processAirQualityData(value);
            case "motion" -> processMotionData(value > 0);
            case "presence" -> processPresenceData(value > 0);
        }
    }

    private void processTemperatureData(Double temperature){
        if(temperature > 26){
            logger.info("Temperatura alta detectada: {}C Sugerindo tarefas em ambiente mais fresco", temperature);
            taskAdjustmentService.adjustForEnvironmentalCondition("high_temperature", temperature);
            sendSmartHomeCommand("climate", "cool", Map.of("target_temperature", 24));
        } else if(temperature < 18){
            logger.info("Temperatura baixa detectada: {}C - Sugeringdo aquecimento", temperature);
            taskAdjustmentService.adjustForEnvironmentalCondition("low_temperature", temperature);
            sendSmartHomeCommand("climate", "heat", Map.of("target_temperature", 22));
        }
    }

    private void processHumidityData(Double humidity){
        if (humidity > 70){
            logger.info("Umidade alta detectada: {}% - Ajustando tarefas", humidity);
            taskAdjustmentService.adjustForEnvironmentalCondition("high_humidity", humidity);
            sendSmartHomeCommand("humidifier", "on", Map.of("target_humidity", 45));
        }
    }

    private void processLightData(Double lightLevel){
        if (lightLevel < 300){
            logger.info("Pouca luminosidade detectda: {} lux - Sugerindo melhor iluminação", lightLevel);
            taskAdjustmentService.adjustForEnvironmentalCondition("low_light", lightLevel);
            sendSmartHomeCommand("lights", "brighten", Map.of("brightness", 80));
        } else if(lightLevel > 1000){
            logger.info("Muita luminosidade detectada: {} luz - Filtrando tarefas sensiveis a luz", lightLevel);
            taskAdjustmentService.adjustForEnvironmentalCondition("high_light", lightLevel);
        }
    }

    private void processNoiseData(Double noiseLevel){
        if (noiseLevel > 60){
            logger.info("Ruido alto detectado: {} dB - Priorizando tarefas tolerantes ao ruido", noiseLevel);
            taskAdjustmentService.adjustForEnvironmentalCondition("high_noise", noiseLevel);
        } else if(noiseLevel < 30){
            logger.info("Ambiente silencioso detectado: {} dB - Priorizando tarefas que requerem concentração", noiseLevel);
            taskAdjustmentService.adjustForEnvironmentalCondition("low_noise", noiseLevel);
        }
    }

    private void processAirQualityData(Double airQualityIndex){
        if (airQualityIndex > 100){
            logger.info("Qualidade do ar ruim detectada: AQI {} - Sugerindo tarefas indoor", airQualityIndex);
            taskAdjustmentService.adjustForEnvironmentalCondition("poor_air_quality", airQualityIndex);
            sendSmartHomeCommand("air_purifier", "on", Map.of("speed", "high"));
        }
    }

    private void processMotionData(boolean motionDetected){
        if(!motionDetected){
            logger.debug("Pouco movimento detectado - Sugerindo lembretes de pausa");
            taskAdjustmentService.adjustForEnvironmentalCondition("low_activity", 0.0);
        }
    }

    private void processPresenceData(boolean presenceDetected){
        if(presenceDetected){
            logger.debug("Presença detectada - Ativando modo de trabalho");
            taskAdjustmentService.adjustForEnvironmentalCondition("word_mode", 1.0);
        } else {
            logger.debug("Ausencia detectada - Pausando tarefas especificas de localização");
            taskAdjustmentService.adjustForEnvironmentalCondition("away_mode", 0.0);
        }
    }

    public void sendSmartHomeCommand(String device, String action, Map<String, Object> parameters){
        if(mqttOutboundChannel == null){
            logger.warn("Canal MQTT não disponivel para enviar comando");
            return;
        }

        try{
            Map<String, Object> command = new HashMap<>();
            command.put("device", device);
            command.put("action", action);
            command.put("parameters", parameters);
            command.put("timestamp", LocalDateTime.now().toString());
            command.put("source", "smart-todo-backend");

            String payload = objectMapper.writeValueAsString(command);

            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(MqttHeaders.TOPIC, ioTProperties.getMqtt().getTopics().getCommands())
                    .build();

            mqttOutboundChannel.send(message);

            logger.info("Comando Smart Home enviado: {} -> {} ({})", device, action, parameters);

        } catch (Exception e){
            logger.error("Erro ao enviar comando Smart Home", e);
        }
    }

    private String extractSensorType(String topic){
        String [] parts = topic.split("/");
        return parts.length > 1 ? parts[1] : "unknown";
    }

    private Double getDoubleValue(Object value){
        if (value == null) return null;
        if (value instanceof Number){
            return ((Number) value).doubleValue();
        }
        try{
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e){
            return null;
        }
    }

    public String getCurrentLocation() {
        return currentUserLocation;
    }

    public Map<String, Object> getCurrentEnvironmentalConditions() {
        return Map.copyOf(environmentalConditions);
    }

    public SensorData getLatestSensorData(String sensorType) {
        return lastSensorReadings.get(sensorType);
    }

    public Map<String, SensorData> getAllLatestSensorData() {
        return Map.copyOf(lastSensorReadings);
    }

    public List<SensorData> getRecentSensorData(String sensorType, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return sensorDataRepository.findBySensorTypeAndTimestampAfter(sensorType, since);
    }
}
