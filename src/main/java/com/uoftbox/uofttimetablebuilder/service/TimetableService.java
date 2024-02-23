package com.uoftbox.uofttimetablebuilder.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableResultInfo;
import com.uoftbox.uofttimetablebuilder.model.frontend.UserPreferences;
import com.uoftbox.uofttimetablebuilder.service.dbservice.CourseDataService;

@Service
public class TimetableService {

    @Autowired
    private CourseDataService courseDataService;
    @Autowired
    private TimetableGeneratService timetableGeneratService;

    @Async("taskExecutor")
    public CompletableFuture<TimetableResultInfo> getTopTimetable(List<String> courseCode, String sectionCode,
            UserPreferences userPreferences, List<String> lockedCourses, boolean returnTime) {
                
        TimetableResultInfo timetableResult = genTopTimetables(courseCode, sectionCode, userPreferences, lockedCourses, returnTime);
        List<List<CourseInfo>> allTimetables = timetableResult.getTopTimetables();
        
        
        for(int i = 0; i < 5; i ++) {
            if(!allTimetables.isEmpty()){
                break;
            }
            Collections.shuffle(courseCode);
            timetableResult = genTopTimetables(courseCode, sectionCode, userPreferences, lockedCourses, returnTime);
            allTimetables = timetableResult.getTopTimetables();
        }

        if (allTimetables.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No timetable find");

        return CompletableFuture.completedFuture(timetableResult);
    }

    public List<List<CourseInfo>> updateTimetable(List<String> courseCode, String sectionCode,
    UserPreferences userPreferences, List<String> targetCourse){

        TimetableResultInfo timetableResult = genTopTimetables(courseCode, sectionCode, userPreferences, courseCode, true);
        List<List<CourseInfo>> allTimetables = timetableResult.getTopTimetables();
        
        
        for(int i = 0; i < 5; i ++) {
            if(!allTimetables.isEmpty()){
                break;
            }
            Collections.shuffle(courseCode);
            timetableResult = genTopTimetables(courseCode, sectionCode, userPreferences, courseCode, true);
            allTimetables = timetableResult.getTopTimetables();
        }

        if (allTimetables.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No timetable find");

        return allTimetables;
    }

    private TimetableResultInfo genTopTimetables(List<String> courseCode, String sectionCode,
    UserPreferences userPreferences, List<String> lockedCourses, boolean isUpdate){

        Map<String, Map<String, Map<String, List<CourseInfo>>>> meetingSections = new HashMap<>();

        TimetableResultInfo timetableResult;
        meetingSections = courseDataService.fetchSpecialSections(courseCode, sectionCode, lockedCourses);
        Map<String, Map<String, CourseInfo>> lockedSections = courseDataService.fetchLockSections(sectionCode,
                lockedCourses);
        timetableResult = timetableGeneratService.generateAllTimetables(meetingSections, userPreferences,
                lockedSections, isUpdate);

        return timetableResult;
    }
}
