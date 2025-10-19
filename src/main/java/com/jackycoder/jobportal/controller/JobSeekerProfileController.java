package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.entity.Languages;
import com.jackycoder.jobportal.entity.Skills;
import com.jackycoder.jobportal.entity.Users;
import com.jackycoder.jobportal.repository.UsersRepository;
import com.jackycoder.jobportal.services.JobSeekerProfileService;
import com.jackycoder.jobportal.services.S3StorageService;
import com.jackycoder.jobportal.util.FileDownloadUtil;
import com.jackycoder.jobportal.util.FileUploadUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {
    private final JobSeekerProfileService jobSeekerProfileService;
    private final UsersRepository usersRepository;

    private final S3StorageService s3StorageService;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService,
                                      UsersRepository usersRepository,
                                      S3StorageService s3StorageService) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
        this.s3StorageService = s3StorageService;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        //create an empty jobSeekerProfile
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        List<Skills> skills = new ArrayList<>();
        List<Languages> languages = new ArrayList<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));

            Optional<JobSeekerProfile> seekerProfile
                    = jobSeekerProfileService.getOne(user.getUserId());

            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();

                if (jobSeekerProfile.getSkills().isEmpty()) {
                    skills.add(new Skills()); //參考IPAD p.354
                    jobSeekerProfile.setSkills(skills);
                }

                if (jobSeekerProfile.getLanguages().isEmpty()) {
                    languages.add(new Languages());
                    jobSeekerProfile.setLanguages(languages);
                }
            }

            model.addAttribute("skills", skills);
            model.addAttribute("languages", languages);
            model.addAttribute("profile", jobSeekerProfile);
        }

        return "job-seeker-profile";
    }

//    @PostMapping("/addNew")
//    public String addNew(JobSeekerProfile jobSeekerProfile, //參考IPAD p.92(可以不用寫@ModelAttribute("XXX"))
//                         @RequestParam("image") MultipartFile image,
//                         @RequestParam("pdf") MultipartFile pdf) {
//        Authentication authentication
//                = SecurityContextHolder.getContext().getAuthentication();
//
//        if (!(authentication instanceof AnonymousAuthenticationToken)) {
//            Users user = usersRepository.findByEmail(authentication.getName())
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
//
//            jobSeekerProfile.setUserId(user);
//            jobSeekerProfile.setUserAccountId(user.getUserId());
//        }
//
//        //associate the skills with the appropriate jobSeekerProfile accordingly
//        for (Skills skills : jobSeekerProfile.getSkills()) {
//            skills.setJobSeekerProfile(jobSeekerProfile);
//        }
//
//        //associate the languages with the appropriate jobSeekerProfile accordingly
//        for (Languages languages : jobSeekerProfile.getLanguages()) {
//            languages.setJobSeekerProfile(jobSeekerProfile);
//        }
//
//        //handle the file upload for the profile image and also for the resume
//        String imageName = "";
//        String resumeName = "";
//        String webpName = "";
//        JobSeekerProfile oldJobSeekerProfile
//                = jobSeekerProfileService.getOne(jobSeekerProfile.getUserAccountId()).orElseThrow(
//                () -> new EntityNotFoundException("JobSeekerProfile not found."));
//        String oldPhotoName = oldJobSeekerProfile.getProfilePhoto();
//        String oldResumeName = oldJobSeekerProfile.getResume();
//
//        if (!Objects.equals(image.getOriginalFilename(), "")) {
//            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
//
//            int dotPosition = imageName.indexOf('.'); //沒找到會return -1
//            String base = (dotPosition > 0) ? imageName.substring(0, dotPosition) : imageName;
//            webpName = base + ".webp";
//
//            jobSeekerProfile.setProfilePhoto(webpName);
//        }
//
//        if (!Objects.equals(pdf.getOriginalFilename(), "")) {
//            resumeName = StringUtils.cleanPath(
//                    Objects.requireNonNull(pdf.getOriginalFilename()));
//
//            jobSeekerProfile.setResume(resumeName);
//        }
//
//        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);
//
//        //save the file to the file system
//        try {
//            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();
//            Path uploadDirPath = Paths.get(uploadDir);
//
//            if (!Objects.equals(image.getOriginalFilename(), "")) {
//                //刪除舊圖片檔案
//                if (oldPhotoName != null) {
//                    Path oldImagePath = uploadDirPath.resolve(oldPhotoName);
//
//                    Files.deleteIfExists(oldImagePath);
//                }
//                    FileUploadUtil.saveImageAsWebp(uploadDir, webpName, image);
//            }
//
//            //刪除舊履歷檔案
//            if (!Objects.equals(pdf.getOriginalFilename(), "")) {
//                if(oldResumeName != null){
//                    Path oldResumePath = uploadDirPath.resolve(oldResumeName);
//
//                    Files.deleteIfExists(oldResumePath);
//                }
//
//                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
//            }
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//
//        return "redirect:/dashboard/";
//    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        //關聯Skills/Languages
        for (Skills skills : jobSeekerProfile.getSkills()) {
            skills.setJobSeekerProfile(jobSeekerProfile);
        }
        for (Languages languages : jobSeekerProfile.getLanguages()) {
            languages.setJobSeekerProfile(jobSeekerProfile);
        }

        //原始檔名準備
        String imageName = "";
        String resumeName = "";
        String webpName = "";

        JobSeekerProfile oldProfile = jobSeekerProfileService
                .getOne(jobSeekerProfile.getUserAccountId())
                .orElseThrow(() -> new EntityNotFoundException("JobSeekerProfile not found."));

        String oldPhotoName = oldProfile.getProfilePhoto();
        String oldResumeName = oldProfile.getResume();

        //準備新圖片檔名
        if (!Objects.equals(image.getOriginalFilename(), "")) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));

            int dot = imageName.indexOf('.');
            String base = (dot > 0) ? imageName.substring(0, dot) : imageName;

            webpName = base + ".webp";

            jobSeekerProfile.setProfilePhoto(webpName);
        }

        //準備新履歷檔名
        if (!Objects.equals(pdf.getOriginalFilename(), "")) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));

            jobSeekerProfile.setResume(resumeName);
        }

        //儲存更新後的Profile資料
        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);

        try {
            //S3上傳目錄(邏輯路徑)
            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();

            // 處理圖片
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                if (oldPhotoName != null) {
                    //刪除舊圖片(S3)
                    s3StorageService.deleteFromS3(uploadDir, oldPhotoName);
                }
                //上傳新圖片(WebP)
                s3StorageService.saveImageAsWebpToS3(uploadDir, webpName, image);
            }

            // 處理履歷
            if (!Objects.equals(pdf.getOriginalFilename(), "")) {
                if (oldResumeName != null) {
                    //刪除舊履歷(S3)
                    s3StorageService.deleteFromS3(uploadDir, oldResumeName);
                }
                //上傳新履歷
                s3StorageService.saveFileToS3(uploadDir, resumeName, pdf);
            }

        } catch (IOException ex) {
            throw new RuntimeException("Error while uploading to S3", ex);
        }

        return "redirect:/dashboard/";
    }

    //show the profile or retrieve the profile for a given candidate id
    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);

        model.addAttribute("profile", seekerProfile.get());

        return "job-seeker-profile";

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
//                    "photos/candidate/" + userId, fileName);
//        } catch (IOException io) {
//            return ResponseEntity.badRequest().build(); //400 Bad Request
//        }
//
//        if (resource == null) { //沒找到檔案
//            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
//        }
//
//        Path filePath = Paths.get("photos/candidate/" + userId).resolve(fileName).normalize();
//
//        String contentType;
//
//        try {
//            contentType = Files.probeContentType(filePath);
//        } catch (IOException e) {
//            contentType = "application/octet-stream"; // fallback
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
}
