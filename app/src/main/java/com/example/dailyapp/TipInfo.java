package com.example.dailyapp;

import java.io.Serializable;

public class TipInfo implements Serializable {
    private String id;
    private String content;
    private String location;
    private String time;
    private String colour="white";

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }


    public String getLocation() {
        return location;
    }

    public String getTime() {
        return time;
    }

    public String getColour() {
        return colour;
    }
}


