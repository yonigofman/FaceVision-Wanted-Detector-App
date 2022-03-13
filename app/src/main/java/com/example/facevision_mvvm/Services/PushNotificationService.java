package com.example.facevision_mvvm.Services;

import android.app.*;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.facevision_mvvm.Activities.MainActivity;
import com.example.facevision_mvvm.Models.Reco.SimilarityClassifier;
import com.example.facevision_mvvm.Models.Wanted;
import com.example.facevision_mvvm.R;
import com.example.facevision_mvvm.ViewModels.FaceRecViewModel;
import com.example.facevision_mvvm.ViewModels.WantedViewModel;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class PushNotificationService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        final String CHANNEL_ID = "NOTI_ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID,
                    "MyNotification",
                    NotificationManager.IMPORTANCE_HIGH);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(R.drawable.ic_facial_recognition)
                    .setAutoCancel(true);

            NotificationManagerCompat.from(this).notify(1, builder.build());

        }
        super.onMessageReceived(remoteMessage);

    }

}




