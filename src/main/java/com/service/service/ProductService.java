package com.service.service;

import com.github.javafaker.Faker;
import com.service.constants.enums.ImgType;
import com.service.constants.enums.Unit;
import com.service.constants.enums.UserRole;
import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.ImageUtility;
import com.service.utilites.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.imageio.ImageIO;
@Service
@Slf4j
@CacheConfig(cacheNames = "productCache")
public class ProductService {

    @Value(value = "${melaa.product.image-path}")
     String imagePath;

    @Value(value = "${melaa.category.id}")
     Long categoryID;
    @Autowired
    Utility utility;
    @Autowired
    QuantityRepo quantityRepo;
    @Autowired
    ImageUtility imageUtility;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ImageServiceImpl imageService;

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
            if (user.getRoles().contains(UserRole.MASTER)) {
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
                product = productRepo.findById(model.getId()).get();
            }
            if (null == product) {
                product = new Product();
                product.setCreatedAt(new Date(System.currentTimeMillis()));
            } else {
                // delete previous Image
                imageUtility.deleteProductImages(product);
            }
            product.setUpdatedAt(new Date(System.currentTimeMillis()));
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
            product.setSellerId(model.getSellerId());
            product = productRepo.save(product);
            saveImage(imageList,product);
//            Stock stock = instockRepo.findStockByProduct(product);
//            if (null == stock) {
//                stock = new Stock();
//            }
//            stock.setProduct(product);
//            stock.setInStock(model.getInStock());
//            instockRepo.save(stock);
            saveQuantityList(model.getQuantityModelList(), product);
            response = new GlobalResponse("success", HttpStatus.OK.value(), true, product);
        } catch (Exception e) {
            e.printStackTrace();
            response = new GlobalResponse("Failed", HttpStatus.BAD_REQUEST.value(), false, product);
        }
        return response;

    }

    public void saveQuantityList(List<QuantityModel> quantityModelList, Product product) throws Exception {
        if (quantityModelList != null && quantityModelList.size() > 0) {
            List<Quantity> quantities = quantityModelList.stream().map(q -> new Quantity(q.getId(), product, q.getUnit(), q.getQuantity(), q.getPrice(),q.getInStock(),q.getQuantityInPacket(),q.getQuantityInPacketUnit())).collect(Collectors.toList());
            quantityRepo.saveAll(quantities);
            System.out.println("Quantity Saved !!!");
        } else {
            throw new Exception("Quantity not found");
        }
    }

//    @Cacheable(value = "product",key = "#page+\"-\"+#size")
public List<DisplayProductModel> fetchFromDB(int page, int size){
        List<DisplayProductModel> productModels = new ArrayList<>();
        Pageable paging = PageRequest.of(page, size,Sort.by("updatedAt").descending());
        List<Product> products = productRepo.findProductByIsValid(true,paging);
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
    public GlobalResponse fetchAllProducts(Integer page,Integer size) {
        try{
            return GlobalResponse.getSuccess(fetchFromDB(page,size));
        }catch (Exception e){
            log.error("----------->> Failed to fetch product list due to {} ",e.getMessage());
        }
        return null;

    }

    public GlobalResponse getProductDetails(Long id) {
        try{
            Product product = productRepo.findById(id).get();
            DisplayProductModel displayProductModel = new DisplayProductModel();
            displayProductModel.setModel(getProductModelByProduct(product));
            displayProductModel.setImages(imageService.getAllImageByProduct(product));
            displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
            displayProductModel.setId(product.getId());
            return GlobalResponse.getSuccess(displayProductModel);
        }catch (Exception e){
            log.error("----------->> Failed to fetch product due to {}",e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }

    }

    public List<QuantityModel> getQuantityModelFromEntity(List<Quantity> quantities) {
        return quantities.stream().map(q -> new QuantityModel(q.getId(), q.getUnit(), q.getPrice(), q.getQuantity(), false,q.getInStock(),q.getQuantityInPacket(),q.getQuantityInPacketUnit())).collect(Collectors.toList());
    }

    public ProductModel getProductModelByProduct(Product product) {
        ProductModel productModel = new ProductModel();
        productModel.setCategory(getCategoryModel(product.getCategory()));
        productModel.setDiscount(product.getDiscount());
        productModel.setBrand(product.getProdBrand());
        productModel.setUnit(product.getUnit());
        productModel.setSellingPrice(product.getSellingPrice());
        productModel.setDesc(product.getDescription());
        productModel.setId(product.getId());
        Stock stock = instockRepo.findStockByProduct(product);
        if (null != stock) {
            productModel.setInStock(stock.getInStock());
        }
        productModel.setName(product.getName());
        productModel.setQuantity(product.getQuantity());
        return productModel;
    }

    public ProductModel getProductModelByProductV2(Product product) {
        ProductModel productModel = new ProductModel();
        productModel.setDiscount(product.getDiscount());
        productModel.setBrand(product.getProdBrand());
        productModel.setUnit(product.getUnit());
        productModel.setSellingPrice(product.getSellingPrice());
        productModel.setDesc(product.getDescription());
        productModel.setId(product.getId());
        productModel.setName(product.getName());
        productModel.setQuantity(product.getQuantity());
        return productModel;
    }


    private CategoryModel getCategoryModel(Category category) {
        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setCategoryType(category.getCatType());
        categoryModel.setCategoryName(category.getCatName());
        categoryModel.setId(category.getId());
        categoryModel.setIsValid(category.getIsValid());
        return categoryModel;
    }

    public GlobalResponse getProductsByCatId(Long catId,Integer page,Integer size) {
        try{
            Pageable pageable =
                    PageRequest.of(page, size);
            Category category = categoryRepo.findByIdAndIsValid(catId, true);
            List<Product> products = productRepo.findProductByCategoryAndIsValid(category, true,pageable);
            List<DisplayProductModel> list =  convertProductIntoDisplayProduct(products);
            return GlobalResponse.getSuccess(list);
        }catch (Exception e){
            log.error("----------->> Failed to fetch products by category due to {} ",e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }

    }

    public List<DisplayProductModel> convertProductIntoDisplayProduct(List<Product> products) {
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

    @Transactional
    public List<DisplayProductModel> convertProductIntoDisplayProductV2(List<Product> products) {
        List<DisplayProductModel> productModels = new ArrayList<>();
        for (Product product : products) {
            DisplayProductModel displayProductModel = new DisplayProductModel();
            displayProductModel.setModel(getProductModelByProductV2(product));
            displayProductModel.setImages(imageService.getAllImageByProduct(product));
            displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
            displayProductModel.setId(product.getId());
            productModels.add(displayProductModel);

        }
        return productModels;
    }

    public GlobalResponse searchProduct(String searchTerm, int page, int size) {

        try{
            Pageable sortedByPriceDescNameAsc =
                    PageRequest.of(page, size, Sort.by("name").descending());
            Pageable paging = PageRequest.of(page, size);
            List<Product> products =  productRepo.findByNameContains(searchTerm,sortedByPriceDescNameAsc);
//        List<Product> products =  pageableProducts.getContent();
            List<DisplayProductModel> productModels = new ArrayList<>();
            for (Product product : products) {
                DisplayProductModel displayProductModel = new DisplayProductModel();
                displayProductModel.setModel(getProductModelByProduct(product));
                displayProductModel.setImages(imageService.getAllImageByProduct(product));
                displayProductModel.setQuantityModelList(getQuantityModelFromEntity(quantityRepo.findAllByProduct(product)));
                displayProductModel.setId(product.getId());
                productModels.add(displayProductModel);

            }
            return GlobalResponse.getSuccess(productModels);
        }catch (Exception e){
            log.error("----------->> Failed to search product due to : {}",e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }

    }

    public GlobalResponse getCount() {
        Long count = productRepo.count();
        return GlobalResponse.getSuccess(count);
    }

    public int addRandom(Long categoryID,List<MultipartFile> files) throws Exception {

        Category category = categoryRepo.findById(categoryID).get();
        log.info(">>>>>>>>>>>>>> ===========================================================");
        log.info(">>>>>>>>>>>>>> ============>> {} =>"+category.getCatName());
        for(MultipartFile file : files){
//            int pickCat = new Random().nextInt(categoryList.size());
            int pick = new Random().nextInt(Unit.values().length);
            String unit = String.valueOf(Unit.values()[pick]);
            Faker faker = new Faker();
            String name =  faker.food().ingredient();
            Product product = new Product();
            product.setName(name);
            product.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.");
            product.setUnit(unit);
            product.setIsValid(true);
            product.setProdBrand("LOCAL BRAND");
            product.setCategory(category);
            log.info(">>>>>>>>>>>>>> JUST BEFORE SAVE ============>> {} =>"+category.getCatName());

            productRepo.save(product);
            List<MultipartFile> multipartFiles = new ArrayList<>();
            multipartFiles.add(file);
            saveImage(multipartFiles,product);
            List<QuantityModel> quantityModelList = new ArrayList<>();
            QuantityModel quantityModel = new QuantityModel();
            quantityModel.setQuantity(2.00);
            quantityModel.setInStock(10.00);
            quantityModel.setPrice(234.00);
            quantityModelList.add(quantityModel);
            saveQuantityList(quantityModelList,product);
            System.out.println("Product saved !!!");

        }
        return 0;
    }

    private MultipartFile pickAnyOne(List<MultipartFile> files) {
        int pick = new Random().nextInt(files.size());
        return files.get(pick);
    }

    public static List<MultipartFile> displayImage(String imagePath){
        String PATH_TO_YOUR_DIRECTORY = imagePath;
         final File dir = new File(PATH_TO_YOUR_DIRECTORY);
         List<MultipartFile> multipartFiles = new ArrayList<>();
         final String[] EXTENSIONS = new String[]{
                "gif", "png", "bmp","jpg","jpeg" // and other formats you need
        };
         final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        return (true);
                    }
                }
                return (false);
            }
        };

        if (dir.isDirectory()) {
            for (final File f : dir.listFiles(IMAGE_FILTER)) {
                BufferedImage img = null;
                try {
//                    img = ImageIO.read(f);
//                    bs64Images.add(encodeToString(img,"png"));
                    multipartFiles.add(convertIntoMultipartFile(f));
                } catch (final IOException e) {
                }
            }
        }

        return multipartFiles;
    }


    public static MultipartFile convertIntoMultipartFile(File file) throws IOException {
        String contentType = "text/plain";
       byte[] content = Files.readAllBytes(Paths.get(file.getPath()));
        MultipartFile multipartFile = new MockMultipartFile(file.getName(),file.getName(), contentType, content);
        return multipartFile;
    }
    public static String encodeToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            Base64.Encoder encoder = Base64.getEncoder();
            imageString = encoder.encodeToString(imageBytes);

            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    private void saveImage(List<MultipartFile> imageList,Product product) throws IOException {
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
    }
    @Cacheable("test2")
    public GlobalResponse fetchAll2(){
        doLongRunningTask();
        return GlobalResponse.getFailure("Failed");

    }


    private void doLongRunningTask() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public GlobalResponse saveTestData(Long id, List<MultipartFile> files) throws Exception {
       return GlobalResponse.getSuccess(addRandom( id,files));
    }
}
