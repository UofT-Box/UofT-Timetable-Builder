package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uoftbox.uofttimetablebuilder.repository.courses.RelevantCourse;
import com.uoftbox.uofttimetablebuilder.service.SearchBarService;


@RestController
public class TimetableContoller {

    @Autowired
    private SearchBarService coursesService;

    //@RequestParam("courseInput") String courseName
    @PostMapping("/course-input")
    public List<RelevantCourse> getRelevantCourses(@RequestParam("courseInput") String info) {
        List<RelevantCourse> relevantCourses = coursesService.getRelevantCoursesInfo(info);
        return relevantCourses;
    }

    // @PostMapping("/get/timetable")
    // public List<CourseDetail> getTimetable(@RequestBody UserInput requestBody) {
    //     List<CourseDetail> timetable = new ArrayList<>();
    //     //TODO: add result to timetable

    //     return timetable;
    // }
    
}
