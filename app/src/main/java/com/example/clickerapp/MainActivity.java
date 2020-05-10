package com.example.clickerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    public static final String TIMES = "TIMES";

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;
    Spinner blinkSpinner;

    int times = 10;




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
    }

    public void discoverButton(View view) {
        System.out.println("discoverButton: looking for unpaired devices");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            System.out.println("discoverButton: canceling discovery");

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

    public void enableDisableBT(View view) {
        System.out.println("onclick: enabling/disabling BT");
        enableDisableBT();
    }

    private void spinner() {
        blinkSpinner = findViewById(R.id.blinkSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blinkSpinner.setAdapter(adapter);
        blinkSpinner.setOnItemSelectedListener(this);
    }

    public void startButton(View view) {
        Intent intent = new Intent(this, ClickActivity.class);
        intent.putExtra(TIMES, times);
        startActivity(intent);
    }

    public void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            } else {
                System.out.println("checkBTPermissions: no need to check permissions. SDK version < LOLLIPOP");
            }
        }
    }

    /*
    setting data from the spinner-menu
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

    //TODO fixa connection till en konfigurationsaktivitet istället för att ha den i mainactivity
    @Override
    protected void onDestroy() {
        System.out.println("onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadCastReceiver1);
        unregisterReceiver(mBroadCastReceiver3);
        unregisterReceiver(mBroadCastReceiver4);
    }


    public void enableDisableBT() {

        if (mBluetoothAdapter == null) {
            System.out.println("does not have bt capabilities");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            System.out.println("EnableDisableBT: enabling BT");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadCastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()) {
            System.out.println("EnableDisableBT: disabling BT");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadCastReceiver1, BTIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothAdapter.cancelDiscovery();

        System.out.println("onItemClick: you clicked on a device");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        System.out.println("onItemClick: devicename = " + deviceName);
        System.out.println("onItemClick: devicename = " + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            System.out.println("trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }












    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: initializing RFCOM Bluetooth connection");

        mBluetoothConnection.startClient(device, uuid);
    }


    public void turnOn(View view) {
        c1();
    }

    public void turnOff(View view) {
        String d = "d";
        byte[] bytes = d.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }

    public void startConnection(View view) {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    public void c1() {
        String a = "a";
        byte[] bytes = a.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }














    public final BroadcastReceiver mBroadCastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        System.out.println("onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        System.out.println("onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        System.out.println("onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        System.out.println("onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    public BroadcastReceiver mBroadCastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                System.out.println("onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };





    public BroadcastReceiver mBroadCastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("onReceive1: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                        System.out.println("broadcastReceiver: BOND BONDED");
                        mBTDevice = mDevice;
                    }
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                        System.out.println("broadcastReceiver: BOND BONDING");
                    }
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                        System.out.println("broadcastReceiver: BOND NONE");
                    }
                }
            }
        }
    };
}