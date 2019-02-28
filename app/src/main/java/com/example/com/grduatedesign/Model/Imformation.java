package com.example.com.grduatedesign.Model;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class Imformation extends LitePalSupport {
    private String authId;
    private String name;
    private Boolean isMale;
    private Date birthDate;
    private String role;

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMale() {
        return isMale;
    }

    public void setMale(Boolean male) {
        isMale = male;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
