package com.example.facevision_mvvm.Models;


import com.example.facevision_mvvm.Models.Reco.SimilarityClassifier;
import com.google.firebase.database.IgnoreExtraProperties;

/**
    Wanted class
 * @author yoni gofman
 * @version 1.0
 *
 */


@IgnoreExtraProperties
 //TODO: 1/21/2022   add firebase annotation
public class Wanted {
    private String firstName;
    private String lastName;
    private String id;
    private Location location;
    private Object recognition;
    private String key;
    private SimilarityClassifier.Recognition RecoObject;


    /**
     * empty constructor
     */
    public Wanted()
    {
        this.recognition = null;
        this.location = null;
        this.id = null;
        this.lastName = null;
        this.firstName = null;
        this.RecoObject = null;
        this.key = null;
        this.recognition = null;


    }

    /**
     * Wanted constructor
     * @param firstName first name
     * @param lastName last name
     * @param id id
     * @param location location
     */
    public Wanted(String firstName, String lastName, String id, Location location, Object recognition) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.location = location;
        this.recognition = recognition;
    }

    public Wanted(String firstName, String lastName, String id, Location location, Object recognition, String key, SimilarityClassifier.Recognition RecoObject) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.location = location;
        this.recognition = recognition;
        this.key = key;
        this.RecoObject = RecoObject;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Object getRecognition() {
        return recognition;
    }

    public void setRecognition(Object reco) {
        this.recognition = recognition;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}

