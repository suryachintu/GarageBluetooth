package com.example.gauravbharti.garagebluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity
{   private final static String TAG = MainActivity.class.getSimpleName();


    Toolbar toolbar;
    LinearLayout layout_connected;
    LinearLayout layout_up;
    LinearLayout layout_down;
    LinearLayout layout_disconnect;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    LinearLayout connection_button;
    private boolean mScanning;
    private Handler mHandler;
    public Dialog dialog;
    private String mDeviceName;
    private String mDeviceAddress;
    ImageButton home;
    ListView listview;
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private BluetoothLeService mBluetoothLeService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {   mBluetoothLeService=((BluetoothLeService.LocalBinder)service).getService();
            if(!mBluetoothLeService.initialize())
            {
                Log.e(TAG," Unable to initialize");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                mBluetoothLeService=null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d("Initialized","Initialized");
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                //updateConnectionState(R.string.connected);
                updateConnectionState("Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                updateConnectionState("Disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };


    private void updateConnectionState(final String connection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("Connection",connection);
            }
        });
    }



    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            //if(SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") { isSerial.setText("Yes, serial :-)"); } else {  isSerial.setText("No, serial :-("); }
            if(SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") { Log.d("Serial","is Serial"); } else {  Log.d("Serial","No Serial"); }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
        }

    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        home=(ImageButton)findViewById(R.id.home_garage);
        layout_connected=(LinearLayout)findViewById(R.id.layout_connected);
        layout_up=(LinearLayout)findViewById(R.id.layout_up);
        layout_down=(LinearLayout)findViewById(R.id.layout_down);
        layout_disconnect=(LinearLayout)findViewById(R.id.layout_disconnect);
        connection_button=(LinearLayout)findViewById(R.id.connection_buttons);
        connection_button.setVisibility(View.GONE);
        mHandler = new Handler();
        final BluetoothManager bluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=bluetoothManager.getAdapter();
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this,getString(R.string.ble_not_supported),Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {   bluetoothenable();
        }
        dialog=new Dialog(this,R.style.FullHeightDialog);
        dialog.setContentView(R.layout.bluetoothlist);
        //processStart(BluetoothLeService.TAG);
        Intent gattServiceIntent=new Intent(this,BluetoothLeService.class);
        bindService(gattServiceIntent,mServiceConnection,BIND_AUTO_CREATE);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection_button.setVisibility(View.GONE);
                layout_connected.setBackgroundResource(R.color.colorAccent);
                mLeDeviceListAdapter = new LeDeviceListAdapter();
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                dialog.show();
                listview=(ListView)dialog.findViewById(R.id.list);
                listview.setAdapter(mLeDeviceListAdapter);
            }
        });
        layout_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str="10";
                Log.d("UP","UP");
                final byte[] tx=str.getBytes();
                if(mConnected) {
                    Log.d("UP","UP");
                    characteristicTX.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristicTX);
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                }
            }
        });
        layout_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str="01";
                Log.d("DOWN","DOWN");
                final byte[] tx=str.getBytes();
                if(mConnected) {
                    Log.d("DOWN","DOWN");
                    characteristicTX.setValue(tx);
                    mBluetoothLeService.writeCharacteristic(characteristicTX);
                    mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                }
            }
        });
        layout_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {   unregisterReceiver(mGattUpdateReceiver);

                }
                catch (Exception e)
                {
                    Log.d("Exception","Exception");
                }
                layout_connected.setBackgroundResource(R.color.colorAccent);
                connection_button.setVisibility(View.GONE);
            }
        });
    }
    private void scanLeDevice(final boolean enable)
    {   if(enable)
        {   mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning=false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }},SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        else
        {   mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

        }
    }

    private class LeDeviceListAdapter extends BaseAdapter
    {   private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator =LayoutInflater.from(dialog.getContext());
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                Log.d(" adding dive to adapter",device.toString());
                mLeDevices.add(device);
            }
        }
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mLeDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView==null)
            {   viewHolder = new ViewHolder();
                convertView=mInflator.inflate(R.layout.full_list,parent,false);
                viewHolder.deviceAddress=(TextView)convertView.findViewById(R.id.device_address);
                viewHolder.deviceName=(TextView)convertView.findViewById(R.id.device_name);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder=(ViewHolder)convertView.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if(deviceName!=null && deviceName.length()>0)
            {
                viewHolder.deviceName.setText(deviceName);
            }
            else
            {
                viewHolder.deviceName.setText(R.string.unknown_device);
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str="10";
                    final byte[] tx;
                    tx=str.getBytes();
                    Log.d("Clicked","Clicked");
                    try
                    {   unregisterReceiver(mGattUpdateReceiver);

                    }
                    catch (Exception e)
                    {
                        Log.d("Exception","Exception");
                    }
                    layout_connected.setBackgroundResource(R.color.green);
                    Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                    connection_button.setVisibility(View.VISIBLE);
                    registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
                    dialog.cancel();
                }
            });
            return convertView;
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    public void bluetoothenable()
    {   if(!mBluetoothAdapter.isEnabled())
        {
            Intent intentBt=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(intentBt, REQUEST_ENABLE_BT);
        }
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("dvice name", device.toString());
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
    private void processStart(final String tag)
    {   Intent intent=new Intent(getApplicationContext(),BluetoothLeService.class);
        intent.addCategory(tag);
        startService(intent);

    }
}