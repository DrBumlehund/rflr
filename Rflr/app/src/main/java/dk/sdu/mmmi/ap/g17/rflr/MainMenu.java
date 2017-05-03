package dk.sdu.mmmi.ap.g17.rflr;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainMenu extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private final UUID myUUID = UUID.fromString("71dea7b0-26e8-4070-89b5-b68d4b91bda7");


    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setupShakeDetector();

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
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                ConnectThread connection = new ConnectThread(device);
                connection.run();
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            //possibly call some method from here? ¯\_(ツ)_/¯
            Log.i("Result_OK" ,"You have bluetooth enabled");
        } if(resultCode == RESULT_CANCELED){
            Log.wtf("Result_CANCLED" ,"You DONT have bluetooth enabled");
        }
    }

    public void hostGame(View view){
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, (60*30));
            startActivityForResult(intent, 1);
        }
        if(mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            // Start server stuff

            // Toast not working  ¯\_(ツ)_/¯
            Toast.makeText(getApplicationContext(), "Blocking 30s to establish connections", Toast.LENGTH_LONG).show();

            AcceptThread host = new AcceptThread();
            host.run();
            host.cancel();
        }
    }

    public void connectToGame(View view){
        // Connect to Hosting device
        mBluetoothAdapter.startDiscovery();


        System.out.println(mBluetoothAdapter.isDiscovering());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReciver);
    }

    private void manageMyConnectedSocket(BluetoothSocket socket){

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Snyd", myUUID);
            } catch (IOException e){
                Log.e(TAG, "Socket's accept() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {

                try{
                    socket = mmServerSocket.accept(30000);
                } catch (IOException e){
                    Toast.makeText(getApplicationContext(), "No connections established ¯\\_(ツ)_/¯", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if(socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                } else {

                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //Register the Sensor Manager onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        //unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
    }

    //Switch activity using an intent
    public void startIngame(View v){
        Intent myIntent = new Intent(MainMenu.this, InGameActivity.class);
        //myIntent.putExtra("key", value); //Optional for passing extra info
        MainMenu.this.startActivity(myIntent);
}
