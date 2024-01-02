package com.github.zhengaugust;

public class TimeAndPlace {
    private int day;
    private long start;
    private long end;
    private String location;

    public TimeAndPlace(int day, long start, long end, String location) {
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

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
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
