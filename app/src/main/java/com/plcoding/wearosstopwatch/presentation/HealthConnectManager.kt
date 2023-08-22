package com.plcoding.wearosstopwatch.presentation

import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import java.time.Instant

val requestPermissionContract = PermissionController.createRequestPermissionResultContract()

//Define permissions
val PERMISSIONS = setOf(
    HealthPermission.getReadPermission(RespiratoryRateRecord::class),
    HealthPermission.getReadPermission(OxygenSaturationRecord::class)
)

class HealthConnectManager(private val mainActivity: MainActivity) {
    private var healthConnectClient: HealthConnectClient? = null
    private var status: HealthConnectStatus = HealthConnectStatus.SDK_UNAVAILABLE

    private val requestPermissions = mainActivity.registerForActivityResult(requestPermissionContract) { perms ->
        status = if (perms.containsAll(PERMISSIONS)) {
            HealthConnectStatus.SUCCESS
        } else {
            HealthConnectStatus.PERMISSION_DENIED
        }
    }

    private var BaudRate: Long = 15000

    fun GetStatus() : HealthConnectStatus
    {
        return status
    }

    //Throws error if the API isn't initialized
    suspend fun initAndRequestPermissions(context: android.content.Context, providerPackageName: String) : HealthConnectStatus
    {
        val status = HealthConnectClient.sdkStatus(context, providerPackageName)
        if (status == HealthConnectClient.SDK_UNAVAILABLE)
        {
            //This app *can't* work on this device because the client isn't supported
            return HealthConnectStatus.SDK_UNAVAILABLE
        }
        if (status == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED)
        {
            //Ask the user to update the health connect app
            val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }
            )
            this.status = HealthConnectStatus.APP_REQUESTED
            return HealthConnectStatus.APP_REQUESTED
        }

        //At this point, we have the app installed. We can get the client and request permissions
        this.healthConnectClient = HealthConnectClient.getOrCreate(context)

        val granted = healthConnectClient!!.permissionController.getGrantedPermissions()

        return if (granted.containsAll(PERMISSIONS)) {
            //Success!
            this.status = HealthConnectStatus.SUCCESS
            HealthConnectStatus.SUCCESS
        } else {
            requestPermissions.launch(PERMISSIONS)
            this.status = HealthConnectStatus.PERMISSION_REQUESTED
            HealthConnectStatus.PERMISSION_REQUESTED
        }
    }

    //This collects all of the data as a callback of sorts
    suspend fun CollectData()
    {
        var lastRead = Instant.now()
        val viewModel = ViewModelProvider(mainActivity)[OODViewModel::class.java]
        while (true)
        {
            var CheckOverdose = false
            //Try getting any new data
            val respResponse = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    RespiratoryRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.after(lastRead)
                )
            )

            if (respResponse.records.isNotEmpty())
            {
                //Send the most recent datapoint
                viewModel.updateRespiratoryRate(respResponse.records.last().rate)
                CheckOverdose = true
            }

            val oxResponse = healthConnectClient!!.readRecords(
                ReadRecordsRequest(
                    OxygenSaturationRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.after(lastRead)
                )
            )

            if (oxResponse.records.isNotEmpty())
            {
                //Send most recent datapoint
                viewModel.updateBloodOxygenLevel(oxResponse.records.last().percentage.value)
                CheckOverdose = true
            }

            lastRead = Instant.now()
            viewModel.checkForOverdose()
            delay(BaudRate)
        }
    }
}

enum class HealthConnectStatus
{
    SDK_UNAVAILABLE,
    APP_REQUESTED,
    PERMISSION_REQUESTED,
    PERMISSION_DENIED,
    SUCCESS,
}