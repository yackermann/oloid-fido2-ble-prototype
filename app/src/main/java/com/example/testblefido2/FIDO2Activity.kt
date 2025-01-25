package com.example.testblefido2

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import com.example.testblefido2.mht.ble.profiles.BLEProfile
import com.example.testblefido2.mht.ble.services.DeviceInformationService
import com.example.testblefido2.mht.ble.services.FIDO2AuthenticatorService
import com.example.testblefido2.mht.fido2.FIDO2Token
import com.example.testblefido2.mht.fido2.FIDO2UserCallback
import com.example.testblefido2.mht.fido2.FIDO2UserInterface


class FIDO2Activity: Activity(), FIDO2UserInterface {

    private var mBleProfile: BLEProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_first)
        title = "FIDO2 Token"
        setupBlePeripheralProvider()
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        val bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBluetoothIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBleProfile?.stopAdvertising()
    }

    fun setupBlePeripheralProvider() {
        val fidoToken = FIDO2Token(
            byteArrayOf(173.toByte(), 51, 225.toByte(), 30, 242.toByte(), 194.toByte(),
                52, 188.toByte(), 136.toByte(), 149.toByte(), 129.toByte(), 254.toByte(), 124.toByte(),
                76, 160.toByte(), 205.toByte())
            , this.getPreferences(MODE_PRIVATE), this)

        mBleProfile = BLEProfile(this, sequenceOf(FIDO2AuthenticatorService(fidoToken), DeviceInformationService()))
        mBleProfile!!.startGattServer()
        mBleProfile!!.startAdvertising()
    }

    override fun onTokenRegistration(relyingPartyId: String, callback: FIDO2UserCallback) {
        BiometricPrompt.Builder(applicationContext)
            .setTitle(title)
            .setSubtitle(relyingPartyId)
            .setDescription(String.format("Do you want to register this device token in %s", relyingPartyId))
            .setNegativeButton("Cancel", mainExecutor, DialogInterface.OnClickListener { _, _ ->
                callback.denied()
            })
            .build()
            .authenticate(CancellationSignal(), mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.granted()
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    super.onAuthenticationHelp(helpCode, helpString)
                    callback.denied()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.denied()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.denied()
                }
            })
    }

    override fun registrationCompleted(relyingPartyId: String) {
    }

    override fun onTokenAuthentication(relyingPartyId: String, callback: FIDO2UserCallback) {
        BiometricPrompt.Builder(applicationContext)
            .setTitle(title)
            .setSubtitle(relyingPartyId)
            .setDescription(String.format("Do you want to authenticate in %s", relyingPartyId))
            .setNegativeButton("Cancel", mainExecutor, DialogInterface.OnClickListener { _, _ ->
                callback.denied()
            })
            .build()
            .authenticate(CancellationSignal(), mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.granted()
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    super.onAuthenticationHelp(helpCode, helpString)
                    callback.denied()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.denied()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.denied()
                }
            })
    }

    override fun authenticationCompeted(relyingPartyId: String) {
    }
}
