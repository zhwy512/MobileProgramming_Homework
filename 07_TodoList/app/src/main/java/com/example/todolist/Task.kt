package com.example.todolist

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Task(
    val name: String,
    val dueDate: LocalDate
) {
    fun formattedDate(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return dueDate.format(formatter)
    }
}
