package com.smarttodo.service;

import com.smarttodo.entity.Todo;
import com.smarttodo.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskAdjustmentService {

    private static final Logger logger = LoggerFactory.getLogger(TaskAdjustmentService.class);

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MoodAnalysisService moodAnalysisService;

    public Map<String, Object> adjustTasksByMood(String currentMood){
        logger.info("Ajustando tarefas para o humor: {}", currentMood);

        List<Todo> activeTasks = todoRepository.findActiveTasks();
        Map<String, Object> adjustmentResult = new HashMap<>();

        int adjustmentCount = 0;
        List<Todo> recommenedTasks = new ArrayList<>();

        for(Todo task : activeTasks){
            double compatibilityScore = calculateMoodCompatibility(task, currentMood);

            task.setMoodCompatibilityScore(compatibilityScore);
            task.setLastMoodCheck(LocalDateTime.now());

            if(Math.abs(compatibilityScore - task.getMoodCompatibilityScore()) > 0.3){
                task.setAutoAdjusted(true);
                adjustmentCount++;
            }

            if (compatibilityScore > 0.7){
                recommenedTasks.add(task);
            }

            todoRepository.save(task);
        }

        recommenedTasks = recommenedTasks.stream()
                .sorted((t1, t2) -> Double.compare(t2.getMoodCompatibilityScore(), t1.getMoodCompatibilityScore()))
                .limit(5)  // Top 5 recomendações
                .collect(Collectors.toList());

        updateSuggestedOrder(recommenedTasks);

        adjustmentResult.put("currentMood", currentMood);
        adjustmentResult.put("moodDescription", moodAnalysisService.getMoodDescription(currentMood));
        adjustmentResult.put("totalTasks", activeTasks.size());
        adjustmentResult.put("adjustedTasks", adjustmentCount);
        adjustmentResult.put("recommendedTasks", recommenedTasks);
        adjustmentResult.put("adjustmentTime", LocalDateTime.now());

        logger.info("Ajuste concluido: {} tarefas analisadas, {} ajustadas, {} recomendadas",
                activeTasks.size(), adjustmentCount, recommenedTasks.size());

        return adjustmentResult;
    }

    private double calculateMoodCompatibility(Todo task, String currentMood){
        double baseScore = 0.5;

        if (task.getRequiredMood() != null){
            if(task.getRequiredMood().equals(currentMood)){
                baseScore += 0.4;
            } else if (areCompatibleMoods(task.getRequiredMood(), currentMood)){
                baseScore += 0.2;
            }else {
                baseScore -= 0.3;
            }
        }

        Integer cognitiveLoad = task.getCognitiveLoad();
        if (cognitiveLoad != null) {
            double cognitiveAdjustment = calculateCognitiveCompatibility(cognitiveLoad ,currentMood);
            baseScore += cognitiveAdjustment;
        }

        baseScore += getPriorityAdjustment(task.getPriority(), currentMood);

        if (task.getDueDate() != null){
            baseScore += getTemporalAdjustment(task.getDueDate());
        }

        if(task.getOptimalEnvironment() != null){
            baseScore += getEnvironmentAdjustment(task.getOptimalEnvironment(), currentMood);
        }

        return Math.max(0.0, Math.min(1.0, baseScore));
    }

    private boolean areCompatibleMoods(String requiredMood, String currentMood){
        Map<String, List<String>> compatibilityMap = Map.of(
                "focused", List.of("neutral", "creative"),
                "energetic", List.of("creative", "focused"),
                "creative", List.of("relaxed", "energetic", "focused"),
                "relaxed", List.of("neutral", "creative"),
                "tired", List.of("neutral"),
                "stressed", List.of("relaxed"),
                "neutral", List.of("focused", "relaxed", "creative")
        );

        return compatibilityMap.getOrDefault(requiredMood, List.of()).contains(currentMood);
    }

    private double calculateCognitiveCompatibility(Integer cognitiveLoad, String currentMood) {
        return switch (currentMood) {
            case "focused" -> {
                // Focado pode lidar com qualquer carga, mas prefere alta
                yield cognitiveLoad >= 3 ? 0.3 : 0.1;
            }
            case "energetic" -> {
                // Energético prefere carga média-alta
                yield cognitiveLoad >= 3 ? 0.2 : cognitiveLoad == 2 ? 0.1 : -0.1;
            }
            case "creative" -> {
                // Criativo prefere carga média
                yield cognitiveLoad == 2 || cognitiveLoad == 3 ? 0.2 : 0.0;
            }
            case "relaxed" -> {
                // Relaxado prefere carga baixa-média
                yield cognitiveLoad <= 2 ? 0.2 : -0.2;
            }
            case "tired" -> {
                // Cansado só para carga muito baixa
                yield cognitiveLoad == 1 ? 0.1 : -0.3;
            }
            case "stressed" -> {
                // Estressado prefere tarefas simples
                yield cognitiveLoad <= 2 ? 0.1 : -0.4;
            }
            default -> 0.0;
        };
    }

    private double getPriorityAdjustment(Todo.Priority priority, String currentMood){
        if (priority == null) return 0.0;

        return switch (priority){
            case URGENT -> currentMood.equals("stressed") ? -0.1 : 0.2;
            case HIGH -> currentMood.equals("focused") || currentMood.equals("energetic") ? 0.15 : 0.0;
            case MEDIUM -> 0.05;
            case LOW -> currentMood.equals("relaxed") || currentMood.equals("tired") ? 0.1 : -0.05;
        };
    }

    private double getTemporalAdjustment(LocalDateTime dueDate){
        long hoursUntilDue = java.time.Duration.between(LocalDateTime.now(), dueDate).toHours();
        if (hoursUntilDue < 0) return 0.5; // Tarefa atrasada - alta prioridade
        if (hoursUntilDue <= 4) return 0.3; // Vence em até 4h
        if (hoursUntilDue <= 24) return 0.1; // Vence hoje
        if (hoursUntilDue <= 48) return 0.05; // Vence amanhã

        return 0.0;
    }

    private double getEnvironmentAdjustment(String optimalEnvironment, String currentMood){
        return switch (optimalEnvironment){
            case "quiet" -> currentMood.equals("focused") || currentMood.equals("creative") ? 0.1 : 0.0;
            case "collaborative" -> currentMood.equals("energetic") ? 0.1 : currentMood.equals("tired") ? -0.2 : 0.0;
            case "bright" -> currentMood.equals("energetic") ? 0.1 : 0.0;
            default -> 0.0;
        };
    }

    private void updateSuggestedOrder(List<Todo> recommendedTasks){
        for (int i = 0; i < recommendedTasks.size(); i++){
            recommendedTasks.get(i).setSuggestedOrder(i + 1);
            todoRepository.save(recommendedTasks.get(i));
        }
    }

    public List<Todo> getSmartSuggestions(String currentMood, int maxSuggestions){
        List<Todo> allTasks = todoRepository.findActiveTasks();


        return allTasks.stream()
                .peek(task -> {
                    double score = calculateMoodCompatibility(task, currentMood);
                    task.setMoodCompatibilityScore(score);
                })
                .sorted((t1, t2) -> Double.compare(
                        t2.getMoodCompatibilityScore(),
                        t1.getMoodCompatibilityScore()
                ))
                .limit(maxSuggestions)
                .collect(Collectors.toList());
    }

    public void resetAutoAdjustment(){
        List<Todo> autoAdjustedTasks = todoRepository.findAll()
                .stream()
                .filter(todo -> Boolean.TRUE.equals(todo.getAutoAdjusted()))
                .collect(Collectors.toList());

        autoAdjustedTasks.forEach(task -> {
            task.setAutoAdjusted(false);
            task.setMoodCompatibilityScore(0.0);
            task.setSuggestedOrder(0);
        });

        todoRepository.saveAll(autoAdjustedTasks);
        logger.info("Reset realizado em {} tarefas", autoAdjustedTasks.size());
    }

    public void adjustForEnvironmentalCondition(String conditionType, Double value) {
        List<Todo> activeTasks = todoRepository.findActiveTasks();

        for (Todo task : activeTasks) {
            double adjustment = calculateEnvironmentalAdjustment(task, conditionType, value);

            if (Math.abs(adjustment) > 0.1) {
                Double currentScore = task.getMoodCompatibilityScore() != null ? task.getMoodCompatibilityScore() : 0.5;
                task.setMoodCompatibilityScore(Math.max(0.0, Math.min(1.0, currentScore + adjustment)));
                task.setAutoAdjusted(true);
                task.setLastMoodCheck(LocalDateTime.now());

                todoRepository.save(task);
            }
        }

        logger.info("Ajuste ambiental aplicado: {} (valor: {})", conditionType, value);
    }


    private double calculateEnvironmentalAdjustment(Todo task, String conditionType, Double value) {
        return switch (conditionType) {
            case "high_temperature" -> {
                // Tarefas que requerem menos energia cognitiva em temperatura alta
                Integer cognitiveLoad = task.getCognitiveLoad();
                yield (cognitiveLoad != null && cognitiveLoad <= 2) ? 0.2 : -0.3;
            }
            case "low_temperature" -> {
                // Temperatura baixa pode reduzir desempenho geral
                yield -0.1;
            }
            case "high_humidity" -> {
                // Alta umidade reduz conforto, prefere tarefas simples
                Integer cognitiveLoad = task.getCognitiveLoad();
                yield (cognitiveLoad != null && cognitiveLoad <= 2) ? 0.1 : -0.2;
            }
            case "low_light" -> {
                // Pouca luz dificulta tarefas visuais/leitura
                String environment = task.getOptimalEnvironment();
                yield ("bright".equals(environment)) ? -0.4 : -0.1;
            }
            case "high_light" -> {
                // Muita luz pode ser boa para algumas tarefas
                String environment = task.getOptimalEnvironment();
                yield ("bright".equals(environment)) ? 0.3 : 0.0;
            }
            case "high_noise" -> {
                // Ruído alto prejudica concentração
                String environment = task.getOptimalEnvironment();
                yield ("quiet".equals(environment)) ? -0.5 : ("collaborative".equals(environment)) ? 0.1 : -0.2;
            }
            case "low_noise" -> {
                // Silêncio favorece concentração
                String environment = task.getOptimalEnvironment();
                yield ("quiet".equals(environment)) ? 0.4 : 0.1;
            }
            case "poor_air_quality" -> {
                // Ar ruim reduz desempenho cognitivo
                yield -0.3;
            }
            case "low_activity" -> {
                // Pouco movimento sugere tarefas de pausa
                Todo.Priority priority = task.getPriority();
                yield (priority == Todo.Priority.LOW) ? 0.2 : -0.1;
            }
            case "work_mode" -> {
                // Presença detectada - boost para tarefas de trabalho
                Todo.Priority priority = task.getPriority();
                yield (priority == Todo.Priority.HIGH || priority == Todo.Priority.URGENT) ? 0.2 : 0.1;
            }
            case "away_mode" -> {
                // Ausência - reduz prioridade de tarefas específicas de localização
                String locationContext = task.getLocationContext();
                yield ("office".equals(locationContext) || "home".equals(locationContext)) ? -0.4 : 0.0;
            }
            default -> 0.0;
        };
    }

    public void adjustTasksByLocation(String newLocation) {
        List<Todo> activeTasks = todoRepository.findActiveTasks();
        int adjustedCount = 0;

        for (Todo task : activeTasks) {
            String taskLocation = task.getLocationContext();
            double adjustment = 0.0;

            if (taskLocation != null) {
                if (taskLocation.equals(newLocation)) {
                    adjustment = 0.4; // Boost forte para tarefas da localização atual
                } else if ("anywhere".equals(taskLocation)) {
                    adjustment = 0.1; // Pequeno boost para tarefas flexíveis
                } else {
                    adjustment = -0.3; // Penalidade para tarefas de outra localização
                }
            }

            if (Math.abs(adjustment) > 0.05) {
                Double currentScore = task.getMoodCompatibilityScore() != null ? task.getMoodCompatibilityScore() : 0.5;
                task.setMoodCompatibilityScore(Math.max(0.0, Math.min(1.0, currentScore + adjustment)));
                task.setAutoAdjusted(true);
                task.setLastMoodCheck(LocalDateTime.now());

                todoRepository.save(task);
                adjustedCount++;
            }
        }

        logger.info("Ajuste por localização: {} tarefas ajustadas para localização '{}'", adjustedCount, newLocation);
    }
}
