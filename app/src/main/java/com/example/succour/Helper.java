package com.example.succour;
public class Helper {
    String name,email,password,phone,Token,contact,occupation,blood;

    public String getToken() {
        return Token;
    }

    public String getBlood() {
        return blood;
    }

    public Helper(String name, String email, String password, String phone, String token, String contact, String occupation, String blood) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.Token = token;
        this.contact = contact;
        this.occupation = occupation;
        this.blood = blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }


    public Helper() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

