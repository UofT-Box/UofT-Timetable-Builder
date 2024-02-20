package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableResult;
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableResultInfo;
import com.uoftbox.uofttimetablebuilder.model.frontend.UserInput;
import com.uoftbox.uofttimetablebuilder.model.frontend.UserPreferences;
import com.uoftbox.uofttimetablebuilder.repository.courses.RelevantCourse;
import com.uoftbox.uofttimetablebuilder.service.TimetableService;
import com.uoftbox.uofttimetablebuilder.service.dbservice.SearchBarService;

@RestController
@CrossOrigin(origins = "*")
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
    public TimetableResult generateTimetable(@RequestBody UserInput userInput) throws InterruptedException, ExecutionException{
        List<String> fallCourseCode = userInput.getFallCourseCode();
        List<String> winterCourseCode = userInput.getWinterCourseCode();

        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setPreferredTimeIndex(userInput.getPreferredTimeIndex());
        userPreferences.setPreferredTimeWeight((double) userInput.getPreferredTimeWeight());
        userPreferences.setBalanceWeight((double)userInput.getBalanceWeight());
        userPreferences.setBreakTimeWeight((double)userInput.getBreakTimeWeight());
        userPreferences.setScoreThreshold(-100000.0);

        List<String> lockedCoursesFall = userInput.getLockedCoursesFall();
        List<String> lockedCoursesWinter = userInput.getLockedCoursesWinter();

        CompletableFuture<TimetableResultInfo> fallResult = timetableService.getTopTimetable(fallCourseCode, "F",userPreferences, lockedCoursesFall);
        CompletableFuture<TimetableResultInfo> winterResult = timetableService.getTopTimetable(winterCourseCode, "S",userPreferences, lockedCoursesWinter);
        CompletableFuture.allOf(fallResult,winterResult).join();

        TimetableResultInfo fallTimetableResult = fallResult.get();
        TimetableResultInfo winterTimetableResult = winterResult.get();
        List<List<CourseInfo>> fallTimetable = fallTimetableResult.getTopTimetables();
        List<List<CourseInfo>> winterTimetable = winterTimetableResult.getTopTimetables();

        TimetableResult timetableResult = new TimetableResult();
        timetableResult.setTotalFallTimetableSize(fallTimetableResult.getTotalTimetableSize());
        timetableResult.setTotalWinterTimetableSize(winterTimetableResult.getTotalTimetableSize());
        timetableResult.setResult(getResult(fallTimetable, winterTimetable));
        return timetableResult;
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
            if(i == 0){
                Collections.shuffle(lst1);
                Collections.shuffle(lst2);
            }
        }

        return result;
    }
}