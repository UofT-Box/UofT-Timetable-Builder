package com.uoftbox.uofttimetablebuilder.model.backend;

import java.util.List;

public class CourseInfo {
    private String course;
    private String section;
    private String prof;
    private List<TimeAndPlace> timeAndPlaceList;
    
    public CourseInfo(String course, String section, String prof, List<TimeAndPlace> timeAndPlaceList) {
        this.course = course;
        this.section = section;
        this.prof = prof;
        this.timeAndPlaceList = timeAndPlaceList;
    }

    public CourseInfo(CourseInfo other) {
        this.course = other.course;
        this.section = other.section;
        this.prof = other.prof;
        this.timeAndPlaceList = other.timeAndPlaceList;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getProf() {
        return prof;
    }

    public void setProf(String prof) {
        this.prof = prof;
    }

    public List<TimeAndPlace> getTimeAndPlaceList() {
        return timeAndPlaceList;
    }

    public void setTimeAndPlaceList(List<TimeAndPlace> timeAndPlaceList) {
        this.timeAndPlaceList = timeAndPlaceList;
    }
    
}
