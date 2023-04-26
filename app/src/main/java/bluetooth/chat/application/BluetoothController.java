package bluetooth.chat.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
public class BluetoothController {
    private BluetoothAdapter mAdapter;

    public BluetoothController(BluetoothAdapter bluetoothAdapter) {
        this.mAdapter = bluetoothAdapter;
    }

    public void turnOnBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (SecurityException e) {
            Log.e("SecurityException", e.getMessage());
        }

    }

    public BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    public void enableVisibly(Context context) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
    }

    public void findDevice() {
        assert (mAdapter != null);
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        } else {
            if (mAdapter.isEnabled()) {
                mAdapter.startDiscovery();

            }
        }
    }

    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

}
