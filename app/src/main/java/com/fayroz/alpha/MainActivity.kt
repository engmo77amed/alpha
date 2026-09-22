package com.fayroz.alpha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fayroz.alpha.ui.AlphaApp
import com.fayroz.alpha.ui.FayrozAlphaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FayrozAlphaTheme { AlphaApp() } }
    }
}
