package com.example.meeting_android.api.room;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.example.meeting_android.R;
import com.example.meeting_android.activity.MeetingActivity;
import com.example.meeting_android.activity.MeetingMainActivity;
import com.example.meeting_android.api.user.Authorization;
import com.example.meeting_android.api.user.User;
import com.example.meeting_android.common.Common;
import com.example.meeting_android.common.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomController {

    public RoomService roomService;
    private Context mContext;
    private Activity mActivity;
    private Common common;

    public RoomController(Context mContext, Activity mActivity) {
        roomService = new RoomService(mContext, mActivity);
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.common = new Common(mContext, mActivity);
    }

    public void createRoom(Room room) {
        roomService.createRoom(room, new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {

                }else {
                    Toast.makeText(mContext, "실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                String errorMessage = t.getMessage();
                Toast.makeText(mContext, "실패했습니다. 오류: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getRoom(String roomId, String name){
        roomService.getRoom(roomId, new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(mContext, MeetingActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("joinRoom", roomId);
                    mContext.startActivity(intent);
                }else {
                    Toast.makeText(mContext, "존재하지 않는 방입니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {

            }
        });
    }

    public void deleteRoom(String roomId){
        roomService.deleteRoom(roomId, new Callback<Room>() {
            @Override
            public void onResponse(Call<Room> call, Response<Room> response) {
                if (response.isSuccessful()) {
                }else {
                    Toast.makeText(mContext, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Room> call, Throwable t) {
                Toast.makeText(mContext, "서버 연결 안됨", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
