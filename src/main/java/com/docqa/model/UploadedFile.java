package com.docqa.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;          // original file name e.g. "report.pdf"

    @Column(nullable = false)
    private String fileType;          // "pdf", "audio", "video"

    @Column(nullable = false)
    private String filePath;          // where the file is saved on disk

    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;     // text from PDF or audio/video transcript

    @Column(columnDefinition = "LONGTEXT")
    private String summary;           // AI-generated summary (cached)

    @Column(nullable = false)
    private Long userId;              // which user uploaded it

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
