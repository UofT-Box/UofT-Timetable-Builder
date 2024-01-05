package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public List<List<List<CourseInfo>>> generateTimetable(@RequestBody UserInput userInput) throws InterruptedException, ExecutionException{
        List<String> fallCourseCode = userInput.getFallCourseCode();
        List<String> winterCourseCode = userInput.getWinterCourseCode();
        
        CompletableFuture<List<List<CourseInfo>>> fallTimetable = timetableService.getTopTimetable(fallCourseCode, "F");
        CompletableFuture<List<List<CourseInfo>>> winterTimetable = timetableService.getTopTimetable(winterCourseCode, "S");
        CompletableFuture.allOf(fallTimetable,winterTimetable).join();

        
        List<List<CourseInfo>> fallTimetableResult = fallTimetable.get();
        List<List<CourseInfo>> winterTimetableResult = winterTimetable.get();

        return getResult(fallTimetableResult, winterTimetableResult);
    }
    
    private List<List<List<CourseInfo>>> getResult(List<List<CourseInfo>> fallTimetableResult, List<List<CourseInfo>> winterTimetableResult){

        int fallTimetableSize = fallTimetableResult.size();
        int winterTimetableSize = winterTimetableResult.size();

        if (fallTimetableSize > winterTimetableSize){
            balancingList(fallTimetableResult, winterTimetableResult);
        }else if (fallTimetableSize < winterTimetableSize){
            balancingList(winterTimetableResult, fallTimetableResult);
        }

        
        return generateResult(fallTimetableResult, winterTimetableResult);
    }

    private void balancingList(List<List<CourseInfo>> target, List<List<CourseInfo>> needAdd){
        while (target.size() > needAdd.size()) {
            List<List<CourseInfo>> temp = new ArrayList<>(needAdd);
            Collections.shuffle(temp);
            needAdd.addAll(temp);
        }
    }

    private List<List<List<CourseInfo>>> generateResult(List<List<CourseInfo>> lst1, List<List<CourseInfo>> lst2){
        List<List<List<CourseInfo>>> result = new ArrayList<>();
        
        int innerSize1 = lst1.size();
        int innerSize2 = lst2.size();

        if (innerSize1 == 0 && innerSize2 == 0){
            return result;
        }

        int maxLen = Math.min(lst1.size(), lst2.size());
        for (int i = 0; i < maxLen; i++){
            result.add(Arrays.asList(lst1.get(i), lst2.get(i)));
        }

        return result;
    }
}
