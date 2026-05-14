package com.docqa.service;

import com.docqa.model.UploadedFile;
import com.docqa.repository.FileRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private OpenAIService openAIService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public UploadedFile saveFile(MultipartFile multipartFile, Long userId) throws IOException {

        // 1. Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. Generate unique filename
        String originalName = multipartFile.getOriginalFilename();
        if (originalName == null) originalName = "unknown_file";
        String uniqueName = System.currentTimeMillis() + "_" + originalName;

        // 3. Full path for saving
        Path filePath = uploadPath.resolve(uniqueName);

        // 4. Save file to disk using NIO (works on Windows)
        Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 5. Detect file type
        String fileType = detectFileType(originalName);

        // 6. Extract text from PDF
        String extractedText = "";
        if ("pdf".equals(fileType)) {
            extractedText = extractPdfText(filePath.toString());
        }

        // 7. Save metadata to database
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setFileName(originalName);
        uploadedFile.setFileType(fileType);
        uploadedFile.setFilePath(filePath.toString());
        uploadedFile.setExtractedText(extractedText);
        uploadedFile.setUserId(userId);
        uploadedFile.setUploadedAt(LocalDateTime.now());

        return fileRepository.save(uploadedFile);
    }

    public List<UploadedFile> getFilesByUser(Long userId) {
        return fileRepository.findByUserId(userId);
    }

    public UploadedFile getFileById(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    public void deleteFile(Long fileId) throws IOException {
        UploadedFile file = getFileById(fileId);
        Files.deleteIfExists(Paths.get(file.getFilePath()));
        fileRepository.delete(file);
    }

    private String extractPdfText(String filePath) {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            return "Error extracting PDF text: " + e.getMessage();
        }
    }

    private String detectFileType(String fileName) {
        if (fileName == null) return "unknown";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))  return "pdf";
        if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a")) return "audio";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "video";
        return "unknown";
    }
}