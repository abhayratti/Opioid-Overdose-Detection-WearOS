package com.plcoding.wearosstopwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Text

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionDialog()
        }
    }
}

@Composable
fun PermissionDialog()
{
    Text(text = "[APP NAME] needs access to your breathing rate and blood oxygen saturation to accurately detect overdoses. " +
            "Your data will not leave your device unless we detect an overdose. If we detect an overdose, we may use your vitals " +
            "from the overdose for the following purposes:\n\n1. To test our algorithm\n2. To improve the app (ex. we may train " +
            "algorithms to detect overdoses sooner)")
}
