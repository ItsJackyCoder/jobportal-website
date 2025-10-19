package com.jackycoder.jobportal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "languages")
public class Languages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String experienceLevel;

    @ManyToOne
    @JoinColumn(name = "job_seeker_profile")
    private JobSeekerProfile jobSeekerProfile;

    public Languages() {
    }

    public Languages(Integer id, String name, String experienceLevel, JobSeekerProfile jobSeekerProfile) {
        this.id = id;
        this.name = name;
        this.experienceLevel = experienceLevel;
        this.jobSeekerProfile = jobSeekerProfile;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public JobSeekerProfile getJobSeekerProfile() {
        return jobSeekerProfile;
    }

    public void setJobSeekerProfile(JobSeekerProfile jobSeekerProfile) {
        this.jobSeekerProfile = jobSeekerProfile;
    }

    @Override
    public String toString() {
        return "Languages{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", experienceLevel='" + experienceLevel + '\'' +
                ", jobSeekerProfile=" + jobSeekerProfile +
                '}';
    }
}
