package com.smarttodo.service;

import com.smarttodo.config.ComputerVisionProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ComputerVisionService {

    private static final Logger logger = LoggerFactory.getLogger(ComputerVisionService.class);

    @Autowired
    private ComputerVisionProperties cvProperties;

    @Autowired
    private MoodAnalysisService moodAnalysisService;

    @Autowired
    private TodoService todoService;

    private VideoCapture camera;
    private CascadeClassifier faceDetector;
    private final AtomicBoolean isAnalysis = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private String currentMood = "neutral";
    private LocalDateTime lastAnalysis;
    private int consecutiveFailures = 0;

    @PostConstruct
    public void initialize(){
        if (!cvProperties.isEnabled()){
            logger.info("Computer Vision desabilitado na configuração");
            return;
        }

        try{
            initializeCamera();
            initializeFaceDetector();
            isInitialized.set(true);
            logger.info("Computer Vision Service inicializado");
        } catch (Exception e){
            logger.error("Erro ao inicializar Computer Vision Service", e);
        }
    }

    private void initializeCamera(){
        try{
            camera = new VideoCapture(cvProperties.getCameraIndex());

            if(!camera.isOpened()){
                logger.warn("Camera não pode ser aberta no indice: {}", cvProperties.getCameraIndex());
                throw new RuntimeException("Camera não disponivel");
            }

            camera.set(3, 640);
            camera.set(4, 480);
            camera.set(5, 30);

            logger.info("Camera inicializada - Indice: {}", cvProperties.getCameraIndex());
        } catch (Exception e){
            logger.error("Erro ao inicializar camera", e);
            throw e;
        }
    }

    private void initializeFaceDetector(){
        try{
            faceDetector = new CascadeClassifier();

            String classfierPath = "haarcascade_frontalface_alt.xml";

            logger.info("Detector facial configurado");
        } catch (Exception e){
            logger.warn("Usando detecção basica sem classficador especifco");
            faceDetector = null;
        }
    }

    @Scheduled(fixedDelayString = "#{@computerVisionProperties.analysisInterval}")
    public void scheduledAnalysis(){
        if(isInitialized.get() && cvProperties.isEnabled() && !isAnalysis.get()){
            analyzeCurrentMoodAsync();
        }
    }

    @Async
    public CompletableFuture<String> analyzeCurrentMoodAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                isAnalysis.set(true);
                String detectedMood = performMoodAnalysis();

                if (detectedMood != null && !detectedMood.equals(currentMood)) {
                    handleMoodChange(detectedMood);
                }

                consecutiveFailures = 0;
                return detectedMood;

            } catch (Exception e) {
                consecutiveFailures++;
                logger.error("❌ Erro durante análise de humor (falha {})", consecutiveFailures, e);

                if (consecutiveFailures > 5) {
                    logger.warn("⚠️ Muitas falhas consecutivas, pausando análises por 30s");
                    try { Thread.sleep(30000); } catch (InterruptedException ignored) {}
                    consecutiveFailures = 0;
                }

                return currentMood;

            } finally {
                isAnalysis.set(false);
                lastAnalysis = LocalDateTime.now();
            }
        });
    }

    private String performMoodAnalysis(){
        if (camera == null || !camera.isOpened()){
            throw new RuntimeException("Camera não disponivel");
        }

        Mat frame = new Mat();
        if(!camera.read(frame)){
            throw new RuntimeException("Não foi possivel capturar frame");
        }

        if (frame.empty()){
            throw new RuntimeException("Frame vazio capturado");
        }

        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        Mat faceRegion = detectLargestFace(grayFrame);

        if(faceRegion != null && cvProperties.isMoodAnalysisEnabled()){
            String mood = moodAnalysisService.analyzeMoodFromFace(faceRegion);
            logger.debug("Humor detectado: {}", mood);
            return mood;
        } else {
            return analyzeGeneralMood(grayFrame);
        }
    }

    private Mat detectLargestFace(Mat grayFrame){
        try{
            if (faceDetector == null || faceDetector.empty()){
                int centerX = grayFrame.cols() / 4;
                int centerY = grayFrame.rows() / 4;
                int faceSize = Math.min(grayFrame.cols(), grayFrame.rows()) / 2;

                Rect faceRect = new Rect(centerX, centerY, faceSize, faceSize);
                return new Mat(grayFrame, faceRect);
            }

            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(grayFrame, faces, 1.1, 3, 0,
                                        new Size(50, 50), new Size());

            Rect[] faceArray = faces.toArray();

            if(faceArray.length > 0){
                Rect largestFace = faceArray[0];
                for (Rect face : faceArray){
                    if(face.area() > largestFace.area()){
                        largestFace = face;
                    }
                }

                return new Mat(grayFrame, largestFace);
            }
        } catch (Exception e){
            logger.debug("Erro na detecção facial, usando analise geral", e);
        }

        return null;
    }

    private String analyzeGeneralMood(Mat frame){
        try{
            Scalar mean = Core.mean(frame);
            double brightness = mean.val[0];

            Mat edges = new Mat();
            Imgproc.Canny(frame, edges, 50, 150);
            double activity = Core.countNonZero(edges) / (double)(frame.rows() * frame.cols());
                    if (brightness > 140 && activity > 0.15){
                        return "energetic";
                    } else if (brightness < 90){
                        return "tired";
                    } else if (activity < 0.08){
                        return "focused";
                    }

                    return "neutral";
            } catch (Exception e){
            logger.error("Erro na analise geral", e);
            return "neutral";
        }
    }

    private void handleMoodChange(String newMood){
        String previosMood = currentMood;
        currentMood = newMood;

        logger.info("Mudança de humor detectada: {} -> {}", previosMood, newMood);
        logger.info("{}", moodAnalysisService.getMoodDescription(newMood));

        // Aqui você pode integrar com outros serviços
        // Por exemplo: ajustar tarefas, enviar notificações, etc.

        // todoService.adjustTasksByMood(newMood);  // Implementamos depois
    }

    public void startAnalysis(){
        if (isInitialized.get()){
            logger.info("Iniciando analise continua de humor");
            analyzeCurrentMoodAsync();
        }
    }

    public void stopAnalysis(){
        isAnalysis.set(false);
        logger.info("Analise de humor pausada");
    }

    public String getCurrentMood(){
        return currentMood;
    }

    public boolean isAnalysisRunning(){
        return isAnalysis.get();
    }

    public LocalDateTime getLastAnalysis(){
        return lastAnalysis;
    }

    @PreDestroy
    public void cleanup(){
        logger.info("Liberando recursos do Computer Vision Service");
        isAnalysis.set(false);

        if(camera != null && camera.isOpened()){
            camera.release();
            logger.info("Camera liberada");
        }
    }

}
