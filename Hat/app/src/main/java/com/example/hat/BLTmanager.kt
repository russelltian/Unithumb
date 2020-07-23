package com.example.hat
//
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//import android.content.ContentValues.TAG
//import android.util.Log
//import java.io.IOException
//import java.io.InputStream
//import java.io.OutputStream
//import java.util.*
//
//
//
//class BLTmanager {
//    private val  HC_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//    private var mBtAdapter : BluetoothAdapter ?= null
//    private var mBtSocket : BluetoothSocket  ?= null
//    private var outStream : OutputStream?= null
//    private var inStream : InputStream ?= null
//    private var mBtFlag = true
//
//    private fun myStartService()
//    {
//        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBtAdapter == null) {
//            Log.i("BLTmanager","Bluetooth unused.");
//            mBtFlag = false;
//            return;
//        }
//        if (!mBtAdapter!!.isEnabled) {
//            mBtFlag = false;
//            myStopService();
//            showToast("Open bluetoooth then restart program!!");
//            return;
//        }
//
//        showToast("Start searching!!");
//        threadFlag = true;
//        mThread = new MyThread ();
//        mThread.start();
//    }
//
//
//
//    public fun myBtConnect()
//    {
//        showToast("Connecting...");
//
//        /* Discovery device */
////	BluetoothDevice mBtDevice = mBtAdapter.getRemoteDevice(HC_MAC);
//         var mBtDevice : BluetoothDevice ?= null;
//        Set<BluetoothDevice> mBtDevices = mBtAdapter . getBondedDevices ();
//        if (mBtDevices.size() > 0) {
//            for (Iterator< BluetoothDevice > iterator = mBtDevices.iterator();
//            iterator.hasNext(); ) {
//                mBtDevice = (BluetoothDevice) iterator . next ();
//                showToast(mBtDevice.getName() + "|" + mBtDevice.getAddress());
//            }
//        }
//
//        try {
//            mBtSocket = mBtDevice.createRfcommSocketToServiceRecord(HC_UUID);
//        } catch (IOException e) {
//            e.printStackTrace();
//            mBtFlag = false;
//            showToast("Create bluetooth socket error");
//        }
//
//        mBtAdapter.cancelDiscovery();
//
//        /* Setup connection */
//        try {
//            mBtSocket.connect();
//            showToast("Connect bluetooth success");
//            Log.i(TAG, "Connect " + HC_MAC + " Success!");
//        } catch (IOException e) {
//            e.printStackTrace();
//            try {
//                showToast("Connect error, close");
//                mBtSocket.close();
//                mBtFlag = false;
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//
//        /* I/O initialize */
//        if (mBtFlag) {
//            try {
//                inStream = mBtSocket?.inputStream;
//                outStream = mBtSocket?.outputStream;
//            } catch ( e : IOException) {
//                e.printStackTrace();
//            }
//        }
//        Log.i("BLTmanager","Bluetooth is ready!");
//    }
//
//
//    public fun readSerial() : Int
//    {
//        var ret : Int = 0;
//        var rsp : ByteArray ?= null;
//
//        if (!mBtFlag) {
//            return -1;
//        }
//        try {
//            rsp = inStream?.available()?.let { ByteArray(it) };
//            ret = inStream?.read(rsp)!!
//        } catch ( e : IOException) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return ret;
//    }
//
//    public fun writeSerial( value : Int)
//    {
//        val ha : String = ""+value;
//        try {
//            outStream?.write(ha.toByteArray());
//            outStream?.flush();
//        } catch ( e : IOException) {
//            e.printStackTrace();
//        }
//    }
//
//    class MyThread : Thread() {
//        @Override
//        public override fun run() {
//            super.run();
//            myBtConnect();
//            while (threadFlag) {
//                readSerial();
//                try {
//                    Thread.sleep(30);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//}