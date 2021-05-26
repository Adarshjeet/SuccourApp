package com.example.succour;
public class Helper {
    String name,email,password,phone,Token;

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public Helper(String name, String email, String password, String phone, String Token) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.Token = Token;
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

