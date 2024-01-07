package com.uoftbox.uofttimetablebuilder.model.backend;

import java.util.List;

public class TimetableWithScore implements Comparable<TimetableWithScore>{
    private List<CourseInfo> timetable;
    private double score;

    public TimetableWithScore(List<CourseInfo> timetable, double score) {
        this.timetable = timetable;
        this.score = score;
    }

    // Getters

    public List<CourseInfo> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<CourseInfo> timetable) {
        this.timetable = timetable;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(TimetableWithScore other) {
        // 按分数降序排列
        return Double.compare(other.score, this.score);
    }
}
