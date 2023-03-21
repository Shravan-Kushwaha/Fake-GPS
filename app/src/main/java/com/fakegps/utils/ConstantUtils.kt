package com.fakegps.utils

import java.util.*

object ConstantUtil {
    const val slash = "-"
    const val PASSWORD_LIMIT = 8
    const val USER_NAME_LIMIT = 2
    const val MOBILE_NO_LIMIT = 10
    const val ageLimit18year = 567993600000
    const val GOOGLE_MAP_ZOOM_LEVEL = 12f

    private val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    var genderArray = arrayOf("Select Gender", "Male", "Female")
    val options = arrayOf<CharSequence>("Take Photo", "Choose From Gallery", "Cancel")
    const val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    const val EMAIL_PATTERN_SPACE = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"

}