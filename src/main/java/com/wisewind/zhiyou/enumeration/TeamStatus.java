package com.wisewind.zhiyou.enumeration;

public enum TeamStatus {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");


    private int value;
    private String text;

    TeamStatus(int value, String text){
        this.value = value;
        this.text = text;
    }

    public int getValue(){
        return this.value;
    }

    public String getText(){
        return this.text;
    }

    public static TeamStatus getByValue(Integer value){
        if(value == null)
            return null;
        TeamStatus[] values = TeamStatus.values();
        for (TeamStatus teamStatus : values) {
            if(teamStatus.getValue() == value){
                return teamStatus;
            }
        }
        return null;
    }
}
