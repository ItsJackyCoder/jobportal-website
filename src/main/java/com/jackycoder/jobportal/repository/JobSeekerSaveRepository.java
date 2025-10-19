package com.jackycoder.jobportal.repository;

import com.jackycoder.jobportal.entity.JobPostActivity;
import com.jackycoder.jobportal.entity.JobSeekerProfile;
import com.jackycoder.jobportal.entity.JobSeekerSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerSaveRepository extends JpaRepository<JobSeekerSave, Integer>{
    List<JobSeekerSave> findByUserIdOrderByJobPostedDateDesc(JobSeekerProfile userAccountId);

    List<JobSeekerSave> findByJob(JobPostActivity job);

    void deleteByUserIdAndJob(JobSeekerProfile user, JobPostActivity job);
}