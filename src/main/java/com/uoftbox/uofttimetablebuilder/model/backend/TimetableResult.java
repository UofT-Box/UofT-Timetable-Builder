package com.uoftbox.uofttimetablebuilder.model.backend;

import java.util.List;

public class TimetableResult {
    private Integer totalFallTimetableSize;
    private Integer totalWinterTimetableSize;
    private List<List<List<CourseInfo>>> result;
    
    public TimetableResult() {
    }

    public TimetableResult(Integer totalFallTimetableSize, Integer totalWinterTimetableSize,
            List<List<List<CourseInfo>>> result) {
        this.totalFallTimetableSize = totalFallTimetableSize;
        this.totalWinterTimetableSize = totalWinterTimetableSize;
        this.result = result;
    }

    public Integer getTotalFallTimetableSize() {
        return totalFallTimetableSize;
    }

    public void setTotalFallTimetableSize(Integer totalFallTimetableSize) {
        this.totalFallTimetableSize = totalFallTimetableSize;
    }

    public Integer getTotalWinterTimetableSize() {
        return totalWinterTimetableSize;
    }

    public void setTotalWinterTimetableSize(Integer totalWinterTimetableSize) {
        this.totalWinterTimetableSize = totalWinterTimetableSize;
    }

    public List<List<List<CourseInfo>>> getResult() {
        return result;
    }

    public void setResult(List<List<List<CourseInfo>>> result) {
        this.result = result;
    }
    
}
