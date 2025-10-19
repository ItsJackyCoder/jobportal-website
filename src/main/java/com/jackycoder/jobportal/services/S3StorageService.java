package com.jackycoder.jobportal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;

@Service
public class S3StorageService {
    private final S3Client s3; //由Spring Cloud AWS建好並注入(自動用EB的IAM Role)

    private final String bucket;

    private final S3Presigner s3Presigner;


    public S3StorageService(S3Client s3,
                            @Value("${spring.cloud.aws.s3.bucket}") String bucket,
                            @Value("${spring.cloud.aws.region.static}") String regionProp) {
        this.s3 = s3;
        this.bucket = bucket;
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(regionProp))
                .build();
    }

    /* 上傳原檔(不落地本機) */
    public void saveFileToS3(String uploadDir, String filename,
                             MultipartFile multipartFile) throws IOException {
        String key = buildS3Key(uploadDir, filename);

        String contentType = (multipartFile.getContentType() != null)
                ? multipartFile.getContentType()
                : detectContentType(filename);

        PutObjectRequest.Builder req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType);

        try (InputStream in = multipartFile.getInputStream()) {
            s3.putObject(req.build(), RequestBody.fromInputStream(in, multipartFile.getSize()));
        }
    }

    /* 轉為WebP後上傳(不落地本機 */
    public void saveImageAsWebpToS3(String uploadDir, String webpFileName, MultipartFile multipartImage) throws IOException {
        // 讀入影像
        BufferedImage buffered;

        try (InputStream in = multipartImage.getInputStream()) {
            buffered = ImageIO.read(in);
        }

        if (buffered == null) {
            throw new IOException("Uploaded file is not a supported image.");
        }

        // 取得 WebP writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");

        if (!writers.hasNext()) {
            throw new IOException("No WebP ImageWriter found. Is webp-imageio on the classpath?");
        }

        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            String[] types = param.getCompressionTypes();

            if (types != null && types.length > 0) {
                param.setCompressionType(types[0]); // 通常 "Lossy"
            }

            param.setCompressionQuality(0.7f);
        }

        //轉到記憶體
        byte[] bytes;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(buffered, null, null), param);
            ios.flush();

            bytes = baos.toByteArray();
        } finally {
            writer.dispose();
        }

        //上傳S3
        String key = buildS3Key(uploadDir, webpFileName);
        PutObjectRequest.Builder req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/webp");

        s3.putObject(req.build(), RequestBody.fromBytes(bytes));
    }

    public void deleteFromS3(String uploadDir, String filename) {
        if (filename == null || filename.isBlank()) return;

        String key = buildS3Key(uploadDir, filename);

        try {
            s3.deleteObject(builder -> builder.bucket(bucket).key(key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 產生S3預簽名下載URL(有效期預設10分鐘）*/
    public String generatePresignedDownloadUrl(String uploadDir, String filename, String downloadFileName, int validMinutes) {
        String key = buildS3Key(uploadDir, filename);

        //String displayName = "resume.pdf";

        //將檔名做UTF-8編碼(避免Header不支援Unicode）
        String encodedName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20"); //讓空白轉成%20而非+

        //使用RFC 5987格式的Content-Disposition標頭
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)

                //可選:指定回應的檔名(讓瀏覽器用原檔名下載)
                .responseContentDisposition("attachment; filename*=UTF-8''" + encodedName)

                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(validMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();

        return presignedUrl.toString();
    }


    //共用小工具:
    private String buildS3Key(String uploadDir, String filename) {
        String dir = (uploadDir == null) ? "" : uploadDir.replace("\\", "/");

        if (dir.startsWith("/")) dir = dir.substring(1);
        if (!dir.isEmpty() && !dir.endsWith("/")) dir += "/";

        return dir + filename;
    }

    private String detectContentType(String filename) {
        String lower = filename.toLowerCase();

        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".doc"))  return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".txt"))  return "text/plain";

        return "application/octet-stream";
    }
}
