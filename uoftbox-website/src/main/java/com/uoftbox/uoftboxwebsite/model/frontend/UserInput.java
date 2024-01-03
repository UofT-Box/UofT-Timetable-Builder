package com.uoftbox.uoftboxwebsite.model.frontend;

import java.util.List;

public class UserInput {
    private List<InputCourseInfo> courses;
    
    public UserInput(){

    }
    public UserInput(List<InputCourseInfo> courses) {
        this.courses = courses;
    }
    public List<InputCourseInfo> getCourses() {
        return courses;
    }
    public void setCourses(List<InputCourseInfo> courses) {
        this.courses = courses;
    }
    
}