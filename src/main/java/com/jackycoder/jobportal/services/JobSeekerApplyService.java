package com.jackycoder.jobportal.services;

import com.jackycoder.jobportal.entity.JobPostActivity;
import com.jackycoder.jobportal.entity.JobSeekerApply;
import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.repository.JobSeekerApplyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSeekerApplyService {
    private final JobSeekerApplyRepository jobSeekerApplyRepository;

    @Autowired
    public JobSeekerApplyService(JobSeekerApplyRepository jobSeekerApplyRepository) {
        this.jobSeekerApplyRepository = jobSeekerApplyRepository;
    }

    public List<JobSeekerApply> getCandidatesJobs(JobSeekerProfile userAccountId){
        return jobSeekerApplyRepository.findByUserId(userAccountId);
    }

    public List<JobSeekerApply> getJobCandidates(JobPostActivity job){
        return jobSeekerApplyRepository.findByJob(job);
    }

    public List<JobSeekerApply> search(int id, int langCount, List<String> workType, List<String> language) {
        return jobSeekerApplyRepository.search(id, langCount, workType, language);
    }

    public void addNew(JobSeekerApply jobSeekerApply) {
        jobSeekerApplyRepository.save(jobSeekerApply);
    }

    @Transactional //修改DB時(delete, update),必須得加上,否則會出錯
    public void withdrawApply(JobSeekerProfile user, JobPostActivity job){
        jobSeekerApplyRepository.deleteByUserIdAndJob(user, job);
    }
}
