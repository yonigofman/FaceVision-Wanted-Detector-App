package com.example.facevision_mvvm.Models;

public class Location {
    private String address;
    private String city;
    private double latitude;
    private double longitude;

  public   static final  int LOCATION_SERVICE_ID = 175;
    public  static final String ACTION_START_SERVICE_LOCATION = "startLocationService";
    public  static final String ACTION_STOP_SERVICE_LOCATION = "stopLocationService";


    /**
     * empty constructor
     */
    public Location() {
        longitude = 0;
        latitude = 0;
    }

    /**
     * constructor with full parameters
     * @param address address of the location
     * @param city city of the location
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     */
    public Location(String address, String city, double latitude, double longitude) {

        this.address = address;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;


    }


    /**
     * constructor with latitude and longitude
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
