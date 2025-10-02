package com.mca.project.securedocsearch.dto;

import java.time.LocalDateTime;

public record SearchResult(
        Long docId,
        String docName,
        String snippet,
        String uploadedBy,
        LocalDateTime uploadDate
) {}
