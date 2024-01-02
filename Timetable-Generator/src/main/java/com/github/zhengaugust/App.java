package com.github.zhengaugust;

import java.sql.Connection;
// import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        DatabaseHelper dbHelper = new DatabaseHelper();
        // 假设数据库文件位于资源目录下
        String dbPath = "src/main/resources/courses.db";
        long startTime = System.currentTimeMillis();
        // 连接数据库
        Connection conn = dbHelper.connectToDatabase(dbPath);
        // Map<String, List<CourseInfo>> test = new HashMap<>();
        Map<String, Map<String, Map<String, List<CourseInfo>>>> meetingSections = new HashMap<>();
        // 检查连接是否成功

        if (conn != null) {
            // 获取特定课程和章节的信息
            // String[] courseCode = { "CSC148H1", "BIO130H1","CHM136H1","CSC165H1","MAT136H1"};
            // String[] courseCode = {"CSCA08H3","MATA31H3","MATA22H3","MATA67H3","MATA37H3"};
            String[] courseCode = {"MAT102H5","MAT135H5","ISP100H5","ECO101H5","CSC108H5"};
            // String[] courseCode = {"ECO101H5"};
            String sectionCode = "F";
            meetingSections = dbHelper.fetchSpecialSections(conn, courseCode, sectionCode);
            // test = dbHelper.fetchAllTuts(conn, "MAT136H1",4,50400000);
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Error closing the connection: " + e.getMessage());
            }
        } else {
            System.out.println("Failed to make connection!");
        }
        TimetableGenerator generator = new TimetableGenerator();

        List<List<CourseInfo>> allTimetables = generator.generateAllTimetables(meetingSections);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("程序运行时间（秒）: " + (executionTime*0.001));
        System.out.println("课程表长度: " + allTimetables.size());

        if (!allTimetables.isEmpty()) {
            List<CourseInfo> firstTimetable = allTimetables.get(0);
            System.out.println("First Timetable:");
            for (CourseInfo course : firstTimetable) {
                System.out.println("Course: " + course.getCourse() +
                        ", Section: " + course.getSection() +
                        ", Prof: " + course.getprof() +
                        ", Time: " + course.getTimeAndPlaceList());
            }
        } else {
            System.out.println("No timetables found.");
        }

    }
}