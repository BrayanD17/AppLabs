package com.labs.applabs.models

data class FormOperador(
    val activityStatus: Int = 0,
    val closingDate: com.google.firebase.Timestamp? = null,
    val createdDate: com.google.firebase.Timestamp? = null,
    val nameForm: String = "",
    val semester: String = "",
    val startDate: com.google.firebase.Timestamp? = null,
    val urlApplicationForm: String = "",
    val year: Int = 0
)
