package com.example.arthub.Auth;

public  class User {
    public String email;
    public String role;

    public String password;


    public User() {}

    public User(String email,String password, String role) {
        this.email = email;
        this.role = role;
        this.password = password;
    }
}
