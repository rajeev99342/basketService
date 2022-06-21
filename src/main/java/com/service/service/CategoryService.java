package com.service.service;

import com.service.constants.ImgType;
import com.service.entities.Category;
import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.model.CategoryDisplayModel;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.repos.CategoryRepo;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    ImageService imageService;
    @Autowired
    ImageDetailsRepository imageDetailsRepository;
    @Autowired
    ImageRepository imageRepository;


    public GlobalResponse addCategory(CategoryModel model, ImageDetails imageDetails){
        Category category = new Category();
        category.setCatName(model.getCategoryName());
        category.setCatType(model.getCategoryType());
        GlobalResponse response = new GlobalResponse();
        try{
           category = categoryRepo.save(category);
            Image image = new Image();
            image.setImgType(ImgType.CATEGORY);
            image.setCategory(category);
            image.setImageDetails(imageDetails);
            imageRepository.save(image);
            response.setMessage("category saved successfully");
            response.setHttpStatusCode(HttpStatus.OK.value());
            response.setBody(category);
        }catch (Exception e){
            e.printStackTrace();
            response.setMessage("failed to save category");
            response.setBody(null);
        }

        return  response;
    }

    public List<CategoryModel> fetchAllCategoryName(){
        List<Category> categories = categoryRepo.findAll();
        List<CategoryModel> list = new ArrayList<>();
        for(Category category : categories){
            CategoryModel categoryModel = new CategoryModel();
            categoryModel.setCategoryName(category.getCatName());
            categoryModel.setCategoryType(category.getCatType());
            categoryModel.setId(category.getId());
            list.add(categoryModel);
        }
        return list;
    }

    public List<CategoryDisplayModel> getAllCategory(){
        List<Category> categories = categoryRepo.findAll();
        List<CategoryDisplayModel> categoryDisplayModelList = new ArrayList();
        for(Category category : categories){
            CategoryDisplayModel categoryDisplayModel = new CategoryDisplayModel();
            Image image = imageRepository.findImageByCategoryId(category.getId());
            categoryDisplayModel.setCategoryName(category.getCatName());
            categoryDisplayModel.setCategoryType(category.getCatType());
            GlobalResponse response =  imageService.getImage(image.getImageDetails().getId());
            categoryDisplayModel.setBase64(response.getBody());
            categoryDisplayModelList.add(categoryDisplayModel);
        }

        return categoryDisplayModelList;
    }
}
