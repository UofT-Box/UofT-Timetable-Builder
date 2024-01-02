package com.github.zhengaugust;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DatabaseHelper {

    public Connection connectToDatabase(String dbPath) {
        Connection conn = null;
        try {
            // 加载 SQLite JDBC 驱动
            Class.forName("org.sqlite.JDBC");

            // db参数是SQLite文件的路径
            String url = "jdbc:sqlite:" + dbPath;
            // 建立连接
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found: " + e.getMessage());
        }
        return conn;
    }

    public Map<String, Map<String, Map<String, List<CourseInfo>>>> fetchSpecialSections(Connection conn,
            String[] courseCodes, String sectionCode) {
        Map<String, Map<String, Map<String, List<CourseInfo>>>> courseSchedule = new HashMap<>();
        for (String courseCode : courseCodes) {
            String query = "SELECT * FROM courses WHERE course_code = ? AND section_code = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, courseCode);
                pstmt.setString(2, sectionCode);
                ResultSet rs = pstmt.executeQuery();

                // 获取课程ID
                String courseId = "nothing";
                if (rs.next()) {
                    courseId = rs.getString("course_id");
                }
                
                if (courseId != "nothing") {
                    query = "SELECT * FROM meeting_sections WHERE course_id = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(query)) {

                        pstmt2.setString(1, courseId);
                        ResultSet rs2 = pstmt2.executeQuery();

                        String courseNumber = "";
                        String courseType = "";
                        
                        ArrayList<Integer> time_list = new ArrayList<>();

                        while (rs2.next()) {
                            List<CourseInfo> courseinfos = new ArrayList<>();
                            courseNumber = rs2.getString("section_code");
                            String prof = rs2.getString("instructors");
                            courseType = courseNumber.substring(0, 3);
                            
                            String tims_jsonString = rs2.getString("times");
                            // System.out.println(tims_jsonString);
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
                                CourseInfo courseinfo = new CourseInfo(courseCode, courseNumber,prof, timeAndPlaceList);
                                courseinfos.add(courseinfo);

                                courseSchedule.computeIfAbsent(courseCode, k -> new HashMap<>())
                                        .computeIfAbsent(courseType, k -> new HashMap<>())
                                        .computeIfAbsent(courseNumber, k -> courseinfos);
                                time_list.add(timeAndPlaceList.get(0).getStart());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        }
        
        return courseSchedule;
    }
    
    public Map<String, List<CourseInfo>> fetchAllTuts(Connection conn, String courseCode, int day, int start) {
        Map<String, List<CourseInfo>> allTuts = new HashMap<>();
        // 构建 LIKE 操作符的模式，匹配包含指定 day 和 start 的 JSON 字符串
        String timePattern = "%\"day\": " + day + ", \"start\": " + start + "%";
        // String courseType = "%TUT%";
        String query = "SELECT * FROM meeting_sections WHERE course_code = ? AND times LIKE ?";
        // String query = "SELECT * FROM meeting_sections WHERE course_code = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, courseCode);
            // pstmt.setString(2, courseType);
            pstmt.setString(2, timePattern);
            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                String sectionCode = rs.getString("section_code");
                String prof = rs.getString("instructors");
                String timesJsonString = rs.getString("times");
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TimeAndPlace>>() {}.getType();
                List<TimeAndPlace> timeAndPlaceList = gson.fromJson(timesJsonString, listType);
    
                CourseInfo courseInfo = new CourseInfo(courseCode, sectionCode, prof, timeAndPlaceList);
                allTuts.computeIfAbsent(sectionCode, k -> new ArrayList<>()).add(courseInfo);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return allTuts;
    }
}