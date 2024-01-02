package com.github.zhengaugust;

import java.util.List;

public class CourseInfo {
    private String course;
    private String section;
    private List<TimeAndPlace> timeAndPlaceList;
    
    // 构造函数
    public CourseInfo(String course, String section, List<TimeAndPlace> timeAndPlaceList) {
        this.course = course;
        this.section = section;
        this.timeAndPlaceList = timeAndPlaceList;
    }

    // Getter 方法
    public String getCourse() {
        return course;
    }

    public String getSection() {
        return section;
    }

    public List<TimeAndPlace> getTime() {
        return timeAndPlaceList;
    }

    public List<TimeAndPlace> getTimeAndPlaceList() {
        return timeAndPlaceList;
    }
}

