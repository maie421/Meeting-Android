package com.example.meeting_android.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Permission {
    public static final int CAMERA_PERMISSION_CODE = 100;
    public static final int LOCATION_PERMISSION_CODE = 300;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;

    public static Context context;
    public static Activity activity;

    public Permission(Context context) {
        this.context = context;
    }

    public Permission(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean checkStoragePermission(Activity activity) {
        boolean result = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
        if (!result) {
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        return result;
    }

    public boolean checkCameraPermission(Activity activity) {
        boolean result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!result) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        return result;
    }

    public boolean checkLocationPermission(Activity activity) {
        boolean result = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!result) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
        return result;
    }

    public boolean checkLocationPermission() {
        boolean result = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!result) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
        return result;
    }
}
