package com.example.meeting_android.api.room;


import com.example.meeting_android.api.user.User;

public class Room {
    public int id;
    public String room_id;
    public User user;
    public Room(String roomId) {
        this.room_id = roomId;
    }
}
