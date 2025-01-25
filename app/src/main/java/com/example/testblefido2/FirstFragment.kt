package com.example.testblefido2


import android.bluetooth.BluetoothHidDevice
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.testblefido2.databinding.FragmentFirstBinding
import com.example.testblefido2.old.webauthn.Authenticator
import com.example.testblefido2.old.webauthn.TransactionManager
import com.example.testblefido2.old.webauthn.fido.hid.Framing

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

//    private var transactionManager: TransactionManager? = null
//    private var mauthr: Authenticator? = null
//
//    private lateinit var fido2Peripheral: Fido2SecurityKeyPeripheral

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//
//        fido2Peripheral = Fido2SecurityKeyPeripheral(this.requireContext())
//        fido2Peripheral.startAdvertising()

        super.onViewCreated(view, savedInstanceState)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
//
//        binding.btnBleTest.setOnClickListener {
//
//            Log.d("FirstFragment", "btnBleTest clicked")
//
//
//            Thread {
//                if (mauthr == null) {
//                    mauthr = Authenticator(
//                        requireActivity(),
//                        false
//                    )
//                }
//                if (transactionManager == null) {
//                    transactionManager =
//                        TransactionManager(
//                            requireActivity(),
//                            mauthr
//                        )
//                }
//
//
//                val u2fListener = object : Framing.U2fAuthnListener {
//                    override fun onRegistrationResponse() {
//                        Log.d("FirstFragment", "onRegistrationResponse")
//                        // Handle registration response
//                    }
//
//                    override fun onAuthenticationResponse() {
//                        Log.d("FirstFragment", "onAuthenticationResponse")
//                        // Handle authentication response
//                    }
//                }
//                val webAuthnListener = object : Framing.WebAuthnListener {
//                    override fun onCompleteMakeCredential() {
//                        Log.d("FirstFragment", "onCompleteMakeCredential")
//                        // Handle complete make credential
//                    }
//
//                    override fun onCompleteGetAssertion() {
//                        Log.d("FirstFragment", "onCompleteGetAssertion")
//                        // Handle complete get assertion
//                    }
//                }
//                transactionManager!!.registerListener(u2fListener)
//                transactionManager!!.registerListener(webAuthnListener)
//
//
//                var inputHost: BluetoothHidDevice? = null
//
////                /*
////                 * Perform application registration and initialization
////                 */
////                val btAppCallback = object : BluetoothHidDevice.Callback() {
////
////                    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
////                        Log.d(TAG, "onAppStatusChanged $pluggedDevice, $registered")
////                        super.onAppStatusChanged(pluggedDevice, registered)
////                        if (registered) {
////                            val pairedDevices = btHidDevice?.getDevicesMatchingConnectionStates(
////                                intArrayOf(
////                                    BluetoothProfile.STATE_CONNECTING,
////                                    BluetoothProfile.STATE_CONNECTED,
////                                    BluetoothProfile.STATE_DISCONNECTED,
////                                    BluetoothProfile.STATE_DISCONNECTING
////                                )
////                            )
////                            pairedDevices?.forEach {
////                                Log.d(TAG, "Paired devices : $it: ${it.name}")
////                            }
////                            mpluggedDevice = pluggedDevice
////                            if (pluggedDevice != null) status = Status.Waiting(btHidDevice!!, pluggedDevice)
////
////                            if (autoPairFlag) {
////                                if (pluggedDevice != null && btHidDevice?.getConnectionState(pluggedDevice) == BluetoothProfile.STATE_DISCONNECTED) {
////                                    btHidDevice?.connect(pluggedDevice)
////                                } else {
////                                    pairedDevices?.firstOrNull()?.let {
////                                        val pairedDState = btHidDevice?.getConnectionState(it)
////                                        Log.d("paired d", pairedDState.toString())
////                                        if (pairedDState == BluetoothProfile.STATE_DISCONNECTED) {
////                                            btHidDevice?.connect(it)
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                    }
////
////                    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
////                        super.onConnectionStateChanged(device, state)
////                        Log.i(TAG, "Connection state ${state.toState()}")
////                        if (state == BluetoothProfile.STATE_CONNECTED) {
////                            hostDevice = device
////                            btHidDevice?.connect(device)
////                            status = Status.Connected(btHidDevice!!, device)
////                        } else {
////                            hostDevice = null
////                            if (state == BluetoothProfile.STATE_DISCONNECTED) {
////                                btHidDevice?.also { status = Status.Waiting(it, device) }
////                            }
////                        }
////                    }
////
////                    override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
////                        super.onSetReport(device, type, id, data)
////                        Log.i("setreport", "$device / $type / $id / $data")
////                    }
////
////                    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
////                        super.onGetReport(device, type, id, bufferSize)
////                        if (type == BluetoothHidDevice.REPORT_TYPE_FEATURE) {
////                            val report: ByteArray = ByteArray(1) { 0 }
////                            val reportID = 6.toByte()
////                            val report_success = btHidDevice?.replyReport(device, type, reportID, report)
////                            Log.i("report success flag:", report_success.toString())
////                        }
////                    }
////                }
//
//                // Perform any additional setup or operations with mtx here
//            }.start()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        super.onDestroy()
//        if (::fido2Peripheral.isInitialized) {
//            fido2Peripheral.stopAdvertising()
//        }
    }
}