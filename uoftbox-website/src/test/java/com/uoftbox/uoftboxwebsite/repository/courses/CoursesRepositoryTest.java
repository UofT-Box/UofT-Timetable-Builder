package com.uoftbox.uoftboxwebsite.repository.courses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CoursesRepositoryTest {

    @Autowired
    private CoursesRepository coursesRepository;

    @Test
    public void testFindMatchCourseId() {
        String expectCourseId = "645a3652fc79d44e5e5d1eae";
        String actualCourseId = coursesRepository.findMatchCourseId("MAT102H5", "F").getCourseId();
        assertEquals(expectCourseId,actualCourseId);
    }
}
