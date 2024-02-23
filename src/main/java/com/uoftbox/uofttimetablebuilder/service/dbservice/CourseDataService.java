package com.uoftbox.uofttimetablebuilder.service.dbservice;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.model.backend.CourseTime;
import com.uoftbox.uofttimetablebuilder.model.backend.TimeAndPlace;
import com.uoftbox.uofttimetablebuilder.model.mysql.MeetingSections;
import com.uoftbox.uofttimetablebuilder.repository.courses.CoursesRepository;
import com.uoftbox.uofttimetablebuilder.repository.meetingsections.MeetingSectionsRepository;

@Service
public class CourseDataService {

    @Autowired
    private CoursesRepository coursesRepository;
    
    @Autowired
    private MeetingSectionsRepository meetingSectionsRepository;

    public List<CourseTime> fethCourseTimes(String courseCode, String sectionCode){
        String courseId = coursesRepository.findMatchCourseId(courseCode, sectionCode);

        List<MeetingSections> meetingSections = meetingSectionsRepository.findMeetingSectionsByCourseId(courseId);
        List<CourseTime> courseTimes = new ArrayList<>();
        for (MeetingSections meetingSection : meetingSections) {
            String lecture = meetingSection.getSectionCode();
            String instructors = meetingSection.getInstructors();
            String times = meetingSection.getTimes();
            Integer size = meetingSection.getSize();
            String notes = meetingSection.getNotes();
            CourseTime tempCourseTime = new CourseTime(lecture, instructors, times, size, notes);
            courseTimes.add(tempCourseTime);
        }
        return courseTimes;
    }

    public Map<String, Map<String, Map<String, List<CourseInfo>>>> fetchSpecialSections(List<String> courseCodeList, String sectionCode, List<String> lockedCourses){
        // 创建用于存储原始课程信息的映射
        Map<String, Map<String, Map<String, List<CourseInfo>>>> originalCourseSchedule = new HashMap<>();
        Map<String, Set<String>> courseSections = getLockedCourseSections(lockedCourses);

        for (String courseCode : courseCodeList) {
            String courseId = coursesRepository.findMatchCourseId(courseCode, sectionCode);

            // 如果courseId为空，则返回404报错
            if (courseCode == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "COURSE NOT FOUND");
            ArrayList<Integer> time_list = new ArrayList<>();
            fetchSpecialSectionsHelper(courseCode, courseId, time_list, originalCourseSchedule, courseSections);
        }
        return originalCourseSchedule;
    }

    public Map<String, Map<String, CourseInfo>> fetchLockSections(String seasonCode, List<String> lockedCourses) {
        Map<String, Map<String, CourseInfo>> lockedCourseSchedule = new HashMap<>();

        for (String lockedCourse : lockedCourses) {
            String[] parts = lockedCourse.split("<br>");
            if (parts.length != 2) {
                continue;
            }
            String courseCode = parts[0];
            String sectionCode = parts[1];

            // 获取课程ID
            String courseId = coursesRepository.findMatchCourseId(courseCode, seasonCode);
            if (courseId == null) {
                continue;
            }

            // 获取课程的会议节次信息
            List<MeetingSections> meetingSections = meetingSectionsRepository.findTimeByCouseAndSectionName(courseCode, sectionCode, courseId);

            // 将会议节次信息转换为时间和地点对象列表
            List<TimeAndPlace> timeAndPlaceList = new ArrayList<>();
            CourseInfo courseInfo = new CourseInfo();
            courseInfo.setCourse(courseCode);
            courseInfo.setSection(sectionCode);
            for (MeetingSections section : meetingSections) {
                String tims_jsonString = section.getTimes();
                String prof = section.getInstructors();
                courseInfo.setProf(prof);

                Gson gson = new Gson();
                Type listType = new TypeToken<List<TimeAndPlace>>() {}.getType();
                List<TimeAndPlace> sectionTimeAndPlaceList = gson.fromJson(tims_jsonString, listType);

                timeAndPlaceList.addAll(sectionTimeAndPlaceList);
            }
            courseInfo.setTimeAndPlaceList(timeAndPlaceList);
            lockedCourseSchedule.computeIfAbsent(courseCode, k -> new HashMap<>())
                               .put(sectionCode, courseInfo);
        }

        return lockedCourseSchedule;
    }

    public Map<String, List<CourseInfo>> fetchAllTuts(String courseCode, int day, int start){
        
        Map<String, List<CourseInfo>> allTuts = new HashMap<>();

        // 构建 LIKE 操作符的模式，匹配包含指定 day 和 start 的 JSON 字符串
        String timePattern = "%\"day\": " + day + ", \"start\": " + start + "%";

        // 从数据库获取数据
        List<MeetingSections> sameTimeTUT = meetingSectionsRepository.findSameTimeTUTForCourse(courseCode, timePattern);
        if (sameTimeTUT.size() == 0){
            return allTuts;
        }

        for (MeetingSections section : sameTimeTUT) {
            String sectionCode = section.getSectionCode();
            String prof = section.getInstructors();
            String timesJsonString = section.getTimes();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<TimeAndPlace>>() {}.getType();
            List<TimeAndPlace> timeAndPlaceList = gson.fromJson(timesJsonString, listType);

            CourseInfo courseInfo = new CourseInfo(courseCode, sectionCode, prof, timeAndPlaceList);

            if (allTuts.containsKey(sectionCode)){
                allTuts.computeIfAbsent(sectionCode, k -> new ArrayList<>()).add(courseInfo);
            }
        }
        return allTuts;
    }

    public Map<String, Set<String>> getLockedCourseSections(List<String> lockedCourses) {
        Map<String, Set<String>> lockedCourseSections = new HashMap<>();
        
        for (String lockedCourse : lockedCourses) {
            String[] parts = lockedCourse.split("<br>");
            if (parts.length != 2) {
                // 如果格式不正确，跳过
                continue;
            }
            String courseCode = parts[0];
            String sectionCode = parts[1].substring(0, 3);

            // 添加锁定的节次到相应课程的集合中
            lockedCourseSections.computeIfAbsent(courseCode, k -> new HashSet<>()).add(sectionCode);
        }
        return lockedCourseSections;
    }


    private void fetchSpecialSectionsHelper(String courseCode, String courseId, ArrayList<Integer> time_list, Map<String, Map<String, Map<String, List<CourseInfo>>>> courseSchedule, Map<String, Set<String>> courseSections){
        List<MeetingSections> meetingSections = meetingSectionsRepository.findMeetingSectionsByCourseId(courseId);

        List<String> lockedSections = new ArrayList<>();

        if (courseSections.containsKey(courseCode)) {
            lockedSections.addAll(courseSections.get(courseCode));
        }

        // 如果meetingSections为空，则返回404报错
        if (meetingSections.size() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SECTION NOT FOUND");
        for (MeetingSections section : meetingSections) {
            List<CourseInfo> courseinfos = new ArrayList<>();
            String courseNumber = section.getSectionCode();
            String prof = section.getInstructors();
            String courseType = courseNumber.substring(0, 3);
            if (lockedSections.contains(courseType)) {
                continue;
            }
            
            String tims_jsonString = section.getTimes();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<TimeAndPlace>>() {
            }.getType();
            List<TimeAndPlace> timeAndPlaceList = gson.fromJson(tims_jsonString, listType);
            boolean isPresent = false;
            if (timeAndPlaceList.size() == 0){
                continue;
            }
            
            if (courseType.equals("TUT")){
                for (int element : time_list) {
                    if (element == timeAndPlaceList.get(0).getStart()) {
                        isPresent = true;
                        break; // 找到了目标元素，可以提前退出循环
                    }
                }
            }
            
            if(!isPresent){
                CourseInfo courseinfo = new CourseInfo(courseCode, courseNumber, prof, timeAndPlaceList);
                courseinfos.add(courseinfo);

                courseSchedule.computeIfAbsent(courseCode, k -> new HashMap<>())
                        .computeIfAbsent(courseType, k -> new HashMap<>())
                        .computeIfAbsent(courseNumber, k -> courseinfos);
                time_list.add(timeAndPlaceList.get(0).getStart());
            }
        }   
    }
}
