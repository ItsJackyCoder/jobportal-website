package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.repository.UsersRepository;
import com.jackycoder.jobportal.services.JobSeekerProfileService;
import com.jackycoder.jobportal.services.RecruiterProfileService;
import com.jackycoder.jobportal.services.S3StorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("recruiter-profile")
public class RecruiterProfileController {
    private final UsersRepository usersRepository;

    private final RecruiterProfileService recruiterProfileService;

    private final S3StorageService s3StorageService;

    private final JobSeekerProfileService jobSeekerProfileService;

    public RecruiterProfileController(UsersRepository usersRepository,
                                      RecruiterProfileService recruiterProfileService,
                                      S3StorageService s3StorageService,
                                      JobSeekerProfileService jobSeekerProfileService) {
        this.usersRepository = usersRepository;
        this.recruiterProfileService = recruiterProfileService;
        this.s3StorageService = s3StorageService;
        this.jobSeekerProfileService = jobSeekerProfileService;
    }

    @GetMapping("/")
    public String recruiterProfile(Model model) {
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();

            Users users = usersRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Could not fine user"));

            Optional<RecruiterProfile> recruiterProfile
                    = recruiterProfileService.getOne(users.getUserId());

            //just make sure that I have some data here
            if (!recruiterProfile.isEmpty()) {
                model.addAttribute("profile", recruiterProfile.get());
            }
        }

        return "recruiter-profile";
    }

    //Spring MVC will actually help us out. It'll create the new "recruiterProfile" in memory
    //based on the form data
//    @PostMapping("/addNew")
//    public String addNew(RecruiterProfile recruiterProfile,
//                         @RequestParam("image") MultipartFile multipartFile, Model model) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if(!(authentication instanceof AnonymousAuthenticationToken)){
//            String currentUsername = authentication.getName();
//
//            Users users = usersRepository.findByEmail(currentUsername)
//                    .orElseThrow(() -> new UsernameNotFoundException("Could not fine user"));
//
//            //Associate recruiter profile with existing user account
//            recruiterProfile.setUserId(users);
//            recruiterProfile.setUserAccountId(users.getUserId());
//        }
//
//        model.addAttribute("profile", recruiterProfile);
//
//        //處理file upload(image)
//        String fileName = "";
//        String webpName = "";
//        RecruiterProfile oldRecruiterProfile
//                = recruiterProfileService.getOne(recruiterProfile.getUserAccountId()).orElseThrow(
//                        () -> new EntityNotFoundException("Recruiter not found."));
//        String oldPhotoName = oldRecruiterProfile.getProfilePhoto();
//
//        if(!Objects.equals(multipartFile.getOriginalFilename(), "")){
//            fileName = StringUtils.cleanPath(
//                    Objects.requireNonNull(multipartFile.getOriginalFilename()));
//
//            int dotPosition = fileName.indexOf('.'); //沒找到會return -1
//            String base = (dotPosition > 0) ? fileName.substring(0, dotPosition) : fileName;
//            webpName = base + ".webp";
//
//            //set image name in recruiter profile
//            recruiterProfile.setProfilePhoto(webpName);
//        }
//
//        //save recruiter profile to db
//        RecruiterProfile savedUser = recruiterProfileService.addNew(recruiterProfile);
//
//        //set up the upload directory of where we want to save the image profile
//        String uploadDir = "photos/recruiter/" + savedUser.getUserAccountId();
//
//        //read profile image from request - multipartfile
//        //save image on the server in directory: photos/recruiter
//        try {
//            if(oldPhotoName != null){
//                Path oldImagePath = Paths.get(uploadDir).resolve(oldPhotoName);
//
//                Files.deleteIfExists(oldImagePath);
//            }
//
//            FileUploadUtil.saveImageAsWebp(uploadDir, webpName, multipartFile);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return "redirect:/dashboard/";
//    }

    @PostMapping("/addNew")
    public String addNew(RecruiterProfile recruiterProfile,
                         @RequestParam("image") MultipartFile multipartFile, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();

            Users users = usersRepository.findByEmail(currentUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Could not fine user"));

            //Associate recruiter profile with existing user account
            recruiterProfile.setUserId(users);
            recruiterProfile.setUserAccountId(users.getUserId());
        }

        model.addAttribute("profile", recruiterProfile);

        //處理file upload(image)
        String fileName = "";
        String webpName = "";
        RecruiterProfile oldRecruiterProfile
                = recruiterProfileService.getOne(recruiterProfile.getUserAccountId()).orElseThrow(
                () -> new EntityNotFoundException("Recruiter not found."));
        String oldPhotoName = oldRecruiterProfile.getProfilePhoto();

        if (!Objects.equals(multipartFile.getOriginalFilename(), "")) {
            fileName = StringUtils.cleanPath(
                    Objects.requireNonNull(multipartFile.getOriginalFilename()));

            int dotPosition = fileName.indexOf('.'); //沒找到會return -1
            String base = (dotPosition > 0) ? fileName.substring(0, dotPosition) : fileName;
            webpName = base + ".webp";

            //set image name in recruiter profile
            recruiterProfile.setProfilePhoto(webpName);
        }

        //save recruiter profile to db
        RecruiterProfile savedUser = recruiterProfileService.addNew(recruiterProfile);

        //set up the upload directory of where we want to save the image profile
        String uploadDir = "photos/recruiter/" + savedUser.getUserAccountId();

        //read profile image from request - multipartfile
        //save image on the server in directory: photos/recruiter
        try {
            if (!Objects.equals(multipartFile.getOriginalFilename(), "")) { //有上傳檔案的情況
                if (oldPhotoName != null) {
                    //刪除舊圖片(S3)
                    s3StorageService.deleteFromS3(uploadDir, oldPhotoName);
                }

                //上傳新圖片(WebP)
                s3StorageService.saveImageAsWebpToS3(uploadDir, webpName, multipartFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "redirect:/dashboard/";
    }

//    @GetMapping("/downloadResume") //@RequestParam裡面的value可以省略!
//    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName") String fileName,
//                                            @RequestParam(value = "userID") String userId) {
//        FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();
//        Resource resource = null;
//
//        try {
//            // photos/candidate/: 相對路徑(如果寫"/photos/就是絕對路徑了!
//            resource = fileDownloadUtil.getFileAsResource(
//                    "photos/recruiter/" + userId, fileName);
//        } catch (IOException io) {
//            return ResponseEntity.badRequest().build(); //400 Bad Request
//        }
//
//        if (resource == null) { //沒找到檔案
//            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
//        }
//
//        Path filePath = Paths.get("photos/recruiter/" + userId).resolve(fileName).normalize();
//
//        String contentType;
//
//        try {
//            contentType = Files.probeContentType(filePath);
//        } catch (IOException e) {
//            contentType = "application/octet-stream"; //fallback
//        }
//
//        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
//
//        System.out.println(">>> downloadResume API called: " + fileName);
//
//        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
//                .body(resource);
//    }

    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam("fileName") String fileName,
                                            @RequestParam("userID") String userId) {
        JobSeekerProfile jobSeekerProfile = jobSeekerProfileService.getOne(Integer.parseInt(userId))
                .orElseThrow(() -> new EntityNotFoundException("JobSeeker not found!"));

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"));
        String downloadFileName = jobSeekerProfile.getFirstName() + "_" + jobSeekerProfile.getLastName() + "_"
                + "resume" + "_" + time;

        //你實際存放的位置(依你的既有結構）
        String dir = "photos/candidate/" + userId;

        //產生一個暫時有效(例如:10分鐘)的下載連結
        String presignedUrl = s3StorageService.generatePresignedDownloadUrl(dir, fileName, downloadFileName, 10);

        //302轉址到S3,由瀏覽器直接向S3下載
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }
}
