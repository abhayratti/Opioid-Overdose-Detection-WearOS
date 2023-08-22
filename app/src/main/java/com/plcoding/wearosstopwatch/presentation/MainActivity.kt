package com.plcoding.wearosstopwatch.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.Text

import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    private var healthConnectManager: HealthConnectManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Init the health connect manager
        healthConnectManager = HealthConnectManager(this)
        val context: Context = this
        val packageName = packageName
        val activityRef = this

        val initAPIsJob = CoroutineScope(Dispatchers.Default).launch {
            //We just set this so it can't be null!
            val status = healthConnectManager!!.initAndRequestPermissions(context, packageName)
            val viewModel = ViewModelProvider(activityRef)[OODViewModel::class.java]
            viewModel.updateSDKState(status)
            var currentStatus = status

            //Probably a better way of doing this, but we need to wait until (/if) the status is SUCCESS. Use a suspend function!
            suspend fun WaitForSDK(): Unit {
                val newStatus = healthConnectManager!!.GetStatus()
                if (newStatus != currentStatus) {
                    //Update the status and *maybe* return
                    currentStatus = newStatus
                    viewModel.updateSDKState(newStatus)
                    if (newStatus == HealthConnectStatus.SUCCESS) {
                        return
                    }
                } else
                {
                    delay(1000)
                }
            }

            val waitJob = CoroutineScope(Dispatchers.Default).launch {
                WaitForSDK()
            }
            //Wait until the SDK is available
            waitJob.join()

            //Create callback to update vitals data (will not terminate)
            healthConnectManager!!.CollectData()
        }

        setContent {
            WatchOSApp()
        }
    }
}

@Composable
fun WatchOSApp() {
    val viewModel = viewModel<OODViewModel>()
    val overdoseState by viewModel.overdoseState.collectAsStateWithLifecycle()
    val bloodOxygenLevel by viewModel.bloodOxygenLevel.collectAsStateWithLifecycle()
    val respiratoryRate by viewModel.respiratoryRate.collectAsStateWithLifecycle()
    val SDKState by viewModel.SDKState.collectAsStateWithLifecycle()

    //SDK availability screens
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (SDKState) {
            HealthConnectStatus.SDK_UNAVAILABLE -> SDK_Unavailable()
            HealthConnectStatus.APP_REQUESTED -> AppRequested()
            HealthConnectStatus.PERMISSION_REQUESTED -> PermissionRequested()
            HealthConnectStatus.PERMISSION_DENIED -> PermissionDenied()
            HealthConnectStatus.SUCCESS -> MainAppScreen(
                ODstate = overdoseState,
                breathingRate = bloodOxygenLevel,
                oxygenSaturation = respiratoryRate
            )
        }
    }
}

//Removed but kept as an archive to guide new UI creation
/*@Composable
fun WatchOSApp() {
    val viewModel = viewModel<OODViewModel>()
    val overdoseState by viewModel.overdoseState.collectAsStateWithLifecycle()
    val bloodOxygenLevel by viewModel.bloodOxygenLevel.collectAsStateWithLifecycle()
    val respiratoryRate by viewModel.respiratoryRate.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val overdoseStateColor = when (overdoseState) {
            OverdoseState.NOTDETECTED -> Color.Green
            OverdoseState.VITALSDROP -> Color.Yellow
            OverdoseState.DETECTED -> Color.Red
        }

        val bloodOxygenLevelColor = if (bloodOxygenLevel >= 90) {
            Color.Green
        } else if (bloodOxygenLevel <= 89 && bloodOxygenLevel >= 70) {
            Color.Yellow
        } else if (bloodOxygenLevel < 70) {
            Color.Red
        } else {
            Color.White
        }

        val respiratoryRateColor = if (respiratoryRate >= 10) {
            Color.Green
        } else if (respiratoryRate < 10 && respiratoryRate >= 8) {
            Color.Yellow
        } else if (respiratoryRate < 8) {
            Color.Red
        } else {
            Color.White
        }

        Text(
            text = when (overdoseState) {
                OverdoseState.NOTDETECTED -> "Overdose Not Detected"
                OverdoseState.VITALSDROP -> "Vitals Drop Detected"
                OverdoseState.DETECTED -> "Overdose Detected. Calling EMS"
            },
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.CenterHorizontally),
            color = overdoseStateColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            text = buildAnnotatedString {
                // Add the "Blood Oxygen Level: " text in white color
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("Blood Oxygen Level: ")
                }

                // Add the bloodOxygenLevel value in bloodOxygenLevelColor
                withStyle(style = SpanStyle(color = bloodOxygenLevelColor)) {
                    append("$bloodOxygenLevel")
                }

                // Add the "/80" text in white color
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("/80")
                }
            },
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = buildAnnotatedString {
                // Add the "Blood Oxygen Level: " text in white color
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("Respiratory Rate: ")
                }

                // Add the bloodOxygenLevel value in bloodOxygenLevelColor
                withStyle(style = SpanStyle(color = respiratoryRateColor)) {
                    append("$respiratoryRate")
                }

                // Add the "/80" text in white color
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("/12")
                }
            },
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}*/