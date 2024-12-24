package com.uploaderprogress

import android.util.Log
import android.content.Intent
import android.content.Context
import android.content.BroadcastReceiver

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

import net.gotev.uploadservice.UploadService

class NotificationActionsReceiver : BroadcastReceiver() {
    private val TAG = "NotificationActionsReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
      Log.e("ON_RECEIVE", "Received notification action: ${intent?.action}")
      if(intent == null || NotificationActions().INTENT_ACTION == intent.action) {
        return
      }

      if(NotificationActions().ACTION_CANCEL_UPLOAD == intent.getStringExtra(NotificationActions().PARAM_ACTION)) {
        onUserRequestedUploadCancellation(context!!, intent.getStringExtra(NotificationActions().PARAM_UPLOAD_ID)!!)
      }
    }

    private fun onUserRequestedUploadCancellation(context: Context, uploadId: String) {
      Log.e("CANCEL_UPLOAD", "User requested to cancel upload with ID: $uploadId")
      UploadService.stopUpload(uploadId)
      val params = Arguments.createMap()
      params.putString("id", uploadId)
      sendEvent("cancelled", params, context)
    }

  /**
  * Sends an event to the JS side
   */
    private fun sendEvent(eventName: String, params: WritableMap?, context: Context) {
        val reactContext = context as ReactApplicationContext
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params) ?: Log.e(TAG, "Failed to send event")
    }
}
