package com.service.service;

import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.GlobalResponse;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    String RESOURCES_DIR ="/home/rajeev/Documents/images/";

    @Autowired
    private ImageDetailsRepository imageDetailsRepository;

    @Autowired
    private ImageRepository imageRepository;

    private ImageDetails saveImageDetails(Path newFile,MultipartFile image,String imageReference){
        try{
            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setImageName(imageReference);
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
            ImageDetails imageDetails = imageDetailsRepository.findImageDetailsById(id);
            File file = new File(imageDetails.getPath());
            InputStream in=new FileInputStream(file);
            Object byteArray =  IOUtils.toByteArray(in);
            globalResponse = new GlobalResponse("fetched successfully",HttpStatus.OK.value(),true,byteArray);
        }catch (Exception e){
            e.printStackTrace();
            globalResponse = new GlobalResponse("unable to fetch image",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return globalResponse;
    }



    public GlobalResponse saveImage(MultipartFile photo,String imageReference) {
        try{
            Path newFile = Paths.get(RESOURCES_DIR + imageReference);
            Files.createDirectories(newFile.getParent());
            Files.write(newFile, photo.getBytes());
            ImageDetails imageDetails = saveImageDetails(newFile,photo,imageReference);
            GlobalResponse successfully_created = null;
            if(null != imageDetails){
                successfully_created  = new GlobalResponse("Successfully created", HttpStatus.OK.value(), true, imageDetails);
            }else{
                successfully_created = new GlobalResponse("Successfully created but unable to save into DB", HttpStatus.INTERNAL_SERVER_ERROR.value(), true, imageDetails);
            }
            System.out.println("Image created");
            return successfully_created;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }


    public List<Object> getAllImageByProduct(Product product){
        List<Image> images =  imageRepository.findImageByProduct(product);
        List<Object> base64List = new ArrayList<>();
        for(Image image : images){
           GlobalResponse res =  getImage(image.getImageDetails().getId());
           base64List.add(res.getBody());
        }

        return base64List;
    }
}
