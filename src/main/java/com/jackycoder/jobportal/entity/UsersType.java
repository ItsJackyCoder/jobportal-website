package com.jackycoder.jobportal.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users_type")
public class UsersType {
    @Id
    //也可以用GenerationType.IDENTITY(在"MySQL"中是一樣的)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userTypeId;

    private String userTypeName;

    @OneToMany(mappedBy = "userTypeId", cascade = CascadeType.ALL)
    private List<Users> users;

    public UsersType() {
    }

    public UsersType(int userTypeId, String userTypeName, List<Users> users) {
        this.userTypeId = userTypeId;
        this.userTypeName = userTypeName;
        this.users = users;
    }

    public int getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(int userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getUserTypeName() {
        return userTypeName;
    }

    public void setUserTypeName(String userTypeName) {
        this.userTypeName = userTypeName;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }
}
