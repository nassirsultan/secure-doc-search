package com.mca.project.securedocsearch.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long docId;

    private String docName;

    /**
     * Stored as relative filename under file.upload-dir (e.g. "uuid.enc").
     * Keeping relative names makes deployments portable.
     */
    private String filePath;

    private String fileType;
    private String uploadedBy;
    private LocalDateTime uploadDate;

    /**
     * true if file is encrypted on disk (we encrypt on upload by default).
     */
    @Column(nullable = false)
    private boolean encrypted = true;
}
