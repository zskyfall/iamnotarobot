package com.powerranger.sow2.iamnotarobot;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String avatar;
    private String email;
    private String gender;

    public User(String name,String avatar,String email,String gender) {
        this.name = name;
        this.avatar = avatar;
        this.email = email;
        this.gender = gender;
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
}
