package com.docqa.repository;

import com.docqa.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findByUserId(Long userId);
    List<UploadedFile> findByUserIdAndFileType(Long userId, String fileType);
}
