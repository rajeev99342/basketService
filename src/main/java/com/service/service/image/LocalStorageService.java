package com.service.service.image;

import com.service.entities.ImageDetails;
import com.service.model.GlobalResponse;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class LocalStorageService implements StorageService {
    @Value("${cloud.aws.credentials.secret-key}")
    private String RESOURCE_PATH2;
    @Value("${melaa.imageResource}")
    private String RESOURCE_PATH;


    @Autowired
    ImageDetailsRepository imageDetailsRepository;

    public LocalStorageService() {
    }

    public void saveIntoLocalFile(String imageReference, MultipartFile photo) throws IOException {

    }

    @Override
    @Transactional
    public GlobalResponse uploadFile(MultipartFile file, String imageReferenceDir) throws IOException {
        log.info("RESOURCE PATH : {} ", RESOURCE_PATH);
        Path newFile = Paths.get(RESOURCE_PATH + imageReferenceDir);
        Files.createDirectories(newFile.getParent());
        Files.write(newFile, file.getBytes());
        return GlobalResponse.getSuccess(newFile);
    }

    @Override
    public GlobalResponse getImage(Long id) {
        GlobalResponse globalResponse = null;
        try {
            ImageDetails imageDetails = imageDetailsRepository.findImageDetailsById(id);
            File file = new File(imageDetails.getPath());
            InputStream in = new FileInputStream(file);
            Object byteArray = IOUtils.toByteArray(in);
            globalResponse = new GlobalResponse("fetched successfully", HttpStatus.OK.value(), true, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse = new GlobalResponse("unable to fetch image", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return globalResponse;
    }
}
