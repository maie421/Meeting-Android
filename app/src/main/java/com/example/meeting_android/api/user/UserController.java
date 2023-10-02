package com.example.meeting_android.api.user;

import android.app.Activity;
import android.content.Context;

public class UserController {

    public UserService userService;
    private Context mContext;
    private Activity mActivity;

    public UserController(Context mContext, Activity mActivity) {
        userService = new UserService();
        this.mContext = mContext;
        this.mActivity = mActivity;
    }


}
