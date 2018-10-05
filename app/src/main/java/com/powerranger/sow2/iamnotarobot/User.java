package com.powerranger.sow2.iamnotarobot;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String name;
    private String avatar;
    private String email;
    private String gender;
    private String birthday;
    private String token;

    public User(String id, String name,String avatar,String email,String gender, String birthday, String token) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
