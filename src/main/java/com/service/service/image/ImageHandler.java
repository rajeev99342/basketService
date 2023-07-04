package com.service.service.image;

import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.GlobalResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ImageHandler {
    public List<Object> getAllImageByProduct(Product product);

    public GlobalResponse getImage(Long id);

    public ImageDetails saveImageDetails(Path newFile, MultipartFile image, String imageReference);

    public GlobalResponse saveImage(MultipartFile photo, String imageReference) throws IOException;
}
