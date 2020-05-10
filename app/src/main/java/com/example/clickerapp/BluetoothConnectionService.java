package com.example.clickerapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService";
    private static final String appName = "MYAPP";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context mContext) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = mContext;
        start();
    }

    private  AcceptThread mInsecureAcceptThread;

    /**
     * this class holds tolds the thread listening to the connected device
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: setting up server using: " + MY_UUID_INSECURE);
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "run: RFCOM server socket start...");
                socket = mmServerSocket.accept();
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            //TODO Fixar senare
            if(socket != null) {
                connected(socket, mmDevice);
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling accept thread");
            try{
                mmServerSocket.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "connectThread: started");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.d(TAG, "RUN mConnectThread");

            try {
                Log.d(TAG, "ConnectThread: trying to create insecureRFcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG, "run: connectThread connected");
            }
            catch (IOException e) {
                System.out.println("1");
                try {
                    mmSocket.close();
                }
                catch(IOException e1) {
                    System.out.println("run: failing at e1");
                    e1.printStackTrace();
                }
                System.out.println("run: failing at e");
                e.printStackTrace();
            }
            //TODO fixar i tredje
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try{
                Log.d(TAG, "cancel: closing client socket");
                mmSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startClient: started");

        mProgressDialog = ProgressDialog.show(mContext, "Connecting BlueTooth", "please wait...", true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try {
                mProgressDialog.dismiss();
            }
            catch(NullPointerException e) {
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }
            catch(IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];

            int bytes;

            while(true) {
                try{
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                }
                catch(IOException e) {
                    Log.d(TAG, "write: error reading input stream. " + e.getMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: writing to outputstream: " + text);

            try{
                mmOutputStream.write(bytes);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try{
                mmSocket.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: starting");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out) {
        Log.d(TAG, "write: write called");
        mConnectedThread.write(out);
    }
}