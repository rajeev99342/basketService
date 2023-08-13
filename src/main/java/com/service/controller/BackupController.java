package com.service.controller;

import com.google.firebase.database.annotations.NotNull;
import com.service.model.GlobalResponse;
import com.service.service.backup.BackupHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    BackupHandler backupHandler;
    @GetMapping("/zip")
    GlobalResponse doZip(@NotNull @RequestParam("sourceFile") String sourceFile,@NotNull @RequestParam("targetFile") String targetFile) {
        try {
           return backupHandler.backupImageFiles(sourceFile,targetFile);
        } catch (Exception e) {

            return new GlobalResponse("Failed " + e, 500);
        }
    }

}
