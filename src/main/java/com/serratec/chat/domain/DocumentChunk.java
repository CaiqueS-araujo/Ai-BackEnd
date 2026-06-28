package com.serratec.chat.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "attachment_id", nullable = false)
    private Long attachmentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    
    @Column(columnDefinition = "TEXT")
    private String embeddingSimulado;

    
    public DocumentChunk() {}

    public DocumentChunk(Long attachmentId, String content, String embeddingSimulado) {
        this.attachmentId = attachmentId;
        this.content = content;
        this.embeddingSimulado = embeddingSimulado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEmbeddingSimulado() { return embeddingSimulado; }
    public void setEmbeddingSimulado(String embeddingSimulado) { this.embeddingSimulado = embeddingSimulado; }
}
