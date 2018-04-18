package com.cyclon.com.med_manager.Data;

public class ProfileData {

    private String name;
    private String email;
    private byte[] image;

    public ProfileData(String name, String email, byte[] image) {
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getImage() {
        return image;
    }
}
