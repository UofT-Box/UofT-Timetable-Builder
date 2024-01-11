package com.uoftbox.uofttimetablebuilder.model.backend;

import java.util.List;

public class TimetableResultInfo {
    private Integer totalTimetableSize;
    private List<List<CourseInfo>> topTimetables;

    public TimetableResultInfo() {
    }
    public TimetableResultInfo(Integer totalTimetableSize, List<List<CourseInfo>> topTimetables) {
        this.totalTimetableSize = totalTimetableSize;
        this.topTimetables = topTimetables;
    }

    public Integer getTotalTimetableSize() {
        return totalTimetableSize;
    }
    public void setTotalTimetableSize(Integer totalTimetableSize) {
        this.totalTimetableSize = totalTimetableSize;
    }
    public List<List<CourseInfo>> getTopTimetables() {
        return topTimetables;
    }
    public void setTopTimetables(List<List<CourseInfo>> topTimetables) {
        this.topTimetables = topTimetables;
    }
}
