package com.service.utilites;

import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.CategoryModel;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Slf4j
public class ImageUtility {
    @Autowired
    ImageDetailsRepository imageDetailsRepository;
    @Autowired
    ImageRepository imageRepository;


    public String getImageName(String type , String name){
        name = name.replaceAll("\\s", "-").replaceAll("\\|","-").replaceAll("---","-");
        return System.currentTimeMillis() + "-" + type + "-" + name;
    }

    public void deleteCategoryImage(CategoryModel model){
        // delete previous image
        Image img = imageRepository.findImageByCategoryId(model.getId());
        File file = new File(img.getImageDetails().getPath());
        file.delete();
        imageRepository.delete(imageRepository.findImageByCategoryId(model.getId()));
        ImageDetails imageDetails1 = imageDetailsRepository.findImageDetailsById(img.getImageDetails().getId());
        if(null != imageDetails1){
            log.error("Details does not deleted");
        }else{
            log.info("Details deleted");
        }
    }


    public void deleteProductImages(Product product){
        // delete previous image
        List<Image> imgs = imageRepository.findImageByProduct(product);
        for(Image img : imgs){
            File file = new File(img.getImageDetails().getPath());
            file.delete();
            imageRepository.delete(img);
            ImageDetails imageDetails1 = imageDetailsRepository.findImageDetailsById(img.getImageDetails().getId());
            if(null != imageDetails1){
                log.error("Details does not deleted");
            }else{
                log.info("Details deleted");
            }
        }

    }
}
