package com.elabs.geolocationnetwork.utils;

/**
 * Created by Tanmay on 28-09-2017.
 */

public class LocationManager {
    private double lat, lon;
    private String email, username, password,time;
    public LocationManager (double lat, double lon,String e, String u, String p,String time){
        this.lat = lat;
        this.lon = lon;
        email = e;
        username = u;
        password = p;
        this.time = time;
    }
    
    public double getLat(){
        return lat;
    }
    
    public double getLon(){
        return lon;
    }

    public String getEmail(){
        return email;

    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }

    public String getTime(){
        return  time;
    }
}
