package com.uoftbox.uofttimetablebuilder.model.frontend;

import java.util.List;

public class UserInput {
    private List<String> winterCourseList;
    private List<String> fallCourseList;

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

    
}