package com.uoftbox.uofttimetablebuilder.model.frontend;

public class InputCourseInfo {
    private String couseName;
    private String sectionCode;

    public InputCourseInfo() {
    }
    public InputCourseInfo(String couseName, String sectionCode) {
        this.couseName = couseName;
        this.sectionCode = sectionCode;
    }
    public String getCouseName() {
        return couseName;
    }
    public void setCouseName(String couseName) {
        this.couseName = couseName;
    }
    public String getSectionCode() {
        return sectionCode;
    }
    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }
    
}
