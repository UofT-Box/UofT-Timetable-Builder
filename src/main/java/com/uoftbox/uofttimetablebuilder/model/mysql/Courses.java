package com.uoftbox.uofttimetablebuilder.model.mysql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Courses {
    
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
    @Column(name="description")
    private String description;
    @Column(name="division")
    private String division;
    @Column(name="department")
    private String department;
    @Column(name="prerequisites")
    private String prerequisites;
    @Column(name="exclusions")
    private String exclusions;
    @Column(name="campus")
    private String campus;
    @Column(name="sessions")
    private String sessions;

    public Courses() {

    }
    
    public Courses(Integer id, String courseId, String courseCode, String sectionCode, String name, String description,
            String division, String department, String prerequisites, String exclusions, String campus,
            String sessions) {
        this.id = id;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.sectionCode = sectionCode;
        this.name = name;
        this.description = description;
        this.division = division;
        this.department = department;
        this.prerequisites = prerequisites;
        this.exclusions = exclusions;
        this.campus = campus;
        this.sessions = sessions;
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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDivision() {
        return division;
    }
    public void setDivision(String division) {
        this.division = division;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getPrerequisites() {
        return prerequisites;
    }
    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }
    public String getExclusions() {
        return exclusions;
    }
    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }
    public String getCampus() {
        return campus;
    }
    public void setCampus(String campus) {
        this.campus = campus;
    }
    public String getSessions() {
        return sessions;
    }
    public void setSessions(String sessions) {
        this.sessions = sessions;
    }
}
