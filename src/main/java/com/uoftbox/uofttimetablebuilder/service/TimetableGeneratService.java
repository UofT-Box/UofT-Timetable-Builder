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
import com.uoftbox.uofttimetablebuilder.model.backend.TimetableResultInfo;
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

    public TimetableResultInfo generateAllTimetables(Map<String, Map<String, Map<String, List<CourseInfo>>>> courses,
            UserPreferences userPreferences, Map<String, Map<String, CourseInfo>> lockedSections) {
        Queue<TimetableWithScore> queue = new PriorityQueue<>();
        TimetableMetrics metrics = new TimetableMetrics();

        List<CourseInfo> currentTimetable = new ArrayList<>();
        CourseInfo[][] timetable = new CourseInfo[5][26];

        for (Map<String, CourseInfo> courseSections : lockedSections.values()) {
            for (CourseInfo courseInfo : courseSections.values()) {
                for (TimeAndPlace timeAndPlace : courseInfo.getTimeAndPlaceList()) {
                    int dayIndex = timeAndPlace.getDay() - 1;
                    int startSlot = (int) ((timeAndPlace.getStart() - 32400000) / 1800000);
                    timetable[dayIndex][startSlot] = courseInfo;
                }
                currentTimetable.add(courseInfo);
            }
        }

        long timeStart = System.currentTimeMillis();
        generateTimetables(timetable, queue, metrics, courses, currentTimetable, userPreferences, 0, 0, timeStart);

        // 创建最终的课程表列表
        TimetableResultInfo timetableResult = new TimetableResultInfo();
        List<List<CourseInfo>> topTimetables = new ArrayList<>();

        timetableResult.setTotalTimetableSize(queue.size());

        // 从优先队列中提取课程表，并添加到列表中
        int counter = 0;
        while (!queue.isEmpty() && counter < 50) {
            TimetableWithScore tws = queue.poll();
            List<CourseInfo> ttb = tws.getTimetable();
            selectOptimalTuts(ttb);
            topTimetables.add(0, ttb);
            counter++;
        }
        timetableResult.setTopTimetables(topTimetables);
        // printNonEmptyTimetableContents(timetable);
        return timetableResult;
    }

    private void generateTimetables(CourseInfo[][] timetable, Queue<TimetableWithScore> queue, TimetableMetrics metrics,
            Map<String, Map<String, Map<String, List<CourseInfo>>>> courses, List<CourseInfo> currentTimetable,
            UserPreferences userPreferences, int courseIndex, int typeIndex, long timeStart) {

        if (courseIndex == courses.size()) {
            double score = metrics.calculateScore(userPreferences);
            if (score > userPreferences.getScoreThreshold() && enoughWalkingTime(timetable)) {
                queue.add(new TimetableWithScore(new ArrayList<>(currentTimetable), score));
            }
            return;
        }

        long timeEnd = System.currentTimeMillis();
        if (queue.size() >= 2000000 || (timeEnd - timeStart) > 10000) {
            return;
        }

        List<String> courseKeys = new ArrayList<>(courses.keySet());
        String courseKey = courseKeys.get(courseIndex);
        Map<String, Map<String, List<CourseInfo>>> courseInfo = courses.get(courseKey);
        List<String> courseTypes = new ArrayList<>(courseInfo.keySet());

        if (typeIndex == courseTypes.size()) {
            generateTimetables(timetable, queue, metrics, courses, currentTimetable, userPreferences, courseIndex + 1,
                    0, timeStart);
            return;
        }

        String courseType = courseTypes.get(typeIndex);
        Map<String, List<CourseInfo>> sections = courseInfo.get(courseType);

        for (Map.Entry<String, List<CourseInfo>> entry : sections.entrySet()) {
            List<CourseInfo> sectionInfo = entry.getValue();

            // deep copy
            List<CourseInfo> sectionCopy = new ArrayList<>();
            for (CourseInfo course : sectionInfo) {
                sectionCopy.add(new CourseInfo(course));
            }
            boolean conflict = sectionCopy.stream().anyMatch(classInfo -> isConflict(classInfo, timetable));

            if (!conflict) {
                CourseInfo[][] timetable_copy = deepCopyTimeTable(timetable);
                TimetableMetrics savedMetrics = new TimetableMetrics(metrics); // 保存当前状态
                for (CourseInfo course : sectionInfo) {
                    for (TimeAndPlace tap : course.getTimeAndPlaceList()) {
                        metrics.updateTimeDistribution(tap);
                        metrics.updateDailyCourseCounts(course);
                        metrics.updateDailyBreakTimes(course); // 更新课间时间
                    }
                }

                currentTimetable.addAll(sectionInfo);
                generateTimetables(timetable_copy, queue, metrics, courses, currentTimetable, userPreferences,
                        courseIndex, typeIndex + 1, timeStart);

                metrics.restore(savedMetrics); // 回溯，还原状态
                currentTimetable.removeAll(sectionInfo);
            }
        }
    }

    public CourseInfo[][] deepCopyTimeTable(CourseInfo[][] original) {
        if (original == null) {
            return null;
        }

        CourseInfo[][] copy = new CourseInfo[original.length][];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                copy[i] = new CourseInfo[original[i].length];
                for (int j = 0; j < original[i].length; j++) {
                    copy[i][j] = original[i][j] != null ? new CourseInfo(original[i][j]) : null;
                }
            }
        }
        return copy;
    }

    public boolean isConflict(CourseInfo course, CourseInfo[][] timetable) {
        List<TimeAndPlace> times = course.getTimeAndPlaceList();
        for (int i = 0; i < times.size(); i++) {
            int dayIndex = times.get(i).getDay() - 1;
            int startSlot = (int) ((times.get(i).getStart() - 32400000) / 1800000);
            int endSlot = (int) ((times.get(i).getEnd() - 32400000) / 1800000);

            // 检查是否有冲突
            if (dayIndex <= 4) {
                for (int j = startSlot; j < endSlot; j++) {
                    if (timetable[dayIndex][j] != null) {
                        return true; // 存在冲突
                    }
                }
                if (startSlot > 0 && timetable[dayIndex][startSlot - 1] != null) {
                    String newCourseCode = course.getCourse();
                    String existingCourseCode = timetable[dayIndex][startSlot - 1].getCourse();
                    int campusesCode = Character.getNumericValue(newCourseCode.charAt(newCourseCode.length() - 1));
                    int campusesExistingCode = Character
                            .getNumericValue(existingCourseCode.charAt(existingCourseCode.length() - 1));
                    if (campusesCode != campusesExistingCode) {
                        return true; // 不同校区不满一小时 冲突
                    }
                }
            }

        }
        // System.out.println("\n\n------START-------\n\n");
        // printNonEmptyTimetableContents(timetable);
        // System.out.println("\n\n------AFTER-------\n\n");
        for (int i = 0; i < times.size(); i++) {
            int dayIndex = times.get(i).getDay() - 1;
            int startSlot = (int) ((times.get(i).getStart() - 32400000) / 1800000);
            int endSlot = (int) ((times.get(i).getEnd() - 32400000) / 1800000);
            if (dayIndex <= 4) {
                for (int j = startSlot; j < endSlot; j++) {
                    List<TimeAndPlace> singleTime = new ArrayList<>();
                    singleTime.add(times.get(i));
                    course.setTimeAndPlaceList(singleTime); // 在timetable里只存一个时间 虽然还是list
                    timetable[dayIndex][j] = course;
                }
            }
        }
        // printNonEmptyTimetableContents(timetable);
        // System.out.println("\n\n------END-------\n\n");
        return false;
    }

    public void printNonEmptyTimetableContents(CourseInfo[][] timetable) {
        for (int i = 0; i < timetable.length; i++) {
            for (int j = 0; j < timetable[i].length; j++) {
                if (timetable[i][j] != null) {
                    CourseInfo courseInfo = timetable[i][j];
                    System.out.println("Index [" + i + "][" + j + "]: " + courseInfo.toString());
                }
            }
        }
    }

    public boolean enoughWalkingTime(CourseInfo[][] timetable) {
        for (int day = 0; day < timetable.length; day++) {

            CourseInfo lastCourse = null;

            for (int timeSlot = 0; timeSlot < timetable[day].length - 1; timeSlot++) {
                if (timetable[day][timeSlot] == null || timetable[day][timeSlot].getCourse().contains("TUT")) {
                    lastCourse = null;
                    continue;
                }
                if (lastCourse == null) {
                    lastCourse = timetable[day][timeSlot];
                    continue;
                }

                CourseInfo currentCourse = timetable[day][timeSlot];
                String currentCourseCode = currentCourse.getCourse();
                int campusesCode = Character.getNumericValue(currentCourseCode.charAt(currentCourseCode.length() - 1));
                if (campusesCode != 1) {
                    lastCourse = null;
                    continue;
                }
                String currentLocation = currentCourse.getTimeAndPlaceList().get(0).getLocation();
                String lastLocation = lastCourse.getTimeAndPlaceList().get(0).getLocation();
                if (lastLocation.equals("") || currentLocation.equals("") || lastLocation.equals(currentLocation)) {
                    lastCourse = null;
                    continue;
                }
                Integer timeTake = distanceService.getDuration(currentLocation, lastLocation);
                if (timeTake > 10) {
                    return false;
                }

                lastCourse = currentCourse;
            }
        }
        return true;
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
        Map<String, List<CourseInfo>> alternativeTuts = courseDataService.fetchAllTuts(currentTut.getCourse(), day,
                start);

        CourseInfo optimalTut = currentTut;
        double shortestDistance = Double.MAX_VALUE;

        for (List<CourseInfo> tuts : alternativeTuts.values()) {
            for (CourseInfo tut : tuts) {
                if (tutIndex > 0) {
                    CourseInfo previousCourse = timetable.get(tutIndex - 1);
                    String origin = previousCourse.getTimeAndPlaceList().get(0).getLocation();
                    String destination = tut.getTimeAndPlaceList().get(0).getLocation();
                    Integer distance = distanceService.getDistance(origin, destination);
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        optimalTut = tut;
                    }
                }
            }
        }

        return optimalTut;
    }

    // private boolean isConflict(CourseInfo newCourse, List<CourseInfo> timetable)
    // {
    // for (CourseInfo existingCourse : timetable) {

    // String newCourseCode = newCourse.getCourse();
    // String existingCourseCode = existingCourse.getCourse();
    // int campusesCode =
    // Character.getNumericValue(newCourseCode.charAt(newCourseCode.length() - 1));
    // int campusesExistingCode =
    // Character.getNumericValue(existingCourseCode.charAt(existingCourseCode.length()
    // - 1));

    // if (campusesCode != campusesExistingCode){
    // if (checkDifferentCompusesConflict(newCourse, existingCourse)) {
    // return true;
    // }
    // }else{
    // if (checkSameCompusesConflict(newCourse, existingCourse, campusesCode)){
    // return true;
    // }
    // }
    // }
    // return false;
    // }

    // private boolean canAdd(TimeAndPlace newTimeSlot, TimeAndPlace
    // existingTimeSlot){
    // if ((existingTimeSlot.getStart() < newTimeSlot.getEnd() &&
    // newTimeSlot.getEnd() < existingTimeSlot.getEnd()) ||
    // (existingTimeSlot.getStart() < newTimeSlot.getStart() &&
    // newTimeSlot.getStart() < existingTimeSlot.getEnd()) ||
    // (newTimeSlot.getStart() < existingTimeSlot.getEnd() &&
    // existingTimeSlot.getEnd() < newTimeSlot.getEnd()) ||
    // (newTimeSlot.getStart() < existingTimeSlot.getStart() &&
    // existingTimeSlot.getStart() < newTimeSlot.getEnd())){
    // return false;
    // }
    // if(existingTimeSlot.getStart() == newTimeSlot.getStart() &&
    // existingTimeSlot.getEnd() == newTimeSlot.getEnd()){
    // return false;
    // }
    // return true;
    // }

    // private boolean enoughWalkingTimeUTSG(TimeAndPlace newTimeSlot, TimeAndPlace
    // existingTimeSlot){
    // String newLocation = newTimeSlot.getLocation();
    // String exisitingLocation = existingTimeSlot.getLocation();
    // if (exisitingLocation.equals("") || newLocation.equals("") ||
    // newLocation.equals(exisitingLocation)){
    // return true;
    // }
    // if (existingTimeSlot.getEnd() == newTimeSlot.getStart()){
    // Integer timeTake =
    // distanceService.getDuration(existingTimeSlot.getLocation(),
    // newTimeSlot.getLocation());
    // if (timeTake > 10){
    // return false;
    // }
    // }
    // return true;
    // }

    // private boolean checkSameCompusesConflict(CourseInfo newCourse, CourseInfo
    // existingCourse, int campusesCode){
    // List<TimeAndPlace> newCourseTimeSlots = newCourse.getTimeAndPlaceList();
    // List<TimeAndPlace> existingCourseTimeSlots =
    // existingCourse.getTimeAndPlaceList();

    // for (TimeAndPlace newTimeSlot : newCourseTimeSlots) {
    // for (TimeAndPlace existingTimeSlot : existingCourseTimeSlots) {
    // if (newTimeSlot.getDay() == existingTimeSlot.getDay()) {
    // if(!canAdd(newTimeSlot, existingTimeSlot)){
    // return true;
    // }
    // // if(campusesCode == 1 && !enoughWalkingTimeUTSG(newTimeSlot,
    // existingTimeSlot)){
    // // return true;
    // // }
    // }
    // }
    // }
    // return false;
    // }

    // private boolean checkDifferentCompusesConflict(CourseInfo course1, CourseInfo
    // course2) {
    // List<TimeAndPlace> timeSlots1 = course1.getTimeAndPlaceList();
    // List<TimeAndPlace> timeSlots2 = course2.getTimeAndPlaceList();

    // for (TimeAndPlace timeSlot1 : timeSlots1) {
    // for (TimeAndPlace timeSlot2 : timeSlots2) {
    // if (timeSlot1.getDay() == timeSlot2.getDay()) {
    // int startTime1 = timeSlot1.getStart();
    // int endTime1 = timeSlot1.getEnd();
    // int startTime2 = timeSlot2.getStart();
    // int endTime2 = timeSlot2.getEnd();

    // if (canAdd(timeSlot1, timeSlot2) ||
    // Math.abs(startTime1 - endTime2) < 3600000 || Math.abs(startTime2 - endTime1)
    // < 3600000) {
    // return true;
    // }
    // }
    // }
    // }
    // return false;
    // }
}