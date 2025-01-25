package com.example.testblefido2.mht.ble.services

import android.annotation.SuppressLint
import android.bluetooth.*
import android.os.ParcelUuid
import android.util.Log
import com.google.common.primitives.Shorts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.testblefido2.mht.ble.util.BleUuidUtils
import com.example.testblefido2.mht.fido2.FIDO2Token
import com.example.testblefido2.mht.fido2.FIDO2TokenCallback
import java.util.*
import kotlin.experimental.and

class FIDO2AuthenticatorService(val fidoToken: FIDO2Token) : GattService {
    private val tag = javaClass.simpleName

    private val DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = BleUuidUtils.fromShortValue(0x2902)

    private val SERVICE_U2F_AUTHENTICATOR = BleUuidUtils.fromShortValue(0xFFFD)
    private val CHARACTERISTIC_U2F_CONTROL_POINT =
        UUID.fromString("F1D0FFF1-DEAA-ECEE-B42F-C9BA7ED623BB")
    private val CHARACTERISTIC_U2F_STATUS = UUID.fromString("F1D0FFF2-DEAA-ECEE-B42F-C9BA7ED623BB")
    private val CHARACTERISTIC_U2F_CONTROL_POINT_LENGTH =
        UUID.fromString("F1D0FFF3-DEAA-ECEE-B42F-C9BA7ED623BB")
    private val CHARACTERISTIC_U2F_SERVICE_REVISION_BITFIELD =
        UUID.fromString("F1D0FFF4-DEAA-ECEE-B42F-C9BA7ED623BB")

    private val serviceRevisionBitfield = byteArrayOf(0x20.toByte())  // FIDO2 support
    private val controlPointLength = byteArrayOf(0x02, 0x00)        // 512 byte packets

    private var fidoPacket: FIDO2Packet? = null

    private var fidoStatusCharacteristic: BluetoothGattCharacteristic? = null

    override fun getPrimaryServiceUUID(): ParcelUuid {
        return ParcelUuid.fromString(SERVICE_U2F_AUTHENTICATOR.toString())
    }

    override fun setupService(): BluetoothGattService {
        val service = BluetoothGattService(
            SERVICE_U2F_AUTHENTICATOR,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        run {
            val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_U2F_CONTROL_POINT,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED
            )

            if (!service.addCharacteristic(characteristic)) {
                throw RuntimeException("failed to add characteristic")
            }
        }
        run {
            val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_U2F_STATUS,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ
            )

            val clientCharacteristicConfigurationDescriptor = BluetoothGattDescriptor(
                DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED
                        or BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED
            )
            clientCharacteristicConfigurationDescriptor.value =
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            characteristic.addDescriptor(clientCharacteristicConfigurationDescriptor)

            if (!service.addCharacteristic(characteristic)) {
                throw RuntimeException("failed to add characteristic")
            }

            fidoStatusCharacteristic = characteristic
        }
        run {
            val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_U2F_CONTROL_POINT_LENGTH,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or BluetoothGattCharacteristic.PERMISSION_READ
            )
            if (!service.addCharacteristic(characteristic)) {
                throw RuntimeException("failed to add characteristic")
            }
        }
        run {
            val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_U2F_SERVICE_REVISION_BITFIELD,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED or BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED
                        or BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            if (!service.addCharacteristic(characteristic)) {
                throw RuntimeException("failed to add characteristic")
            }
        }

        return service
    }

    override fun getCharacteristics(): Set<UUID> {
        Log.v(tag, "getCharacteristics")
        return setOf(
            CHARACTERISTIC_U2F_CONTROL_POINT, CHARACTERISTIC_U2F_CONTROL_POINT_LENGTH,
            CHARACTERISTIC_U2F_SERVICE_REVISION_BITFIELD, CHARACTERISTIC_U2F_STATUS
        )
    }

    override fun getDescriptors(): Set<UUID> {
        Log.v(tag, "getDescriptors")
        return setOf(DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION)
    }

    @SuppressLint("MissingPermission")
    override fun onCharacteristicsRead(
        gattServer: BluetoothGattServer,
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.v(tag, "onCharacteristicsRead uuid: ${characteristic.uuid}")

        when (characteristic.uuid) {
            CHARACTERISTIC_U2F_SERVICE_REVISION_BITFIELD -> gattServer.sendResponse(
                device, requestId,
                BluetoothGatt.GATT_SUCCESS, offset, serviceRevisionBitfield
            )

            CHARACTERISTIC_U2F_CONTROL_POINT_LENGTH -> gattServer.sendResponse(
                device, requestId,
                BluetoothGatt.GATT_SUCCESS, offset, controlPointLength
            )

            else -> gattServer.sendResponse(
                device, requestId,
                BluetoothGatt.GATT_SUCCESS, offset, byteArrayOf()
            )
        }
    }

    data class FIDO2Packet(val command: Byte, val length: Short, val data: MutableList<Byte>)

    @SuppressLint("MissingPermission")
    override fun onCharacteristicsWrite(
        gattServer: BluetoothGattServer,
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        when (characteristic.uuid) {
            CHARACTERISTIC_U2F_SERVICE_REVISION_BITFIELD -> {
                Log.v(
                    tag,
                    "onCharacteristicsWrite SERVICE_REVISION_BITFIELD response: $serviceRevisionBitfield"
                )
                gattServer.sendResponse(
                    device, requestId,
                    BluetoothGatt.GATT_SUCCESS, offset, byteArrayOf()
                )
            }

            CHARACTERISTIC_U2F_CONTROL_POINT -> {
                Log.v(tag, "onCharacteristicsWrite U2F_CONTROL_POINT recv")
                if (value[0] and 0x80.toByte() == 0x80.toByte()) {
                    assert(fidoPacket == null)
                    val command = value[0]
                    val length = Shorts.fromByteArray(value.sliceArray(1..2))
                    val data = value.slice(3 until value.size)
                    fidoPacket = FIDO2Packet(command, length, data.toMutableList())
                } else {
                    assert(fidoPacket != null)
                    val data = value.slice(1 until value.size)
                    fidoPacket!!.data.addAll(data)
                }

                fidoPacket!!.let {
                    if (it.length.toInt() == it.data.size) {
                        Log.v(
                            tag,
                            "onCharacteristicsWrite FIDO2Packet received (${fidoPacket!!.command})"
                        )
                        GlobalScope.launch(Dispatchers.Default) {
                            fidoToken.dispatch(it.data.toByteArray(), object : FIDO2TokenCallback {
                                override fun sendKeepAlive() {
                                    Log.v(tag, "sendKeepAlive")
                                    val payload = byteArrayOf(0x82.toByte(), 0, 1, 0x02)
                                    fidoStatusCharacteristic!!.value = payload
                                    gattServer.notifyCharacteristicChanged(
                                        device,
                                        fidoStatusCharacteristic!!,
                                        false
                                    )
                                }

                                override fun sendMessage(message: ByteArray) {
                                    Log.v(
                                        tag,
                                        "sendMessage, for command: ${fidoPacket!!.command}, message size: ${message.size},"
                                    )
                                    val messageSize = message.size.toShort()
                                    val payload = byteArrayOf(
                                        -125,
                                        *Shorts.toByteArray(messageSize),
                                        *message
                                    )
                                    fidoStatusCharacteristic!!.value = payload
                                    gattServer.notifyCharacteristicChanged(
                                        device,
                                        fidoStatusCharacteristic!!,
                                        false
                                    )
                                }
                            })
                        }
                    }
                }
            }
        }

        if (responseNeeded) {
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf())
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDescriptorWrite(
        gattServer: BluetoothGattServer,
        device: BluetoothDevice,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        descriptor.value = value
        if (responseNeeded) {
            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf())
        }
    }
}