package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.todolist.ui.theme.TodolistTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodolistTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Todo List") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TodoListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TodoListScreen(modifier: Modifier = Modifier) {
    var taskName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var sortOrder by remember { mutableStateOf(SortOrder.DATE) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Task Name Input
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date Picker Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Due date: ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        }
        
        // Show DatePicker Dialog when requested
        if (showDatePicker) {
            SystemDatePickerDialog(
                initialDate = selectedDate,
                onDateSelected = { 
                    selectedDate = it
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add Task Button
        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    val newTask = Task(taskName, selectedDate)
                    tasks = tasks + newTask
                    taskName = ""
                    selectedDate = LocalDate.now()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sort Options Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort by:", modifier = Modifier.padding(end = 8.dp))
            
            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(if (sortOrder == SortOrder.NAME) "Name" else "Date")
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown menu"
                    )
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort by name") },
                        onClick = {
                            sortOrder = SortOrder.NAME
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by date") },
                        onClick = {
                            sortOrder = SortOrder.DATE
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task List
        val sortedTasks = when (sortOrder) {
            SortOrder.NAME -> tasks.sortedBy { it.name }
            SortOrder.DATE -> tasks.sortedBy { it.dueDate }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sortedTasks) { task ->
                TaskItem(task = task)
                Divider()
            }
        }
    }
}

enum class SortOrder {
    NAME, DATE
}

@Composable
fun TaskItem(task: Task) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = task.formattedDate(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SystemDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,  // Month is 0-based in Android DatePickerDialog
            initialDate.dayOfMonth
        ).apply {
            setOnCancelListener { onDismiss() }
        }
    }

    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose {
            datePickerDialog.dismiss()
        }
    }
}