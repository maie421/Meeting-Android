package com.example.meeting_android.activity.chatting;

public class Message {
    private String text;
    private MemberData memberData;
    private boolean belongsToCurrentUser;
    private String type;
    private String time;
    public static String MESSAGE = "text";
    public static String GUIDE = "guide";

    public Message(String text, MemberData memberData, boolean belongsToCurrentUser, String type, String time) {
        this.text = text;
        this.memberData = memberData;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.type = type;
        this.time = time;
    }

    public String getText() {
        return text;
    }
    public String getType() {
        return type;
    }
    public String getTime() {
        return time;
    }

    public MemberData getMemberData() {
        return memberData;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
