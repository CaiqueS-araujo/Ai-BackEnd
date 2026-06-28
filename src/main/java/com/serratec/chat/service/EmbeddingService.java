package com.serratec.chat.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class EmbeddingService {

    public float[] getEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("O texto para geração de embedding não pode ser nulo ou vazio.");
        }

        int dimensao = 1536; 
        float[] vector = new float[dimensao];
        
       
        long seed = text.hashCode();
        Random random = new Random(seed);
        
        float somaQuadradros = 0;
        for (int i = 0; i < dimensao; i++) {
            vector[i] = random.nextFloat() - 0.5f; 
            somaQuadradros += vector[i] * vector[i];
        }
        
        
        float magnitude = (float) Math.sqrt(somaQuadradros);
        for (int i = 0; i < dimensao; i++) {
            vector[i] = vector[i] / magnitude;
        }

        return vector;
    }
}