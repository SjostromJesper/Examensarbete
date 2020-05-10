package com.example.clickerapp.broadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ListView;

import com.example.clickerapp.DeviceListAdapter;
import com.example.clickerapp.R;

import java.util.ArrayList;

public class GetDevicesReceiver {
    private ListView lvNewDevices;
    private ArrayList<BluetoothDevice> mBTDevices;
    private DeviceListAdapter mDeviceListAdapter;

    public GetDevicesReceiver(ListView lvNewDevices, ArrayList<BluetoothDevice> mBTDevices, DeviceListAdapter mDeviceListAdapter) {
        this.lvNewDevices = lvNewDevices;
        this.mBTDevices = mBTDevices;
        this.mDeviceListAdapter = mDeviceListAdapter;
    }

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
}
