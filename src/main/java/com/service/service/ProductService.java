package com.service.service;

import com.service.constants.enums.ImgType;
import com.service.constants.enums.Role;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import com.service.utilites.ImageUtility;
import com.service.utilites.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    Utility utility;
    @Autowired
    QuantityRepo quantityRepo;
    @Autowired
    ImageUtility imageUtility;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ImageService imageService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    InstockRepo instockRepo;

    @Autowired
    JwtTokenUtility jwtTokenUtility;

    @Autowired
    UserRepo userRepo;

    public GlobalResponse deleteProductQuantity(Long id, String token) {
        try {
            User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(token));
            if (user.getRoles().contains(Role.MASTER)) {
                quantityRepo.deleteById(id);
                return new GlobalResponse("Deleted", HttpStatus.OK.value(), true, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new GlobalResponse("Failed to delete", HttpStatus.INTERNAL_SERVER_ERROR.value(), false, null);

    }

    public GlobalResponse deleteProduct(Long id) {
        Product product = productRepo.findProductByIdAndIsValid(id, true);
        product.setIsValid(false);
        productRepo.save(product);
        return new GlobalResponse("Deleted", HttpStatus.OK.value(), true, null);
    }

    @Transactional
    public GlobalResponse addProduct(ProductModel model, List<MultipartFile> imageList) {
        Product product = null;
        GlobalResponse response = null;
        try {

            if (null != model.getId()) {
                product = productRepo.getById(model.getId());
            }
            if (null == product) {
                product = new Product();
            } else {
                // delete previous Image
                imageUtility.deleteProductImages(product);
            }
            product.setPricePerUnit(model.getPriceForGivenUnit());
            product.setProdBrand(model.getBrand());
            product.setDescription(model.getDesc());
            product.setName(model.getName());
            product.setUnit(model.getUnit());
            product.setQuantity(model.getQuantity());
            product.setSellingPrice(model.getSellingPrice());
            product.setIsValid(model.getIsValid());
            product.setDiscount(model.getDiscount());
            Category category = new Category();
            category.setCatType(model.getCategory().getCategoryType());
            category.setCatName(model.getCategory().getCategoryName());
            category.setId(model.getCategory().getId());
            product.setCategory(category);
            product = productRepo.save(product);
            for (MultipartFile file : imageList) {
                String imageReference = imageUtility.getImageName("product", product.getName());
                GlobalResponse imageResponse = imageService.saveImage(file, imageReference);
                ImageDetails imageDetails = (ImageDetails) imageResponse.getBody();
                Image image = new Image();
                image.setImageDetails(imageDetails);
                image.setImgType(ImgType.PRODUCT);
                image.setProduct(product);
                imageRepository.save(image);
            }
            Stock stock = instockRepo.findStockByProduct(product);
            if (null == stock) {
                stock = new Stock();
            }
            stock.setProduct(product);
            stock.setInStock(model.getInStock());
            instockRepo.save(stock);
            saveQuantityList(model.getQuantityModelList(), product);
            response = new GlobalResponse("success", HttpStatus.OK.value(), true, product);
        } catch (Exception e) {
            e.printStackTrace();
            response = new GlobalResponse("Failed", HttpStatus.BAD_REQUEST.value(), false, product);
        }
        return response;

    }

    private void saveQuantityList(List<QuantityModel> quantityModelList, Product product) throws Exception {
        if (quantityModelList != null && quantityModelList.size() > 0) {
            List<Quantity> quantities = quantityModelList.stream().map(q -> new Quantity(q.getId(), product, q.getUnit(), q.getQuantity(), q.getPrice())).collect(Collectors.toList());
            quantityRepo.saveAll(quantities);
        } else {
            throw new Exception("Quantity not found");
        }
    }


    public List<DisplayProductModel> fetchAllProducts() {
        List<DisplayProductModel> productModels = new ArrayList<>();
        List<Product> products = productRepo.findProductByIsValid(true);
        for (Product product : products) {
            DisplayProductModel displayProductModel = new DisplayProductModel();
            displayProductModel.setModel(getProductModelByProduct(product));
            displayProductModel.setImages(imageService.getAllImageByProduct(product));
            displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
            displayProductModel.setId(product.getId());
            productModels.add(displayProductModel);

        }
        return productModels;
    }

    public DisplayProductModel getProductDetails(Long id) {
        Product product = productRepo.getById(id);
        DisplayProductModel displayProductModel = new DisplayProductModel();
        displayProductModel.setModel(getProductModelByProduct(product));
        displayProductModel.setImages(imageService.getAllImageByProduct(product));
        displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
        displayProductModel.setId(product.getId());
        return displayProductModel;
    }

    public List<QuantityModel> getQuantityModelFromEntity(List<Quantity> quantities) {
        return quantities.stream().map(q -> new QuantityModel(q.getId(), q.getUnit(), q.getPrice(), q.getQuantity(), false)).collect(Collectors.toList());
    }

    public ProductModel getProductModelByProduct(Product product) {
        ProductModel productModel = new ProductModel();
        productModel.setCategory(getCategoryModel(product.getCategory()));
        productModel.setDiscount(product.getDiscount());
        productModel.setBrand(product.getProdBrand());
        productModel.setUnit(product.getUnit());
        productModel.setSellingPrice(product.getSellingPrice());
        productModel.setDesc(product.getDescription());
        productModel.setPriceForGivenUnit(product.getPricePerUnit());
        productModel.setId(product.getId());
        Stock stock = instockRepo.findStockByProduct(product);
        if (null != stock) {
            productModel.setInStock(stock.getInStock());
        }
        productModel.setName(product.getName());
        productModel.setQuantity(product.getQuantity());
        return productModel;
    }

    private CategoryModel getCategoryModel(Category category) {
        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setCategoryType(category.getCatType());
        categoryModel.setCategoryName(category.getCatName());
        categoryModel.setId(category.getId());
        return categoryModel;
    }

    public List<DisplayProductModel> getProductsByCatId(Long catId) {
        Category category = categoryRepo.findByIdAndIsValid(catId, true);
        List<Product> products = productRepo.findProductByCategoryAndIsValid(category, true);
        return convertProductIntoDisplayProduct(products);
    }

    private List<DisplayProductModel> convertProductIntoDisplayProduct(List<Product> products) {
        List<DisplayProductModel> productModels = new ArrayList<>();

        for (Product product : products) {
            DisplayProductModel displayProductModel = new DisplayProductModel();
            displayProductModel.setModel(getProductModelByProduct(product));
            displayProductModel.setImages(imageService.getAllImageByProduct(product));
            displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
            displayProductModel.setId(product.getId());
            productModels.add(displayProductModel);

        }
        return productModels;
    }
}
