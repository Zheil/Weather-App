package com.example.nikhil.group22_hw08;

/**
 * Assignment - Homework #08
 * File name - City.java
 * Full Name - Naga Manikanta Sri Venkata Jonnalagadda
 *             Karthik Gorijavolu
 * Group #22
 * **/
public class City {

    private String cityName,country;
    private boolean favorite;
    private String lastUpdated;
    private Long cityKey;
    private Double temperature;
    private String uid;

    public City() {

    }

    public City(Long cityKey, String cityName, String country, Double temperature, boolean favorite) {
        this.cityKey = cityKey;
        this.cityName = cityName;
        this.country = country;
        this.temperature = temperature;
        this.favorite = favorite;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getCityKey() {
        return cityKey;
    }

    public void setCityKey(Long cityKey) {
        this.cityKey = cityKey;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public String toString() {
        return "City{" +
                "cityName='" + cityName + '\'' +
                ", country='" + country + '\'' +
                ", favorite=" + favorite +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", key=" + cityKey +
                ", temperature=" + temperature +
                '}';
    }
}
