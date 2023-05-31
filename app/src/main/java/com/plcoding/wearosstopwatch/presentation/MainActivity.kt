package com.plcoding.wearosstopwatch.presentation

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}