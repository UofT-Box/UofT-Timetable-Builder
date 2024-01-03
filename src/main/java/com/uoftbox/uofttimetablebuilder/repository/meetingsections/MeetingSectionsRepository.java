package com.uoftbox.uofttimetablebuilder.repository.meetingsections;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uoftbox.uofttimetablebuilder.model.mysql.MeetingSections;

@Repository
public interface MeetingSectionsRepository extends JpaRepository<MeetingSections, Integer>{
    @Query("SELECT m FROM MeetingSections m WHERE m.courseId = ?1")
    List<MeetingSections> findMeetingSectionsByCourseId(String CourseId);
    
    @Query("SELECT m FROM MeetingSections m WHERE m.courseCode = %?1% AND m.times LIKE %?2%")
    List<MeetingSections> findSameTimeTUTForCourse(String CourseCode, String timePattern);
}