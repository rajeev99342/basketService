package com.service.service;

import com.service.constants.enums.ImgType;
import com.service.entities.Category;
import com.service.entities.Image;
import com.service.entities.ImageDetails;
import com.service.model.CategoryDisplayModel;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.repos.CategoryRepo;
import com.service.repos.ImageDetailsRepository;
import com.service.repos.ImageRepository;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.ImageUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CategoryService {
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    ImageServiceImpl imageService;
    @Autowired
    ImageDetailsRepository imageDetailsRepository;
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageUtility imageUtility;

    public GlobalResponse deleteCategory(Long id){
           Category category =  categoryRepo.findByIdAndIsValid(id,true);
           if(category == null){
               return new GlobalResponse("Already deleted or not found",HttpStatus.NOT_FOUND.value(),false,null);
           }
           category.setIsValid(false);
           categoryRepo.save(category);
           return new GlobalResponse("Deleted",HttpStatus.OK.value(),true,null);
    }

    @Transactional
    public GlobalResponse addCategory(CategoryModel model, ImageDetails imageDetails){
        Category category = new Category();
        category.setCatName(model.getCategoryName());
        category.setCatType(model.getCategoryType());
        category.setId(model.getId());
        category.setIsValid(model.getIsValid());
        GlobalResponse response = new GlobalResponse();
        try{
            if(null != model.getId()){
                imageUtility.deleteCategoryImage(model);
            }
           category = categoryRepo.save(category);
            Image image = new Image();
            image.setImgType(ImgType.CATEGORY);
            image.setCategory(category);
            image.setImageDetails(imageDetails);
            imageRepository.save(image);
            response.setMessage("category saved successfully");
            response.setHttpStatusCode(HttpStatus.OK.value());
            response.setBody(null);
        }catch (Exception e){
            e.printStackTrace();
            response.setMessage("failed to save category");
            response.setBody(null);
        }

        return  response;
    }

    public GlobalResponse fetchAllCategoryName(){
        try{
            List<Category> categories = categoryRepo.findAll(Sort.by("catName"));
            List<CategoryModel> list = new ArrayList<>();
            for(Category category : categories){
                CategoryModel categoryModel = new CategoryModel();
                categoryModel.setCategoryName(category.getCatName());
                categoryModel.setCategoryType(category.getCatType());
                categoryModel.setId(category.getId());
                list.add(categoryModel);
            }
            return GlobalResponse.getSuccess(list);
        }catch (Exception e){
            log.error("Failed to fetch category name due to : {}",e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }

    }

    public GlobalResponse getAllCategory(){
        try{
            List<Category> categories = categoryRepo.findCategoryByIsValid(true);


            return GlobalResponse.getSuccess(getCategoryModel(categories));
        }catch (Exception e){
            log.error("Failed to fetch category due to {} ",e.getMessage());
        }

        return null;
    }

    public  List<CategoryDisplayModel>  getCategoryModel(List<Category> categories){
        List<CategoryDisplayModel> categoryDisplayModelList = new ArrayList();
        for(Category category : categories){
            CategoryDisplayModel categoryDisplayModel = new CategoryDisplayModel();
            Image image = imageRepository.findImageByCategoryId(category.getId());
            categoryDisplayModel.setCategoryName(category.getCatName());
            categoryDisplayModel.setCategoryType(category.getCatType());
            GlobalResponse response =  imageService.getImage(image.getImageDetails().getId());
            categoryDisplayModel.setBase64(response.getBody());
            categoryDisplayModel.setId(category.getId());
            categoryDisplayModelList.add(categoryDisplayModel);
        }
        return categoryDisplayModelList;
    }

    public GlobalResponse getCategoryById(Long id) {
        try{
            Category category = categoryRepo.getById(id);
            Image image = imageRepository.findImageByCategoryId(category.getId());
            CategoryDisplayModel displayModel = new CategoryDisplayModel();
            displayModel.setCategoryType(category.getCatType());
            displayModel.setCategoryName(category.getCatName());
            displayModel.setId(category.getId());
            GlobalResponse res = imageService.getImage(image.getId());
            if(res != null){
                displayModel.setBase64(res.getBody());
            }
            return GlobalResponse.getSuccess(displayModel);
        }catch (Exception e){
            log.error("Failed due to : {}",e.getMessage());
            return GlobalResponse.getFailure(e.getMessage());
        }

    }

    public GlobalResponse getAllCategoryByPage(int page, int size) {
        Pageable pageable =
                PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<Category> categories = categoryRepo.findAll(pageable);
        if(categories.getContent() != null){
            return GlobalResponse.getSuccess(getCategoryModel(categories.getContent()));
        }
        return GlobalResponse.getFailure("Failed to fetch data");
    }
}
