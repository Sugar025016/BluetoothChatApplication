package bluetooth.chat.application.connect;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInputStream ;
    private final OutputStream mmOutputStream;
    private Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        this.mmSocket = socket;
        this.mHandler = handler;
        InputStream tmpIn =null;
        OutputStream tmpOut =null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mmInputStream=tmpIn;
        this.mmOutputStream=tmpOut;
    }



    public void  run(){
        System.out.println("~~~~~~~~ConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        while (true){
            try {
                bytes=mmInputStream.read(buffer);
                System.out.println("!!!!!!!!!!!!!!!"+buffer.toString());
                if(bytes>0){
                    Message message = mHandler.obtainMessage(Constant.MSG_GOT_DATA, new String(buffer, 0, bytes, "utf-8"));
                    mHandler.sendMessage(message);
                                   }
                Log.d("GOTMSG", "message size"+bytes);
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
        }
    }
    public void  write(byte[] bytes){
        try {
            mmOutputStream.write(bytes);
            mHandler.sendEmptyMessage(Constant.MSG_SEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void  cancel(){
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendData(byte[] data) {
            write(data);

    }


}
