package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.services.*;
import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerApplyController {
    private final JobPostActivityService jobPostActivityService;
    private final UsersService usersService;

    private final JobSeekerApplyService jobSeekerApplyService;

    private final JobSeekerSaveService jobSeekerSaveService;

    private final RecruiterProfileService recruiterProfileService;

    private final JobSeekerProfileService jobSeekerProfileService;

    @Autowired
    public JobSeekerApplyController(JobPostActivityService jobPostActivityService,
                                    UsersService usersService,
                                    JobSeekerApplyService jobSeekerApplyService,
                                    JobSeekerSaveService jobSeekerSaveService,
                                    RecruiterProfileService recruiterProfileService,
                                    JobSeekerProfileService jobSeekerProfileService) {
        this.jobPostActivityService = jobPostActivityService;
        this.usersService = usersService;
        this.jobSeekerApplyService = jobSeekerApplyService;
        this.jobSeekerSaveService = jobSeekerSaveService;
        this.recruiterProfileService = recruiterProfileService;
        this.jobSeekerProfileService = jobSeekerProfileService;
    }

    //display the details for a given job
    @GetMapping("/job-details-apply/{id}")
    public String display(@PathVariable("id") int id, Model model){
        JobPostActivity jobDetails = jobPostActivityService.getOne(id);

        //get a list of "job candidates" that have applied for a given job
        List<JobSeekerApply> jobSeekerApplyList
                = jobSeekerApplyService.getJobCandidates(jobDetails);

        //get a list of "job seekers" who have saved this job
        List<JobSeekerSave> jobSeekerSaveList
                = jobSeekerSaveService.getJobCandidates(jobDetails);

        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        if(!(authentication instanceof AnonymousAuthenticationToken)){
            if(authentication.getAuthorities().contains
                    (new SimpleGrantedAuthority("Recruiter"))){
                RecruiterProfile user = recruiterProfileService.getCurrentRecruiterProfile();

                if(user != null){
                    model.addAttribute("applyList", jobSeekerApplyList);
                }
            }else{
                JobSeekerProfile user = jobSeekerProfileService.getCurrentSeekerProfile();

                if(user != null){
                    boolean exists = false;
                    boolean saved = false;

                    for(JobSeekerApply jobSeekerApply : jobSeekerApplyList){
                        if(jobSeekerApply.getUserId().getUserAccountId()
                                == user.getUserAccountId()){
                            exists = true;

                            break;
                        }
                    }

                    for(JobSeekerSave jobSeekerSave : jobSeekerSaveList){
                        if(jobSeekerSave.getUserId().getUserAccountId()
                        == user.getUserAccountId()){
                            saved = true;

                            break;
                        }
                    }

                    model.addAttribute("alreadyApplied", exists);
                    model.addAttribute("alreadySaved", saved);
                }
            }
        }

        JobSeekerApply jobSeekerApply = new JobSeekerApply();

        model.addAttribute("applyJob", jobSeekerApply);
        model.addAttribute("jobDetails",jobDetails);
        model.addAttribute("user", usersService.getCurrentUserProfile());

        return "job-details";
    }

    @PostMapping("/job-details/apply/{id}")
    //persist the applied job or save the applied job
    public String apply(@PathVariable("id") int id){
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();
        
        if(!(authentication instanceof  AnonymousAuthenticationToken)){
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);

            Optional<JobSeekerProfile> seekerProfile
                    = jobSeekerProfileService.getOne(user.getUserId());

            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

            JobSeekerApply jobSeekerApply; //和老師不同,老師寫在方法參數裡面

            if(seekerProfile.isPresent() && jobPostActivity != null){
                jobSeekerApply = new JobSeekerApply();

                jobSeekerApply.setUserId(seekerProfile.get());
                jobSeekerApply.setJob(jobPostActivity);
                jobSeekerApply.setApplyDate(new Date());
            }else{
                throw new RuntimeException("User not found");
            }

            jobSeekerApplyService.addNew(jobSeekerApply);
        }

        return "redirect:/job-details-apply/" + id;
    }

    @PostMapping("/job-details/withdraw/{id}")
    public String withdraw(@PathVariable("id") int id){
        JobSeekerProfile currentSeekerProfile = jobSeekerProfileService.getCurrentSeekerProfile();
        JobPostActivity job = jobPostActivityService.getOne(id);

        jobSeekerApplyService.withdrawApply(currentSeekerProfile, job);

        return "redirect:/job-details-apply/" + id;
    }
}
