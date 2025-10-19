package com.jackycoder.jobportal.services;

import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.entity.*;
import com.jackycoder.jobportal.repository.JobPostActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class JobPostActivityService {
    private final JobPostActivityRepository jobPostActivityRepository;

    @Autowired
    public JobPostActivityService(JobPostActivityRepository jobPostActivityRepository) {
        this.jobPostActivityRepository = jobPostActivityRepository;
    }

    public JobPostActivity addNew(JobPostActivity jobPostActivity) {
        return jobPostActivityRepository.save(jobPostActivity);
    }

    //DTO: data transfer object
    //just a holder for some of the method, for some of the data
    public List<RecruiterJobsDto> getRecruiterJobs(int recruiter) {
        List<IRecruiterJobs> recruiterJobsDtos
                = jobPostActivityRepository.getRecruiterJobs(recruiter);

        List<RecruiterJobsDto> recruiterJobsDtoList = new ArrayList<>();

        //convert info from database to DTOs
        for (IRecruiterJobs rec : recruiterJobsDtos) {
            //construct a DTO based on information that we retrieved from the database
            JobLocation loc = new JobLocation
                    (rec.getLocationId(), rec.getCity(), rec.getState(), rec.getCountry());

            JobCompany comp = new JobCompany(rec.getCompanyId(), rec.getName(), "");

            recruiterJobsDtoList.add(new RecruiterJobsDto(
                    rec.getTotalCandidates(), rec.getJob_post_id(), rec.getJob_title(), loc, comp));
        }

        return recruiterJobsDtoList;
    }

    public JobPostActivity getOne(int id) {
        return jobPostActivityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public List<JobPostActivity> getAll() {
        return jobPostActivityRepository.findAllByOrderByPostedDateDesc(); //Desc:從新到舊
    }

    public List<JobPostActivity> search(String job, String location, List<String> type,
                                        List<String> remote, LocalDate searchDate) {
        return Objects.isNull(searchDate)
                ? jobPostActivityRepository.searchWithoutDate(job, location, remote, type)
                : jobPostActivityRepository.search(job, location, remote, type, searchDate);
    }

    public void delete(int id){
        jobPostActivityRepository.deleteById(id);
    }
}
