package com.docqa.controller;

import com.docqa.model.UploadedFile;
import com.docqa.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * POST /api/files/upload
     * Form-data: file=<file>, userId=<number>
     * Returns: uploaded file metadata
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty."));
            }
            UploadedFile saved = fileService.saveFile(file, userId);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/files/user/{userId}
     * Returns all files uploaded by a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UploadedFile>> getFilesByUser(@PathVariable Long userId) {
        List<UploadedFile> files = fileService.getFilesByUser(userId);
        return ResponseEntity.ok(files);
    }

    /**
     * GET /api/files/{fileId}
     * Returns a single file's metadata.
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFile(@PathVariable Long fileId) {
        try {
            UploadedFile file = fileService.getFileById(fileId);
            return ResponseEntity.ok(file);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/files/{fileId}
     * Deletes a file from disk and database.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileService.deleteFile(fileId);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
