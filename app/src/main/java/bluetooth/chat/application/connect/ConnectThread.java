package bluetooth.chat.application.connect;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresPermission;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import bluetooth.chat.application.MainActivity;

public class ConnectThread extends Thread {

    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);
    //    private final UUID MY_UUID = ParcelUuid.fromString(Constant.CONNECTTION_UUID).getUuid();
    private BluetoothSocket mmSocket;

    private final BluetoothDevice mmDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;
    private MainActivity mMainActivity;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    @SuppressLint("MissingPermission")
    @RequiresPermission(value = "android.permission.PERMISSIONS_STORAGE")
    public ConnectThread(MainActivity mainActivity,BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        this.mMainActivity = mainActivity;
        this.mmDevice = device;
        this.mBluetoothAdapter = adapter;
        this.mHandler = handler;
        BluetoothSocket tmp = null;

        try {
            tmp = device.createInsecureRfcommSocketToServiceRecord(Constant.MY_UUID_INSECURE);
            Log.d("createInsecureRfcommSocketToServiceRecord", mmDevice.getUuids()[0].getUuid().toString());


        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
            e.printStackTrace();
        }
        mmSocket = tmp;
        run();
    }
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @SuppressLint("MissingPermission")
    public void run() {
        System.out.println("~~~~~~~~ConnectThread");
        Log.d("mmSocket", "連接?" + mmDevice.getName());

        mBluetoothAdapter.cancelDiscovery();
            try {
                if (!mmSocket.isConnected()) {
                    Log.d("isConnected", "NO");

                    mmSocket.connect();
                    mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_GOT_A_CLINET, mmDevice.getName()));
                }
                Log.d("mmSocket", "連接");
            } catch (IOException e) {
                if (mmSocket != null) {
                    try {
                        mmSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    mmSocket = null;
                }
                Log.d("mmSocket", "連接:" + e);
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                return;
        }

        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        mHandler.sendEmptyMessage(Constant.MSG_CONNECTED_TO_SERVER);
        mConnectedThread = new ConnectedThread(mmSocket, mHandler);
        System.out.println("~~~~~~~~ConnectedThread  2  ");
        mConnectedThread.start();
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendData(byte[] data) {
        if (mConnectedThread != null) {
            mConnectedThread.write(data);
        }
    }
}
