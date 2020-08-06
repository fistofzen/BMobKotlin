package com.company.bmobkotlin.app

import android.content.Context
import android.content.Intent
import com.company.bmobkotlin.R
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import org.slf4j.LoggerFactory
import java.lang.Exception

import java.util.LinkedList
import java.util.Queue

class ErrorPresenterByNotification(currentContext: Context): ErrorPresenter {
    private val context: Context = currentContext.applicationContext

    override fun presentError(errorTitle: String, errorDetail: String, exception: Exception?, isFatal: Boolean) {
        val startNotification = Intent(context, ErrorNotificationDialog::class.java)
        startNotification.putExtra(ErrorNotificationDialog.TITLE, errorTitle)
        startNotification.putExtra(ErrorNotificationDialog.MSG, getErrorMessage(errorDetail, exception))
        var logString = "$errorTitle: $errorDetail"
        if(isFatal) logString = "Fatal - $logString"
        LOGGER.error(logString, exception);
        startNotification.putExtra(ErrorNotificationDialog.FATAL, isFatal)
        startNotification.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        synchronized(notificationIntentQueue){
            if(!isErrorDialogShowing){
                context.startActivity(startNotification)
                isErrorDialogShowing = true
            }
            else notificationIntentQueue.add(startNotification)
        }
    }

    private fun getErrorMessage(defaultMessage: String, exception: Exception?): String {
        return exception?.message?.let { msg ->
            when {
                ErrorMessage.REQUEST_ENTITY_TOO_LARGE_ERROR in msg ->
                    "$defaultMessage ${context.resources.getString(R.string.offline_backend_big_data_error_detail)}"
                ErrorMessage.GATEWAY_TIME_OUT_ERROR in msg ->
                    "$defaultMessage ${context.resources.getString(R.string.offline_backend_time_out_error_detail)}"
                else ->
                    "$defaultMessage\n${exception.localizedMessage}"
            }
        } ?: defaultMessage
    }

    companion object {
        private var notificationIntentQueue: Queue<Intent> = LinkedList<Intent>()
            private val LOGGER = LoggerFactory.getLogger(ErrorPresenterByNotification::class.java)
        private var isErrorDialogShowing = false

        fun errorDialogDismissed(){
            synchronized(notificationIntentQueue) {
                val nextIntent = notificationIntentQueue.poll()
                if (nextIntent != null) {
                    AppLifecycleCallbackHandler.getInstance().activity!!.startActivity(nextIntent)
                } else {
                    isErrorDialogShowing = false
                }
            }
        }
    }
}