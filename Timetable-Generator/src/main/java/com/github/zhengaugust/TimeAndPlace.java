package com.github.zhengaugust;

public class TimeAndPlace {
    private int day;
    private int start;
    private int end;
    private String location;

    public TimeAndPlace(int day, int start, int end, String location) {
        this.day = day;
        this.start = start;
        this.end = end;
        this.location = location;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "TimeAndPlace{" +
                "day=" + day +
                ", start=" + start +
                ", end=" + end +
                ", location='" + location + '\'' +
                '}';
    }
}
