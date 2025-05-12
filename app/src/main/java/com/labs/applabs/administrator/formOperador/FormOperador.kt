package com.labs.applabs.administrator.formOperador

data class FormOperador(
    val activityStatus: Int = 0,
    val closingDate: com.google.firebase.Timestamp? = null,
    val createdDate: com.google.firebase.Timestamp? = null,
    val nameForm: String = "",
    val requirementsAndBenefits: List<String> = emptyList(),
    val semester: String = "",
    val startDate: com.google.firebase.Timestamp? = null,
    val urlApplicationForm: String = "",
    val year: Int = 0
)
