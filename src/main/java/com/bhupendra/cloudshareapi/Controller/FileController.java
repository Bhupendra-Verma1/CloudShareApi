package com.bhupendra.cloudshareapi.Controller;

import com.bhupendra.cloudshareapi.document.UserCredits;
import com.bhupendra.cloudshareapi.dto.FileMetadataDTO;
import com.bhupendra.cloudshareapi.service.FileMetadataService;
import com.bhupendra.cloudshareapi.service.UserCreditsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class FileController {
    private final FileMetadataService fileMetadataService;
    private final UserCreditsService userCreditsService;

    @PostMapping("/upload")
    public ResponseEntity<?> uplaodFiles(@RequestPart("Files") MultipartFile[] files) throws IOException {
        Map<String, Object> response = new HashMap<>();
        List<FileMetadataDTO> fileMetadatas = fileMetadataService.uploadFiles(files);
        UserCredits userCredits = userCreditsService.getUserCredits();
        response.put("files", fileMetadatas);
        response.put("remainingCredits", userCredits.getCredits());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getFilesForCurrentUser() {
        List<FileMetadataDTO> files = fileMetadataService.getFiles();
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicFile(@PathVariable String id) {
        FileMetadataDTO file = fileMetadataService.getPublicFile(id);
        return ResponseEntity.ok(file);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> dowload(@PathVariable String id) throws Exception {
        String signedUrl = fileMetadataService.getDownloadableFile(id);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, signedUrl)
                .build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteFile(@PathVariable String id) {
        fileMetadataService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-public")
    public ResponseEntity<?> togglePublic(@PathVariable String id) {
        FileMetadataDTO file = fileMetadataService.togglePublic(id);
        return ResponseEntity.ok(file);
    }

}
