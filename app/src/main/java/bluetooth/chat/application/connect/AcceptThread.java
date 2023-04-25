package bluetooth.chat.application.connect;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private  static final String NAME = "BlueToothClass";
    private static final UUID MY_UUID =UUID.fromString(Constant.CONNECTTION_UUID);

    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothAdapter mBluetoothAdapter ;
    private final Handler mHandler;
    private ConnectedThread mConnectedThread;

    @SuppressLint("MissingPermission")
    public AcceptThread(BluetoothAdapter bluetoothAdapter, Handler handler) {
        mBluetoothAdapter =bluetoothAdapter;
        mHandler=handler;
        BluetoothServerSocket tmp = null;
        try {
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    NAME, Constant.MY_UUID_INSECURE);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        System.out.println("~~~~~~~~AcceptThread");
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
                Log.e( "accept", "mmServerSocket.accept()1");
                socket = mmServerSocket.accept();
                Log.e( "accept", "mmServerSocket.accept()2");
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR,e));
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageConnectedSocket(socket);
//                sendMessage(Message.obtain(handler, what))
                try {
                    mmServerSocket.close();
                    mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        if(mConnectedThread != null){
             mConnectedThread.cancel();
         }
        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
//            mHandler.send
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
    public void sendData(byte[] data) {
        if(mConnectedThread!=null){
            mConnectedThread.write(data);
        }
    }


}
