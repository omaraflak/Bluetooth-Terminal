package me.aflak.bluetoothterminal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Omar on 16/07/2015.
 */
public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        Bluetooth b = new Bluetooth();
        b.enableBluetooth();

        new Wait().start();
    }

    private class Wait extends Thread implements Runnable{
        public void run(){
            try {
                Thread.sleep(1000);
                Intent i = new Intent(SplashScreen.this, Select.class);
                startActivity(i);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
