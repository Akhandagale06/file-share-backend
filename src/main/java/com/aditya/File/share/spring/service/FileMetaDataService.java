package com.aditya.File.share.spring.service;

import com.aditya.File.share.spring.documents.FileMetaDataDoc;
import com.aditya.File.share.spring.documents.ProfileDocument;
import com.aditya.File.share.spring.dto.FileMetaDataDTO;
import com.aditya.File.share.spring.repository.FileMetaDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetaDataService {

    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final FileMetaDataRepo fileMetaDataRepo;
    private final SupabaseStorageService supabaseStorageService;


    public List<FileMetaDataDTO> uploadFiles(MultipartFile files[]) throws IOException {
     ProfileDocument currentProfile= profileService.getCurrentProfile();
        List<FileMetaDataDoc> savedFiles = new ArrayList<>();

        if(!userCreditsService.hasEnoughCredits(files.length)){
         throw new RuntimeException("Not enough credits");

     }
        for(MultipartFile file : files){
         Map uploadResult = supabaseStorageService.uploadFile(file);
         String fileUrl = uploadResult.get("secure_url").toString();
         String publicId = uploadResult.get("public_id").toString();

         FileMetaDataDoc fileMetaData = FileMetaDataDoc.builder()
                 .fileLocation(fileUrl)
                 .publicId(publicId)
                 .name(file.getOriginalFilename())
                 .size(file.getSize())
                 .type(file.getContentType())
                 .clerkId(currentProfile.getClerkId())
                 .isPublic(false)
                 .uploadAt(LocalDateTime.now())
                 .build();


             userCreditsService.consumeCredit();


            savedFiles.add(fileMetaDataRepo.save(fileMetaData));

        }

        return savedFiles.stream().map(fileMetaDataDoc -> mapToDTO(fileMetaDataDoc))
                .collect(Collectors.toList());

    }

    private FileMetaDataDTO mapToDTO(FileMetaDataDoc fileMetaDataDoc) {
      return    FileMetaDataDTO.builder()
                .id(fileMetaDataDoc.getId())
                .fileLocation(fileMetaDataDoc.getFileLocation())
                .publicId(fileMetaDataDoc.getPublicId())
                .name(fileMetaDataDoc.getName())
                .size(fileMetaDataDoc.getSize())
                .type(fileMetaDataDoc.getType())
                .clerkId(fileMetaDataDoc.getClerkId())
                .isPublic(fileMetaDataDoc.getIsPublic())
                .uploadAt(fileMetaDataDoc.getUploadAt())
                .build();
    }

    public List<FileMetaDataDTO> getFiles(){
        ProfileDocument currentProfile= profileService.getCurrentProfile();
        List<FileMetaDataDoc> files = fileMetaDataRepo.findByClerkId(currentProfile.getClerkId());
        return files.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    public FileMetaDataDTO getPublicFile(String id){
        Optional<FileMetaDataDoc> fileOptional = fileMetaDataRepo.findById(id);
        if(fileOptional.isEmpty() || !fileOptional.get().getIsPublic()){
            throw new RuntimeException("Not able to fetch file");
        }
        FileMetaDataDoc fileMetaDataDoc = fileOptional.get();
        return mapToDTO(fileMetaDataDoc);
    }
    public FileMetaDataDTO getDownloadFiles(String id){
        FileMetaDataDoc file = fileMetaDataRepo.findById(id).orElseThrow(()->new RuntimeException("File Not found"));
        return mapToDTO(file);
    }

    public void deleteFile(String id){
        try{
            ProfileDocument currentProfile= profileService.getCurrentProfile();
            FileMetaDataDoc file = fileMetaDataRepo.findById(id).orElseThrow(()->new RuntimeException("File Not found"));
            if(!file.getClerkId().equals(currentProfile.getClerkId())){
                throw new RuntimeException("File does not belong to existing user");
            }
            if (file.getPublicId() != null) {
                supabaseStorageService.deleteFile(file.getPublicId());
            } else {
                try {
                    java.nio.file.Path filePath = java.nio.file.Paths.get(file.getFileLocation());
                    java.nio.file.Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    System.err.println("Failed to delete legacy local file: " + e.getMessage());
                }
            }
            fileMetaDataRepo.deleteById(id);

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Deleting file failed");
        }
    }
     public FileMetaDataDTO togglePublic(String id){
        FileMetaDataDoc file = fileMetaDataRepo.findById(id).orElseThrow(()->new RuntimeException("File Not found"));
        file.setIsPublic(!file.getIsPublic());
        fileMetaDataRepo.save(file);
        return mapToDTO(file);
     }
}
