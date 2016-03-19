package me.aflak.bluetoothterminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;
    private InputStream in;
    private OutputStream out;
    private boolean connected=false;

    private OnConnectedListener listener=null;
    private OnReceivedMessageListener listener2=null;
    private OnConnectionClosedListener listener3=null;

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

    public boolean isEnabled(){
        if(bluetoothAdapter==null)
            return false;
        return bluetoothAdapter.isEnabled();
    }

    public void connect(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        ConnectThread thread = new ConnectThread(device);
        thread.start();
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public void connectByName(String name) {
        for (BluetoothDevice blueDevice : bluetoothAdapter.getBondedDevices()) {
            System.out.println("Device = " + blueDevice.getName() + "   Address = " + blueDevice.getAddress());
            if (blueDevice.getName().equals(name)) {
                connect(blueDevice.getAddress());
                return;
            }
        }
    }

    public void sendMessage(String msg){
        try{
            out.write(msg.getBytes());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            connected=false;
        }
    }

    public boolean isConnected(){
        return connected;
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            Bluetooth.this.device=device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
            Bluetooth.this.socket=mmSocket;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Bluetooth.this.in = this.mmSocket.getInputStream();
                Bluetooth.this.out = this.mmSocket.getOutputStream();
                connected=true;
                new Receiver(in).start();
                System.out.println("CONNECTED!");

                if(listener!=null)
                    listener.OnConnected(device);

            } catch (IOException connectException) {
                if(listener!=null)
                    listener.ErrorConnecting(connectException);

                System.out.println("Error Connecting : " + connectException.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    System.out.println("Error: " + closeException.getMessage());
                }
                return;
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private class Receiver extends Thread implements Runnable{
        private InputStream in=null;
        private boolean continuer=true;
        private BufferedReader input;

        public Receiver(InputStream in){
            this.in=in;
            this.input = new BufferedReader(new InputStreamReader(in));
        }

        public void run(){
            while(continuer){
                try {
                    String msg="";

                    if(in.available()>0){
                        msg = input.readLine();
                    }

                    if(listener2!=null && msg!="")
                        listener2.OnReceivedMessage(msg);

                } catch (IOException e) {
                    continuer=false;
                    if(listener3!=null)
                        listener3.OnConnectionClosed(device, e.getMessage());
                }
            }
        }
    }

    public List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
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

    public String getDeviceName(){
        return device.getName();
    }

    public String getDeviceAddress(){
        return device.getAddress();
    }

    public interface OnConnectedListener{
        public void OnConnected(BluetoothDevice device);
        public void ErrorConnecting(IOException e);
    }

    public void setOnConnectedListener(OnConnectedListener listener) {
        this.listener = listener;
    }

    public void removeOnConnectedListener(){
        listener = null;
    }



    public interface OnReceivedMessageListener{
        public void OnReceivedMessage(String message);
    }

    public void setOnReceivedMessageListener(OnReceivedMessageListener listener) {
        this.listener2 = listener;
    }

    public void removeReceivedMessageListener(){
        listener2 = null;
    }



    public interface OnConnectionClosedListener{
        public void OnConnectionClosed(BluetoothDevice device, String message);
    }

    public void setOnConnectionClosedListener(OnConnectionClosedListener listener) {
        this.listener3 = listener;
    }

    public void removeOnConnectionClosedListener(){
        listener3 = null;
    }
}


