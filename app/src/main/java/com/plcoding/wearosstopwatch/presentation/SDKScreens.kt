package com.plcoding.wearosstopwatch.presentation

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Text

@Composable
fun SDK_Unavailable()
{
    Text(text = "Your device doesn't support the Health Connect platform. Try updating your operating system. If that does not fix this, Oxy will not run on your device")
}

@Composable
fun AppRequested()
{
    Text(text = "Good news! Your device supports the Health Connect platform. You just need to install the app")
}

@Composable
fun PermissionRequested()
{
    Text(text = "Oxy needs access to your breathing rate and blood oxygen saturation to accurately detect overdoses. Please grant that permission! ")
}

@Composable
fun PermissionDenied()
{
    Text(text = "It looks like you didn't give Oxy access to your breathing rate or blood oxygen saturation. We need that data to detect overdoses. Please change that permission in settings")
}