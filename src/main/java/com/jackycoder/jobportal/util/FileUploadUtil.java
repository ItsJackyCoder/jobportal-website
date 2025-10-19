package com.jackycoder.jobportal.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

public class FileUploadUtil {
    public static void saveFile(String uploadDir, String filename,
                                MultipartFile multipartFile) throws IOException {
        //multipartFile has the image file that the user uploaded from the form
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try{
            InputStream inputStream = multipartFile.getInputStream();
            Path path = uploadPath.resolve(filename);

            //copy the content from the inputStream to the path
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

        }catch(IOException ioe){
            throw new IOException("Could not save image file: " + filename, ioe);
        }
    }

    public static void saveImageAsWebp(String uploadDir, String webpFileName,
                                       MultipartFile multipartImage) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        //若目錄不存,則建立
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        //讀入影像
        BufferedImage buffered = null;

        try (InputStream in = multipartImage.getInputStream()) {
            buffered = ImageIO.read(in);
        }

        if (buffered == null) {
            throw new IOException("Uploaded file is not a supported image.");
        }

        Path outPath = uploadPath.resolve(webpFileName);

        //取得WebP writer(由webp-imageio外掛提供）
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");

        if (!writers.hasNext()) {
            throw new IOException("No WebP ImageWriter found. Is webp-imageio on the classpath?");
        }

        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            String[] compressionTypes = param.getCompressionTypes();

            if (compressionTypes != null && compressionTypes.length > 0) {
                //設定為第一種壓縮方式（通常是"Lossy"）
                param.setCompressionType(compressionTypes[0]);
            }

            //0.0 ~ 1.0,數值越低壓縮越小(檔案更小),可自行調整
            param.setCompressionQuality(0.7f);
        }

        try (OutputStream os = Files.newOutputStream(outPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(buffered, null, null), param);
        } finally {
            writer.dispose();
        }
    }
}
