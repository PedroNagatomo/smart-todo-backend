package com.smarttodo.config;

import jakarta.annotation.PostConstruct;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenCVConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenCVConfig.class);

    @PostConstruct
    public void initOpenCV(){
        try{
            nu.pattern.OpenCV.loadShared();
            logger.info("OpenCV carregado com sucesso!");
            logger.info("Versão OpenCV: {}", Core.VERSION);
        } catch (Exception e){
            logger.error("Erro ao carregar OpenCV: ", e);
            logger.warn("Computer Vision será desabilitado");
        }
    }
}
