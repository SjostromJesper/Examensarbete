package com.example.clickerapp.broadcastReceivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BondingReceiver {
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
