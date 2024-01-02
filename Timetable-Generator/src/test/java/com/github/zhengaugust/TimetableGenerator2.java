package com.github.zhengaugust;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import java.text.DecimalFormat;
import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableGenerator2 {
    private DatabaseHelper dbHelper;
    private Connection conn;
    private Connection conn_dis;

    public TimetableGenerator2() {
        dbHelper = new DatabaseHelper();

    }

    public List<List<CourseInfo>> generateAllTimetables(
            Map<String, Map<String, Map<String, List<CourseInfo>>>> courses) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + "src\\main\\resources\\courses.db");
            conn_dis = DriverManager.getConnection("jdbc:sqlite:" + "src\\main\\resources\\walking_distances.db");
        } catch (SQLException e) {
            System.out.println("Error fetching distance: " + e.getMessage());
        }

        List<List<CourseInfo>> allTimetables = new ArrayList<>();
        List<CourseInfo> currentTimetable = new ArrayList<>();
        UserPreferences userPreferences = new UserPreferences(2,1,2,3,0);
        generateTimetables(courses, currentTimetable, allTimetables,new TimetableMetrics(),userPreferences,  0, 0);

        return allTimetables;
    }

    private void generateTimetables(
        Map<String, Map<String, Map<String, List<CourseInfo>>>> courses,
        List<CourseInfo> currentTimetable,
        List<List<CourseInfo>> allTimetables,
        TimetableMetrics metrics,
        UserPreferences userPreferences,
        int courseIndex,
        int typeIndex) {

    if (courseIndex == courses.size()) {
        double score = metrics.calculateScore(userPreferences); // 根据用户偏好计算分数
        // boolean a = score > userPreferences.getScoreThreshold();
        // System.out.println(score + " > " + userPreferences.getScoreThreshold() + " is " + a);
        // DecimalFormat df = new DecimalFormat("0.00");
        // String scores = df.format(score);
        // System.out.println(score);
        if (score > userPreferences.getScoreThreshold()) { // 满足某个分数阈值
            List<CourseInfo> newcurrentTimetable = new ArrayList<>(currentTimetable);
            // selectOptimalTuts(newcurrentTimetable);
            allTimetables.add(newcurrentTimetable);
        }
        return;
    }

    if (allTimetables.size() >= 1000000){
        return;
    }

    List<String> courseKeys = new ArrayList<>(courses.keySet());
    String courseKey = courseKeys.get(courseIndex);
    Map<String, Map<String, List<CourseInfo>>> courseInfo = courses.get(courseKey);
    List<String> courseTypes = new ArrayList<>(courseInfo.keySet());

    if (typeIndex == courseTypes.size()) {
        generateTimetables(courses, currentTimetable, allTimetables, metrics, userPreferences, courseIndex + 1, 0);
        return;
    }

    String courseType = courseTypes.get(typeIndex);
    Map<String, List<CourseInfo>> sections = courseInfo.get(courseType);

    for (Map.Entry<String, List<CourseInfo>> entry : sections.entrySet()) {
        List<CourseInfo> sectionInfo = entry.getValue();

        boolean conflict = sectionInfo.stream()
                .anyMatch(classInfo -> isConflict(classInfo, currentTimetable));

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
            generateTimetables(courses, currentTimetable, allTimetables, metrics, userPreferences, courseIndex, typeIndex + 1);

            metrics.restore(savedMetrics); // 回溯，还原状态
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
                    int startTime1 = timeSlot1.getStart();
                    int endTime1 = timeSlot1.getEnd();
                    int startTime2 = timeSlot2.getStart();
                    int endTime2 = timeSlot2.getEnd();

                    if (Math.abs(startTime1 - endTime2) < 3600000 || Math.abs(startTime2 - endTime1) < 3600000) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void selectOptimalTuts(List<CourseInfo> timetable) {
        for (int i = 0; i < timetable.size(); i++) {
            CourseInfo currentCourse = timetable.get(i);
            if (currentCourse.getSection().startsWith("TUT")) {
                CourseInfo optimalTut = findOptimalTut(conn, currentCourse, timetable, i);
                if (optimalTut != null) {
                    timetable.set(i, optimalTut);
                }
            }
        }
    }

    private CourseInfo findOptimalTut(Connection conn, CourseInfo currentTut, List<CourseInfo> timetable,
            int tutIndex) {
        int day = currentTut.getTimeAndPlaceList().get(0).getDay();
        int start = currentTut.getTimeAndPlaceList().get(0).getStart();
        Map<String, List<CourseInfo>> alternativeTuts = dbHelper.fetchAllTuts(conn, currentTut.getCourse(), day, start);

        CourseInfo optimalTut = currentTut;
        double shortestDistance = Double.MAX_VALUE;

        for (List<CourseInfo> tuts : alternativeTuts.values()) {
            for (CourseInfo tut : tuts) {
                if (tutIndex > 0) {
                    CourseInfo previousCourse = timetable.get(tutIndex - 1);
                    double distance = getDistance(previousCourse.getTime().get(0).getLocation(),
                            tut.getTime().get(0).getLocation());
                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        optimalTut = tut;
                    }
                }
            }
        }

        return optimalTut;
    }

    public double getDistance(String origin, String destination) {
        double distance = -1; // 如果未找到距离，返回 -1

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 连接到数据库

            String query = "SELECT distance FROM distances WHERE origin = ? AND destination = ?";
            pstmt = conn_dis.prepareStatement(query);
            pstmt.setString(1, origin);
            pstmt.setString(2, destination);
            rs = pstmt.executeQuery();

            // 检索距离
            if (rs.next()) {
                String ddd = rs.getString("distance");
                ddd = ddd.substring(0, ddd.length() - 3);
                distance = Double.parseDouble(ddd);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching distance: " + e.getMessage());
        }
        return distance;
    }
}
