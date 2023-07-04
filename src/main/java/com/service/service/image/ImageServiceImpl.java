package com.service.service.image;

import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.GlobalResponse;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImageServiceImpl implements ImageHandler {
    @Value("${melaa.imageResource}")
    private String RESOURCE_PATH;

    @Autowired
    ImageFactory imageFactory;
    @Value("${spring.profiles.active}")
    private String env;


    @Autowired
    private ImageDetailsRepository imageDetailsRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public ImageDetails saveImageDetails(Path newFile, MultipartFile image, String imageReference) {

            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setImageName(imageReference);
            imageDetails.setType(image.getContentType());
            imageDetails.setPath(newFile.toAbsolutePath()
                    .toString());
            return imageDetailsRepository.save(imageDetails);
    }

    @Override
    public GlobalResponse getImage(Long id) {
        GlobalResponse globalResponse = null;
        try {
            ImageDetails imageDetails = imageDetailsRepository.findImageDetailsById(id);
//            File file = new File(imageDetails.getPath());
//            InputStream in = new FileInputStream(file);
//            Object byteArray = IOUtils.toByteArray(in);
            StorageService storage = imageFactory.getStorageType(env);
            return storage.getImage(id);
//            globalResponse = new GlobalResponse("fetched successfully", HttpStatus.OK.value(), true, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse = new GlobalResponse("unable to fetch image", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return globalResponse;
    }

    @Override
    public GlobalResponse saveImage(MultipartFile photo, String imageReference) throws IOException {
        StorageService storage = imageFactory.getStorageType(env);
        GlobalResponse res = storage.uploadFile(photo, imageReference);
        ImageDetails imageDetails = saveImageDetails((Path) res.getBody(), photo, imageReference);
        GlobalResponse successfully_created = null;
        if (null != imageDetails) {
            successfully_created = new GlobalResponse("Successfully created", HttpStatus.OK.value(), true, imageDetails);
        } else {
            successfully_created = new GlobalResponse("Successfully created but unable to save into DB", HttpStatus.INTERNAL_SERVER_ERROR.value(), true, imageDetails);
        }
        log.info("Image write success");
        return successfully_created;
    }


    @Override
    public List<Object> getAllImageByProduct(Product product) {
        List<Image> images = imageRepository.findImageByProduct(product);
        List<Object> base64List = new ArrayList<>();
        for (Image image : images) {
            GlobalResponse res = getImage(image.getImageDetails().getId());
            base64List.add(res.getBody());
        }

        return base64List;
    }
}
