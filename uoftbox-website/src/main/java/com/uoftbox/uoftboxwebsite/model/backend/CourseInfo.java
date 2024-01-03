package com.uoftbox.uoftboxwebsite.model.backend;

import java.util.List;

public class CourseInfo {
    private String course;
    private String section;
    private String getprof;
    private List<TimeAndPlace> timeAndPlaceList;
    
    public CourseInfo(String course, String section, String getprof, List<TimeAndPlace> timeAndPlaceList) {
        this.course = course;
        this.section = section;
        this.getprof = getprof;
        this.timeAndPlaceList = timeAndPlaceList;
    }

    // Getter 方法
    public String getCourse() {
        return course;
    }

    public String getSection() {
        return section;
    }

    public String getprof(){
        return getprof;
    }

    public List<TimeAndPlace> getTime() {
        return timeAndPlaceList;
    }

    public List<TimeAndPlace> getTimeAndPlaceList() {
        return timeAndPlaceList;
    }
}
