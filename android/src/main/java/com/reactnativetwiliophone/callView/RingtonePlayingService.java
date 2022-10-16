package com.reactnativetwiliophone.callView;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtonePlayingService extends Service
{
  private Ringtone ringtone;

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {


    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

    if(ringtoneUri == null){
      ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      if(ringtoneUri == null) {
        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
      }
    }
    this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
    ringtone.play();

    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy()
  {
    ringtone.stop();
  }
}
