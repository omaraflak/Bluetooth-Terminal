package me.aflak.bluetoothterminal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.pulltorefresh.PullToRefresh;

/**
 * Created by Omar on 16/07/2015.
 */
public class Select extends Activity implements PullToRefresh.OnRefreshListener {
    private Bluetooth bt;
    private ListView listView;
    private Button not_found;
    private List<BluetoothDevice> paired;
    private PullToRefresh pull_to_refresh;
    private boolean registered=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        bt = new Bluetooth(this);
        bt.enableBluetooth();

        pull_to_refresh = (PullToRefresh)findViewById(R.id.pull_to_refresh);
        listView =  (ListView)findViewById(R.id.list);
        not_found =  (Button) findViewById(R.id.not_in_list);

        pull_to_refresh.setListView(listView);
        pull_to_refresh.setOnRefreshListener(this);
        pull_to_refresh.setSlide(500);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(Select.this, Chat.class);
                i.putExtra("pos", position);
                if(registered) {
                    unregisterReceiver(mReceiver);
                    registered=false;
                }
                startActivity(i);
                finish();
            }
        });

        not_found.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Select.this, Scan.class);
                startActivity(i);
            }
        });

        addDevicesToList();
    }

    @Override
    public void onRefresh() {
        List<String> names = new ArrayList<String>();
        for (BluetoothDevice d : bt.getPairedDevices()){
            names.add(d.getName());
        }

        String[] array = names.toArray(new String[names.size()]);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, array);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.removeViews(0, listView.getCount());
                listView.setAdapter(adapter);
                paired = bt.getPairedDevices();
            }
        });
        pull_to_refresh.refreshComplete();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    private void addDevicesToList(){
        paired = bt.getPairedDevices();

        List<String> names = new ArrayList<>();
        for (BluetoothDevice d : paired){
            names.add(d.getName());
        }

        String[] array = names.toArray(new String[names.size()]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, array);

        listView.setAdapter(adapter);

        not_found.setEnabled(true);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listView.setEnabled(false);
                            }
                        });
                        Toast.makeText(Select.this, "Turn on bluetooth", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addDevicesToList();
                                listView.setEnabled(true);
                            }
                        });
                        break;
                }
            }
        }
    };
}

