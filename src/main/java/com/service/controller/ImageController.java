package com.service.controller;
import com.service.model.GlobalResponse;
import com.service.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:8100")
@RestController
public class ImageController {


    @Autowired
    private ImageService imageService;


    @PostMapping("/upload-image")
    public GlobalResponse uploadImage(@RequestParam("file") MultipartFile photo) throws IOException {
        try {
           return this.imageService.saveImage(photo);
        }catch (Exception e){
            e.printStackTrace();
        }
    return null;
    }

    @GetMapping("/get-uploaded-image")
    public GlobalResponse getImage(@RequestParam("id") Long id) throws IOException {
        try {
            return this.imageService.getImage(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
