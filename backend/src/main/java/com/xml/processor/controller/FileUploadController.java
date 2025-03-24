package com.xml.processor.controller;

import com.xml.processor.model.ProcessedFile;
import com.xml.processor.service.interfaces.XmlProcessorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final XmlProcessorService xmlProcessorService;

    public FileUploadController(XmlProcessorService xmlProcessorService) {
        this.xmlProcessorService = xmlProcessorService;
    }

    @PostMapping("/upload/{interfaceId}")
    public ResponseEntity<ProcessedFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long interfaceId) {
        return ResponseEntity.ok(xmlProcessorService.processXmlFileAsync(file, interfaceId).join());
    }

    @GetMapping("/processed")
    public ResponseEntity<Page<ProcessedFile>> getProcessedFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(xmlProcessorService.getProcessedFiles(pageRequest));
    }

    @GetMapping("/errors")
    public ResponseEntity<Page<ProcessedFile>> getErrorFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(xmlProcessorService.getErrorFiles(pageRequest));
    }

    @PostMapping("/reprocess/{fileId}")
    public ResponseEntity<Void> reprocessFile(@PathVariable Long fileId) {
        xmlProcessorService.reprocessFile(fileId);
        return ResponseEntity.ok().build();
    }
} 