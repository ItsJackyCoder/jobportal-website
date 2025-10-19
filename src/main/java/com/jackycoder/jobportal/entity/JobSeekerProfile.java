package com.jackycoder.jobportal.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "job_seeker_profile")
public class JobSeekerProfile {
    @Id
    private Integer userAccountId;

    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId //此外鍵同時也是主鍵! 所以會直接沿用Users裡面的主鍵－userId
    private Users userId;

    private String firstName;

    private String lastName;

    private String city;

    private String state;

    private String country;

    private String workAuthorization;

    private String employmentType;

    private String resume;

    @Column(nullable = true, length = 64) //參考IPAD p.122(nullable預設是true; length是255)
    private String profilePhoto;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobSeekerProfile", orphanRemoval = true)
    private List<Skills> skills;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "jobSeekerProfile", orphanRemoval = true)
    private List<Languages> languages;

    public JobSeekerProfile() {
    }

    public JobSeekerProfile(Users userId) {
        this.userId = userId;
    }

    public JobSeekerProfile(Integer userAccountId, Users userId, String firstName,
                            String lastName, String city, String state, String country,
                            String workAuthorization, String employmentType, String resume,
                            String profilePhoto, List<Skills> skills, List<Languages> languages) {
        this.userAccountId = userAccountId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.workAuthorization = workAuthorization;
        this.employmentType = employmentType;
        this.resume = resume;
        this.profilePhoto = profilePhoto;
        this.skills = skills;
        this.languages = languages;
    }

    public Integer getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Integer userAccountId) {
        this.userAccountId = userAccountId;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWorkAuthorization() {
        return workAuthorization;
    }

    public void setWorkAuthorization(String workAuthorization) {
        this.workAuthorization = workAuthorization;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public List<Skills> getSkills() {
        return skills;
    }

    public void setSkills(List<Skills> skills) {
        this.skills = skills;
    }

    public List<Languages> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Languages> languages) {
        this.languages = languages;
    }

    //get the photo's image path
    @Transient
    public String getPhotosImagePath(){
        if(profilePhoto == null || userAccountId == null) return null;

        String bucket = "jackycoder-jobportal-uploads";
        String region = "us-east-2";

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/photos/candidate/"
                + userAccountId + "/" + profilePhoto;
    }

    //get the resume's path
    @Transient
    public String getResumePath(){
        if(resume == null || userAccountId == null) return null;

        String bucket = "jackycoder-jobportal-uploads";
        String region = "us-east-2";

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/photos/candidate/"
                + userAccountId + "/" + resume;
    }

    @Override
    public String toString() {
        return "JobSeekerProfile{" +
                "userAccountId=" + userAccountId +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", workAuthorization='" + workAuthorization + '\'' +
                ", employmentType='" + employmentType + '\'' +
                ", resume='" + resume + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                '}';
    }
}

