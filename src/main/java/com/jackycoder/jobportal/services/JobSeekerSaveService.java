package com.jackycoder.jobportal.services;

import com.jackycoder.jobportal.entity.JobPostActivity;
import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.entity.JobSeekerSave;
import com.jackycoder.jobportal.repository.JobSeekerSaveRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerSaveService {
    private final JobSeekerSaveRepository jobSeekerSaveRepository;

    public JobSeekerSaveService(JobSeekerSaveRepository jobSeekerSaveRepository) {
        this.jobSeekerSaveRepository = jobSeekerSaveRepository;
    }

    public List<JobSeekerSave> getCandidatesJob(JobSeekerProfile userAccountId){
        return jobSeekerSaveRepository.findByUserIdOrderByJobPostedDateDesc(userAccountId);
    }

    public List<JobSeekerSave> getJobCandidates(JobPostActivity job){
        return jobSeekerSaveRepository.findByJob(job);
    }

    public void addNew(JobSeekerSave jobSeekerSave) {
        jobSeekerSaveRepository.save(jobSeekerSave);
    }

    @Transactional //修改DB時(delete, update),必須得加上,否則會出錯
    public void unsave(JobSeekerProfile user, JobPostActivity job){
        jobSeekerSaveRepository.deleteByUserIdAndJob(user, job);
    }
}
