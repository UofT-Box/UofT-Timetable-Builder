package com.uoftbox.uofttimetablebuilder.model.frontend;

import java.util.List;

public class UserInput {
    private List<String> courseList;
    private String sectionCode;
    
    public UserInput(){

    }

    public UserInput(List<String> courseList, String sectionCode) {
        this.courseList = courseList;
        this.sectionCode = sectionCode;
    }

    public List<String> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<String> courseList) {
        this.courseList = courseList;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }
    
    
}