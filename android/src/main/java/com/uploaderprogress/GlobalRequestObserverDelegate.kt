package com.uploaderprogress

import android.util.Log
import android.content.Context
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.DeviceEventManagerModule

import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate

class GlobalRequestObserverDelegate(
  reactContext: ReactApplicationContext,
) : RequestObserverDelegate {
  private val TAG = "UploadReceiver"
  private val notificationManager = reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  private var reactContext: ReactApplicationContext = reactContext

  override fun onCompleted(context: Context, uploadInfo: UploadInfo) {

  }

  override fun onCompletedWhileNotObserving() {

  }

  override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
    val message = "Error while uploading: ${exception.message}"
    Log.e(TAG, message)
    val params = Arguments.createMap()
    params.putString("id", uploadInfo.uploadId)

    if(exception != null){
      params.putString("error", exception.message)
    } else {
      params.putString("error", "Unknown error")
    }
  }

  override fun onProgress(context: Context, uploadInfo: UploadInfo) {

    //  val progress = uploadInfo.progressPercent
    //     val notification = NotificationCompat.Builder(context, "BackgroundUploadChannel")
    //         .setContentTitle("Uploading...")
    //         .setContentText("$progress% uploaded")
    //         .setSmallIcon(android.R.drawable.stat_sys_upload)
    //         .setProgress(100, progress, false)
    //         .build()
    // notificationManager.notify(uploadInfo.uploadId.hashCode(), notification)
    Log.e(TAG, "onProgress upload info: ${uploadInfo}")
    val params = Arguments.createMap()
    params.putString("id", uploadInfo.uploadId)
    params.putInt("progress", uploadInfo.progressPercent)
    sendEvent("progress", params, context)
  }

  override fun onSuccess(context: Context, uploadInfo: UploadInfo, serverResponse: ServerResponse) {
    val message = "Upload successful: ${serverResponse}"
    Log.i(TAG, message)
    val headers = Arguments.createMap()
    for ((key, value) in serverResponse.headers) {
      headers.putString(key, value)
    }
    val params = Arguments.createMap()
    params.putString("id", uploadInfo.uploadId)
    params.putInt("responseCode", serverResponse.code)
    params.putString("responseBody", serverResponse.bodyString)
    params.putMap("responseHeaders", headers)
    sendEvent("completed", params, context)
  }

  private fun sendEvent(eventName: String, params: WritableMap?, context: Context) {
    reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.emit("UploaderProgress-$eventName", params)
      ?: Log.e(TAG, "Failed to send event $eventName")
  }
}
