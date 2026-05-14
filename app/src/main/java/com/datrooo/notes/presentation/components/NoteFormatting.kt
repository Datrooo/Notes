package com.datrooo.notes.presentation.components

import java.text.DateFormat
import java.util.Date

fun Long.formatForUi(): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))
}
