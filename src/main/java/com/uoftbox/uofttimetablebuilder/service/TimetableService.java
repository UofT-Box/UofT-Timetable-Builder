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
import com.uoftbox.uofttimetablebuilder.service.dbservice.CourseDataService;

@Service
public class TimetableService {
    
    @Autowired
    private CourseDataService courseDataService;
    @Autowired
    private TimetableGeneratService timetableGeneratService;
    
    @Async("taskExecutor")
    public CompletableFuture<List<List<CourseInfo>>> getTopTimetable(List<String> courseCode, String sectionCode){
        
        Map<String, Map<String, Map<String, List<CourseInfo>>>> meetingSections = new HashMap<>();
        
        List<List<CourseInfo>> allTimetables;
        meetingSections = courseDataService.fetchSpecialSections(courseCode, sectionCode);
        allTimetables = timetableGeneratService.generateAllTimetables(meetingSections);
        
        if (allTimetables.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Collections.shuffle(allTimetables);
        
        return CompletableFuture.completedFuture(allTimetables);
    }
}
