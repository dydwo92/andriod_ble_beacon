package com.example.myapplication

import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        bluetoothAdapter!!.startLeScan(leScanCallback)
    }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
    private val arrHeader = byteArrayOfInts(0x02, 0x01, 0x04, 0x1a, 0xff, 0x59, 0x00,
        0x02, 0x15, 0xd0, 0x6b, 0xda, 0xd2, 0x7f, 0xe5, 0x4c, 0xdf, 0x89, 0x03,
        0x06, 0x7a, 0x3f, 0x75, 0x67, 0xe4)

    private val arrData = UIntArray(256)
    private val arrCnt = UIntArray(256)

    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            var scanRecordHeader = scanRecord.sliceArray(0..arrHeader.size-1)
            if(scanRecordHeader contentEquals arrHeader){
                var offset = arrHeader.size
                var major:UInt = (scanRecord[offset].toUByte().toUInt() * 256u) + scanRecord[offset + 1].toUByte().toUInt()
                var minor:UInt = (scanRecord[offset + 2].toUByte().toUInt() * 256u) + scanRecord[offset + 3].toUByte().toUInt()

                if(major < 256U){
                    arrData[major.toInt()] = minor
                    arrCnt[major.toInt()] = 10U
                }

                var text = ""
                var i = 0;
                for (data in arrData){
                    if(arrCnt[i] != 0U) arrCnt[i] = arrCnt[i] - 1U
                    if(arrCnt[i] != 0U) text = text + i.toString() + " : " + arrData[i].toString() + "\n"
                    i = i + 1
                }
                list_beacon.setText(text)
            }
        }
    }


}
