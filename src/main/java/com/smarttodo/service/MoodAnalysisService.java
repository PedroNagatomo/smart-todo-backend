package com.smarttodo.service;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;

@Service
public class MoodAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(MoodAnalysisService.class);

    public String analyzeMoodFromFace(Mat faceRegion){
        try{
            Map<String, Double> features = extractFacialFeatures(faceRegion);
            String mood = determineMoodFromFeatures(features);

            logger.debug("Analise de humor - Features: {}, Resultado: {}", features, mood);
            return mood;
        } catch (Exception e){
            logger.error("Erro ao analisar humor da face", e);
            return "neutral";
        }
    }

    private Map<String, Double> extractFacialFeatures(Mat faceRegion){
        Map<String, Double> features = new HashMap<>();

        Scalar meanScalar = Core.mean(faceRegion);
        features.put("brightness", meanScalar.val[0]);

        Mat laplacian = new Mat();
        Imgproc.Laplacian(faceRegion, laplacian, CvType.CV_64F);
        MatOfDouble variance = new MatOfDouble();
        MatOfDouble mean = new MatOfDouble();
        Core.meanStdDev(laplacian, mean, variance);
        features.put("contast", variance.get(0,0)[0] * variance.get(0,0)[0]);

        features.put("symmetry", calculateFacialSymmetry(faceRegion));

        features.put("edge_density", calculateEdgeDensity(faceRegion));

        features.put("aspect_ratio", (double)faceRegion.cols() / faceRegion.rows());

        return features;
    }

    private double calculateFacialSymmetry(Mat faceRegion){
        try{
            int width = faceRegion.cols();
            int height = faceRegion.rows();

            Mat leftHalf = new Mat(faceRegion, new Rect(0,0, width/2, height));
            Mat rightHalf = new Mat(faceRegion, new Rect(width/2, 0, width/2, height));

            Mat rightHalfFlipped = new Mat();
            Core.flip(rightHalf, rightHalfFlipped, 1);

            Mat diff = new Mat();
            Core.absdiff(leftHalf, rightHalfFlipped, diff);
            Scalar meanDiff = Core.mean(diff);

            return 255.0 - meanDiff.val[0];
        } catch (Exception e){
            return 128.0;
        }
    }

    private double calculateEdgeDensity(Mat faceRegion){
        try{
            Mat edges = new Mat();
            Imgproc.Canny(faceRegion, edges, 50, 150);
            int nonZeroPixels = Core.countNonZero(edges);
            int totalPixels = faceRegion.rows() * faceRegion.cols();
            return (double)nonZeroPixels / totalPixels;
        } catch (Exception e){
            return 0.1;
        }
    }

    private String determineMoodFromFeatures(Map<String, Double> features){
        double brightness = features.getOrDefault("brightness", 128.0);
        double contrast = features.getOrDefault("contrast", 500.0);
        double symmetry = features.getOrDefault("symmetry", 150.0);
        double edgeDensity = features.getOrDefault("edge_density", 0.1);
        double aspectRatio = features.getOrDefault("aspect_ration", 1.0);

        if (symmetry > 180 && contrast > 800 && edgeDensity < 0.15) {
            return "focused";
        }

        // Pessoa energética: alto contraste, densidade de bordas alta, brilho alto
        if (contrast > 1000 && edgeDensity > 0.2 && brightness > 140) {
            return "energetic";
        }

        // Pessoa cansada: baixo contraste, baixo brilho
        if (contrast < 400 && brightness < 100) {
            return "tired";
        }

        // Pessoa estressada: baixa simetria, alta densidade de bordas
        if (symmetry < 140 && edgeDensity > 0.25) {
            return "stressed";
        }

        // Pessoa criativa: contraste médio, simetria média, aspect ratio não extremo
        if (contrast > 500 && contrast < 900 && symmetry > 140 && symmetry < 180) {
            return "creative";
        }

        // Pessoa relaxada: valores equilibrados
        if (brightness > 110 && brightness < 160 && contrast > 300 && symmetry > 150) {
            return "relaxed";
        }

        return "neutral";
    }

    public String getMoodDescription(String mood){
        return switch (mood){
            case "focused" -> " Concentrado e produtivo";
            case "energetic" -> "Energético e motivado";
            case "creative" -> "Criativo e inspirado";
            case "relaxed" -> "Relaxado e tranquilo";
            case "tired" -> "Cansado";
            case "stressed" -> "Estressado ou tenso";
            default -> "Estado neutro";
        };
    }

    public String[] getAllMoods(){
        return new String[]{"focused", "energetic", "creative", "relaxed", "tired", "stressed", "neutral"};
    }
}
