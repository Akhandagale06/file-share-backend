package com.aditya.File.share.spring.controller;

import com.aditya.File.share.spring.documents.UserCredits;
import com.aditya.File.share.spring.dto.FileMetaDataDTO;
import com.aditya.File.share.spring.service.FileMetaDataService;
import com.aditya.File.share.spring.service.UserCreditsService;
import com.aditya.File.share.spring.service.SupabaseStorageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {

    private final FileMetaDataService fileMetaDataService;
    private final UserCreditsService userCreditsService;
    private final SupabaseStorageService supabaseStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile [] files) throws IOException {
        Map<String, Object> response = new HashMap<>();

       List<FileMetaDataDTO> list= fileMetaDataService.uploadFiles(files);

        UserCredits finalCredits= userCreditsService.getUserCredits();
        response.put("files", list);
        response.put("remainingCredits",finalCredits.getCredits());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser(){
        List<FileMetaDataDTO> files= fileMetaDataService.getFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id){
        FileMetaDataDTO file =fileMetaDataService.getPublicFile(id);
        return ResponseEntity.ok(file);
    }
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable String id) {
        FileMetaDataDTO file = fileMetaDataService.getDownloadFiles(id);
        String fileLocation = file.getFileLocation();
        boolean isLocal = !fileLocation.startsWith("http");

        try {
            String encodedFilename = java.net.URLEncoder.encode(file.getName(), java.nio.charset.StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            if (isLocal) {
                Resource resource = new UrlResource(java.nio.file.Paths.get(fileLocation).toUri());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                // Supabase Strategy: Redirect to the public URL.
                // Appending '?download=' forces most browsers to download rather than open.
                String downloadUrl = fileLocation.contains("?") ? fileLocation + "&download=" : fileLocation + "?download=";
                
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, downloadUrl)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error downloading file: " + e.getMessage(), e);
        }
    }

    @GetMapping("/download-local/{id}")
    public ResponseEntity<Resource> downloadLocal(@PathVariable String id) {
        FileMetaDataDTO file = fileMetaDataService.getDownloadFiles(id);
        String fileLocation = file.getFileLocation();

        if (fileLocation.startsWith("http")) {
            throw new RuntimeException("This endpoint is only for local proxy downloads.");
        }

        try {
            Resource resource = new UrlResource(java.nio.file.Paths.get(fileLocation).toUri());
            String encodedFilename = java.net.URLEncoder.encode(file.getName(), java.nio.charset.StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error downloading local file", e);
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?>  deleteFile(@PathVariable String id){
        fileMetaDataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic (@PathVariable String id){
     FileMetaDataDTO file= fileMetaDataService.togglePublic(id);
        return ResponseEntity.ok(file);
    }
}
