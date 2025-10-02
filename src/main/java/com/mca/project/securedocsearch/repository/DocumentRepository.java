package com.mca.project.securedocsearch.repository;

import com.mca.project.securedocsearch.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // add custom queries if needed (e.g., findByUploadedBy)
}
