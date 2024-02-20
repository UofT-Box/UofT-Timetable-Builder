package com.uoftbox.uofttimetablebuilder.repository.meetingsections;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uoftbox.uofttimetablebuilder.model.mysql.MeetingSections;

@Repository
public interface MeetingSectionsRepository extends JpaRepository<MeetingSections, Integer>{
    @Query("SELECT m FROM MeetingSections m WHERE m.courseId = ?1")
    List<MeetingSections> findMeetingSectionsByCourseId(String courseId);
    
    @Query("SELECT m FROM MeetingSections m WHERE m.courseCode = ?1 AND m.times LIKE %?2%")
    List<MeetingSections> findSameTimeTUTForCourse(String courseCode, String timePattern);

    @Query("SELECT m FROM MeetingSections m WHERE m.courseCode = ?1 AND m.sectionCode = ?2 AND m.courseId = ?3")
    List<MeetingSections> findTimeByCouseAndSectionName(String courseCode, String sectionCode, String courseId);
}