package com.service.service.spark;


import com.service.constants.enums.ImgType;
import com.service.entities.Category;
import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.model.QuantityModel;
import com.service.repos.CategoryRepo;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import com.service.repos.ProductRepo;
import com.service.service.CategoryService;
import com.service.service.ProductService;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.ImageUtility;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CSVReaderService {
    @Value("${melaa.storagePathNew}")
    private String storageFolder;


    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepo productRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    ImageDetailsRepository imageDetailsRepository;
    @Autowired
    ImageServiceImpl imageService;


    @Autowired
    CategoryService categoryService;

    @Autowired
    ImageUtility imageUtility;
//    @Autowired
//    private JavaSparkContext sc;

    public static String TYPE = "text/csv";
    static String[] HEADERs = {"Id", "Title", "Description", "Published"};


    public GlobalResponse saveCategoryFromCSV(InputStream is) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord : csvRecords) {
                String categoryName = csvRecord.get("name");
                String imageUrl = csvRecord.get("image");
                CategoryModel category = new CategoryModel();
                category.setCategoryName(categoryName);
                category.setCategoryType(ImgType.CATEGORY.name());
                category.setIsValid(true);
                ImageDetails imageDetails = downloadAndSaveImage(imageUrl, storageFolder, categoryName, ImgType.CATEGORY.name());
                saveCategory(category, imageDetails);
            }
            return GlobalResponse.getSuccess(null);
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }


    private GlobalResponse saveCategory(CategoryModel categoryModel, ImageDetails imageDetails) {
        String imageReference = imageUtility.getImageName("category", categoryModel.getCategoryName());
        return categoryService.addCategory(categoryModel, imageDetails);
    }

    public ImageDetails downloadAndSaveImage(String imageUrl, String destinationPath, String fileName, String imageType) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            long timestamp = System.currentTimeMillis();
            fileName = fileName + "-" + timestamp;
            Path outputPath = Path.of(destinationPath, fileName);
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            String fullPath = destinationPath + fileName;
            ImageDetails imageDetails = new ImageDetails();
            imageDetails.setImageName(fileName);
            imageDetails.setType(imageType);
            imageDetails.setPath(fullPath);
            return imageDetailsRepository.save(imageDetails);
        }
    }

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    @Transactional
    public GlobalResponse saveProducts(InputStream is) throws Exception {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (CSVRecord csvRecord : csvRecords) {

                getProductModelFromCsvRecord(csvRecord);
//                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                    try {
//                        getProductModelFromCsvRecord(csvRecord);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }, executorService);
//                futures.add(future);

            }
//            System.out.println("========================>>> loop completed");
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//            System.out.println("========================>>>  completed");
//            executorService.shutdown();

            return GlobalResponse.getSuccess(null);
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    @Async
    private void getProductModelFromCsvRecord(CSVRecord csvRecord) throws Exception {
        String name = csvRecord.get("name");
        String desc = csvRecord.get("desc");
        String brand = csvRecord.get("brand");

        String img1 = csvRecord.get("img_1");
        String img2 = csvRecord.get("img_2");
        String img3 = csvRecord.get("img_3");
        List<String> imageUrls = new ArrayList<>();
        if (!img1.isBlank()) {
            imageUrls.add(img1);
        }
        if (!img2.isBlank()) {
            imageUrls.add(img2);
        }
        if (!img3.isBlank()) {
            imageUrls.add(img3);
        }
        String catId = csvRecord.get("cat_id");
        Category category = categoryRepo.getById(Long.valueOf(catId));
        Product product = new Product();
        product.setName(name);
        product.setDescription(desc);
        product.setIsValid(true);
        product.setCategory(category);
        product.setUnit(csvRecord.get("unit_1"));
        product.setUpdatedAt(new Date());
        product.setCreatedAt(new Date());
        product.setProdBrand(brand);
        productRepo.save(product);
        List<QuantityModel> qunatityList = getQuantityModelList(csvRecord);
        productService.saveQuantityList(qunatityList, product);
        saveImage(imageUrls, product);
        System.out.println("product saved");
    }

    private List<QuantityModel> getQuantityModelList(CSVRecord csvRecord) {
        String packetQuantity = csvRecord.get("quantity_packet");
        String packetUnit = csvRecord.get("unit_packet");
        List<QuantityModel> quantityModelList = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            QuantityModel quantityModel = new QuantityModel();
            String unitKey = "unit_" + i;
            String quantKey = "quantity_" + i;
            String priceKey = "price_" + i;
            String stockKey = "stock_" + i;
            if (!packetQuantity.isBlank()) {
                quantityModel.setQuantityInPacket(Double.valueOf(packetQuantity));
                quantityModel.setQuantityInPacketUnit(packetUnit);
            }
            if (!csvRecord.get(quantKey).isBlank()) {
                quantityModel.setQuantity(Double.valueOf(csvRecord.get(quantKey)));
                quantityModel.setPrice(Double.valueOf(csvRecord.get(priceKey)));
                quantityModel.setUnit(csvRecord.get(unitKey));
                quantityModel.setInStock(Double.valueOf(csvRecord.get(stockKey)));
                quantityModelList.add(quantityModel);
            }


        }
        return quantityModelList;
    }

    private void saveImage(List<String> imageUrls, Product product) throws IOException {
        for (String url : imageUrls) {
            ImageDetails imageDetails = downloadAndSaveImage(url, storageFolder, product.getName(), ImgType.PRODUCT.name());
            Image image = new Image();
            image.setImageDetails(imageDetails);
            image.setImgType(ImgType.PRODUCT);
            image.setProduct(product);
            imageRepository.save(image);
        }
    }

}