package com.service.service;

import com.service.entities.ImageDetails;
import com.service.model.GlobalResponse;
import com.service.repos.ImageDetailsRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Service
public class ImageService {
    String RESOURCES_DIR ="/home/rajeev/Documents/images/";

    @Autowired
    private ImageDetailsRepository imageDetailsRepository;

    public GlobalResponse saveImage(MultipartFile photo) {
        try{
            Path newFile = Paths.get(RESOURCES_DIR + new Date().getTime()  + photo.getOriginalFilename());
            Files.createDirectories(newFile.getParent());
            Files.write(newFile, photo.getBytes());
            ImageDetails imageDetails = saveImageDetails(newFile,photo);
            GlobalResponse successfully_created = null;
            if(null != imageDetails){
                successfully_created  = new GlobalResponse("Successfully created", HttpStatus.CREATED, true, imageDetails);
            }else{
                successfully_created = new GlobalResponse("Successfully created but unable to save into DB", HttpStatus.INTERNAL_SERVER_ERROR, true, imageDetails);
            }
            System.out.println("Image created");
            return successfully_created;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    private ImageDetails saveImageDetails(Path newFile,MultipartFile image){
        try{
            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setImageName(newFile.getFileName().toString());
            imageDetails.setType(image.getContentType());
            imageDetails.setPath(newFile.toAbsolutePath()
                    .toString());
            return imageDetailsRepository.save(imageDetails);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    public GlobalResponse getImage(Long id){
        GlobalResponse globalResponse = null;
        try{
            ImageDetails imageDetails = imageDetailsRepository.getById(id);
            InputStream in = getClass()
                    .getResourceAsStream(imageDetails.getPath());
            Object byteArray =  IOUtils.toByteArray(in);
            globalResponse = new GlobalResponse("fetched successfully",HttpStatus.OK,true,byteArray);
        }catch (Exception e){
            e.printStackTrace();
            globalResponse = new GlobalResponse("unable to fetch image",HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return globalResponse;
    }

}
