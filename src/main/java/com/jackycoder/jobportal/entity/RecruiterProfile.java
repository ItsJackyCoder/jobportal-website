package com.jackycoder.jobportal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recruiter_profile")
public class RecruiterProfile {
    @Id
    private int userAccountId;

    //foreign key在recruiter_profile的user_account_id欄位!
    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId //此外鍵同時也是主鍵! 所以會直接沿用Users裡面的主鍵－userId
    private Users userId;

    private String firstName;

    private String lastName;

    private String city;

    private String state;

    private String country;

    private String company;

    @Column(nullable = true, length = 64) //參考IPAD p.122(nullable預設是true; length是255)
    private String profilePhoto;

    public RecruiterProfile() {
    }

    public RecruiterProfile(Users users) {
        this.userId = users;
    }

    public RecruiterProfile(int userAccountId, Users userId, String firstName,
                            String lastName, String city, String state, String country,
                            String company, String profilePhoto) {
        this.userAccountId = userAccountId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.company = company;
        this.profilePhoto = profilePhoto;
    }

    public int getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(int userAccountId) {
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    @Transient //because we're not persisting this data in the database!!!
    public String getPhotosImagePath(){
        if(profilePhoto == null) return null;

        String bucket = "jackycoder-jobportal-uploads";
        String region = "us-east-2";

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/photos/recruiter/"
                + userAccountId + "/" + profilePhoto;
    }

    @Override
    public String toString() {
        return "RecruiterProfile{" +
                "userAccountId=" + userAccountId +
                ", userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", company='" + company + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                '}';
    }
}
