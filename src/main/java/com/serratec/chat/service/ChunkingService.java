package com.serratec.chat.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    public List<String> createChunks(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("O tamanho do chunk deve ser maior que zero.");
        }

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("O overlap não pode ser maior ou igual ao tamanho do chunk.");
        }

        int inputLength = text.length();
        int start = 0;

        while (start < inputLength) {
            int end = Math.min(start + chunkSize, inputLength);
            
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= inputLength) {
                break;
            }

            start += (chunkSize - chunkOverlap);
        }

        return chunks;
    }
}