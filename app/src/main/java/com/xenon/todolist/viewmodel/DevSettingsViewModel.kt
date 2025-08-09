package com.xenon.todolist.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xenon.todolist.SharedPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class DevSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferenceManager = SharedPreferenceManager(application)

    private val _devModeToggleState = MutableStateFlow(sharedPreferenceManager.developerModeEnabled)
    val devModeToggleState: StateFlow<Boolean> = _devModeToggleState.asStateFlow()

    fun setDeveloperModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sharedPreferenceManager.developerModeEnabled = enabled
            _devModeToggleState.value = enabled

            if (!enabled) {
            }
        }
    }

    fun triggerExampleDevAction() {
        viewModelScope.launch {
            Toast.makeText(getApplication(), "Developer Action Triggered!", Toast.LENGTH_SHORT).show()
        }
    }
}