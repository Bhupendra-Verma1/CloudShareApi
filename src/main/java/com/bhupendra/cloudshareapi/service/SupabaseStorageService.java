package com.bhupendra.cloudshareapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service.key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate restTemplate = new RestTemplate();

    // upload file
    public void upload(String storagePath, MultipartFile file) {
        try {
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);
            headers.set("Content-Type", file.getContentType());

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    // delete file
    public void delete(String storagePath) {
        try {
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + serviceRoleKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, entity, String.class);

        } catch (Exception e) {
            throw new RuntimeException("Delete failed", e);
        }
    }

    // download url
    public String createSignedUrl(String storagePath, int expiresInSeconds) throws Exception {
        String url = supabaseUrl + "/storage/v1/object/sign/" + bucket;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("expiresIn", expiresInSeconds);
        body.put("paths", List.of(storagePath));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        String raw = response.getBody();
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> list = mapper.readValue(
                raw,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        Map<String, Object> entry = list.getFirst();
        String signedUrl = entry.get("signedURL").toString();

        if (signedUrl != null) {
            return supabaseUrl + "/storage/v1" + signedUrl;
        }

        throw new RuntimeException("Unable to create signed url");
    }

    public String getSignedDownloadUrl(String storagePath) throws Exception {
        return createSignedUrl(storagePath, 60);
    }
}
