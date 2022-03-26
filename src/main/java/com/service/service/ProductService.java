package com.service.service;

import com.service.constants.ImgType;
import com.service.entities.Category;
import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.GlobalResponse;
import com.service.model.ProductModel;
import com.service.repos.ImageRepository;
import com.service.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepo productRepo;
    @Autowired
    ImageService imageService;

    @Autowired
    ImageRepository imageRepository;

    public GlobalResponse addProduct(ProductModel model, List<MultipartFile> imageList){
        Product product = null;
        GlobalResponse response = null;
        try{

            product = new Product();
            product.setProdPrice(model.getPrice());
            product.setProdBrand(model.getBrand());
            product.setProdDesc(model.getDesc());
            product.setProdName(model.getName());
            product.setProdWeight(model.getWeight());
            product.setSellingPrice(model.getSellingPrice());
            product.setDiscount(model.getDiscount());
            Category category = new Category();
            category.setCatType(model.getCategory().getCategoryType());
            category.setCatName(model.getCategory().getCategoryName());
            category.setId(model.getCategory().getId());
            product.setCategory(category);
            product = productRepo.save(product);
            for (MultipartFile file : imageList)
            {
                GlobalResponse imageResponse = imageService.saveImage(file);
                ImageDetails imageDetails = (ImageDetails) imageResponse.getBody();
                Image image = new Image();
                image.setImageDetails(imageDetails);
                image.setImgType(ImgType.PRODUCT);
                image.setProduct(product);
                imageRepository.save(image);
            }
            response = new GlobalResponse("success", HttpStatus.OK.value(),true,product);
        }catch (Exception e){
            e.printStackTrace();
            response = new GlobalResponse("Failed", HttpStatus.BAD_REQUEST.value(),false,product);
        }
        return response;

    }

}
