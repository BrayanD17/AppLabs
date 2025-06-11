package com.labs.applabs.models

data class ScheduleSelection(
    val days: MutableMap<String, MutableSet<String>>
)