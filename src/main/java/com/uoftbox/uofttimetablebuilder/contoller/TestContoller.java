package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.service.CourseDataService;
import com.uoftbox.uofttimetablebuilder.service.TimetableGeneratService;


@RestController
public class TestContoller {

    @Autowired
    private CourseDataService courseDataService;
    @Autowired
    private TimetableGeneratService timetableGeneratService;
    

    @PostMapping("/test/generateTimetable")
    public List<CourseInfo> generateTimetable(){
        Map<String, Map<String, Map<String, List<CourseInfo>>>> meetingSections = new HashMap<>();

        // 获取特定课程和章节的信息
        // String[] courseCode = { "CSC148H1", "BIO130H1","CHM136H1","CSC165H1","MAT136H1"};
        // String[] courseCode = {"CSCA08H3","MATA31H3","MATA22H3","MATA67H3","MATA37H3"};
        String[] courseCode = {"MAT102H5","MAT135H5","ISP100H5","ECO101H5","CSC108H5"};
        // String[] courseCode = {"ECO101H5"};
        String sectionCode = "F";

        meetingSections = courseDataService.fetchSpecialSections(courseCode, sectionCode);

        List<List<CourseInfo>> allTimetables = timetableGeneratService.generateAllTimetables(meetingSections);

        if (allTimetables.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        
        return allTimetables.get(0);
    }
}
