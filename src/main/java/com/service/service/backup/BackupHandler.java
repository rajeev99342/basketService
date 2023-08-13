package com.service.service.backup;

import com.service.model.GlobalResponse;

public interface BackupHandler {
    public GlobalResponse backupImageFiles(String sourceFolderPath, String outputZipFilePath);
}
