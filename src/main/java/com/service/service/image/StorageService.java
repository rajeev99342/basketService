package com.service.service.image;

import com.service.model.GlobalResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface StorageService {
    public GlobalResponse uploadFile(MultipartFile file, String fileName) throws IOException;

    public GlobalResponse getImage(Long id);
}
