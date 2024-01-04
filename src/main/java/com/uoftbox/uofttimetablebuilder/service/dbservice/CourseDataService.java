package com.uoftbox.uofttimetablebuilder.service.dbservice;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
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

    public Map<String, Map<String, Map<String, List<CourseInfo>>>> fetchSpecialSections(String[] courseCodeList, String sectionCode){
        Map<String, Map<String, Map<String, List<CourseInfo>>>> courseSchedule = new HashMap<>();
        for (String courseCode : courseCodeList) {
            String courseId = coursesRepository.findMatchCourseId(courseCode, sectionCode);

            // 如果courseId为空，则返回404报错
            if (courseCode == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "COURSE NOT FOUND");
            ArrayList<Integer> time_list = new ArrayList<>();
            fetchSpecialSectionsHelper(courseCode, courseId, time_list, courseSchedule);
        }
        return courseSchedule;
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
                // List<CourseInfo> temp = new ArrayList<>();
                // temp.add(courseInfo);
                // allTuts.put(sectionCode,temp);
                allTuts.computeIfAbsent(sectionCode, k -> new ArrayList<>()).add(courseInfo);
            }
        }
        return allTuts;
    }

    private void fetchSpecialSectionsHelper(String courseCode, String courseId, ArrayList<Integer> time_list, Map<String, Map<String, Map<String, List<CourseInfo>>>> courseSchedule){
        List<MeetingSections> meetingSections = meetingSectionsRepository.findMeetingSectionsByCourseId(courseId);

        // 如果meetingSections为空，则返回404报错
        if (meetingSections.size() == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SECTION NOT FOUND");
        for (MeetingSections section : meetingSections) {
            List<CourseInfo> courseinfos = new ArrayList<>();
            String courseNumber = section.getSectionCode();
            String prof = section.getInstructors();
            String courseType = courseNumber.substring(0, 3);
            
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
