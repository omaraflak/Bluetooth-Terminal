package me.aflak.bluetoothterminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Omar on 14/07/2015.
 */
public class Bluetooth {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread thread;
    private boolean connected=false;

    private BluetoothCallback listener=null;

    public Bluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void enableBluetooth(){
        if(bluetoothAdapter!=null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    public void disableBluetooth(){
        if(bluetoothAdapter!=null) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            }
        }
    }

    public void connectToAddress(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        thread = new ConnectThread(device);
        thread.start();
    }

    public void connectToName(String name) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            if (blueDevice.getName().equals(name)) {
                connectToAddress(blueDevice.getAddress());
                return;
            }
        }
    }

    public void connectToDevice(BluetoothDevice device){
        thread = new ConnectThread(device);
        thread.start();
    }

    public void disconnect() {
        thread.close();
        thread.interrupt();
    }

    public boolean isConnected(){
        return connected;
    }

    public void send(String msg){
        thread.send(msg);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private BluetoothDevice device;
        BufferedReader input;
        private OutputStream out;
        private boolean stop=false;

        public ConnectThread(BluetoothDevice device) {
            this.device=device;
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(listener!=null)
                    listener.onError(e.getMessage());
            }
        }

        public void send(String msg){
            try{
                out.write(msg.getBytes());
            } catch (IOException e) {
                connected=false;
                if(listener!=null)
                    listener.onDisconnect(device, e.getMessage());
            }
        }

        public BluetoothDevice getDevice(){
            return device;
        }

        public BluetoothSocket getSocket(){
            return socket;
        }

        public void close(){
            stop=true;
            try {
                socket.close();
            } catch (IOException e) {
                if(listener!=null)
                    listener.onError(e.getMessage());
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                connected=true;

                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = socket.getOutputStream();

                if(listener!=null)
                    listener.onConnect(device);
            } catch (IOException e) {
                if(listener!=null)
                    listener.onConnectError(socket.getRemoteDevice(), e.getMessage());

                try {
                    socket.close();
                } catch (IOException closeException) {
                    if (listener != null)
                        listener.onError(closeException.getMessage());
                }
            }

            if(connected) {
                String msg;

                try {
                    while (!stop && input!=null && (msg = input.readLine()) != null) {
                        if (listener != null)
                            listener.onMessage(msg);
                    }

                    if(!stop) {
                        if (listener != null)
                            listener.onDisconnect(device, "null");

                        socket.close();
                    }
                } catch (IOException e) {
                    if (listener != null)
                        listener.onDisconnect(device, e.getMessage());
                }
            }
        }
    }

    public List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            devices.add(blueDevice);
        }
        return devices;
    }

    public BluetoothSocket getSocket(){
        return thread.getSocket();
    }

    public BluetoothDevice getDevice(){
        return thread.getDevice();
    }

    public String getDeviceName(){
        return thread.getDevice().getName();
    }

    public String getDeviceAddress(){
        return thread.getDevice().getAddress();
    }


    public interface BluetoothCallback{
        void onConnect(BluetoothDevice device);
        void onDisconnect(BluetoothDevice device, String message);
        void onMessage(String message);
        void onError(String message);
        void onConnectError(BluetoothDevice device, String message);
    }

    public void setBluetoothCallback(BluetoothCallback listener) {
        this.listener = listener;
    }

    public void removeBluetoothCallback(){
        this.listener = null;
    }

}


