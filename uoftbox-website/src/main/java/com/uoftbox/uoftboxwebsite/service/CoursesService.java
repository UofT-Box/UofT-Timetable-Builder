package com.uoftbox.uoftboxwebsite.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uoftbox.uoftboxwebsite.repository.courses.CoursesRepository;
import com.uoftbox.uoftboxwebsite.repository.courses.RelevantCourse;

@Service
public class CoursesService {
    
    @Autowired
    private CoursesRepository coursesRepository;

    public List<RelevantCourse> getRelevantCoursesInfo(String info){
        List<RelevantCourse> course = coursesRepository.findAllRelevantCourse(info);
        return course;
    }
}
