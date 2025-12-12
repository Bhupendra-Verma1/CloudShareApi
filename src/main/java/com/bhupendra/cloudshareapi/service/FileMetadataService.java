package com.bhupendra.cloudshareapi.service;


import com.bhupendra.cloudshareapi.document.FileMetadataDocument;
import com.bhupendra.cloudshareapi.document.ProfileDocument;
import com.bhupendra.cloudshareapi.dto.FileMetadataDTO;
import com.bhupendra.cloudshareapi.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {
    private final FileMetadataRepository fileMetadataRepository;
    private final ProfileService profileService;
    private final UserCreditsService userCreditsService;
    private final SupabaseStorageService supabaseStorageService;

    public List<FileMetadataDTO> uploadFiles(MultipartFile[] files) throws IOException {
        ProfileDocument currentProfile = profileService.getCurrentProfile();
        List<FileMetadataDocument> savedFiles = new ArrayList<>();

        if (!userCreditsService.haveEnoughCredits(files.length)) {
            throw new RuntimeException("Not enough credits to upload files. Please purchase more credits");
        }

        for (MultipartFile file : files) {

            String newName = UUID.randomUUID() + "." +
                    StringUtils.getFilenameExtension(file.getOriginalFilename());

            // The internal storage key
            String storagePath = currentProfile.getClerkId() + "/" + newName;

            // Upload file to Supabase
            supabaseStorageService.upload(storagePath, file);

            FileMetadataDocument fileMetadata = FileMetadataDocument.builder()
                    .name(file.getOriginalFilename())
                    .storagePath(storagePath)
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            userCreditsService.consumeCredit();
            savedFiles.add(fileMetadataRepository.save(fileMetadata));
        }

        return savedFiles.stream().map(this::mapToDTO).toList();
    }


    public List<FileMetadataDTO> getFiles() {
        ProfileDocument existingProfile = profileService.getCurrentProfile();
        return fileMetadataRepository.findByClerkId(existingProfile.getClerkId())
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileMetadataDTO getPublicFile(String id) {
        Optional<FileMetadataDocument> fileOptional = fileMetadataRepository.findById(id);
        if(fileOptional.isEmpty() || !fileOptional.get().getIsPublic()) {
            throw new RuntimeException("Unable to get the file");
        }
        return mapToDTO(fileOptional.get());
    }

    public String getDownloadableFile(String id) throws Exception {
        FileMetadataDocument file = fileMetadataRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
        return supabaseStorageService.getSignedDownloadUrl(file.getStoragePath());
    }

    public void deleteFile(String id) {
        try {
            ProfileDocument currentProfile = profileService.getCurrentProfile();
            FileMetadataDocument file = fileMetadataRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if (!file.getClerkId().equals(currentProfile.getClerkId())) {
                throw new RuntimeException("File does not belong to current user");
            }

            // Delete from Supabase using storagePath
            supabaseStorageService.delete(file.getStoragePath());

            // Delete from DB
            fileMetadataRepository.deleteById(id);

        } catch (Exception e) {
            throw new RuntimeException("Error deleting the file: " + e.getMessage());
        }
    }

    public FileMetadataDTO togglePublic(String id) {
        FileMetadataDocument existingFile = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        existingFile.setIsPublic(!existingFile.getIsPublic());
        existingFile = fileMetadataRepository.save(existingFile);

        return mapToDTO(existingFile);
    }

    public void deleteAllUserFiles(String clerkId) {
        List<FileMetadataDocument> files = fileMetadataRepository.findByClerkId(clerkId);

        for (FileMetadataDocument file : files) {
            try {
                supabaseStorageService.delete(file.getStoragePath()); // Supabase delete
            } catch (Exception e) {
                // Log, but continue deleting others
                System.err.println("Failed to delete: " + file.getStoragePath());
            }
        }
        fileMetadataRepository.deleteByClerkId(clerkId);
    }

    private FileMetadataDTO mapToDTO(FileMetadataDocument fileMetadataDocument) {
        return FileMetadataDTO.builder()
                .id(fileMetadataDocument.getId())
                .storagePath(fileMetadataDocument.getStoragePath())
                .name(fileMetadataDocument.getName())
                .size(fileMetadataDocument.getSize())
                .type(fileMetadataDocument.getType())
                .clerkId(fileMetadataDocument.getClerkId())
                .isPublic(fileMetadataDocument.getIsPublic())
                .uploadedAt(fileMetadataDocument.getUploadedAt())
                .build();
    }

}
