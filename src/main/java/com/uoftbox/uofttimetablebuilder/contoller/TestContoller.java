package com.uoftbox.uofttimetablebuilder.contoller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.service.TimetableService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestContoller {

    @Autowired
    private TimetableService timetableService;


    @PostMapping("/test/generateTimetable")
    public List<CourseInfo> generateTimetableAsnyc() throws InterruptedException, ExecutionException{
        long start = System.currentTimeMillis();
        // 获取特定课程和章节的信息
        // String[] courseCode = { "CSC148H1", "BIO130H1","CHM136H1","CSC165H1","MAT136H1"};
        // String[] courseCode = {"CSCA08H3","MATA31H3","MATA22H3","MATA67H3","MATA37H3"};
        String[] courseCode = {"MAT102H5","MAT135H5","ISP100H5","ECO101H5","CSC108H5"};
        // String[] courseCode = {"ECO101H5"};
        String sectionCode = "F";
        
        CompletableFuture<List<CourseInfo>> fallTimetable = timetableService.getTopOneTimetableAsync(courseCode, sectionCode);
        CompletableFuture<List<CourseInfo>> winterTimetable = timetableService.getTopOneTimetableAsync(courseCode, sectionCode);
        CompletableFuture.allOf(fallTimetable,winterTimetable).join();
        long end = System.currentTimeMillis();
        log.info("Asnyc time take: " + (end-start) / 1000);
        return fallTimetable.get();
        
    }
}
