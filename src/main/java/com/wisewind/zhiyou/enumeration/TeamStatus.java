package com.wisewind.zhiyou.enumeration;

public enum TeamStatus {

    PRIVATE(0, "私有"),
    PUBLIC(1, "公开"),
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
    }
}
