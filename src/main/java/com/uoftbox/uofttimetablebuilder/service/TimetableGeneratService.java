package com.uoftbox.uofttimetablebuilder.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uoftbox.uofttimetablebuilder.model.backend.CourseInfo;
import com.uoftbox.uofttimetablebuilder.model.backend.TimeAndPlace;
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableMetrics;
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableWithScore;
import com.uoftbox.uofttimetablebuilder.model.frontend.UserPreferences;
import com.uoftbox.uofttimetablebuilder.service.dbservice.CourseDataService;
import com.uoftbox.uofttimetablebuilder.service.dbservice.DistanceService;

@Service
public class TimetableGeneratService {

    @Autowired
    private CourseDataService courseDataService;
    @Autowired
    private DistanceService distanceService;
    
    public List<List<CourseInfo>> generateAllTimetables(Map<String, Map<String, Map<String, List<CourseInfo>>>> courses, UserPreferences userPreferences) {
        Queue<TimetableWithScore> queue = new PriorityQueue<>();
        TimetableMetrics metrics = new TimetableMetrics();
        
        List<CourseInfo> currentTimetable = new ArrayList<>();

        long timeStart = System.currentTimeMillis();
        generateTimetables(queue, metrics, courses, currentTimetable, userPreferences, 0, 0,timeStart);

        // 创建最终的课程表列表
        List<List<CourseInfo>> topTimetables = new ArrayList<>();

        // 从优先队列中提取课程表，并添加到列表中
        int counter = 0;
        while (!queue.isEmpty() && counter < 50) {
            TimetableWithScore tws = queue.poll();
            List<CourseInfo> ttb = tws.getTimetable();
            selectOptimalTuts(ttb);
            topTimetables.add(0,ttb);
            counter++;
        }
        // topTimetables.add()
        return topTimetables;
    }

    private void generateTimetables(Queue<TimetableWithScore> queue, TimetableMetrics metrics, Map<String, Map<String, Map<String, List<CourseInfo>>>> courses, List<CourseInfo> currentTimetable, UserPreferences userPreferences, int courseIndex, int typeIndex, long timeStart) {

        if (courseIndex == courses.size()) {
            double score = metrics.calculateScore(userPreferences);
            if (score > userPreferences.getScoreThreshold()) {
                queue.add(new TimetableWithScore(new ArrayList<>(currentTimetable), score));
            }
            return;
        }

        if (queue.size() >= 2000000){
            return;
        }

        long timeEnd = System.currentTimeMillis();
        if ((timeEnd - timeStart) > 10000){
            return;
        }


        List<String> courseKeys = new ArrayList<>(courses.keySet());
        String courseKey = courseKeys.get(courseIndex);
        Map<String, Map<String, List<CourseInfo>>> courseInfo = courses.get(courseKey);
        List<String> courseTypes = new ArrayList<>(courseInfo.keySet());

        if (typeIndex == courseTypes.size()) {
            generateTimetables(queue, metrics, courses, currentTimetable, userPreferences, courseIndex + 1, 0, timeStart);
            return;
        }

        String courseType = courseTypes.get(typeIndex);
        Map<String, List<CourseInfo>> sections = courseInfo.get(courseType);

        for (Map.Entry<String, List<CourseInfo>> entry : sections.entrySet()) {
            List<CourseInfo> sectionInfo = entry.getValue();

            boolean conflict = sectionInfo.stream().anyMatch(classInfo -> isConflict(classInfo, currentTimetable));

            if (!conflict) {
                
                TimetableMetrics savedMetrics = new TimetableMetrics(metrics); // 保存当前状态

                for (CourseInfo course : sectionInfo) {
                    for (TimeAndPlace tap : course.getTimeAndPlaceList()) {
                        metrics.updateTimeDistribution(tap);
                        metrics.updateDailyCourseCounts(course);
                        metrics.updateDailyBreakTimes(course); // 更新课间时间
                    }
                }

                currentTimetable.addAll(sectionInfo);
                generateTimetables(queue, metrics, courses, currentTimetable, userPreferences, courseIndex, typeIndex + 1, timeStart);

                metrics.restore(savedMetrics); // 回溯，还原状态
                currentTimetable.removeAll(sectionInfo);
            }
        }
    }

    private boolean isConflict(CourseInfo newCourse, List<CourseInfo> timetable) {
        for (CourseInfo existingCourse : timetable) {

            String newCourseCode = newCourse.getCourse();
            String existingCourseCode = existingCourse.getCourse();
            int campusesCode = Character.getNumericValue(newCourseCode.charAt(newCourseCode.length() - 1));
            int campusesExistingCode = Character.getNumericValue(existingCourseCode.charAt(existingCourseCode.length() - 1));

            if (campusesCode != campusesExistingCode){
                if (checkDifferentCompusesConflict(newCourse, existingCourse)) {
                    return true;
                }
            }else{
                if (checkSameCompusesConflict(newCourse, existingCourse, campusesCode)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAdd(TimeAndPlace newTimeSlot, TimeAndPlace existingTimeSlot){
        if ((existingTimeSlot.getStart() < newTimeSlot.getEnd() && newTimeSlot.getEnd() < existingTimeSlot.getEnd()) || 
            (existingTimeSlot.getStart() < newTimeSlot.getStart() && newTimeSlot.getStart() < existingTimeSlot.getEnd()) ||
            (newTimeSlot.getStart() < existingTimeSlot.getEnd() && existingTimeSlot.getEnd() < newTimeSlot.getEnd()) || 
            (newTimeSlot.getStart() < existingTimeSlot.getStart() && existingTimeSlot.getStart() < newTimeSlot.getEnd())){
                return false;
            }
        if(existingTimeSlot.getStart() == newTimeSlot.getStart() && existingTimeSlot.getEnd() == newTimeSlot.getEnd()){
            return false;
        }
        return true;
    }
    
    private boolean enoughWalkingTimeUTSG(TimeAndPlace newTimeSlot, TimeAndPlace existingTimeSlot){
        String newLocation = newTimeSlot.getLocation();
        String exisitingLocation = existingTimeSlot.getLocation();
        if (exisitingLocation.equals("") || newLocation.equals("") || newLocation.equals(exisitingLocation)){
            return true;
        }
        if (existingTimeSlot.getEnd() == newTimeSlot.getStart()){
            Integer timeTake = distanceService.getDuration(existingTimeSlot.getLocation(), newTimeSlot.getLocation());
            if (timeTake > 10){
                return false;
            }
        }
        return true;
    }

    private boolean checkSameCompusesConflict(CourseInfo newCourse, CourseInfo existingCourse, int campusesCode){
        List<TimeAndPlace> newCourseTimeSlots = newCourse.getTimeAndPlaceList();
        List<TimeAndPlace> existingCourseTimeSlots = existingCourse.getTimeAndPlaceList();

        for (TimeAndPlace newTimeSlot : newCourseTimeSlots) {
            for (TimeAndPlace existingTimeSlot : existingCourseTimeSlots) {
                if (newTimeSlot.getDay() == existingTimeSlot.getDay()) {
                    if(!canAdd(newTimeSlot, existingTimeSlot)){
                        return true;
                    }
                    // if(campusesCode == 1 && !enoughWalkingTimeUTSG(newTimeSlot, existingTimeSlot)){
                    //     return true;
                    // }
                }
            }
        }
        return false;
    }

    private boolean checkDifferentCompusesConflict(CourseInfo course1, CourseInfo course2) {
        List<TimeAndPlace> timeSlots1 = course1.getTimeAndPlaceList();
        List<TimeAndPlace> timeSlots2 = course2.getTimeAndPlaceList();
        
        for (TimeAndPlace timeSlot1 : timeSlots1) {
            for (TimeAndPlace timeSlot2 : timeSlots2) {
                if (timeSlot1.getDay() == timeSlot2.getDay()) {
                    int startTime1 = timeSlot1.getStart();
                    int endTime1 = timeSlot1.getEnd();
                    int startTime2 = timeSlot2.getStart();
                    int endTime2 = timeSlot2.getEnd();

                    if (canAdd(timeSlot1, timeSlot2) || 
                    Math.abs(startTime1 - endTime2) < 3600000 || Math.abs(startTime2 - endTime1) < 3600000) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void selectOptimalTuts(List<CourseInfo> timetable) {
        for (int i = 0; i < timetable.size(); i++) {
            CourseInfo currentCourse = timetable.get(i);
            if (currentCourse.getSection().startsWith("TUT")) {
                CourseInfo optimalTut = findOptimalTut(currentCourse, timetable, i);
                if (optimalTut != null) {
                    timetable.set(i, optimalTut);
                }
            }
        }
    }

    private CourseInfo findOptimalTut(CourseInfo currentTut, List<CourseInfo> timetable, int tutIndex) {
        int day = currentTut.getTimeAndPlaceList().get(0).getDay();
        int start = currentTut.getTimeAndPlaceList().get(0).getStart();
        Map<String, List<CourseInfo>> alternativeTuts = courseDataService.fetchAllTuts(currentTut.getCourse(), day, start);

        CourseInfo optimalTut = currentTut;
        double shortestDistance = Double.MAX_VALUE;

        for (List<CourseInfo> tuts : alternativeTuts.values()) {
            for (CourseInfo tut : tuts) {
                if (tutIndex > 0) {
                    CourseInfo previousCourse = timetable.get(tutIndex - 1);
                    String origin = previousCourse.getTimeAndPlaceList().get(0).getLocation();
                    String destination = tut.getTimeAndPlaceList().get(0).getLocation();
                    Integer distance = distanceService.getDistance(origin,destination);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        optimalTut = tut;
                    }
                }
            }
        }

        return optimalTut;
    }
    
}