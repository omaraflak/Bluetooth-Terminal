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
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BufferedReader input;
    private OutputStream out;

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
        new ConnectThread(device).start();
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
        new ConnectThread(device).start();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            if(listener!=null)
                listener.onError(e.getMessage());
        }
    }

    public boolean isConnected(){
        return connected;
    }

    public void send(String msg){
        try {
            out.write(msg.getBytes());
        } catch (IOException e) {
            connected=false;
            if(listener!=null)
                listener.onDisconnect(device, e.getMessage());
        }
    }

    private class ReceiveThread extends Thread implements Runnable{
        public void run(){
            String msg;
            try {
                while ((msg = input.readLine()) != null) {
                    if (listener != null)
                        listener.onMessage(msg);
                }
            } catch (IOException e) {
                connected=false;
                if (listener != null)
                    listener.onDisconnect(device, e.getMessage());
            }
        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread(BluetoothDevice device) {
            Bluetooth.this.device=device;
            try {
                Bluetooth.this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if(listener!=null)
                    listener.onError(e.getMessage());
            }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                out = socket.getOutputStream();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected=true;

                new ReceiveThread().start();

                if(listener!=null)
                    listener.onConnect(device);
            } catch (IOException e) {
                if(listener!=null)
                    listener.onConnectError(device, e.getMessage());

                try {
                    socket.close();
                } catch (IOException closeException) {
                    if (listener != null)
                        listener.onError(closeException.getMessage());
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
        return socket;
    }

    public BluetoothDevice getDevice(){
        return device;
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


