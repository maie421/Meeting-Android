package com.example.meeting_android.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.meeting_android.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Common {
    private String[] items = {"앨범에서 선택", "사진 찍기", "프로필 사진 삭제"};
    private AlertDialog.Builder builder;
    public Context context;
    public Activity activity;
    public Uri selectedImage;
    public Permission permission;

    public int REQUEST_CODE_CAMERA = 2;
    public static int PICK_IMAGES_MULTIPLE = 3;

    public Common(Context context, Activity activity) {
        this.permission = new Permission(context);
        this.context = context;
        this.activity = activity;
    }

    public void setSelectedImage(Uri imageUrl, ImageView imageView) {
        Log.d("api data", String.valueOf(imageUrl));
        Picasso.get().load(imageUrl).into(imageView);
        selectedImage = imageUrl;

    }
    public void multiSelectImage(Uri[] imageUris, ImageView imageView){
        for (Uri uri : imageUris) {
            Picasso.get()
                    .load(uri)
                    .into(imageView);
        }
    }

    public void selectImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) view.getContext()).startActivityForResult(intent, 1);
    }

    public void multiImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        ((Activity) view.getContext()).startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGES_MULTIPLE);
    }

    public String getRealPathFromUri(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.context.getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

        return cursor.getString(columnIndex);
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ((Activity) view.getContext()).startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void alertDialogCamera(ImageView imageView, Activity activity) {
        builder = new AlertDialog.Builder(context);
        builder.setTitle("목록 선택")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 선택 항목 처리
                        switch (which) {
                            case 0: //앨범에서 선택
                                if (permission.checkStoragePermission(activity)) {
                                    selectImage(imageView);
                                }
                                break;
                            case 1: //카메라 찍기
                                if (permission.checkCameraPermission(activity)) {
                                    takePhoto(imageView);
                                }
                                break;
                            case 2:
                                imageView.setImageResource(R.drawable.profile);
                                selectedImage = null;
                                break;
                        }
                    }
                });
    }

    public void addTakePhoto(ImageView imageView, Activity activity){
        if (permission.checkCameraPermission(activity)) {
            takePhoto(imageView);
        }else{
            takePhoto(imageView);
        }
    }
    public void addAlbumCamera(ImageView imageView, Activity activity){
        if (permission.checkStoragePermission(activity)) {
            multiImage(imageView);
        }else{
            multiImage(imageView);
        }
    }

    public void findAlbumCamera(ImageView imageView, Activity activity){
        if (permission.checkStoragePermission(activity)) {
            selectImage(imageView);
        }else{
            selectImage(imageView);
        }
    }

    public void getImage() {
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, String.valueOf(System.currentTimeMillis()), null);
        return Uri.parse(path);
    }

    public int deleteImageUri(Uri img_uri) {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.delete(img_uri, null, null);
    }

    public Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    //현재 년도,달,일,시간,분,초
    public static String getNowTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시 mm분");

        String formattedTime = currentTime.format(formatter);
        return formattedTime;
    }
    //현재 시간 분
    public static String getNowHTime(String dateTimeString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();

        return  hour + ":" + minute;
    }

    public void alertDialog(Context mContext, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
