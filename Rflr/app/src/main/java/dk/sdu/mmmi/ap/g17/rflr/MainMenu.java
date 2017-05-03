package dk.sdu.mmmi.ap.g17.rflr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainMenu extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setupShakeDetector();

        BluetoothService.getInstance().setmContext(getApplicationContext());

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

    private void setupShakeDetector() {
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new OnShakeListener() {

            //Define what should be done on shake event
            @Override
            public void onShake() {
                Toast.makeText(getApplicationContext(), "Shake that bad boy!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private final BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothService.getInstance().connect(device);
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
            // Start server stuff

            // Toast not working  ¯\_(ツ)_/¯
//            Toast.makeText(getApplicationContext(), "Blocking 30s to establish connections", Toast.LENGTH_LONG).show();

            BluetoothService.getInstance().start();
        }
    }

    public void connectToGame(View view) {
        // Connect to Hosting device
        mBluetoothAdapter.startDiscovery();

        System.out.println(mBluetoothAdapter.isDiscovering());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReciver);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Register the Sensor Manager onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        //unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }


    //Switch activity using an intent
    public void startIngame(View v) {
        Intent myIntent = new Intent(MainMenu.this, InGameActivity.class);
        //myIntent.putExtra("key", value); //Optional for passing extra info
        MainMenu.this.startActivity(myIntent);
    }
}
