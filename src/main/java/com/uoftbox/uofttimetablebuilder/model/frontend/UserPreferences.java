package com.uoftbox.uofttimetablebuilder.model.frontend;

public class UserPreferences {
    private int preferredTimeIndex; // 0 代表早上, 1 代表中午, 2 代表晚上
    private double preferredTimeWeight; // 时间分布偏好的权重---前端用户选择的顺序 (1,2,3,4)
    private double balanceWeight; // 课程均衡性的权重---max = (1,2,3,4); min = -(1,2,3,4)
    private double breakTimeWeight; // 课间时间的权重---short = (1,2,3,4); long = -(1,2,3,4)
    private double scoreThreshold; // 阈值时间

    public UserPreferences() {
    }

    public UserPreferences(int preferredTimeIndex, double preferredTimeWeight, double balanceWeight, double breakTimeWeight) {
        this.preferredTimeIndex = preferredTimeIndex;
        this.preferredTimeWeight = preferredTimeWeight;
        this.balanceWeight = balanceWeight;
        this.breakTimeWeight = breakTimeWeight;
        this.scoreThreshold = -100000.0;
    }
    
    public UserPreferences(int preferredTimeIndex, double preferredTimeWeight, double balanceWeight, double breakTimeWeight,double scoreThreshold) {
        this.preferredTimeIndex = preferredTimeIndex;
        this.preferredTimeWeight = preferredTimeWeight;
        this.balanceWeight = balanceWeight;
        this.breakTimeWeight = breakTimeWeight;
        this.scoreThreshold = scoreThreshold;
    }

    public int getPreferredTimeIndex() {
        return preferredTimeIndex;
    }

    public void setPreferredTimeIndex(int preferredTimeIndex) {
        this.preferredTimeIndex = preferredTimeIndex;
    }

    public double getPreferredTimeWeight() {
        return preferredTimeWeight;
    }

    public void setPreferredTimeWeight(double preferredTimeWeight) {
        this.preferredTimeWeight = preferredTimeWeight;
    }

    public double getBalanceWeight() {
        return balanceWeight;
    }

    public void setBalanceWeight(double balanceWeight) {
        this.balanceWeight = balanceWeight;
    }

    public double getBreakTimeWeight() {
        return breakTimeWeight;
    }

    public void setBreakTimeWeight(double breakTimeWeight) {
        this.breakTimeWeight = breakTimeWeight;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }
}
