package com.example.clickerapp.broadcastReceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CurrentStateReceiver {
    private BluetoothAdapter mBluetoothAdapter;

    public CurrentStateReceiver(BluetoothAdapter adapter) {
        this.mBluetoothAdapter = adapter;
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
}
