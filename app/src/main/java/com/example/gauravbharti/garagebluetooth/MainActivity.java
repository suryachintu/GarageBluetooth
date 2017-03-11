package com.example.gauravbharti.garagebluetooth;

import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity
{   private final static String TAG = MainActivity.class.getSimpleName();


    Toolbar toolbar;
    LinearLayout layout_connected;
    LinearLayout layout_up;
    LinearLayout connection_buttons2;
    LinearLayout layout_up_down;
    LinearLayout layout_disconnect2;
    ImageButton setting_button;
    SharedPreferences prefs;
    Button register_device;
    LinearLayout layout_down;
    public String connected_address=null;
    GarageBluetoothApplication garageBluetoothApplication;
    LinearLayout layout_disconnect;
    BluetoothDevice saveddevice = null;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    LinearLayout connection_button;
    RegisteredDevicesFragment registeredDevicesFragment;
    private boolean mScanning;
    private Handler mHandler;
    SessionManager sessionManager;
    public Dialog dialog;
    public Dialog dialog_register;
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
            Log.d("Initialized met","Initialized method");
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
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        connection_buttons2=(LinearLayout)findViewById(R.id.connection_buttons2);
        layout_up_down=(LinearLayout)findViewById(R.id.layout_up_down);
        layout_disconnect2=(LinearLayout)findViewById(R.id.layout_disconnect2);
        garageBluetoothApplication=(GarageBluetoothApplication)this.getApplication();
        garageBluetoothApplication.setCurrentActivity(this);
        home=(ImageButton)findViewById(R.id.home_garage);
        layout_connected=(LinearLayout)findViewById(R.id.layout_connected);
        layout_up=(LinearLayout)findViewById(R.id.layout_up);
        setting_button=(ImageButton)findViewById(R.id.setting_click);
        layout_down=(LinearLayout)findViewById(R.id.layout_down);
        layout_disconnect=(LinearLayout)findViewById(R.id.layout_disconnect);
        connection_button=(LinearLayout)findViewById(R.id.connection_buttons);
        connection_button.setVisibility(View.GONE);
        connection_buttons2.setVisibility(View.GONE);
        sessionManager=new SessionManager(getApplicationContext());
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
        //registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Intent gattServiceIntent=new Intent(this,BluetoothLeService.class);
        bindService(gattServiceIntent,mServiceConnection,BIND_AUTO_CREATE);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connection_button.setVisibility(View.GONE);
                connection_buttons2.setVisibility(View.GONE);
                layout_connected.setBackgroundResource(R.color.colorAccent);
                mLeDeviceListAdapter = new LeDeviceListAdapter();
                mLeDeviceListAdapter.clear();
                try
                {   mBluetoothLeService.disconnect();

                }
                catch (Exception e)
                {

                }
                connected_address=null;
                prefs.edit().putString("current","[]").commit();
                scanLeDevice(true);
                dialog.show();
                listview=(ListView)dialog.findViewById(R.id.list);
                listview.setAdapter(mLeDeviceListAdapter);
            }
        });
        layout_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject=new JSONObject();
                String str=null;
                try
                {   JSONArray jsonArray=new JSONArray(prefs.getString("current","[]"));
                    jsonObject=jsonArray.getJSONObject(0);
                    str="#"+jsonObject.getString("password")+"#st#A0#00";
                }
                catch (Exception e)
                {

                }
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
                JSONObject jsonObject=new JSONObject();
                String str=null;
                try
                {   JSONArray jsonArray=new JSONArray(prefs.getString("current","[]"));
                    jsonObject=jsonArray.getJSONObject(0);
                    str="#"+jsonObject.getString("password")+"#st#B0#00";
                }
                catch (Exception e)
                {

                }
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
        layout_up_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        layout_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    //unregisterReceiver(mGattUpdateReceiver);
                    mBluetoothLeService.disconnect();
                    connected_address=null;
                    prefs.edit().putString("current","[]").commit();

                }
                catch (Exception e)
                {
                    Log.d("Exception","Exception");
                }
                layout_connected.setBackgroundResource(R.color.colorAccent);
                connection_button.setVisibility(View.GONE);
            }
        });
        layout_disconnect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {   mBluetoothLeService.disconnect();
                    connected_address=null;
                    prefs.edit().putString("current","[]").commit();
                }
                catch (Exception e)
                {

                }
                layout_connected.setBackgroundResource(R.color.colorAccent);
                connection_buttons2.setVisibility(View.GONE);
            }
        });
        setting_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected_address!=null)
                {

                }
                else
                {
                    connected_address="0";
                }
                registeredDevicesFragment=RegisteredDevicesFragment.newInstance(connected_address);
                switchFragment(registeredDevicesFragment);
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

            final BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            try
            {   JSONArray jsonArray=new JSONArray();
                JSONObject jsonObject=new JSONObject();
                jsonArray=new JSONArray(getSharedPrefrences());
                for(int i=0;i<jsonArray.length();i++)
                {   jsonObject= (JSONObject) jsonArray.get(i);
                    if(device.getAddress().equals(jsonObject.getString("address")))
                    {
                        viewHolder.deviceName.setText(jsonObject.getString("name"));
                    }
                    else
                    {   if(deviceName!=null && deviceName.length()>0)
                        {
                        viewHolder.deviceName.setText(deviceName);
                        }
                        else
                        {
                            viewHolder.deviceName.setText(R.string.unknown_device);
                        }
                    }
                }
            }
            catch (Exception e)
            {   if(deviceName!=null && deviceName.length()>0)
                {
                    viewHolder.deviceName.setText(deviceName);
                }
                else
                {
                    viewHolder.deviceName.setText(R.string.unknown_device);
                }
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Clicked","Clicked");
                    try
                    {   unregisterReceiver(mGattUpdateReceiver);

                    }
                    catch (Exception e)
                    {
                        Log.d("Exception","Exception");
                    }
                    layout_connected.setBackgroundResource(R.color.green);
                    //connection_button.setVisibility(View.VISIBLE);
                    //boolean isConnected= mBluetoothLeService.connect(device.getAddress());
                    mConnected=mBluetoothLeService.connect(device.getAddress());
                    saveddevice=device;
                    registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
                    if (mConnected){
                        //characteristicTX.setValue(tx);
                        //mBluetoothLeService.writeCharacteristic(characteristicTX);
                        //mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
                        Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this," Not Connected",Toast.LENGTH_SHORT).show();
                    }
                    //device.connectGatt(getApplicationContext(),false,new BluetoothGattCallback());
                    //device.connectGatt(this,false,mLeScanCallback);
                    //registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());


                    JSONArray jsonArray=new JSONArray();
                    JSONArray jsonArray1=new JSONArray();
                    JSONObject jsonObject=new JSONObject();
                    try
                    {   jsonArray=new JSONArray(prefs.getString("garage","[]"));
                        if(jsonArray.length()==0)
                        {
                            dialog.cancel();
                            enter_details();
                        }
                        else
                        {   dialog.cancel();
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                jsonObject=jsonArray.getJSONObject(i);
                                if(jsonObject.getString("address").equals(device.getAddress()))
                                {
                                    jsonArray1.put(jsonObject);
                                    prefs.edit().putString("current",jsonArray1.toString()).commit();
                                    if(jsonObject.getString("type").equals("Type 1"))
                                    {   connection_button.setVisibility(View.VISIBLE);

                                    }
                                    else
                                    {   connection_buttons2.setVisibility(View.VISIBLE);

                                    }
                                }
                            }

                        }

                    }
                    catch (Exception e)
                    {

                    }
                }
            });
            return convertView;
        }
    }
    public void enter_details()
    {   dialog_register=new Dialog(this,R.style.FullHeightDialog);
        dialog_register.setContentView(R.layout.register_device);
        dialog_register.setCancelable(false);
        dialog_register.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        dialog_register.show();
    }
    public void register_name(View view)
    {   prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Log.d("prefs",prefs.getString("garage","[]"));
        EditText get_name=(EditText)dialog_register.findViewById(R.id.get_name);
        RadioGroup radioGroup=(RadioGroup)dialog_register.findViewById(R.id.radio_group);
        RadioButton radioButton=(RadioButton)dialog_register.findViewById(radioGroup.getCheckedRadioButtonId());
        if(radioGroup.getCheckedRadioButtonId() == -1)
        {
            Toast.makeText(this,"Select type",Toast.LENGTH_SHORT).show();
        }
        else if(get_name.getText().toString().length()==0)
        {   Toast.makeText(this,"Name Not Specified",Toast.LENGTH_SHORT).show();

        }
        else
        {   JSONArray arrayObj = new JSONArray();
            JSONArray jsonArray=new JSONArray();
            JSONObject jsonObject=new JSONObject();
            try
            {   arrayObj=new JSONArray(prefs.getString("garage","[]"));
                for (int i=0;i<arrayObj.length();i++)
                {
                    arrayObj.get(i);
                }
            }
            catch (Exception e)
            {   Log.d("Error array","Error array");
                arrayObj = new JSONArray();
            }
            try
            {   jsonObject.put("name",get_name.getText().toString());
                jsonObject.put("address",saveddevice.getAddress());
                connected_address=saveddevice.getAddress();
                jsonObject.put("type",radioButton.getText().toString());
                jsonObject.put("password","000000");
                Log.d("name",get_name.getText().toString());
                Log.d("radio",radioButton.getText().toString());
                Log.d("radio",saveddevice.getAddress());
                arrayObj.put(jsonObject);
                jsonArray.put(jsonObject);
//            arrayObj.put(get_name.getText().toString());
//            arrayObj.put(saveddevice.getAddress());
//            arrayObj.put(radioButton.getText().toString());
                prefs.edit().putString("current",jsonArray.toString()).commit();
                prefs.edit().putString("garage", arrayObj.toString()).commit();
                update_name(get_name.getText().toString());
                Toast.makeText(this,"HEHEHE",Toast.LENGTH_SHORT).show();
                if(radioButton.getText().toString().equals("Type 1"))
                {   connection_button.setVisibility(View.VISIBLE);

                }
                else
                {   connection_buttons2.setVisibility(View.VISIBLE);

                }
                dialog_register.cancel();
            }
            catch (Exception e)
            {   Toast.makeText(this,"Enter all details",Toast.LENGTH_SHORT).show();
            }

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
    public void switchFragment(Fragment targetFragment){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, targetFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public String getSharedPrefrences()
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        return prefs.getString("garage","[]");
    }
    public void update_name(String name)
    {   String str="#000000#up#"+name+"#000000#";
        final byte[] tx=str.getBytes();
        if(mConnected)
        {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
        }
    }
    public void update_password(String currpass,String username,String new_pass)
    {   String str="#"+currpass+"#up#"+username+"#"+new_pass+"#";
        final byte[] tx=str.getBytes();
        if(mConnected)
        {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX,true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        garageBluetoothApplication.setCurrentActivity(this);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
}
