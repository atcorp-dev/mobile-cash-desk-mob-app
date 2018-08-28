package ua.com.atcorp.mobilecashdesk.Models;

import java.io.Serializable;

public class Company implements Serializable {
    private String id;
    private String code;
    private String name;
    private String phone;
    private String email;

    public Company(String id, String code, String name, String phone, String email) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
