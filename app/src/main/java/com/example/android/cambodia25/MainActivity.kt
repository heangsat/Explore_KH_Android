package com.example.android.cambodia25

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.android.cambodia25.ui.screen.LoginScreen
import com.example.android.cambodia25.ui.screen.MainScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }

            if (isLoggedIn) {
                MainScreen()
            } else {
                LoginScreen(onSignInSuccess = { isLoggedIn = true })
            }
        }
    }
}
