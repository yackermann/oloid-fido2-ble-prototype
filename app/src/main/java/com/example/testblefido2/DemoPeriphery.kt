package com.example.testblefido2

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
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
            // Handle connection state changes
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            // Handle read requests
        }

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
            // Handle write requests
        }
    }

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val fido2Service = BluetoothGattService(fido2ServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val challengeCharacteristic = BluetoothGattCharacteristic(
            challengeCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val responseCharacteristic = BluetoothGattCharacteristic(
            responseCharacteristicUuid,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        fido2Service.addCharacteristic(challengeCharacteristic)
        fido2Service.addCharacteristic(responseCharacteristic)

        gattServer?.addService(fido2Service)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
    }
}