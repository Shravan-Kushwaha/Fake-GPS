package com.fakegps.extension

import android.app.Activity
import android.view.ViewGroup
import com.fakegps.utils.Logger

enum class ValidationStatus {
    Empty_Password,
    EMPTY_NEW_PASSWORD,
    EMPTY_EMAIL,
    EMPTY_GENDER,
    UNKNOWN
}

object Validation {
    fun showMessageDialog(
        id: ViewGroup,
        isError: Boolean = false,
        activity: Activity,
        validationStatus: ValidationStatus
    ) {
        val message = getMessage(activity = activity, validationStatus = validationStatus)
        if (!message.isNullOrEmpty()) {
            activity.showToast(id, message, isError = isError)
        }

    }

    private fun getMessage(activity: Activity, validationStatus: ValidationStatus): String {
        return when (validationStatus) {
          //  ValidationStatus.Empty_Password -> activity.getString(R.string.please_enter_password)
           // ValidationStatus.EMPTY_NEW_PASSWORD -> activity.getString(R.string.please_enter_new_password)
            ValidationStatus.UNKNOWN -> ""
            else -> ""
        }
    }

    fun showMessageDialog(activity: Activity, validationStatus: ValidationStatus) {
        val message = getMessage(activity, validationStatus)
        if (message.isNotEmpty()) {
            activity.showErrorAlert(message = message)
            //activity.alertDialog(message = message)
        }
        Logger.e( "Validation message=>${message.isNotEmpty()}")
    }

}