package com.uoftbox.uoftboxwebsite.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MeetingSections {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name="course_id")
    private String courseId;
    @Column(name="course_code")
    private String courseCode;
    @Column(name="section_code")
    private String sectionCode;
    @Column(name="name")
    private String name;
    @Column(name="instructors")
    private String instructors;
    @Column(name="times")
    private String times;
    @Column(name="size")
    private Integer size;
    @Column(name="enrolment")
    private Integer enrolment;
    @Column(name="notes")
    private String notes;

    public MeetingSections() {

    }
    
    public MeetingSections(Integer id, String courseId, String courseCode, String sectionCode, String name,
            String instructors, String times, Integer size, Integer enrolment, String notes) {
        this.id = id;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.sectionCode = sectionCode;
        this.name = name;
        this.instructors = instructors;
        this.times = times;
        this.size = size;
        this.enrolment = enrolment;
        this.notes = notes;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getCourseId() {
        return courseId;
    }
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    public String getCourseCode() {
        return courseCode;
    }
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }
    public String getSectionCode() {
        return sectionCode;
    }
    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public Integer getEnrolment() {
        return enrolment;
    }
    public void setEnrolment(Integer enrolment) {
        this.enrolment = enrolment;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
}
