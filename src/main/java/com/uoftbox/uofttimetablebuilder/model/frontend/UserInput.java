package com.uoftbox.uofttimetablebuilder.model.frontend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserInput {
    private List<String> fallCourseList;
    private List<String> winterCourseList;
    private int preferredTimeIndex; // 0 代表早上, 1 代表中午, 2 代表晚上
    private int preferredTimeWeight; // 时间分布偏好的权重
    private int balanceWeight; // 课程均衡性的权重
    private int breakTimeWeight; // 课间时间的权重
    private List<String> lockedCoursesFall;
    private List<String> lockedCoursesWinter;

    public UserInput() {

    }

    public UserInput(List<String> fallCourseList, List<String> winterCourseList, int preferredTimeIndex,
            int preferredTimeWeight, int balanceWeight, int breakTimeWeight, List<String> lockedCoursesFall, List<String> lockedCoursesWinter) {
        this.fallCourseList = fallCourseList;
        this.winterCourseList = winterCourseList;
        this.preferredTimeIndex = preferredTimeIndex;
        this.preferredTimeWeight = preferredTimeWeight;
        this.balanceWeight = balanceWeight;
        this.breakTimeWeight = breakTimeWeight;
        this.lockedCoursesFall = lockedCoursesFall;
        this.lockedCoursesWinter = lockedCoursesWinter;
    }


    public List<String> getLockedCoursesFall() {
        return lockedCoursesFall;
    }

    public void setLockedCoursesFall(List<String> lockedCoursesFall) {
        this.lockedCoursesFall = lockedCoursesFall;
    }

    public List<String> getLockedCoursesWinter() {
        return lockedCoursesWinter;
    }

    public void setLockedCoursesWinter(List<String> lockedCoursesWinter) {
        this.lockedCoursesWinter = lockedCoursesWinter;
    }

    public List<String> getFallCourseList() {
        return fallCourseList;
    }

    public void setFallCourseList(List<String> fallCourseList) {
        this.fallCourseList = fallCourseList;
    }

    public List<String> getWinterCourseList() {
        return winterCourseList;
    }

    public void setWinterCourseList(List<String> winterCourseList) {
        this.winterCourseList = winterCourseList;
    }

    public int getPreferredTimeIndex() {
        return preferredTimeIndex;
    }

    public void setPreferredTimeIndex(int preferredTimeIndex) {
        this.preferredTimeIndex = preferredTimeIndex;
    }

    public int getPreferredTimeWeight() {
        return preferredTimeWeight;
    }

    public void setPreferredTimeWeight(int preferredTimeWeight) {
        this.preferredTimeWeight = preferredTimeWeight;
    }

    public int getBalanceWeight() {
        return balanceWeight;
    }

    public void setBalanceWeight(int balanceWeight) {
        this.balanceWeight = balanceWeight;
    }

    public int getBreakTimeWeight() {
        return breakTimeWeight;
    }

    public void setBreakTimeWeight(int breakTimeWeight) {
        this.breakTimeWeight = breakTimeWeight;
    }

    public List<String> getFallCourseCode() {
        return convertToCourseCode(fallCourseList);
    }

    public List<String> getWinterCourseCode() {
        return convertToCourseCode(winterCourseList);
    }

    private List<String> convertToCourseCode(List<String> courseList) {
        List<String> result = new ArrayList<>();
        for (String course : courseList) {
            int courseCodeEndIdx = getCourseCodeEndIdx(course);
            if (courseCodeEndIdx == -1)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Did not finde course code index");
            String courseCode = course.substring(0, courseCodeEndIdx);
            result.add(courseCode);
        }
        return result;
    }

    private int getCourseCodeEndIdx(String course) {
        int len = course.length();
        for (int i = len - 2; i >= 0; i++) {
            char tempChar = course.charAt(i);
            if (tempChar >= 48 && tempChar <= 57) {
                return i + 1;
            }
        }
        return -1;
    }

}