package com.uoftbox.uofttimetablebuilder.model.frontend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserInput {
    private List<String> fallCourseList;
    private List<String> winterCourseList;

    public UserInput() {

    }

    public UserInput(List<String> winterCourseList, List<String> fallCourseList) {
        this.winterCourseList = winterCourseList;
        this.fallCourseList = fallCourseList;
    }

    public List<String> getWinterCourseList() {
        return winterCourseList;
    }

    public List<String> getFallCourseList() {
        return fallCourseList;
    }

    public void setWinterCourseList(List<String> winterCourseList) {
        this.winterCourseList = winterCourseList;
    }

    public void setFallCourseList(List<String> fallCourseList) {
        this.fallCourseList = fallCourseList;
    }

    public List<String> getFallCourseCode(){
        return convertToCourseCode(fallCourseList);
    }

    public List<String> getWinterCourseCode(){
        return convertToCourseCode(winterCourseList);
    }

    private List<String> convertToCourseCode(List<String> courseList){
        List<String> result = new ArrayList<>();
        for (String course : courseList) {
            int courseCodeEndIdx = getCourseCodeEndIdx(course);
            if (courseCodeEndIdx == -1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            String courseCode = course.substring(0, courseCodeEndIdx);
            result.add(courseCode);
        }
        return result;
    }
    
    private int getCourseCodeEndIdx(String course){
        int len = course.length();
        for (int i = len - 2; i >= 0; i++){
            char tempChar = course.charAt(i);
            if (tempChar >= 48 &&  tempChar <= 57){
                return i + 1;
            }
        }
        return -1;
    }
    
}