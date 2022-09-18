package com.reactnativetwiliophone.callView;

import static android.app.PendingIntent.getBroadcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.facebook.react.HeadlessJsTaskService;
import com.reactnativetwiliophone.Const;
import com.reactnativetwiliophone.R;
import com.reactnativetwiliophone.boradcastReceivers.NotificationsHeadlessReceiver;
import com.reactnativetwiliophone.callView.ViewService;
import com.reactnativetwiliophone.callView.CallView;

import java.util.UUID;


public class CallViewService extends ViewService {
  private  Intent mIntent ;
  private Bundle extras;
  private TextView textView;

  @Override
  public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
  /*  if (intent.getAction().equals("StopService")) {
      stopForeground(true);
      Log.d("callMyService", "StopService onStartCommand");
      stopSelf();
      return START_NOT_STICKY;
    }*/
    if (intent != null) {
      String action = intent.getAction();
      if(action==Const.ACTION_STOP_LISTEN){
        Log.d("callMyService", "ACTION_STOP_LISTEN");
        stopForeground(true);
        stopSelf();
      }
    }
    mIntent= intent;
      Log.d("callMyService", "onStartCommand");
    extras = intent.getExtras();
    if(extras!=null&&textView!=null){
      textView.setText(extras.getString(Const.CALLER_NAME));
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @NonNull
  @Override
  public Notification setupNotificationBuilder(@NonNull String channelId) {
    return new NotificationCompat.Builder(this, channelId)
      .setOngoing(true)
      .setSmallIcon(R.drawable.logo_round)
      .setContentTitle("Incomming Call")
      .setTicker("Call_STATUS")
      .setPriority(NotificationCompat.PRIORITY_MIN)
      .setCategory(Notification.CATEGORY_SERVICE)
      .build();
  }


  @Nullable
    @Override
    public CallView.Builder setupCallView(@NonNull CallView.Action action) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.call_view, null);


        layout.findViewById(R.id.expanded).setOnClickListener(v -> {
//            Toast.makeText(this, "hello from card view from java", Toast.LENGTH_SHORT).show();
            action.popCallView();
        });
        TextView textView= layout.findViewById(R.id.callerNameV);

        if(extras!=null){
          textView.setText(extras.getString(Const.CALLER_NAME));
        }

       ImageButton imgDeclineBtn =layout.findViewById(R.id.imgDecline);
       ImageButton imgAnswerBtn =layout.findViewById(R.id.imgAnswer);

    imgAnswerBtn.setOnClickListener(v->{
      handleIntent(extras,Const.ANSWER);
    });
    imgDeclineBtn.setOnClickListener(v->{
      handleIntent(extras,Const.REJECT);
    });
        return new CallView.Builder()
                .with(this)
                .setCallView(layout)
                .setDimAmount(0.8f)
                .addCallViewListener(new CallView.Action() {
                    @Override
                    public void popCallView() {
                        this.popCallView();
                    }

                    @Override
                    public void onOpenCallView() {
                      Log.d("<>", "onOpenFloatingView: ");
                    }

                  /*  @Override
                    public void onCloseCallView() {
                        Log.d("callMyService", "onCloseFloatingView: ");
                       // tryStopService();
                    }*/
                });
    }

  public void handleIntent(Bundle extras, String type) {
    Log.d("callMyService", "callExtra not null");
    try {

    int pendingFlags;

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      pendingFlags = PendingIntent.FLAG_IMMUTABLE;
    } else {
      pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    }
    //  if(!isAppRunning()){
        Intent  appIntent = new Intent(this, Class.forName("com.iriscrm.MainActivity"));
        PendingIntent contentIntent = PendingIntent.getActivity(
          this,
          0,
          appIntent,
          pendingFlags
        );
        contentIntent.send();
    //  }

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
      Log.d("callMyService", "finish service A");

      tryStopService();

      ComponentName cmp = new ComponentName(this.getApplicationContext(), CallViewService.class);
      this.getPackageManager().setComponentEnabledSetting(
        cmp, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.DONT_KILL_APP
      );
     // android.os.Process.killProcess(android.os.Process.myPid());

    } catch (Exception e) {

      Log.d("callMyService", "Exception ="+e.toString());
     // android.os.Process.killProcess(android.os.Process.myPid());
      tryStopService();

    }
  }
  @Override
  public void onTaskRemoved(Intent rootIntent) {
    Log.d("callMyService", "onTaskRemoved");

    super.onTaskRemoved(rootIntent);
    //do something you want
    //stop service
    this.stopSelf();
  }
}
