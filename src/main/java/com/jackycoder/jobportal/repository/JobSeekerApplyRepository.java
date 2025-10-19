package com.jackycoder.jobportal.repository;

import com.jackycoder.jobportal.entity.JobPostActivity;
import com.jackycoder.jobportal.entity.JobSeekerApply;
import com.jackycoder.jobportal.entity.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSeekerApplyRepository extends JpaRepository<JobSeekerApply, Integer> {
    List<JobSeekerApply> findByUserId(JobSeekerProfile userId);

    List<JobSeekerApply> findByJob(JobPostActivity job);

    void deleteByUserIdAndJob(JobSeekerProfile user, JobPostActivity job);

    //DISTINCT:為了防止出現重複相同的資料(但因為現在有GROUP BY,然後目前的設計是一個人只能投同一個職缺一次,所以我把它省略了)
    @Query(value = "SELECT j.* " +
            "FROM job_seeker_profile s " +
            "INNER JOIN job_seeker_apply j on j.user_id = s.user_account_id " +
            "INNER JOIN languages l on l.job_seeker_profile = s.user_account_id " +
            "WHERE (j.job = :id) " +
            "AND (s.work_authorization IN(:workType)) " +
            "AND (l.name IN(:language)) " +
            "GROUP BY s.user_account_id " +
            "HAVING (:langCount = 0 OR COUNT(DISTINCT l.name) = :langCount) " +
            "ORDER BY j.apply_date ASC", nativeQuery = true)
    List<JobSeekerApply> search(@Param("id") int id,
                                @Param("langCount") int langCount,
                                @Param("workType") List<String> workType,
                                @Param("language") List<String> language);
}
