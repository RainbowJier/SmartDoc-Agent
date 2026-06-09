package com.smartdoc.api;

import com.smartdoc.common.entity.AjaxResult;
import com.smartdoc.common.enums.FileTypeENum;
import com.smartdoc.rag.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final List<String> uploadedDocuments = new CopyOnWriteArrayList<>();

    /**
     * upload files.
     */
    @PostMapping("/upload")
    public AjaxResult<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return AjaxResult.badRequest("File name is required", null);
        }

        String extension = getExtension(originalFilename);
        FileTypeENum.fromCode(extension);

        Path tempDir = Path.of("data/temp");
        Path tempFile = null;
        try {
            Files.createDirectories(tempDir);
            String safeFilename = Path.of(originalFilename).getFileName().toString();
            tempFile = tempDir.resolve(System.currentTimeMillis() + "_" + safeFilename);

            file.transferTo(tempFile.toAbsolutePath().toFile());

            ingestionService.ingestDocument(tempFile);

            uploadedDocuments.add(originalFilename);
            log.info("Uploaded and ingested: {}", originalFilename);

            return AjaxResult.success(originalFilename);
        } catch (Exception e) {
            log.error("Failed to process document: {}", originalFilename, e);
            return AjaxResult.failed("Failed to process document. Please check the file format and try again.");
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }

    @GetMapping
    public AjaxResult<List<String>> listDocuments() {
        return AjaxResult.success(new ArrayList<>(uploadedDocuments));
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }

}
