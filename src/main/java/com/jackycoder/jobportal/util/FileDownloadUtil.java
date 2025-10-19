package com.jackycoder.jobportal.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloadUtil {
    private Path foundfile; //因為是field/成員變數,所以有預設值:null(區域變數不會有)

    public Resource getFileAsResource(String downloadDir, String fileName) throws IOException {
        Path path = Paths.get(downloadDir);

        Files.list(path).forEach(file -> {
            if (file.getFileName().toString().startsWith(fileName)) {
                foundfile = file;
            }
        });

        if (foundfile != null) {
            //give a reference to the location of that file
            return new UrlResource(foundfile.toUri());
        }

        return null;
    }
}
