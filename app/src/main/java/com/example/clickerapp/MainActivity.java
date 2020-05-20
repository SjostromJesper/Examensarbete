package com.example.clickerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.telecom.Connection.STATE_DISCONNECTED;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    public static final String TIMES = "TIMES";

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;
    Spinner blinkSpinner;

    private int times = 10;
    ArrayList<Button> buttons = new ArrayList<>();

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice mBTDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadCastReceiver4, filter);

        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        lvNewDevices.setOnItemClickListener(MainActivity.this);
        spinner();

        buttons.add((Button) findViewById(R.id.upperLeft));
        buttons.add((Button) findViewById(R.id.upperRight));
        buttons.add((Button) findViewById(R.id.bottomLeft));
        buttons.add((Button) findViewById(R.id.bottomRight));
    }

    /**
     * onClick-method used to activate bluetooth device discovery
     */
    public void discoverButton(View view) {
        Log.d(TAG, "discoverButton: looking for unpaired devices");



        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "discoverButton: canceling discovery");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadCastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadCastReceiver3, discoverDevicesIntent);
        }

    }

    /**
     * spinner used to set the recovery time between clicks
     */
    private void spinner() {
        blinkSpinner = findViewById(R.id.blinkSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blinkSpinner.setAdapter(adapter);
        blinkSpinner.setOnItemSelectedListener(this);
    }

    /**
     * onClick-method
     * starts game
     */
    public void startButton(View view) {
        Intent intent = new Intent(this, ClickActivity.class);
        intent.putExtra(TIMES, times);
        startActivity(intent);
    }

    /**
     * checks if version of current device is up to date
     * and changing data if necessary
     */
    public void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            } else {
                Log.d(TAG, "checkBTPermissions: no need to check permissions. SDK version < LOLLIPOP");
            }
        }
    }

    /**
     * method to grab selected difficulty chosen in the spinner menu
     * @param position of item in spinner
     * @param id of item in spinner
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
        times = Integer.parseInt(text);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * shuts down all receivers when app is destroyed to avoid memory leaks
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver1);
        unregisterReceiver(mBroadCastReceiver3);
        unregisterReceiver(mBroadCastReceiver4);
    }




    /**
     * Enabling Bluetooth if disabled
     * disabling Bluetooth if enabled
     */
    public void enableDisableBT(View view) {
        Log.d(TAG, "enableDisableBT: enabling/disabling BT");
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "does not have bt capabilities");
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "EnableDisableBT: enabling BT");
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBTIntent);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                registerReceiver(mBroadCastReceiver1, BTIntent);
            }
            if (mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "EnableDisableBT: disabling BT");
                mBluetoothAdapter.disable();

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                registerReceiver(mBroadCastReceiver1, BTIntent);
            }
        }
    }

    /**
     * method used to get the data of selected slave-device needed to establish a connection to master-device
     * @param position position of selected device
     * @param id of selected device
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: you clicked on a device");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG, "onItemClick: devicename = " + deviceName);
        Log.d(TAG, "onItemClick: devicename = " + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }

    /**
     * start-method for connection between slave and master
     * @param device current selected device
     * @param uuid set on this device
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: initializing RFCOM Bluetooth connection");

        mBluetoothConnection.startClient(device, uuid);
    }

    //TODO döpa om
    public void turnOn(View view) {
        String a = "a";
        byte[] bytes = a.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
        TimeHandler.startTime = System.currentTimeMillis();
    }




    BluetoothGatt mBluetoothGatt;

    /**
     * onClick-method used to establish a connection between selected slave-device and this device
     * using the slave-device address and this device's UUID
     */
    public void startConnection(View view) {
       // mBluetoothGatt = mBTDevice.connectGatt(this, false, mGattCallback);
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }



    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "1");
            }
            else{
                Log.d(TAG, "2");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            System.out.println("10");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            System.out.println("11");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            System.out.println("12");
        }
    };
































    /**
     * broadcaster for bluetooth state disabled/enabling/enabled/disabling
     */
    public final BroadcastReceiver mBroadCastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * broadcaster for bluetooth discovery
     */
    public BroadcastReceiver mBroadCastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "mBroadcastReceiver3: ACTION FOUND");

            try {
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBTDevices.add(device);
                    Log.d(TAG, "mBroadcastReceiver3: " + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);
                }
            }
            catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * broadcaster for bonding
     * sets selected device data to local variable
     */
    public BroadcastReceiver mBroadCastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive1: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "broadcastReceiver: BOND BONDED");
                        mBTDevice = mDevice;
                    }
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        Log.d(TAG, "broadcastReceiver: BOND BONDING");
                    }
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        Log.d(TAG, "broadcastReceiver: BOND NONE");
                    }
                }
            }
        }
    };
}