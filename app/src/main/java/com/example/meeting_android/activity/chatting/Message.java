package com.example.meeting_android.activity.chatting;

public class Message {
    private String text;
    private MemberData memberData;
    private boolean belongsToCurrentUser;
    private String type;
    public static String MESSAGE = "text";
    public static String GUIDE = "guide";

    public Message(String text, MemberData memberData, boolean belongsToCurrentUser, String type) {
        this.text = text;
        this.memberData = memberData;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.type = type;
    }

    public String getText() {
        return text;
    }
    public String getType() {
        return type;
    }

    public MemberData getMemberData() {
        return memberData;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
