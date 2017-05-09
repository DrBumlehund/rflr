package dk.sdu.mmmi.ap.g17.rflr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainMenu extends AppCompatActivity {

  /*
   * Be sure to check out our README.txt, it contains information on how this
   * application (should) works.  
   */


    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "MAIN_MENU_ACTIVITY";
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothService mBTService;
    private boolean mBtServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mBTServiceConnection, Context.BIND_IMPORTANT);
        startService(intent);

        //BluetoothConnection
        System.out.println("dfs");
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReciver, filter);
    }


    private final BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mBtServiceBound) {
                    mBTService.connect(device);
                }
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //possibly call some method from here? ¯\_(ツ)_/¯
            Log.i("Result_OK", "You have bluetooth enabled");
        }
        if (resultCode == RESULT_CANCELED) {
            Log.wtf("Result_CANCLED", "You DONT have bluetooth enabled");
        }
    }

    public void hostGame(View view) {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, (60 * 30));
            startActivityForResult(intent, 1);
        }
        if (mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            if (mBtServiceBound) {
                mBTService.start();
            }
        }
    }

    public void connectToGame(View view) {
        // Connect to Hosting device
        mBluetoothAdapter.startDiscovery();

        System.out.println(mBluetoothAdapter.isDiscovering());
    }


    private ServiceConnection mBTServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "bound bt service");
            BluetoothService.BluetoothServiceBinder binder = (BluetoothService.BluetoothServiceBinder) service;
            mBTService = binder.getService();
            mBtServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtServiceBound = false;
        }
    };


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mBTServiceConnection);
    }

    //Switch activity using an intent
    public void startIngame(View v) {
        Intent myIntent = new Intent(MainMenu.this, InGameActivity.class);
        //myIntent.putExtra("key", value); //Optional for passing extra info
        MainMenu.this.startActivity(myIntent);
    }
}
