package com.uoftbox.uofttimetablebuilder.model.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.uoftbox.uofttimetablebuilder.model.frontend.UserPreferences;

public class TimetableMetrics {
    private int[] timeDistribution; // 存储早中晚的课程分布
    private List<Integer> dailyCourseCounts; // 每天的课程量

    private List<Integer> dailyBreakTimes; // 存储每天的课间时间
    private List<Integer> dailyEarliestStartTimes; // 每天的最早开始时间
    private List<Integer> dailyLatestEndTimes; // 每天的最晚结束时间

    public TimetableMetrics() {
        // 假设一周有5个工作日
        this.dailyBreakTimes = new ArrayList<>(Collections.nCopies(5, 0));
        this.dailyEarliestStartTimes = new ArrayList<>(Collections.nCopies(5, Integer.MAX_VALUE));
        this.dailyLatestEndTimes = new ArrayList<>(Collections.nCopies(5, Integer.MIN_VALUE));
        this.dailyCourseCounts = new ArrayList<>(Collections.nCopies(5, 0));
        this.timeDistribution = new int[3]; // 三个时间段：早、中、晚
    }

    public void updateTimeDistribution(TimeAndPlace tap) {
        int period = getPeriodOfTheDay(tap);
        timeDistribution[period]++;
    }

    public void backtrackTimeDistribution(TimeAndPlace tap) {
        int period = getPeriodOfTheDay(tap);
        timeDistribution[period]--;
    }

    public void updateDailyCourseCounts(CourseInfo course) {
        for (TimeAndPlace tap : course.getTimeAndPlaceList()) {
            if (tap.getDay() > 5){
                continue;
            }
            int dayIndex = tap.getDay() - 1;
            dailyCourseCounts.set(dayIndex, dailyCourseCounts.get(dayIndex) + 1);
        }
    }

    public void backtrackDailyCourseCounts(CourseInfo course) {
        for (TimeAndPlace tap : course.getTimeAndPlaceList()) {
            if (tap.getDay() > 5){
                continue;
            }
            int dayIndex = tap.getDay() - 1;
            dailyCourseCounts.set(dayIndex, dailyCourseCounts.get(dayIndex) - 1);
        }
    }

    public void updateDailyBreakTimes(CourseInfo course) {
        for (TimeAndPlace tap : course.getTimeAndPlaceList()) {
            if (tap.getDay() > 5){
                continue;
            }
            int dayIndex = tap.getDay() - 1;
            int start = tap.getStart();
            int end = tap.getEnd();
            int duration = end - start;

            // 更新最早和最晚时间
            dailyEarliestStartTimes.set(dayIndex, Math.min(dailyEarliestStartTimes.get(dayIndex), start));
            dailyLatestEndTimes.set(dayIndex, Math.max(dailyLatestEndTimes.get(dayIndex), end));

            // 更新课间时间
            double totalActiveTime = dailyLatestEndTimes.get(dayIndex) - dailyEarliestStartTimes.get(dayIndex);
            dailyBreakTimes.set(dayIndex, (int)(totalActiveTime - duration));
        }
    }

    public void backtrackDailyBreakTimes() {
        if (!dailyBreakTimes.isEmpty()) {
            dailyBreakTimes.remove(dailyBreakTimes.size() - 1);
        }
    }

    private int getPeriodOfTheDay(TimeAndPlace tap) {
        // 根据 TimeAndPlace 确定它属于一天中的哪个时间段
        // 早上：0，中午：1，晚上：2
        int hour = tap.getStart();
        if (hour <=  36000000) {
            return 0;
        } else if (hour > 36000000 && hour < 57600000) {
            return 1;
        } else {
            return 2;
        }
    }

    // Getters
    public int[] getTimeDistribution() {
        return timeDistribution;
    }

    public List<Integer> getDailyBreakTimes() {
        return dailyBreakTimes;
    }

    public List<Integer> getDailyCourseCounts() {
        return dailyCourseCounts;
    }

    public List<Integer> getDailyEarliestStartTimes() {
        return dailyEarliestStartTimes;
    }

    public List<Integer> getDailyLatestEndTimes() {
        return dailyLatestEndTimes;
    }

    public void setTimeDistribution(int[] timeDistribution) {
        this.timeDistribution = timeDistribution;
    }

    public void setDailyCourseCounts(List<Integer> dailyCourseCounts) {
        this.dailyCourseCounts = dailyCourseCounts;
    }

    public void setDailyBreakTimes(List<Integer> dailyBreakTimes) {
        this.dailyBreakTimes = dailyBreakTimes;
    }

    public void setDailyEarliestStartTimes(List<Integer> dailyEarliestStartTimes) {
        this.dailyEarliestStartTimes = dailyEarliestStartTimes;
    }

    public void setDailyLatestEndTimes(List<Integer> dailyLatestEndTimes) {
        this.dailyLatestEndTimes = dailyLatestEndTimes;
    }

    // Copy constructor for saving the state
    public TimetableMetrics(TimetableMetrics other) {
        this.timeDistribution = other.timeDistribution.clone();
        this.dailyBreakTimes = new ArrayList<>(other.dailyBreakTimes);
        this.dailyCourseCounts = new ArrayList<>(other.dailyCourseCounts);
        this.dailyEarliestStartTimes = new ArrayList<>(other.dailyEarliestStartTimes);
        this.dailyLatestEndTimes = new ArrayList<>(other.dailyLatestEndTimes);
    }

    public void restore(TimetableMetrics savedMetrics) {
        this.timeDistribution = savedMetrics.timeDistribution.clone();
        this.dailyBreakTimes = new ArrayList<>(savedMetrics.dailyBreakTimes);
        this.dailyCourseCounts = new ArrayList<>(savedMetrics.dailyCourseCounts);
        this.dailyEarliestStartTimes = new ArrayList<>(savedMetrics.dailyEarliestStartTimes);
        this.dailyLatestEndTimes = new ArrayList<>(savedMetrics.dailyLatestEndTimes);
    }

    public double calculateScore(UserPreferences userPreferences) {
        double score = 0.0;

        // 根据时间分布偏好评分
        double preferredTimeRatio;

        // 计算总课程数量
        double totalCourses = Arrays.stream(timeDistribution).sum();
        // System.out.println("总课程数量:" + totalCourses);
        // 检查是否有课程
        if (totalCourses > 0) {
            int pefer = userPreferences.getPreferredTimeIndex();
            preferredTimeRatio = ((double) timeDistribution[pefer]) / totalCourses;
            if (pefer == 2){
                preferredTimeRatio -= 10 * ((double) timeDistribution[0]) / totalCourses;
            }else if(pefer == 0){
                preferredTimeRatio -= 10 * ((double) timeDistribution[2]) / totalCourses;
            }
        } else {
            // 如果没有课程，则比例为0
            preferredTimeRatio = 0;
        }
        
        score += 20 * preferredTimeRatio * userPreferences.getPreferredTimeWeight();

        // 每天课程数量的均衡性评分
        double sum = 0;
        for (int count : dailyCourseCounts) {
            sum += count;
        }
        double averageCoursesPerDay = sum / dailyCourseCounts.size();
        
        double varianceSum = 0;
        for (int count : dailyCourseCounts) {
            varianceSum += Math.pow(count - averageCoursesPerDay, 2);
        }
        double variance = varianceSum / dailyCourseCounts.size();
        score += 10 * (1 - Math.sqrt(variance)) * userPreferences.getBalanceWeight(); // 低差异得高分

        // 平均每天课间时间评分
        double totalBreakTime = 0;
        for (int breakTime : dailyBreakTimes) {
            totalBreakTime += breakTime;
        }
        double averageBreakTime = dailyBreakTimes.isEmpty() ? 0 : totalBreakTime / dailyBreakTimes.size();
        averageBreakTime = averageBreakTime/36000000;
        score += 10 * averageBreakTime * userPreferences.getBreakTimeWeight();
        
        return score;
    }
}
