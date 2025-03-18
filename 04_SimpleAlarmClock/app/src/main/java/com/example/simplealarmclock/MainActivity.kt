package com.example.simplealarmclock

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var timePicker: TimePicker
    private lateinit var setAlarmButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize UI components
        timePicker = findViewById(R.id.timePicker)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        
        // Set 24-hour view for the TimePicker
        timePicker.setIs24HourView(true)
        
        // Set click listener for the button
        setAlarmButton.setOnClickListener {
            setAlarm()
        }
    }
    
    private fun setAlarm() {
        // Get hour and minute from time picker
        val hour: Int
        val minute: Int
        
        // Different API level handling for TimePicker
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = timePicker.hour
            minute = timePicker.minute
        } else {
            hour = timePicker.currentHour
            minute = timePicker.currentMinute
        }
        
        // Create an intent to set an alarm
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Wake up for class!")
            putExtra(AlarmClock.EXTRA_SKIP_UI, false) // Set to true if you want to skip the UI and set the alarm directly
        }
        
        // Check if there's an app that can handle this intent
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            Toast.makeText(
                this,
                "Alarm set for $hour:${String.format("%02d", minute)}",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Handle the case where no app can handle the intent
            Toast.makeText(
                this,
                "No alarm app available on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}