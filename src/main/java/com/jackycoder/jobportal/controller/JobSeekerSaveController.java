package com.jackycoder.jobportal.controller;

import com.jackycoder.jobportal.entity.JobPostActivity;
import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.entity.JobSeekerSave;
import com.jackycoder.jobportal.entity.Users;
import com.jackycoder.jobportal.services.JobPostActivityService;
import com.jackycoder.jobportal.services.JobSeekerProfileService;
import com.jackycoder.jobportal.services.JobSeekerSaveService;
import com.jackycoder.jobportal.services.UsersService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class JobSeekerSaveController {
    private final UsersService usersService;

    private final JobSeekerProfileService jobSeekerProfileService;

    private final JobPostActivityService jobPostActivityService;

    private final JobSeekerSaveService jobSeekerSaveService;

    public JobSeekerSaveController(UsersService usersService,
                                   JobSeekerProfileService jobSeekerProfileService,
                                   JobPostActivityService jobPostActivityService,
                                   JobSeekerSaveService jobSeekerSaveService) {
        this.usersService = usersService;
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.jobPostActivityService = jobPostActivityService;
        this.jobSeekerSaveService = jobSeekerSaveService;
    }

    @PostMapping("/job-details/save/{id}")
    public String save(@PathVariable("id") int id) {
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUsername = authentication.getName();
            Users user = usersService.findByEmail(currentUsername);

            Optional<JobSeekerProfile> seekerProfile
                    = jobSeekerProfileService.getOne(user.getUserId());

            JobPostActivity jobPostActivity = jobPostActivityService.getOne(id);

            JobSeekerSave jobSeekerSave; //和老師不同,老師寫在方法參數裡面

            if (seekerProfile.isPresent() && jobPostActivity != null) {
                jobSeekerSave = new JobSeekerSave();

                jobSeekerSave.setJob(jobPostActivity);
                jobSeekerSave.setUserId(seekerProfile.get());
            } else {
                throw new RuntimeException("User not found");
            }

            jobSeekerSaveService.addNew(jobSeekerSave);
        }

        return "redirect:/job-details-apply/" + id;
    }

    @PostMapping("/job-details/unsave/{id}")
    public String unsave(@PathVariable("id") int id){
        JobSeekerProfile currentSeekerProfile = jobSeekerProfileService.getCurrentSeekerProfile();
        JobPostActivity job = jobPostActivityService.getOne(id);

        jobSeekerSaveService.unsave(currentSeekerProfile, job);

        return "redirect:/job-details-apply/" + id;
    }

    //show a list of saved jobs
    @GetMapping("/saved-jobs/")
    public String savedJobs(Model model) {
        List<JobPostActivity> jobPost = new ArrayList<>();
        Object currentUserProfile = usersService.getCurrentUserProfile();

        List<JobSeekerSave> jobSeekerSaveList
                = jobSeekerSaveService.getCandidatesJob((JobSeekerProfile) currentUserProfile);

        for (JobSeekerSave jobSeekerSave : jobSeekerSaveList) {
            jobPost.add(jobSeekerSave.getJob());
        }

        model.addAttribute("jobPost", jobPost);
        model.addAttribute("user", currentUserProfile);

        return "saved-jobs";
    }
}
