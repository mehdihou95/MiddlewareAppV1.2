package com.xml.processor.controller;

import com.xml.processor.config.ClientContextHolder;
import com.xml.processor.model.ProcessedFile;
import com.xml.processor.service.XmlProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final XmlProcessorService xmlProcessorService;

    public FileUploadController(XmlProcessorService xmlProcessorService) {
        this.xmlProcessorService = xmlProcessorService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ProcessedFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientId") Long clientId,
            @RequestParam("interfaceId") Long interfaceId) {
        
        // Set client context from parameters
        ClientContextHolder.setClientId(clientId);
        
        // Process file with interface ID
        ProcessedFile processedFile = xmlProcessorService.processXmlFile(file, interfaceId);
        return ResponseEntity.ok(processedFile);
    }

    @GetMapping("/files/processed")
    public ResponseEntity<List<ProcessedFile>> getProcessedFiles() {
        return ResponseEntity.ok(xmlProcessorService.getProcessedFiles());
    }

    @GetMapping("/files/errors")
    public ResponseEntity<List<ProcessedFile>> getErrorFiles() {
        return ResponseEntity.ok(xmlProcessorService.getErrorFiles());
    }
} 