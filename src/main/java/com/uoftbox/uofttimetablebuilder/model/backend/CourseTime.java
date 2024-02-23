package com.uoftbox.uofttimetablebuilder.model.backend;

public class CourseTime {
    private String sectionCode;
    private String instructors;
    private String times;
    private Integer size;
    private String notes;

    public CourseTime() {
    }
    public CourseTime(String sectionCode, String instructors, String times, Integer size, String notes) {
        this.sectionCode = sectionCode;
        this.instructors = instructors;
        this.times = times;
        this.size = size;
        this.notes = notes;
    }
    public String getSectionCode() {
        return sectionCode;
    }
    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }
    public String getInstructors() {
        return instructors;
    }
    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }
    public String getTimes() {
        return times;
    }
    public void setTimes(String times) {
        this.times = times;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    
}
