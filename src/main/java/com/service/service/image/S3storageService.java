package com.service.service.image;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.service.entities.ImageDetails;
import com.service.model.GlobalResponse;
import com.service.repos.ImageDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class S3storageService implements StorageService {

    @Value("${cloud.aws.bucketName}")
    private String bucketName;

    @Autowired
    ImageDetailsRepository imageDetailsRepository;
    @Autowired
    private AmazonS3 s3Client;

    public GlobalResponse uploadFile(MultipartFile file, String fileName) {
        File fileObj = convertMultiPartFileToFile(file);
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        Path path = Paths.get(fileName);
        return GlobalResponse.getSuccess(path);
    }

    @Override
    public GlobalResponse getImage(Long id) {
        GlobalResponse globalResponse = null;
        try {
            ImageDetails imageDetails = imageDetailsRepository.findImageDetailsById(id);
            S3Object s3Object = s3Client.getObject(bucketName, imageDetails.getImageName());
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            Object byteArray = org.apache.commons.io.IOUtils.toByteArray(inputStream);
//                byte[] content = IOUtils.toByteArray(inputStream);
//                return content;

            globalResponse = new GlobalResponse("fetched successfully", HttpStatus.OK.value(), true, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse = new GlobalResponse("unable to fetch image", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return globalResponse;
    }


    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
}