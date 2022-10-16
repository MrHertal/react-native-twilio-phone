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

import android.content.Context
import android.net.Uri
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface CallRepository {
  fun getContact(contact: Contact): LiveData<Contact>
  fun findContact(contact: Contact): LiveData<Contact?>
  fun findMessages(call: Call): LiveData<List<Message>>
  fun sendMessage(call: Call, text: String, photoUri: Uri?, photoMimeType: String?)
  fun updateNotification(call: Call)
  fun activateCall(call: Call)
  fun deactivateCall(call: Call)
  fun showAsBubble(call: Call)
  fun canBubble(call: Call): Boolean
}

class DefaultCallRepository internal constructor(
  private val notificationHelper: NotificationHelper,
  private val executor: Executor
) : CallRepository {

  companion object {
    private var instance: DefaultCallRepository? = null

    fun getInstance(context: Context): DefaultCallRepository {
      return instance ?: synchronized(this) {
        instance ?: DefaultCallRepository(
          NotificationHelper(context),
          Executors.newFixedThreadPool(4)
        ).also {
          instance = it
        }
      }
    }
  }

  private var currentCall: String = 0L.toString()

/*  private val call = Contact.CONTACTS.map { contact ->
    contact.id to Call(contact)
  }.toMap()*/

  init {
    //TODO CHANGE WHEN USE NOT USED NOW
   // notificationHelper.setUpNotificationChannels()
  }

  @MainThread
  override fun getContact(contact: Contact): LiveData<Contact> {
    return MutableLiveData<Contact>().apply {
      postValue(contact)
    }
  }

  @MainThread
  override fun findContact(contact: Contact): LiveData<Contact?> {
    return MutableLiveData<Contact>().apply {
      postValue(contact)
    }
  }

  @MainThread
  override fun findMessages(call: Call): LiveData<List<Message>> {
    return object : LiveData<List<Message>>() {

      private val listener = { messages: List<Message> ->
        postValue(messages)
      }

      override fun onActive() {
        value = call.messages
        call.addListener(listener)
      }

      override fun onInactive() {
        call.removeListener(listener)
      }
    }
  }

  @MainThread
  override fun sendMessage(call: Call, text: String, photoUri: Uri?, photoMimeType: String?) {
    call.addMessage(Message.Builder().apply {
      sender = 0L.toString() // User
      this.text = text
      timestamp = System.currentTimeMillis()
      this.photo = photoUri
      this.photoMimeType = photoMimeType
    })
    executor.execute {
      // The animal is typing...
      Thread.sleep(5000L)
      // Receive a reply.
      call.addMessage(call.contact.reply(text))
      // Show notification if the call is not on the foreground.
      if (call.contact.id != currentCall) {
        notificationHelper.showNotification(call, false)
      }
    }
  }

  override fun updateNotification(call: Call) {
    notificationHelper.showNotification(call, false, true)
  }

  override fun activateCall(call: Call) {
    currentCall = call.contact.id
    val isPrepopulatedMsgs =
      call.messages.size == 2 && call.messages[0] != null && call.messages[1] != null
    notificationHelper.updateNotification(call, call.contact.id, isPrepopulatedMsgs)
  }

  override fun deactivateCall(call: Call) {
    if (currentCall == call.contact.id) {
      currentCall = "0"
    }
  }

  override fun showAsBubble(call: Call) {
    executor.execute {
      notificationHelper.showNotification(call, true)
    }
  }

  override fun canBubble(call: Call): Boolean {
    return notificationHelper.canBubble(call.contact)
  }
}
