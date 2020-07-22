package com.avnish.wecare;

public class User {
    private String name;
    private String email;
    private String id;
    private String phone;
    private String latitude;
    private String longitude;
    private String token;

    public User(String name, String email, String id, String phone,String latitude,String longitude,String token) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.token = token;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

