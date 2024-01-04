package com.uoftbox.uofttimetablebuilder.service.dbservice;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uoftbox.uofttimetablebuilder.repository.courses.CoursesRepository;
import com.uoftbox.uofttimetablebuilder.repository.courses.RelevantCourse;

@Service
public class SearchBarService {
    
    @Autowired
    private CoursesRepository coursesRepository;

    public List<RelevantCourse> getRelevantCoursesInfo(String info){
        List<RelevantCourse> course = coursesRepository.findAllRelevantCourse(info);
        return course;
    }

}
