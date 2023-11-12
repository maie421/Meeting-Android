package com.example.meeting_android.activity.meeting;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.meeting_android.R;

public class YourForegroundService extends Service{
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MediaProjectionServiceChannel";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }
    private Notification createNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_title)) // 제목 설정
                .setContentText(getString(R.string.notification_message)); // 메시지 설정
//                .setSmallIcon(R.drawable.ic_notification); // 아이콘 설정

        // 오레오(Oreo) 이상에서는 채널이 필요함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        // 오레오(Oreo) 이상에서는 알림 채널이 필요함
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name); // 채널 이름
            String description = getString(R.string.channel_description); // 채널 설명
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // 중요도
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // 채널을 시스템에 등록
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 필요한 정리 작업을 수행합니다.
    }
}
