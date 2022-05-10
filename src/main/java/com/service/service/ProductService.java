package com.service.service;

import com.service.constants.ImgType;
import com.service.entities.*;
import com.service.model.CategoryModel;
import com.service.model.DisplayProductModel;
import com.service.model.GlobalResponse;
import com.service.model.ProductModel;
import com.service.repos.ImageRepository;
import com.service.repos.InstockRepo;
import com.service.repos.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepo productRepo;
    @Autowired
    ImageService imageService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    InstockRepo instockRepo;

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
            Stock stock = new Stock();
            stock.setProduct(product);
            stock.setInStock(model.getInStock());
            instockRepo.save(stock);
            response = new GlobalResponse("success", HttpStatus.OK.value(),true,product);
        }catch (Exception e){
            e.printStackTrace();
            response = new GlobalResponse("Failed", HttpStatus.BAD_REQUEST.value(),false,product);
        }
        return response;

    }


    public List<DisplayProductModel> fetchAllProducts(){
        List<DisplayProductModel> productModels = new ArrayList<>();
        List<Product> products = productRepo.findAll();
        for (Product product : products){
            DisplayProductModel displayProductModel = new DisplayProductModel();
            displayProductModel.setModel(getProductModelByProduct(product));
            displayProductModel.setImages(imageService.getAllImageByProduct(product.getId()));
            displayProductModel.setId(product.getId());
            productModels.add(displayProductModel);

        }
        return productModels;
    }

    public DisplayProductModel getProductDetails(Long id){
        Product product = productRepo.getById(id);
        DisplayProductModel displayProductModel = new DisplayProductModel();
        displayProductModel.setModel(getProductModelByProduct(product));
        displayProductModel.setImages(imageService.getAllImageByProduct(product.getId()));
        displayProductModel.setId(product.getId());
        return displayProductModel;
    }


    private ProductModel getProductModelByProduct(Product product){
        ProductModel productModel = new ProductModel();
        productModel.setCategory(getCategoryModel(product.getCategory()));
        productModel.setDiscount(product.getDiscount());
        productModel.setBrand(product.getProdBrand());
        productModel.setSellingPrice(product.getSellingPrice());
        productModel.setDesc(product.getProdDesc());
        productModel.setPrice(product.getProdPrice());
        productModel.setId(product.getId());
        Stock stock = instockRepo.findStockByProductId(product.getId());
        if(null != stock){
            productModel.setInStock(stock.getInStock());
        }
        productModel.setName(product.getProdName());
        productModel.setWeight(product.getProdWeight());
        return productModel;
    }

    private CategoryModel getCategoryModel(Category category){
        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setCategoryType(category.getCatType());
        categoryModel.setCategoryName(category.getCatName());
        categoryModel.setId(category.getId());
        return categoryModel;
    }

}
