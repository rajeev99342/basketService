package com.service.controller;

import com.service.model.GlobalResponse;
import com.service.service.UserService;
import com.service.service.spark.CSVReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@RestController
@RequestMapping("/v1/master")
@CrossOrigin(origins = "*")
public class MasterController {

    @Autowired
    CSVReaderService csvReaderService;
    @Autowired
    UserService userService;

    @CrossOrigin(origins = "*")
    @GetMapping("/getUserCompleteDetails")
    public GlobalResponse getCompleteOrder(
            @RequestParam("role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.userCompleteDetails(role,page, size);
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/bulkSaveCategoryFromCSV")
    @Transactional
    public GlobalResponse readCSV(@RequestParam("file") MultipartFile file) throws IOException {
      return csvReaderService.saveCategoryFromCSV(file.getInputStream());
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/bulkSaveProductFromCSV")
    @Transactional
    public GlobalResponse readProductCSVFile(@RequestParam("file") MultipartFile file) throws Exception {
        return csvReaderService.saveProducts(file.getInputStream());
    }

}
