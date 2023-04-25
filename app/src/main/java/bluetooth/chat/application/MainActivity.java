package bluetooth.chat.application;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import bluetooth.chat.application.connect.AcceptThread;
import bluetooth.chat.application.connect.ConnectThread;
import bluetooth.chat.application.connect.ConnectedThread;
import bluetooth.chat.application.connect.Constant;
import bluetooth.chat.application.util.PermissionsUtil;

public class MainActivity extends AppCompatActivity {
    private BluetoothSocket mmSocket;

    private static String p = "";
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<>();
    private BluetoothController mController;
    private Toast mToast;
    private int REQUEST_CODE = 0;
    private ListView mListView;
    private TextView tv_message;
    private EditText edit_text_out;
    private Button button_send;
    private DeviceAdapter mAdapter;
    private BluetoothAdapter defaultAdapter;
    private ProgressBar progressBar;
    private LinearLayout ll_message;
    private AcceptThread mAcceptThread;
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    private Handler handler;
    private PermissionsUtil permissionsUtil = new PermissionsUtil(this);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("*********************onRequestPermissionsResult********************");
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN)) {
                    Toast.makeText(this, "需要藍芽權限，請給予權限", Toast.LENGTH_LONG).show();
                    //開啟應用程式資訊，讓使用者手動給予權限
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "按下拒絕", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "允許權限", Toast.LENGTH_LONG).show();
            }
        }
    }

    private final BroadcastReceiver mHeadsetReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "廣播");
            Log.d("intent.getAction()", intent.getAction());
            String state = intent.getAction();
            switch (state) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    showToast("ACTION_DISCOVERY_STARTED");
                    Log.d("BroadcastReceiver", "ACTION_DISCOVERY_STARTED");
                    progressBar.setVisibility(View.VISIBLE);
                    mDeviceList.clear();
                    mAdapter.notifyDataSetChanged();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    showToast("ACTION_DISCOVERY_FINISHED");
                    Log.d("BroadcastReceiver", "ACTION_DISCOVERY_FINISHED");
                    progressBar.setVisibility(View.GONE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    showToast("ACTION_FOUND");
                    Log.d("BroadcastReceiver", "ACTION_FOUND");
                    BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDeviceList.add(remoteDevice);
                    mAdapter.notifyDataSetChanged();
                    break;
                case BluetoothAdapter.ACTION_SCAN_MODE_CHANGED:
                    showToast("ACTION_SCAN_MODE_CHANGED");
                    Log.d("BroadcastReceiver", "ACTION_SCAN_MODE_CHANGED");
                    int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                    if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    Log.d("ACTION_BOND_STATE_CHANGED", "行動 _ 邦德 _ 狀態 _ 改變");
                    showToast("ACTION_BOND_STATE_CHANGED");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device == null) {
                        showToast("~~~~~ no device ");
                        return;
                    }
                    int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);

                    switch (status) {
                        case BluetoothDevice.BOND_BONDED:
                            showToast("BOND_BONDED " + device.toString());
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            showToast("BOND_BONDING " + device.toString());
                            break;
                        case BluetoothDevice.BOND_NONE:
                            showToast("BOND_NONE " + device.toString());
                            break;
                    }
                    break;
                default:
                    showToast("mHeadsetReceiver2 OTHER ");
                    break;
            }
        }

    };

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = "android.permission.PERMISSIONS_STORAGE")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsUtil.openPermissions();
        openBluetooth();
        setTitle("AA bluetooth");
        defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter == null) {
            showToast("Device doesn't support Bluetooth");
            // Device doesn't support Bluetooth
            return;
        }
        initUI();

        /** 廣播過濾器，過濾接廣播條件 **/
        IntentFilter filter2 = new IntentFilter();//監聽
        filter2.addAction(BluetoothDevice.ACTION_FOUND);
        filter2.addAction(Manifest.permission.BLUETOOTH_SCAN);
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter2.addAction(Manifest.permission.ACCESS_FINE_LOCATION);
        filter2.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter2.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mHeadsetReceiver2, filter2);
//        handler = new Handler();
        handler = new MyHandler();


    }


    private void initUI() {
        mListView = findViewById(R.id.action_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(bindDeviceClick);
        mController = new BluetoothController(defaultAdapter);
        progressBar = findViewById(R.id.progress_bar);
        ll_message = findViewById(R.id.ll_message);
        button_send = findViewById(R.id.button_send);
        edit_text_out = findViewById(R.id.edit_text_out);
        button_send.setOnClickListener(sendClick);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHeadsetReceiver2);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showToast(String test) {
        if (mToast == null) {
            mToast = Toast.makeText(this, test, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(test);
        }
        mToast.show();
    }

    public boolean openBluetooth() {
        if (permissionsUtil.checkPermissions()) {
            Log.d("----  !!!! openBluetooth  --------", "openBluetooth");
            if (defaultAdapter==null||!defaultAdapter.isEnabled()) {
                Log.d("----  !!!! defaultAdapter.isEnabled()  --------", "openBluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mGetContent.launch(enableBtIntent);
//                startActivityForResult(enableBtIntent, 1);
                return false;
            }
        }
        return true;

    }

    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.d("----  !!!!   --------", "需要開啟藍芽");
            if (result != null) {

                Log.d("ACTION_REQUEST_ENABLE2", String.valueOf(result.getResultCode()));
                if (result.getResultCode() == REQUEST_CODE) {
                    showToast("需要開啟藍芽");
                    Log.d("ACTION_REQUEST_ENABLE", "需要開啟藍芽");

                }
            }
        }
    });

    @SuppressLint("MissingPermission")
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH})
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( !permissionsUtil.openPermissions()){
            return false;
        }
        if( !openBluetooth()){
            return false;
        }

        int id = item.getItemId();

        if (id == R.id.enable_visiblity) {
            mController.enableVisibly(this);
        } else if (id == R.id.find_device) {
            System.out.println("--------------------");
            mListView.setVisibility(View.VISIBLE);
            mController.findDevice();
            mAdapter.refresh(mDeviceList);
            mListView.setOnItemClickListener(bindDeviceClick);
        } else if (id == R.id.bonded_device) {

            mListView.setVisibility(View.VISIBLE);

            mBluetoothDeviceList = mController.getBondedDeviceList();
            mAdapter.refresh(mBluetoothDeviceList);

//            mAcceptThread = new AcceptThread(mController.getmAdapter(), handler);
//            mAcceptThread.start();
            mListView.setOnItemClickListener(bindedDeviceClick);
        } else if (id == R.id.listening) {

            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
            mAcceptThread = new AcceptThread(mController.getmAdapter(), handler);
            mAcceptThread.start();
        } else if (id == R.id.stop_listening) {
            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
        } else if (id == R.id.disconnect) {
            if (mAcceptThread != null) {
                mAcceptThread.cancel();
            }
        } else if (id == R.id.say_hello) {
            try {
                say("HELLO");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void say(String hello) throws UnsupportedEncodingException {

        if (mAcceptThread != null) {
            Log.d("mAcceptThread", "sendData");
            mAcceptThread.sendData(hello.getBytes("utf-8"));
        } else if (mConnectThread != null) {
            Log.d("mConnectedThread", "sendData");
            mConnectThread.sendData(hello.getBytes("utf-8"));
        }

    }

    private AdapterView.OnClickListener sendClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (edit_text_out.getText() != null) {
                try {
                    say(edit_text_out.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @SuppressLint("MissingPermission")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = mDeviceList.get(position);
            /** 小於Build.VERSION_CODES.KITKAT不支持device.createBond() */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
        }
    };

    private AdapterView.OnItemClickListener bindedDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @SuppressLint("MissingPermission")
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
            mController.getmAdapter().cancelDiscovery();

            BluetoothDevice device = mBluetoothDeviceList.get(i);
            String address = device.getAddress();
            Log.d("address", address);

            /***********************************/
            BluetoothSocket tmp = null;
            showToast("i  : " + i);
            try {

                System.out.println("BluetoothDevice: " + device.getName());
            } catch (SecurityException e) {
                Log.e("device.getName()", "e");
            }
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
            }
            mConnectThread = new ConnectThread(MainActivity.this, device, mController.getmAdapter(), handler);

        }
    };



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        System.out.println("requestCode: " + requestCode);
        System.out.println("resultCode: " + resultCode);
        System.out.println("data: " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showToast("藍牙 open ok");
        } else {
            showToast("藍牙 open error");
        }
    }


    private void initActionBar() {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setDisplayUseLogoEnabled(false);
        setProgressBarIndeterminate(true);
        ViewConfiguration config = ViewConfiguration.get(this);
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            System.out.println("msg !!!!!!!" + msg.getData());
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MSG_START_LISTENING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case Constant.MSG_FINISH_LISTENING:
                    progressBar.setVisibility(View.GONE);
                    System.out.println("MSG_FINISH_LISTENING !!!!!!!" + msg.getData());
                    break;
                case Constant.MSG_GOT_A_CLINET:

                    mListView.setVisibility(View.GONE);
                    System.out.println("MSG_GOT_A_CLINET !!!!!!!" + msg.getData());
                    System.out.println("~~~~~~~~~~~~~ !!!!!!!" + msg.obj.toString());
                    p = msg.obj.toString();
                    break;
                case Constant.MSG_GOT_DATA:
//                    progressBar.setVisibility(View.GONE);
//                    mListView.setVisibility(View.GONE);
                    System.out.println("MSG_GOT_DATA !!!!!!!" + msg.getData());
                    System.out.println("MSG_GOT_DATA obj !!!!!!!" + msg.obj.toString());

                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_message, null);
                    tv_message = view.findViewById(R.id.tv_message);
                    tv_message.setText(p + " : " + msg.obj.toString());
//                    ll_message.addView(view,ll_message.indexOfChild());
                    ll_message.addView(view, ll_message.getChildCount() - 1);
                    break;
                case Constant.MSG_ERROR:
                    progressBar.setVisibility(View.GONE);
                    break;
                case Constant.MSG_SEND:
                    View viewSend = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_message, null);
                    tv_message = viewSend.findViewById(R.id.tv_message);
                    tv_message.setText("ME : " + edit_text_out.getText().toString());
                    ll_message.addView(viewSend, ll_message.getChildCount() - 1);
                    edit_text_out.setText(null);
                    break;
            }
        }
    }


}