/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactnativetwiliophone.data

import android.app.*
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.Person.Builder
import androidx.core.app.RemoteInput
import androidx.core.content.LocusIdCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.reactnativetwiliophone.Const
import com.reactnativetwiliophone.R
import com.reactnativetwiliophone.log

/**
 * Handles all operations related to [Notification].
 */
class NotificationHelper(private val context: Context) {
  var backageName :String = ""

  private var channel_name = Const.INCOMING_CALL_CHANNEL_NAME;
  private var channel_id = Const.INCOMING_CALL_CHANNEL_ID;
    companion object {
        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */


        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
    }

    private val notificationManager: NotificationManager =
        context.getSystemService() ?: throw IllegalStateException()

     fun setUpNotificationChannels(channelId:String,channelName: String) : String{
      val prefs: SharedPreferences = context.getSharedPreferences(Const.PREFS_NAME,
        Service.MODE_PRIVATE
      )
      backageName = prefs.getString(Const.BAKAGE_NAME, "com.iriscrm").toString() //"No name defined" is the default value.
      log("======================== setUpNotificationChannels get Bakage name =====================${backageName}")

      channel_name=channelName
      channel_id=channelId

        if (notificationManager.getNotificationChannel(channelId) == null) {
          val channel =  NotificationChannel(
            channelId,
            channelName,
            // The importance must be IMPORTANCE_HIGH to show Bubbles.
            NotificationManager.IMPORTANCE_HIGH,
          ).apply {
            description = context.getString(R.string.channel_new_calls_description)
          }
          channel.lightColor = Color.BLUE
          channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
          notificationManager.createNotificationChannel(channel)
        }
        updateShortcuts(null)

      return channelId
    }

    @WorkerThread
    fun updateShortcuts(importantContact: Contact?,) {
      val shortcutManager = context.getSystemService(ShortcutManager::class.java)

      if (shortcutManager!!.isRequestPinShortcutSupported) {

        if(importantContact != null){
          log("======================updateShortcuts ${importantContact.scId}")

          val icon = Icon.createWithAdaptiveBitmap(
            context.resources.assets.open("app_icon.png").use { input ->
              BitmapFactory.decodeStream(input)
            })

          val personIcon = Icon.createWithAdaptiveBitmap(
            context.resources.assets.open("icon-missed-call.png").use { input ->
              BitmapFactory.decodeStream(input)
            })
            // Assumes there's already a shortcut with the ID "my-shortcut".
            // The shortcut must be enabled.
            val pinShortcutInfo = ShortcutInfo.Builder(context,
              importantContact.scId)
              .setLocusId(LocusId(importantContact.scId))
              .setShortLabel("Missed Call")
              .setIcon(icon)
              //.setIntent(Intent(Intent.ACTION_MAIN))
              .setLongLived(true)
              .setCategories(setOf("com.iriscrm.category.TEXT_SHARE_TARGET"))
              .setIntent(
                Intent(context, Class.forName(backageName+ ".MainActivity")::class.java)
                  .setAction(Intent.ACTION_VIEW)
                  .setData(
                    Uri.parse(
                      "https://com.iriscrm/call/${importantContact.id}"
                    )
                  )
              )
              .setPerson(
                android.app.Person.Builder()
                  .setName(importantContact.name)
                  .setIcon(icon)
                  .build()
              )
              .build()

            // Create the PendingIntent object only if your app needs to be notified
            // that the user allowed the shortcut to be pinned. Note that, if the
            // pinning operation fails, your app isn't notified. We assume here that the
            // app has implemented a method called createShortcutResultIntent() that
            // returns a broadcast intent.
            val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo)

            // Configure the intent so that your app's broadcast receiver gets
            // the callback successfully.For details, see PendingIntent.getBroadcast().
            val successCallback = PendingIntent.getBroadcast(context, REQUEST_BUBBLE,
              pinnedShortcutCallbackIntent,flagUpdateCurrent(mutable = true) )

            shortcutManager.requestPinShortcut(pinShortcutInfo,
              successCallback.intentSender)

          val listShorts = listOf(importantContact.scId)
          shortcutManager.removeLongLivedShortcuts(listShorts)
          shortcutManager.removeAllDynamicShortcuts()
        /* // val pinShortcutInfo = ShortcutInfo.Builder(context, importantContact.shortcutId).build()
          val icon = IconCompat.createWithAdaptiveBitmap(
            context.resources.assets.open("ic_notify.png").use { input ->
              BitmapFactory.decodeStream(input)
            })

                val shortcut = ShortcutInfoCompat.Builder(context, importantContact.shortcutId)
                  .setLocusId(LocusIdCompat(importantContact.shortcutId))
                  .setIntent(Intent(Intent.ACTION_MAIN))
                  //.setCategories(setOf(Notification.CATEGORY_CALL))
                  .setActivity(ComponentName(context, Class.forName(backageName+ ".MainActivity")::class.java))
                  .setShortLabel(importantContact.name)
                  .setLongLabel(importantContact.message)
                  .setIcon(icon)
                  .setLongLived(true)
                  .setCategories(setOf("com.iriscrm.category.TEXT_SHARE_TARGET"))
                 *//* .setIntent(
                    Intent(context, Class.forName(backageName+ ".MainActivity")::class.java)
                      .setAction(Intent.ACTION_VIEW)
                      .setData(
                        Uri.parse(
                          "https://com.iriscrm/call/${importantContact.id}"
                        )
                      )
                  )*//*
                  .setPerson(
                    Person.Builder()
                      .setName(importantContact.name)
                       .setIcon(icon)
                      .build()
                  )
                  .build()
        //  val contentUri = "https://com.iriscrm/call/${importantContact.shortcutId}".toUri()

        *//*  val pinnedShortcutCallbackIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            // Launch BubbleActivity as the expanded bubble.
            Intent(context, BubbleActivity::class.java)
              .setAction(Intent.ACTION_VIEW)
              .setData(contentUri),
            flagUpdateCurrent(mutable = true)
          )*//*

        //  shortcutManager.requestPinShortcut(shortcut,
         //   pinnedShortcutCallbackIntent.intentSender)
          ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
*/
        }

      }

    }

    private fun flagUpdateCurrent(mutable: Boolean): Int {
        return if (mutable) {
            if (Build.VERSION.SDK_INT >= 31) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
    }


  fun setupNotificationBuilder(call: Call, fromUser: Boolean, update: Boolean = false, channelId : String): Notification {

    updateShortcuts(call.contact)


    val icon = IconCompat.createWithAdaptiveBitmap(
      context.resources.assets.open("icon-missed-call.png").use { input ->
        BitmapFactory.decodeStream(input)
      })
    val user = Builder().setName(context.getString(R.string.sender_you)).build()
    val person = Builder().setName(call.contact.name).setIcon(icon).build()
    val contentUri = "https://com.iriscrm/call/${call.contact.id}".toUri()

    val pendingIntent = PendingIntent.getActivity(
      context,
      REQUEST_BUBBLE,
      // Launch BubbleActivity as the expanded bubble.
      Intent(context,Class.forName(backageName+ ".MainActivity")::class.java)
        .setAction(Intent.ACTION_VIEW)
        .setData(contentUri),
      flagUpdateCurrent(mutable = true)
    )
    // Let's add some more content to the notification in case it falls back to a normal
    // notification.
    val messagingStyle = NotificationCompat.MessagingStyle(user)
    val lastId = call.messages.last().id
    for (message in call.messages) {
      val m = NotificationCompat.MessagingStyle.Message(
        message.text,
        message.timestamp,
        if (message.isIncoming) person else null
      ).apply {
        if (message.photoUri != null) {
          setData(message.photoMimeType, message.photoUri)
        }
      }
      if (message.id < lastId) {
        messagingStyle.addHistoricMessage(m)
      } else {
        messagingStyle.addMessage(m)
      }
    }

    val builder = NotificationCompat.Builder(context, channel_id)
      // A notification can be shown as a bubble by calling setBubbleMetadata()
      .setBubbleMetadata(
        NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
          // The height of the expanded bubble.
          .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
          .apply {
            // When the bubble is explicitly opened by the user, we can show the bubble
            // automatically in the expanded state. This works only when the app is in
            // the foreground.
           // if (fromUser) {
              setAutoExpandBubble(false)
              setSuppressNotification(false)
          //  }
           // if (fromUser || update) {
             // setSuppressNotification(true)
                //  }
          }
          .build()
      )
      // The user can turn off the bubble in system settings. In that case, this notification
      // is shown as a normal notification instead of a bubble. Make sure that this
      // notification works as a normal notification as well.
      .setContentTitle(call.contact.name)
      .setSmallIcon(R.drawable.ic_notify)
      .setCategory(Notification.CATEGORY_CALL)
      .setShortcutId(call.contact.scId)
      .setTicker("Call_STATUS")
      // This ID helps the intelligence services of the device to correlate this notification
      // with the corresponding dynamic shortcut.
      .setLocusId(LocusIdCompat(call.contact.scId))
      .addPerson(person)
      .setShowWhen(true)
      // The content Intent is used when the user clicks on the "Open Content" icon button on
      // the expanded bubble, as well as when the fall-back notification is clicked.
      .setContentIntent(
        PendingIntent.getActivity(
          context,
          REQUEST_CONTENT,
          Intent(context, Class.forName(backageName+ ".MainActivity")::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(contentUri),
          flagUpdateCurrent(mutable = false)
        )
      )
      // Direct Reply
      .addAction(
        NotificationCompat.Action
          .Builder(
            IconCompat.createWithResource(context, R.drawable.ic_send),
            context.getString(R.string.label_reply),
            PendingIntent.getBroadcast(
              context,
              REQUEST_CONTENT,
              Intent(context, Class.forName(backageName+ ".MainActivity")::class.java).setData(contentUri),
              flagUpdateCurrent(mutable = true)
            )
          )
          .build()
      )
      // Let's add some more content to the notification in case it falls back to a normal
      // notification.
      .setStyle(messagingStyle)
      .setWhen(call.messages.last().timestamp)
    // Don't sound/vibrate if an update to an existing notification.
    if (update) {
      builder.setOnlyAlertOnce(true)
    }
    return builder.build()
  }

  @WorkerThread
    fun showNotification(call: Call, fromUser: Boolean, update: Boolean = false) {


    val prefs: SharedPreferences = context.getSharedPreferences(Const.PREFS_NAME,
      Service.MODE_PRIVATE
    )
    backageName = prefs.getString(Const.BAKAGE_NAME, "com.iriscrm").toString() //"No name defined" is the default value.
    log("======================== showNotification get Bakage name =====================${backageName}")

    updateShortcuts(call.contact)
        val icon = IconCompat.createWithAdaptiveBitmapContentUri(call.contact.iconUri)
        val user = Builder().setName(context.getString(R.string.sender_you)).build()
        val person = Builder().setName(call.contact.name).setIcon(icon).build()
        val contentUri = "https://com.iriscrm/call/${call.contact.id}".toUri()

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_BUBBLE,
            // Launch BubbleActivity as the expanded bubble.
            Intent(context,Class.forName(backageName+ ".MainActivity")::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(contentUri),
            flagUpdateCurrent(mutable = true)
        )
        // Let's add some more content to the notification in case it falls back to a normal
        // notification.
        val messagingStyle = NotificationCompat.MessagingStyle(user)
        val lastId = call.messages.last().id
        for (message in call.messages) {
            val m = NotificationCompat.MessagingStyle.Message(
                message.text,
                message.timestamp,
                if (message.isIncoming) person else null
            ).apply {
                if (message.photoUri != null) {
                    setData(message.photoMimeType, message.photoUri)
                }
            }
            if (message.id < lastId) {
                messagingStyle.addHistoricMessage(m)
            } else {
                messagingStyle.addMessage(m)
            }
        }

        val builder = NotificationCompat.Builder(context, channel_id)
            // A notification can be shown as a bubble by calling setBubbleMetadata()
            .setBubbleMetadata(
                NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
                    // The height of the expanded bubble.
                    .setDesiredHeight(context.resources.getDimensionPixelSize(R.dimen.bubble_height))
                    .apply {
                        // When the bubble is explicitly opened by the user, we can show the bubble
                        // automatically in the expanded state. This works only when the app is in
                        // the foreground.
                        if (fromUser) {
                            setAutoExpandBubble(true)
                        }
                        if (fromUser || update) {
                            setSuppressNotification(true)
                        }
                    }
                    .build()
            )
            // The user can turn off the bubble in system settings. In that case, this notification
            // is shown as a normal notification instead of a bubble. Make sure that this
            // notification works as a normal notification as well.
            .setContentTitle(call.contact.name)
            .setSmallIcon(R.drawable.ic_message)
            .setCategory(Notification.CATEGORY_CALL)
            .setShortcutId(call.contact.scId)
            // This ID helps the intelligence services of the device to correlate this notification
            // with the corresponding dynamic shortcut.
            .setLocusId(LocusIdCompat(call.contact.scId))
            .addPerson(person)
            .setShowWhen(true)
            // The content Intent is used when the user clicks on the "Open Content" icon button on
            // the expanded bubble, as well as when the fall-back notification is clicked.
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    REQUEST_CONTENT,
                    Intent(context, Class.forName(backageName+ ".MainActivity")::class.java)
                        .setAction(Intent.ACTION_VIEW)
                        .setData(contentUri),
                    flagUpdateCurrent(mutable = false)
                )
            )
            // Direct Reply
            .addAction(
                NotificationCompat.Action
                    .Builder(
                        IconCompat.createWithResource(context, R.drawable.ic_send),
                        context.getString(R.string.label_reply),
                        PendingIntent.getBroadcast(
                            context,
                            REQUEST_CONTENT,
                            Intent(context, Class.forName(backageName+ ".MainActivity")::class.java).setData(contentUri),
                            flagUpdateCurrent(mutable = true)
                        )
                    ).build()
            )
            // Let's add some more content to the notification in case it falls back to a normal
            // notification.
            .setStyle(messagingStyle)
            .setWhen(call.messages.last().timestamp)
        // Don't sound/vibrate if an update to an existing notification.
        if (update) {
            builder.setOnlyAlertOnce(true)
        }
        notificationManager.notify(call.contact.id.toInt(), builder.build())
    }

    private fun dismissNotification(id: String) {
        notificationManager.cancel(id.toInt())
    }

    fun canBubble(contact: Contact): Boolean {
        val channel = notificationManager.getNotificationChannel(
          channel_id,
            contact.scId
        )
        return notificationManager.areBubblesAllowed() || channel?.canBubble() == true
    }

    fun updateNotification(call: Call, callId: String, prepopulatedMsgs: Boolean) {
        if (!prepopulatedMsgs) {
            // Update notification bubble metadata to suppress notification so that the unread
            // message badge icon on the collapsed bubble is removed.
            showNotification(call, fromUser = false, update = true)
        } else {
            dismissNotification(callId)
        }
    }
}

