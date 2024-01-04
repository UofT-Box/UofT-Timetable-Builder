package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.model.frontend.UserInput;
import com.uoftbox.uofttimetablebuilder.repository.courses.RelevantCourse;
import com.uoftbox.uofttimetablebuilder.service.TimetableService;
import com.uoftbox.uofttimetablebuilder.service.dbservice.SearchBarService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TimetableContoller {

    @Autowired
    private SearchBarService coursesService;
    @Autowired
    private TimetableService timetableService;

    //@RequestParam("courseInput") String courseName
    @PostMapping("/course-input")
    public List<RelevantCourse> getRelevantCourses(@RequestParam("courseInput") String info) {
        List<RelevantCourse> relevantCourses = coursesService.getRelevantCoursesInfo(info);
        return relevantCourses;
    }

    @PostMapping("/generateTimetable")
    public List<List<CourseInfo>> generateTimetableAsnyc(@RequestBody UserInput userInput) throws InterruptedException, ExecutionException{
        List<String> fallCourseCode = userInput.getFallCourseCode();
        List<String> winterCourseCode = userInput.getWinterCourseCode();
        
        CompletableFuture<List<CourseInfo>> fallTimetable = timetableService.getTopTimetable(fallCourseCode, "F");
        CompletableFuture<List<CourseInfo>> winterTimetable = timetableService.getTopTimetable(winterCourseCode, "S");
        CompletableFuture.allOf(fallTimetable,winterTimetable).join();
        log.info("Generate Seccess");
        List<List<CourseInfo>> result = new ArrayList<>(Arrays.asList(fallTimetable.get(), winterTimetable.get()));
        return result;
        
    }
    
}
