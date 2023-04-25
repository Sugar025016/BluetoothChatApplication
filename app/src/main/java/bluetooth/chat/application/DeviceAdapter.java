package bluetooth.chat.application;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private List<BluetoothDevice> bluetoothDeviceList;
    private Context context;
    public DeviceAdapter(List<BluetoothDevice> bluetoothDeviceList, Context context){
        this.bluetoothDeviceList=bluetoothDeviceList;
        this.context=context.getApplicationContext();
    }


    @Override
    public int getCount() {
        return bluetoothDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
          View itemView =convertView;
          if(itemView==null){
              itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2,parent,false);
          }

        TextView text1 = itemView.findViewById(android.R.id.text1);
        TextView text2 = itemView.findViewById(android.R.id.text2);

        BluetoothDevice bluetoothDevice = (BluetoothDevice) getItem(position);
        text1.setText(bluetoothDevice.getName());
        text2.setText(bluetoothDevice.getAddress());

        return itemView;
    }



    public void refresh(List<BluetoothDevice> data){

        bluetoothDeviceList = data;
        notifyDataSetChanged();
    }
}
