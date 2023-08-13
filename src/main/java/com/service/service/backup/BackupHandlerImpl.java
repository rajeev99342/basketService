package com.service.service.backup;

import com.service.model.GlobalResponse;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;

@Component
public class BackupHandlerImpl implements BackupHandler{
    @Override
    public GlobalResponse backupImageFiles(String sourceFolderPath, String outputZipFilePath) {
       return GlobalResponse.getSuccess(zipFolder(sourceFolderPath,outputZipFilePath));
    }

    public File zipFolder(String sourceFolderPath, String outputZipFilePath) {
        File zipFile = new File(outputZipFilePath);
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            File sourceFolder = new File(sourceFolderPath);
            zipFile(sourceFolder, sourceFolder.getName(), zipOut);
            return zipFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }

        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }

            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }
}
