package com.uploaderprogress

import java.io.File
import java.util.concurrent.TimeUnit

import android.util.Log
import android.net.Uri
import android.os.Build
import android.content.Context
import android.app.Application
import android.webkit.MimeTypeMap
import android.app.NotificationChannel
import android.app.NotificationManager

import com.facebook.react.bridge.*
import com.facebook.react.BuildConfig
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule

import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.UploadServiceConfig.httpStack
import net.gotev.uploadservice.UploadServiceConfig.initialize
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.observer.request.GlobalRequestObserver
import net.gotev.uploadservice.okhttp.OkHttpStack
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import net.gotev.uploadservice.protocols.binary.BinaryUploadRequest

import okhttp3.OkHttpClient

@ReactModule(name = UploaderProgressModule.NAME)
class UploaderProgressModule(val reactContext: ReactApplicationContext) :
  NativeUploaderProgressSpec(reactContext), LifecycleEventListener {

  private val CONTEXT = "RN_UPLOADER"
  private var notificationChannelID = "BackgroundUploadChannel"
  private var isGlobalRequestObserver = false

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun add(a: Double, b: Double): Double {
    return a + b
  }

    override fun getFileInfo(path: String?, promise: Promise) {
      Log.d(CONTEXT, "getFileInfo: $path")
      val params = Arguments.createMap()
      try {
      val decodedPath = Uri.decode(path)
      val fileInfo = File(decodedPath)

      params.putString("name", fileInfo.getName())
      if(!fileInfo.exists()) {
        params.putBoolean("exists", false)
      } else {
        params.putBoolean("exists", true)
        params.putString("path", fileInfo.getAbsolutePath())
        params.putString("size", fileInfo.length().toString())

        val extension = MimeTypeMap.getFileExtensionFromUrl(fileInfo.getAbsolutePath());
        params.putString("extension", extension)

        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        params.putString("mimeType", mimeType)
      }
      promise.resolve(params)
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e(CONTEXT, e.message, e)
        promise.reject(e)
      }
  }

    private fun createNotificationChannel() {
    if(Build.VERSION.SDK_INT >= 26) {
      val notificationManager = reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(notificationChannelID, "Uploads", NotificationManager.IMPORTANCE_DEFAULT)
      notificationManager.createNotificationChannel(channel)
    }
  }

    private fun configureHTTPStack(options: ReadableMap, promise: Promise) {
    var followRedirects = true
    var followSslRedirects = true
    var connectTimeout = 15
    var readTimeout = 30
    var writeTimeout = 30
    var retryOnConnectionFailure = true

    if(options.hasKey("followRedirects")) {
      if(options.getType("followRedirects") != ReadableType.Boolean) {
        promise.reject(IllegalArgumentException("followRedirects must be a boolean"))
        return
      }
      followRedirects = options.getBoolean("followRedirects")
    }

    if(options.hasKey("followSslRedirects")) {
      if(options.getType("followSslRedirects") != ReadableType.Boolean) {
        promise.reject(IllegalArgumentException("followSslRedirects must be a boolean"))
        return
      }
      followSslRedirects = options.getBoolean("followSslRedirects")
    }

    if(options.hasKey("connectTimeout")) {
      if(options.getType("connectTimeout") != ReadableType.Number) {
        promise.reject(IllegalArgumentException("connectTimeout must be a number"))
        return
      }
      connectTimeout = options.getInt("connectTimeout")
    }

    if(options.hasKey("readTimeout")) {
      if(options.getType("readTimeout") != ReadableType.Number) {
        promise.reject(IllegalArgumentException("readTimeout must be a number"))
        return
      }
      readTimeout = options.getInt("readTimeout")
    }

    if(options.hasKey("writeTimeout")) {
      if(options.getType("writeTimeout") != ReadableType.Number) {
        promise.reject(IllegalArgumentException("writeTimeout must be a number"))
        return
      }
      writeTimeout = options.getInt("writeTimeout")
    }

    if(options.hasKey("retryOnConnectionFailure")) {
      if(options.getType("retryOnConnectionFailure") != ReadableType.Boolean) {
        promise.reject(IllegalArgumentException("retryOnConnectionFailure must be a boolean"))
        return
      }
      retryOnConnectionFailure = options.getBoolean("retryOnConnectionFailure")
    }

    httpStack = OkHttpStack(OkHttpClient.Builder()
      .followRedirects(followRedirects)
      .followSslRedirects(followSslRedirects)
      .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
      .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
      .writeTimeout(writeTimeout.toLong(), TimeUnit.SECONDS)
      .retryOnConnectionFailure(retryOnConnectionFailure)
      .cache(null)
      .build())
  }

  /**
  * Starts a file upload.
  * Returns a promise with the uploadId
   */
  @ReactMethod
  override fun startUpload(options: ReadableMap, promise: Promise) {
    for(key in arrayOf("url", "path", "method")) {
      if(!options.hasKey(key)) {
        promise.reject(IllegalArgumentException("$key is required"))
        return
      }
      if(options.getType(key) != ReadableType.String) {
        promise.reject(IllegalArgumentException("$key must be a string"))
        return
      }
    }

    if(options.hasKey("headers") && options.getType("headers") != ReadableType.Map) {
      promise.reject(IllegalArgumentException("headers must be a hash"))
      return
    }

    if(options.hasKey("notification") && options.getType("notification") != ReadableType.Map) {
      promise.reject(IllegalArgumentException("notification must be a hash"))
      return
    }
    configureHTTPStack(options, promise)
    var requestType: String? = "raw"
    if(options.hasKey("type")) {
      requestType = options.getString("type")
      if(requestType == null) {
        promise.reject(IllegalArgumentException("type must be a string"))
        return
      }
      if(requestType != "raw" && requestType != "multipart") {
        promise.reject(IllegalArgumentException("type must be 'raw' or 'multipart'"))
        return
      }
    }

    val notification: WritableMap = WritableNativeMap()
    notification.putBoolean("enabled", true)
    if(options.hasKey("notification")){
      notification.merge(options.getMap("notification") ?: WritableNativeMap())
    }

    val application = reactContext.applicationContext as Application
    reactContext.addLifecycleEventListener(this)

    if(notification.hasKey("notificationChannel")) {
      notificationChannelID = notification.getString("notificationChannel")!!
    }

    createNotificationChannel()
    Log.d(CONTEXT, "Notification channel created")
    Log.i(CONTEXT, "Application ${application.packageName} is in debug mode: ${BuildConfig.DEBUG}")
    initialize(application, notificationChannelID, BuildConfig.DEBUG)

    if(!isGlobalRequestObserver) {
      isGlobalRequestObserver = true
      GlobalRequestObserver(application, GlobalRequestObserverDelegate(reactContext))
    }

    val url = options.getString("url")
    val filePath = options.getString("path")
    val method = if(options.hasKey("method") && options.getType("method") == ReadableType.String) options.getString("method") else "POST"
    val maxRetries = if(options.hasKey("maxRetries") && options.getType("maxRetries") == ReadableType.Number) options.getInt("maxRetries") else 3
    val customUploadId = if(options.hasKey("customUploadId") && options.getType("method") == ReadableType.String) options.getString("customUploadId") else null

    try {
      val request = if(requestType == "raw") {
        BinaryUploadRequest(this.reactApplicationContext, url!!)
          .setFileToUpload(filePath!!)
      } else {
        if(!options.hasKey("field")) {
          promise.reject(IllegalArgumentException("field is required for multipart request"))
          return
        }

        if(options.getType("field") != ReadableType.String) {
          promise.reject(IllegalArgumentException("field must be a string"))
          return
        }

        MultipartUploadRequest(this.reactApplicationContext, url!!)
          .addFileToUpload(filePath!!, options.getString("field")!!)
      }
      request.setMethod(method!!)
        .setMaxRetries(maxRetries)

      if(notification.getBoolean("enabled") && false) {
        val notificationConfig = UploadNotificationConfig(
          notificationChannelId = notificationChannelID,
          isRingToneEnabled = false,
          progress = UploadNotificationStatusConfig(
            title = notification.getString("onProgressTitle") ?: "Uploading",
            message = notification.getString("onProgressMessage") ?: "Uploading in progress",
            autoClear = notification.hasKey("progressAutoClear") && notification.getBoolean("progressAutoClear")
          ),
          success = UploadNotificationStatusConfig(
            title = notification.getString("onSuccessTitle") ?: "Upload Success",
            message = notification.getString("onSuccessMessage") ?: "Upload success",
            autoClear = notification.hasKey("autoClear") && notification.getBoolean("autoClear")
          ),
          error = UploadNotificationStatusConfig(
            title = notification.getString("onErrorTitle") ?: "Upload Error",
            message = notification.getString("onErrorMessage") ?: "Upload error",
            autoClear = notification.hasKey("autoClear") && notification.getBoolean("autoClear")
          ),
          cancelled = UploadNotificationStatusConfig(
            title = notification.getString("onCancelledTitle") ?: "Upload Cancelled",
            message = notification.getString("onCancelledMessage") ?: "Upload cancelled",
            autoClear = notification.hasKey("autoClear") && notification.getBoolean("autoClear")
          )
        )
        request.setNotificationConfig{_, _ ->
          notificationConfig
        }
      }

      if(options.hasKey("parameters")) {
        if(requestType == "raw") {
          promise.reject(IllegalArgumentException("parameters are not allowed for raw request"))
          return
        }
        val parameters = options.getMap("parameters")
        val keys = parameters!!.keySetIterator()
        while(keys.hasNextKey()) {
          val key = keys.nextKey()
          if(parameters.getType(key) != ReadableType.String) {
            promise.reject(IllegalArgumentException("Parameters values must be string key/values. Value was invalid for '$key'"))
            return
          }
          val value = parameters.getString(key)!!
          request.addParameter(key, value)
        }
      }

      if(options.hasKey("headers")) {
        val headers = options.getMap("headers")
        val keys = headers!!.keySetIterator()
        while(keys.hasNextKey()) {
          val key = keys.nextKey()
          if(headers.getType(key) != ReadableType.String) {
            promise.reject(IllegalArgumentException("Header values must be string key/values. Value was invalid for '$key'"))
            return
          }
          val value = headers.getString(key)!!
          request.addHeader(key, value)
        }
      }

      if(customUploadId != null) {
        request.setUploadID(customUploadId)
      }

      val uploadId = request.startUpload()
      promise.resolve(uploadId)
    }
    catch(e: Exception) {
      e.printStackTrace()
      Log.e(CONTEXT, e.message, e)
      promise.reject(e)
    }
  }

  companion object {
    const val NAME = "UploaderProgress"
  }

    override fun onHostResume() {

  }

  override fun onHostPause() {

  }

  override fun onHostDestroy() {

  }
}
