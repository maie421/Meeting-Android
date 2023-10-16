package com.example.meeting_android.api.room;

import android.app.Activity;
import android.content.Context;

import com.example.meeting_android.api.ApiService;
import com.example.meeting_android.common.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;


public class RoomService extends ApiService {
    public Context mContext;
    public Activity mActivity;
    public RoomApi roomApi;
    public RoomService(Context mContext, Activity mActivity){
        this.mContext = mContext;
        this.mActivity = mActivity;
        roomApi = retrofit.create(RoomApi.class);
    }

    public void createRoom(Room room, Callback<Room> callback){
        TokenManager tokenManager = new TokenManager(mContext);
        String token = tokenManager.getToken();

        Call<Room> call = roomApi.createRoom(room, "Bearer " + token);
        call.enqueue(callback);
    }
    public void getRoom(String roomId, Callback<Room> callback){
        Call<Room> call = roomApi.getRoom(roomId);
        call.enqueue(callback);
    }

    public void deleteRoom(String roomId, Callback<Room> callback) {
        RoomApi roomApi = retrofit.create(RoomApi.class);
        Call<Room> call = roomApi.deleteRoom(roomId);
        call.enqueue(callback);
    }
}
