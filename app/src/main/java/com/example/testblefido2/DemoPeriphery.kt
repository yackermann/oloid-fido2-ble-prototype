package com.example.testblefido2

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.*


class Fido2SecurityKeyPeripheral(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter.bluetoothLeAdvertiser

    private val fido2ServiceUuid = UUID.fromString("0000fffd-0000-1000-8000-00805f9b34fb") // FIDO U2F Service UUID
    private val challengeCharacteristicUuid = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    private val responseCharacteristicUuid = UUID.fromString("0000dcba-0000-1000-8000-00805f9b34fb")

    private var gattServer: BluetoothGattServer? = null

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    println("Device connected: ${device?.address}")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    println("Device disconnected: ${device?.address}")
                }
            }
        }

//        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)


            print("onCharacteristicReadRequest: $requestId, $offset $characteristic")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("NO PERMISSIONS GIVEN onCharacteristicReadRequest")
            }
            println("Characteristic read request received for UUID: ${characteristic?.uuid}")

            if (characteristic != null) {
                // Example: Return a mock value for the challenge characteristic
                val responseValue = when (characteristic.uuid) {
                    challengeCharacteristicUuid -> "MockChallengeValue".toByteArray(Charsets.UTF_8)
                    else -> ByteArray(0)
                }

                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, responseValue)
            } else {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
            }
        }

//        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            print("onCharacteristicWriteRequest: $requestId, $offset $characteristic")

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("NO PERMISSIONS GIVEN onCharacteristicWriteRequest")
            }

            println("Write request received for UUID: ${characteristic?.uuid}, Value: ${value?.decodeToString()}")

            if (characteristic != null && value != null) {
                // Example: Update the value of the challenge characteristic
                when (characteristic.uuid) {
                    challengeCharacteristicUuid -> {
                        characteristic.value = value
                        println("Challenge characteristic updated to: ${value.decodeToString()}")
                    }
                    else -> {
                        println("Unknown characteristic write request.")
                    }
                }

                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
                }
            } else {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
                }
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            println("Notification sent to device: ${device?.address}, Status: $status")
        }
    }

//    @SuppressLint("MissingPermission")
    fun startAdvertising() {


        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(fido2ServiceUuid))
            .build()

    if (ActivityCompat.checkSelfPermission(
            this.context,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        println("NO PERMISSIONS GIVEN")
    }

    advertiser?.startAdvertising(settings, data, advertiseCallback)
        setupGattServer()
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            // Advertising started successfully
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Handle advertising failure
        }
    }

//    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        if (ActivityCompat.checkSelfPermission(
                this.context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("NO PERMISSIONS GIVEN setupGattServer")
        }

        // Open GATT server
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        // Define FIDO2 primary service
        val fido2ServiceName = "FIDO2 Authentication Service"
        val fido2Service = BluetoothGattService(fido2ServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Define Challenge Characteristic
        val challengeCharacteristicName = "Challenge Characteristic"
        val challengeCharacteristic = BluetoothGattCharacteristic(
            challengeCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // Define Response Characteristic
        val responseCharacteristicName = "Response Characteristic"
        val responseCharacteristic = BluetoothGattCharacteristic(
            responseCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Add characteristics to the service
        fido2Service.addCharacteristic(challengeCharacteristic)
        fido2Service.addCharacteristic(responseCharacteristic)

        // Add service to the GATT server
        val successfullyAdded = gattServer!!.addService(fido2Service)
        println("Successfully added $successfullyAdded")

        // Log or print names for clarity (optional)
        println("Added GATT Service: $fido2ServiceName")
        println("Added Characteristic: $challengeCharacteristicName")
        println("Added Characteristic: $responseCharacteristicName")
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
    }
}