package com.reactnativetwiliophone.overlayView;

import static android.app.PendingIntent.getBroadcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.facebook.react.HeadlessJsTaskService;
import com.reactnativetwiliophone.Const;
import com.reactnativetwiliophone.R;
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver;
import com.reactnativetwiliophone.overlyView.OverlayService;
import com.reactnativetwiliophone.overlyView.OverLyView;

import java.util.UUID;


public class MyOverViewService extends OverlayService {
  private  Intent mIntent ;
  private Bundle extras;
  private TextView textView;
  @Override
  public int onStartCommand(@Nullable Intent intent, int flags, int startId) {

    mIntent= intent;
      Log.d("callMyService", "onStartCommand");
    extras = intent.getExtras();
    if(extras!=null&&textView!=null){
      textView.setText(extras.getString(Const.CALLER_NAME));
    }
    return super.onStartCommand(intent, flags, startId);
  }

  private PendingIntent createNotificationIntent(
            Context  context ,
          //  Bundle notificationDataBundle  ,
            String  actionType  ,
            int notificationId  ,
            String   activityName
            ) {
      int pendingFlags;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        pendingFlags = PendingIntent.FLAG_IMMUTABLE;
      } else {
        pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
      }

      Intent clickIntentData = null;
      try {
        clickIntentData = new Intent(this, Class.forName(context.getPackageName()+".MainActivity"));
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }



        clickIntentData.putExtra("action", actionType);
        clickIntentData.putExtra("notificationId", notificationId);
        clickIntentData.putExtra("activityName", activityName);


        int requestCode = UUID.randomUUID().hashCode();
        return getBroadcast(context, requestCode, clickIntentData, pendingFlags );
    }
    // for android 8 and above
    @NonNull
    @Override
    public Notification setupNotificationBuilder(@NonNull String channelId) {

        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.notification_view);
       // Bundle notificationDataBundle = Arguments.toBundle(notificationData);

        PendingIntent bodyIntent =
            createNotificationIntent(this,"tabbed", Const.NOTIFICATION_ID, this.getPackageName()+".MainActivity");
        //remoteView.setOnClickPendingIntent(R.id.imgAnswer, answerIntent)
        // remoteView.setOnClickPendingIntent(R.id.imgDecline, rejectIntent)
        remoteView.setTextViewText(R.id.callerNameN, "test");
        NotificationCompat.Builder notificationBuilder=
                new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                        .setSmallIcon(R.drawable.logo_round)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .setTicker("Call_STATUS")
                        .setOngoing(true);
                       // .setCustomContentView(remoteView)
                       // .setFullScreenIntent(bodyIntent, true);
        return notificationBuilder.build();
    }




    @Nullable
    @Override
    public OverLyView.Builder setupOverLyView(@NonNull OverLyView.Action action) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.overly_view, null);


        layout.findViewById(R.id.expanded).setOnClickListener(v -> {
//            Toast.makeText(this, "hello from card view from java", Toast.LENGTH_SHORT).show();
            action.popOverLyView();
        });
        TextView textView= layout.findViewById(R.id.callerNameV);

        if(extras!=null){
          textView.setText(extras.getString(Const.CALLER_NAME));
        }

        layout.findViewById(R.id.imgDecline).setOnClickListener(v->{
          handleIntent(extras,Const.REJECT);
          tryStopService();
        });

        layout.findViewById(R.id.imgAnswer).setOnClickListener(v->{

          handleIntent(extras,Const.ANSWER);
          tryStopService();
        });

        return new OverLyView.Builder()
                .with(this)
                .setOverLyView(layout)
                .setDimAmount(0.8f)
                .addOverLyViewListener(new OverLyView.Action() {
                    @Override
                    public void popOverLyView() {
                        this.popOverLyView();
                    }

                    @Override
                    public void onOpenOverLyView() {
                        Log.d("<>", "onOpenFloatingView: ");
                    }

                    @Override
                    public void onCloseOverLyView() {
                        Log.d("<>", "onCloseFloatingView: ");
                    }
                });
    }

  public void handleIntent(Bundle extras, String type) {
    Log.d("callMyService", "callExtra not null");
    Intent  appIntent = null;
    try {
      appIntent = new Intent(this, Class.forName("com.iriscrm.MainActivity"));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    int pendingFlags;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      pendingFlags = PendingIntent.FLAG_IMMUTABLE;
    } else {
      pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    }
    PendingIntent contentIntent = PendingIntent.getActivity(
      this,
      0,
      appIntent,
      pendingFlags
    );
    try {
      Log.d("callMyService", " contentIntent.send");
      contentIntent.send();
    } catch (PendingIntent.CanceledException e) {
      Log.d("callMyService", " contentIntent.send CanceledException = $e");
    }
    try {
      Intent headlessIntent = new Intent(
        this,
        NotificationsHeadlessReceiver.class
      );
      extras.putString(Const.ACTION, type);
      headlessIntent.putExtra(Const.EXTRA_NOTIFIER, extras);
      ComponentName name = startService(headlessIntent);
      if (name != null) {
        HeadlessJsTaskService.acquireWakeLockNow(this);
      }
      tryStopService();
    } catch (IllegalStateException ignored) {
    }
  }

}
