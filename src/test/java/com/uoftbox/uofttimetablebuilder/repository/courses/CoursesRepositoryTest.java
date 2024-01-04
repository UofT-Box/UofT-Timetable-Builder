package com.uoftbox.uofttimetablebuilder.repository.courses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CoursesRepositoryTest {
    
    @Autowired
    private CoursesRepository coursesRepository;
    
    @Test
    void testFindMatchCourseId() {
        String expect = "6445bd58c1e5fe0004158f49";
        String actual = coursesRepository.findMatchCourseId("MAT137Y1", "F");
        assertEquals(expect, actual);
    }
}
