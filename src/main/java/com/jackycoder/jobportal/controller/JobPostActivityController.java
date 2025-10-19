package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.services.JobPostActivityService;
import com.jackycoder.jobportal.services.JobSeekerApplyService;
import com.jackycoder.jobportal.services.JobSeekerSaveService;
import com.jackycoder.jobportal.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Controller
public class JobPostActivityController {
    private final UsersService usersService;
    private final JobPostActivityService jobPostActivityService;

    private final JobSeekerApplyService jobSeekerApplyService;

    private final JobSeekerSaveService jobSeekerSaveService;

    @Autowired
    public JobPostActivityController(UsersService usersService,
                                     JobPostActivityService jobPostActivityService,
                                     JobSeekerApplyService jobSeekerApplyService,
                                     JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    //search for jobs
    @GetMapping("/dashboard/")
    public String searchJobs(Model model,
                             @RequestParam(value = "job", required = false) String job,
                             @RequestParam(value = "location", required = false) String location,
                             @RequestParam(value = "partTime", required = false) String partTime,
                             @RequestParam(value = "fullTime", required = false) String fullTime,
                             @RequestParam(value = "freelance", required = false) String freelance,
                             @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                             @RequestParam(value = "officeOnly", required = false) String officeOnly,
                             @RequestParam(value = "partialRemote", required = false) String partialRemote,
                             @RequestParam(value = "today", required = false) boolean today,
                             @RequestParam(value = "days7", required = false) boolean days7,
                             @RequestParam(value = "days30", required = false) boolean days30
    ) {
        //按下form送出後,回到dashboard.html還能保持剛剛選擇的篩選資料!
        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }

        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if (officeOnly == null && remoteOnly == null && partialRemote == null) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }

        //If we don't have any of the items selected as far as the flags, then we simply
        //get all jobs
        if (!dateSearchFlag && !remote && !type && !StringUtils.hasText(job)
                && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            //If any of the flags are selected, then we'll search using those appropriate flags
            jobPost = jobPostActivityService.search(
                    job,
                    location,
                    Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote),
                    searchDate);
        }

        //get current user profile
        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();

            model.addAttribute("username", currentUsername);

            if (authentication.getAuthorities().contains
                    (new SimpleGrantedAuthority("Recruiter"))) {

                List<RecruiterJobsDto> recruiterJobs = jobPostActivityService.getRecruiterJobs(
                        ((RecruiterProfile) currentUserProfile).getUserAccountId());

                model.addAttribute("jobPost", recruiterJobs);
            } else {
                List<JobSeekerApply> jobSeekerApplyList
                        = jobSeekerApplyService.getCandidatesJobs
                        ((JobSeekerProfile) currentUserProfile);

                List<JobSeekerSave> jobSeekerSaveList
                        = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

                boolean exist;
                boolean saved;

                for (JobPostActivity jobActivity : jobPost) {
                    exist = false;
                    saved = false;

                    for (JobSeekerApply jobSeekerApply : jobSeekerApplyList) {
                        if (Objects.equals(jobActivity.getJobPostId(),
                                jobSeekerApply.getJob().getJobPostId())) {
                            jobActivity.setIsActive(true);

                            exist = true;

                            break;
                        }
                    }

                    for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
                        if (Objects.equals(jobActivity.getJobPostId(),
                                jobSeekerSave.getJob().getJobPostId())) {
                            jobActivity.setIsSaved(true);

                            saved = true;

                            break;
                        }
                    }

                    if (!exist) {
                        jobActivity.setIsActive(false);
                    }

                    if (!saved) {
                        jobActivity.setIsSaved(false);
                    }
                }

                //老師放在 if(!saved){}的正下方(也就是迴圈內)--->助教有提到會修正(講座44)
                model.addAttribute("jobPost", jobPost);
            }
        }

        model.addAttribute("user", currentUserProfile);

        return "dashboard";
    }

    //post new job
    @GetMapping("/dashboard/add")
    //show the form
    public String addJobs(Model model) {
        model.addAttribute("jobPostActivity", new JobPostActivity());
        model.addAttribute("user", usersService.getCurrentUserProfile());

        return "add-jobs";
    }

    //送出post new job的button
    @PostMapping("/dashboard/addNew")
    public String addNew(JobPostActivity jobPostActivity, Model model) {
        Users user = usersService.getCurrentUser();

        if (user != null) {
            jobPostActivity.setPostedById(user);
        }

        jobPostActivity.setPostedDate(new Date());
        model.addAttribute("jobPostActivity", jobPostActivity);

        JobPostActivity saved = jobPostActivityService.addNew(jobPostActivity);

        return "redirect:/dashboard/";
    }

    @PostMapping("/dashboard/edit/{id}")
    public String editJob(@PathVariable("id") int id, Model model) {
        JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

        model.addAttribute("jobPostActivity", jobPostActivity);
        model.addAttribute("user", usersService.getCurrentUserProfile());

        return "add-jobs";
    }

    @PostMapping("/dashboard/deleteJob/{id}")
    public String deleteJob(@PathVariable("id") int id) {
        jobPostActivityService.delete(id);

        return "redirect:/dashboard/";
    }

    @GetMapping("/dashboard/jobApplicantsList/{id}")
    public String jobApplicantsList(@PathVariable("id") int id,
                                    @RequestParam(value = "dTwo", required = false) String dTwo,
                                    @RequestParam(value = "dTen", required = false) String dTen,
                                    @RequestParam(value = "fVisa", required = false) String fVisa,
                                    @RequestParam(value = "korean", required = false) String korean,
                                    @RequestParam(value = "chinese", required = false) String chinese,
                                    @RequestParam(value = "english", required = false) String english,
                                    @RequestParam(value = "japanese", required = false) String japanese,
                                    @RequestParam(value = "spanish", required = false) String spanish,
                                    @RequestParam(value = "german", required = false) String german,
                                    @RequestParam(value = "french", required = false) String french,
                                    @RequestParam(value = "vietnamese", required = false) String vietnamese,
                                    Model model) {

        //按下form送出後,回到dashboard.html還能保持剛剛選擇的篩選資料!
        model.addAttribute("dTwo", Objects.equals(dTwo, "D2"));
        model.addAttribute("dTen", Objects.equals(dTen, "D10"));
        model.addAttribute("fVisa", Objects.equals(fVisa, "F-Visa"));

        model.addAttribute("korean", Objects.equals(korean, "Korean"));
        model.addAttribute("chinese", Objects.equals(chinese, "Chinese"));
        model.addAttribute("english", Objects.equals(english, "English"));
        model.addAttribute("japanese", Objects.equals(japanese, "Japanese"));
        model.addAttribute("spanish", Objects.equals(spanish, "Spanish"));
        model.addAttribute("german", Objects.equals(german, "German"));
        model.addAttribute("french", Objects.equals(french, "French"));
        model.addAttribute("vietnamese", Objects.equals(vietnamese, "Vietnamese"));

        boolean workType = true;
        boolean language = true;

        //前端選項要更改!
        if (dTwo == null && dTen == null && fVisa == null) {
            dTwo = "D2";
            dTen = "D10";
            fVisa = "F-Visa";
            workType = false;
        }

        if (korean == null && chinese == null && english == null && japanese == null && spanish == null
                && german == null && french == null && vietnamese == null) {
            korean = "Korean";
            chinese = "Chinese";
            english = "English";
            japanese = "Japanese";
            spanish = "Spanish";
            german = "German";
            french = "French";
            vietnamese = "Vietnamese";
            language = false; //沒選擇語言搜尋條件!
        }

        List<JobSeekerApply> jobSeekerApplyList;
        JobPostActivity job = jobPostActivityService.getOne(id);

        int langCount = 0;

        //只有在有選擇語言的搜尋條件下才去計算user選擇了幾個語言
        if(language) { //true代表使用者有選擇語言的搜尋條件
            langCount = (int) Stream.of(korean, chinese, english, japanese, spanish, german, french, vietnamese)
                    .filter(Objects::nonNull)
                    .count();
        }

        //If we don't have any of the items selected as far as the flags, then we simply
        //get all jobs

        //要是找不到資料的話,是回傳空集合回來而不是null(就是規定而已)
        if (!workType && !language) {
            jobSeekerApplyList = jobSeekerApplyService.getJobCandidates(job);
        } else {
            //If any of the flags are selected, then we'll search using those appropriate flags
            jobSeekerApplyList = jobSeekerApplyService.search(
                    id,
                    langCount,
                    Arrays.asList(dTwo, dTen, fVisa),
                    Arrays.asList(korean, chinese, english, japanese, spanish, german, french, vietnamese));
        }

        model.addAttribute("jobSeekerApplyList", jobSeekerApplyList);

        //get current user profile
        Object currentUserProfile = usersService.getCurrentUserProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();

            model.addAttribute("username", currentUsername);
        }

        model.addAttribute("user", currentUserProfile);
        model.addAttribute("jobId", id);

        return "job-applicants-list";
    }

    @GetMapping("/global-search/")
    public String globalSearch(Model model,
                               @RequestParam(value = "job", required = false) String job,
                               @RequestParam(value = "location", required = false) String location,
                               @RequestParam(value = "partTime", required = false) String partTime,
                               @RequestParam(value = "fullTime", required = false) String fullTime,
                               @RequestParam(value = "freelance", required = false) String freelance,
                               @RequestParam(value = "remoteOnly", required = false) String remoteOnly,
                               @RequestParam(value = "officeOnly", required = false) String officeOnly,
                               @RequestParam(value = "partialRemote", required = false) String partialRemote,
                               @RequestParam(value = "today", required = false) boolean today,
                               @RequestParam(value = "days7", required = false) boolean days7,
                               @RequestParam(value = "days30", required = false) boolean days30
    ) {
        model.addAttribute("partTime", Objects.equals(partTime, "Part-Time"));
        model.addAttribute("fullTime", Objects.equals(fullTime, "Full-Time"));
        model.addAttribute("freelance", Objects.equals(freelance, "Freelance"));

        model.addAttribute("remoteOnly", Objects.equals(remoteOnly, "Remote-Only"));
        model.addAttribute("officeOnly", Objects.equals(officeOnly, "Office-Only"));
        model.addAttribute("partialRemote", Objects.equals(partialRemote, "Partial-Remote"));

        model.addAttribute("today", today);
        model.addAttribute("days7", days7);
        model.addAttribute("days30", days30);

        model.addAttribute("job", job);
        model.addAttribute("location", location);

        LocalDate searchDate = null;
        List<JobPostActivity> jobPost = null;
        boolean dateSearchFlag = true;
        boolean remote = true;
        boolean type = true;

        if (days30) {
            searchDate = LocalDate.now().minusDays(30);
        } else if (days7) {
            searchDate = LocalDate.now().minusDays(7);
        } else if (today) {
            searchDate = LocalDate.now();
        } else {
            dateSearchFlag = false;
        }

        if (partTime == null && fullTime == null && freelance == null) {
            partTime = "Part-Time";
            fullTime = "Full-Time";
            freelance = "Freelance";
            remote = false;
        }

        if (officeOnly == null && remoteOnly == null && partialRemote == null) {
            officeOnly = "Office-Only";
            remoteOnly = "Remote-Only";
            partialRemote = "Partial-Remote";
            type = false;
        }

        //If we don't have any of the items selected as far as the flags, then we simply
        //get all jobs
        if (!dateSearchFlag && !remote && !type && !StringUtils.hasText(job)
                && !StringUtils.hasText(location)) {
            jobPost = jobPostActivityService.getAll();
        } else {
            //If any of the flags are selected, then we'll search using those appropriate flags
            jobPost = jobPostActivityService.search(
                    job,
                    location,
                    Arrays.asList(partTime, fullTime, freelance),
                    Arrays.asList(remoteOnly, officeOnly, partialRemote),
                    searchDate);
        }

        model.addAttribute("jobPost", jobPost);

        return "global-search";
    }
}
