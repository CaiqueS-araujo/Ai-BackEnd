package com.serratec.chat.service;

import com.serratec.chat.domain.DocumentChunk;
import com.serratec.chat.repository.DocumentChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class DocumentIngestionService {
    @Autowired
    private ChunkingService chunkingService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    public void ingestDocument(Long attachmentId, byte[] fileBytes, String fileName) {
        String textoExtraido = new String(fileBytes); 
        if (fileName.endsWith(".txt")) {
            textoExtraido = new String(fileBytes);
        } else {
            textoExtraido = "Conteúdo simulado do arquivo " + fileName + "\n" + textoExtraido;
        }

        List<String> chunksText = chunkingService.createChunks(textoExtraido, 500, 50);

        for (String chunk : chunksText) {
            
            float[] embedding = embeddingService.getEmbedding(chunk);
            
            String vetorString = Arrays.toString(embedding);

            DocumentChunk documentChunk = new DocumentChunk(attachmentId, chunk, vetorString);
            chunkRepository.save(documentChunk);
        }

        System.out.println("LOG INTERNO: Documento " + fileName + " indexado com sucesso. Total de chunks: " + chunksText.size());
    }
}