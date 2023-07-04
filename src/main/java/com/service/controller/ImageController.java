package com.service.controller;
import com.service.model.GlobalResponse;
import com.service.service.image.ImageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
public class ImageController {


    @Autowired
    private ImageServiceImpl imageService;


    @PostMapping("/upload-image")
    public GlobalResponse uploadImage(@RequestParam("file") MultipartFile photo) throws IOException {
        try {
           return this.imageService.saveImage(photo,"simple");
        }catch (Exception e){
            e.printStackTrace();
        }
    return null;
    }

    @GetMapping("/get-uploaded-image")
    public GlobalResponse getImage(@RequestParam(name = "id") Long id) {
        try {
            return imageService.getImage(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
