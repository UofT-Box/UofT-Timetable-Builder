package com.uoftbox.uofttimetablebuilder.service;

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
    public CompletableFuture<TimetableResultInfo> getTopTimetable(List<String> courseCode, String sectionCode, UserPreferences userPreferences, List<String> lockedCourses){
        
        Map<String, Map<String, Map<String, List<CourseInfo>>>> meetingSections = new HashMap<>();
        
        TimetableResultInfo timetableResult;
        meetingSections = courseDataService.fetchSpecialSections(courseCode, sectionCode, lockedCourses);
        Map<String, Map<String, CourseInfo>> lockedSections = courseDataService.fetchLockSections(sectionCode,lockedCourses);
        timetableResult = timetableGeneratService.generateAllTimetables(meetingSections, userPreferences, lockedSections);
        List<List<CourseInfo>> allTimetables = timetableResult.getTopTimetables();
        
        if (allTimetables.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No timetable find");
        
        return CompletableFuture.completedFuture(timetableResult);
    }
}
