package com.uoftbox.uofttimetablebuilder.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Distances {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name="origin")
    private String origin;
    @Column(name="destination")
    private String destination;
    @Column(name="distance")
    private Integer distance;
    @Column(name="duration")
    private Integer duration;
    
    
    public Distances() {
    }
    public Distances(Integer id, String origin, String destination, Integer distance, Integer duration) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.duration = duration;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getOrigin() {
        return origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public Integer getDistance() {
        return distance;
    }
    public void setSectdistanceionCode(Integer distance) {
        this.distance = distance;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    
}
