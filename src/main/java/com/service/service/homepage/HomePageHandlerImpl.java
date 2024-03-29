package com.service.service.homepage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.config.StaticMapConfig;
import com.service.entities.Category;
import com.service.entities.Image;
import com.service.entities.Product;
import com.service.entities.TopCategory;
import com.service.model.*;
import com.service.repos.CategoryRepo;
import com.service.repos.ImageRepository;
import com.service.repos.ProductRepo;
import com.service.repos.TopCategoryRepo;
import com.service.service.CategoryService;
import com.service.service.ProductService;
import com.service.service.image.ImageServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.service.config.StaticMapConfig.DISPLAY_CATEGORY;


@Service
public class HomePageHandlerImpl implements HomePageHandler {
    private final ProductRepo productRepo;

    private final TopCategoryRepo topCategoryRepo;

    private final ProductService productService;

    private final CategoryRepo categoryRepo;

    private final CategoryService categoryService;

    private final ImageRepository imageRepository;
    private final ImageServiceImpl imageService;

    public HomePageHandlerImpl(ProductRepo productRepo, TopCategoryRepo topCategoryRepo, ProductService productService, CategoryRepo categoryRepo, CategoryService categoryService, ImageRepository imageRepository, ImageServiceImpl imageService) {
        this.productRepo = productRepo;
        this.topCategoryRepo = topCategoryRepo;
        this.productService = productService;
        this.categoryRepo = categoryRepo;
        this.categoryService = categoryService;
        this.imageRepository = imageRepository;
        this.imageService = imageService;
    }

    @Override
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public void putHomePageData() {
        List<TopCategory> topCategories = topCategoryRepo.findAll();
        List<Category> categories = categoryRepo.findAll();
        Map<Long, List<DisplayProductModel>> map = new HashMap<>();
        categories.forEach(cat -> {
            CategoryDisplayModel categoryDisplayModel = new CategoryDisplayModel();
            Image image = imageRepository.findImageByCategoryId(cat.getId());
            categoryDisplayModel.setCategoryName(cat.getCatName());
            categoryDisplayModel.setCategoryType(cat.getCatType());
            GlobalResponse response = imageService.getImage(image.getImageDetails().getId());
            categoryDisplayModel.setBase64(response.getBody());
            categoryDisplayModel.setId(cat.getId());
            DISPLAY_CATEGORY.add(categoryDisplayModel);
            Pageable pageable =
                    PageRequest.of(0, 10);
            List<Product> products = productRepo.findProductByCategory_IdAndIsValid(cat.getId(), true, pageable);
            List<DisplayProductModel> displayProductModels = productService.convertProductIntoDisplayProductV2(products);
            if (displayProductModels.size() > 0) {
                displayProductModels.add(null);
                StaticMapConfig.HOME_PAGE_CATEGORY_PRODUCTS.put(cat.getCatName(), displayProductModels);
            }
        });
    }

    @Override
    public Map<String, List<DisplayProductModel>> getHomePageData(int pageIndex, int pageSize) {
        List<HomePageData> homePageDataList = new ArrayList<>();
        Map<String, List<DisplayProductModel>> productMap = new HashMap<>();
        Pageable pageable =
                PageRequest.of(pageIndex, pageSize, Sort.by("catName").ascending());

        Pageable pageableForProduct =
                PageRequest.of(pageIndex, 10);

        List<Category> categories = categoryRepo.findAll(pageable).getContent();
        for (Category category : categories) {
            HomePageData homePageData = new HomePageData();
            List<Product> products = productRepo.findProductByCategory_IdAndIsValid(category.getId(), true, pageableForProduct);
            List<DisplayProductModel> displayProducts = productService.convertProductIntoDisplayProduct(products);
            homePageData.setCategory(category);
            homePageData.setDisplayProductModelList(displayProducts);
            homePageDataList.add(homePageData);
            productMap.put(category.getCatName(), displayProducts);
        }
        return productMap;
    }


    @Override
    public List<HomePageData> getHomePageDataV2(int pageIndex, int pageSize) {
        List<HomePageData> homePageDataList = new ArrayList<>();
        Pageable pageable =
                PageRequest.of(pageIndex, pageSize, Sort.by("updatedAt").ascending());

        Pageable pageableForProduct =
                PageRequest.of(pageIndex, 10);

        List<Category> categories = categoryRepo.findAll(pageable).getContent();
        for (Category category : categories) {
            HomePageData homePageData = new HomePageData();
            List<Product> products = productRepo.findProductByCategory_IdAndIsValid(category.getId(), true, pageableForProduct);
            List<DisplayProductModel> displayProducts = productService.convertProductIntoDisplayProduct(products);
            displayProducts.add(null);
            homePageData.setCategory(category);
            homePageData.setDisplayProductModelList(displayProducts);
            homePageDataList.add(homePageData);
        }
        return homePageDataList;
    }

    @Override
    public HomePageModel getHomeDate() {
        HomePageModel homePageData = new HomePageModel(StaticMapConfig.HOME_PAGE_CATEGORY_PRODUCTS, DISPLAY_CATEGORY);
        return homePageData;
    }
}
