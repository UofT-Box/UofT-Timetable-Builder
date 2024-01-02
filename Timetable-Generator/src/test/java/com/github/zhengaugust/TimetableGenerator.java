package com.github.zhengaugust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableGenerator {
    public List<List<CourseInfo>> generateAllTimetables(
            Map<String, Map<String, Map<String, List<CourseInfo>>>> courses) {
        List<List<CourseInfo>> allTimetables = new ArrayList<>();
        generateTimetables(courses, new ArrayList<>(), allTimetables, 0, 0);
        return allTimetables;
    }

    private void generateTimetables(
            Map<String, Map<String, Map<String, List<CourseInfo>>>> courses,
            List<CourseInfo> currentTimetable,
            List<List<CourseInfo>> allTimetables,
            int courseIndex,
            int typeIndex) {

        if (courseIndex == courses.size()) {
            allTimetables.add(new ArrayList<>(currentTimetable));
            return;
        }

        List<String> courseKeys = new ArrayList<>(courses.keySet());
        String courseKey = courseKeys.get(courseIndex);
        Map<String, Map<String, List<CourseInfo>>> courseInfo = courses.get(courseKey);
        List<String> courseTypes = new ArrayList<>(courseInfo.keySet());

        if (typeIndex == courseTypes.size()) {
            generateTimetables(courses, currentTimetable, allTimetables, courseIndex + 1, 0);
            return;
        }

        String courseType = courseTypes.get(typeIndex);
        Map<String, List<CourseInfo>> sections = courseInfo.get(courseType);

        for (Map.Entry<String, List<CourseInfo>> entry : sections.entrySet()) {
            List<CourseInfo> sectionInfo = entry.getValue();

            // 检查整个课程部分是否与当前时间表冲突
            boolean conflict = sectionInfo.stream()
                    .anyMatch(classInfo -> isConflict(classInfo, currentTimetable));

            if (!conflict) {
                // 将整个课程部分添加到当前时间表
                currentTimetable.addAll(sectionInfo);

                // 移动到下一个课程种类
                generateTimetables(courses, currentTimetable, allTimetables, courseIndex, typeIndex + 1);

                // 回溯：从当前时间表中移除整个课程部分
                currentTimetable.removeAll(sectionInfo);
            }
        }
    }

    private boolean isConflict(CourseInfo newCourse, List<CourseInfo> timetable) {
        for (CourseInfo existingCourse : timetable) {
            if (!areCampusesDifferent(newCourse, existingCourse)) {
                List<TimeAndPlace> newCourseTimeSlots = newCourse.getTimeAndPlaceList();
                List<TimeAndPlace> existingCourseTimeSlots = existingCourse.getTimeAndPlaceList();

                for (TimeAndPlace newTimeSlot : newCourseTimeSlots) {
                    for (TimeAndPlace existingTimeSlot : existingCourseTimeSlots) {
                        if (newTimeSlot.getDay() == existingTimeSlot.getDay() &&
                                !(newTimeSlot.getEnd() <= existingTimeSlot.getStart() ||
                                        newTimeSlot.getStart() >= existingTimeSlot.getEnd())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean areCampusesDifferent(CourseInfo course1, CourseInfo course2) {
        String courseCode1 = course1.getCourse();
        String courseCode2 = course2.getCourse();

        // 获取课程代码的最后一个数字
        int lastDigit1 = Character.getNumericValue(courseCode1.charAt(courseCode1.length() - 1));
        int lastDigit2 = Character.getNumericValue(courseCode2.charAt(courseCode2.length() - 1));

        // 如果最后一个数字不一样且时间间隔不足1小时，返回 true
        if (lastDigit1 != lastDigit2 && !isHourInterval(course1, course2)) {
            return true;
        }

        return false;
    }

    private boolean isHourInterval(CourseInfo course1, CourseInfo course2) {
        List<TimeAndPlace> timeSlots1 = course1.getTimeAndPlaceList();
        List<TimeAndPlace> timeSlots2 = course2.getTimeAndPlaceList();

        for (TimeAndPlace timeSlot1 : timeSlots1) {
            for (TimeAndPlace timeSlot2 : timeSlots2) {
                if (timeSlot1.getDay() == timeSlot2.getDay()) {
                    long startTime1 = timeSlot1.getStart();
                    long endTime1 = timeSlot1.getEnd();
                    long startTime2 = timeSlot2.getStart();
                    long endTime2 = timeSlot2.getEnd();

                    if (Math.abs(startTime1 - endTime2) < 3600000 || Math.abs(startTime2 - endTime1) < 3600000) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // 创建课程数据的示例
        Map<String, Map<String, Map<String, List<CourseInfo>>>> courses = new HashMap<>();

        // 添加课程信息到courses

        TimetableGenerator generator = new TimetableGenerator();
        List<List<CourseInfo>> allTimetables = generator.generateAllTimetables(courses);

        // 打印所有时间表
        for (List<CourseInfo> timetable : allTimetables) {
            System.out.println("Timetable:");
            for (CourseInfo course : timetable) {
                System.out.println("Course: " + course.getCourse() +
                        ", Section: " + course.getSection() +
                        ", Time: " + course.getTime());
            }
        }
    }
}
