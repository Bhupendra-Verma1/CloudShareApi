package com.bhupendra.cloudshareapi.repository;

import com.bhupendra.cloudshareapi.document.FileMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileMetadataRepository extends MongoRepository<FileMetadataDocument, String> {

    List<FileMetadataDocument> findByClerkId(String clerkId);
    List<FileMetadataDocument> findByClerkIdOrderByUploadedAtDesc(String clerkId);
    Long countByClerkId(String clerkId);
    void deleteByClerkId(String clerkId);
}
