package com.uploaderprogress

import android.content.Intent
import android.content.Context
import android.app.PendingIntent

class NotificationActions {
  var INTENT_ACTION = "com.uploaderprogress.NOTIFICATION_ACTION"

  val PARAM_ACTION = "action"
  val PARAM_UPLOAD_ID = "uploadId"
  val ACTION_CANCEL_UPLOAD = "cancelUpload"

  fun getCancelUploadAction(context: Context?, requestCode: Int, uploadId: String?): PendingIntent {
    val intent = Intent(INTENT_ACTION)
    intent.putExtra(PARAM_ACTION, ACTION_CANCEL_UPLOAD)
    intent.putExtra(PARAM_UPLOAD_ID, uploadId)
    return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }
}
